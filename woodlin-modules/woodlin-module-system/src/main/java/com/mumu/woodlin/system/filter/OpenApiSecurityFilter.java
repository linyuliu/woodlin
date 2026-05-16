package com.mumu.woodlin.system.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.authorization.mapper.AuthorizationQueryMapper;
import com.mumu.woodlin.authorization.model.AuthorizationContext;
import com.mumu.woodlin.authorization.model.AuthorizationDecision;
import com.mumu.woodlin.authorization.model.AuthorizationRequest;
import com.mumu.woodlin.authorization.model.AuthorizationResource;
import com.mumu.woodlin.authorization.model.AuthorizationSubject;
import com.mumu.woodlin.authorization.service.AuthorizationService;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.openapi.OpenApiSecurityKit;
import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSecurityMode;
import com.mumu.woodlin.common.openapi.exception.OpenApiSecurityException;
import com.mumu.woodlin.common.openapi.model.OpenApiCredentialMaterial;
import com.mumu.woodlin.common.openapi.model.OpenApiEncryptedPayload;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.config.OpenApiSecurityProperties;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.entity.SysOpenAppCredential;
import com.mumu.woodlin.system.model.EffectiveOpenApiSecurityProfile;
import com.mumu.woodlin.system.model.OpenApiAuthenticationResult;
import com.mumu.woodlin.system.model.OpenApiRuntimeContext;
import com.mumu.woodlin.system.openapi.OpenApiSecurityProfileResolver;
import com.mumu.woodlin.system.service.IOpenApiSecurityService;
import com.mumu.woodlin.system.service.ISysOpenAppCredentialService;
import com.mumu.woodlin.system.service.ISysOpenAppService;
import com.mumu.woodlin.system.util.OpenApiRequestCanonicalizer;
import com.mumu.woodlin.system.util.OpenApiSecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 开放 API 安全过滤器。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Slf4j
@RequiredArgsConstructor
public class OpenApiSecurityFilter extends OncePerRequestFilter {

    private final OpenApiSecurityProperties properties;
    private final ObjectMapper objectMapper;
    private final IOpenApiSecurityService openApiSecurityService;
    private final ISysOpenAppService openAppService;
    private final ISysOpenAppCredentialService credentialService;
    private final RedissonClient redissonClient;
    private final AuthorizationService authorizationService;
    private final AuthorizationQueryMapper authorizationQueryMapper;
    private final OpenApiSecurityProfileResolver profileResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String configuredPrefix = StrUtil.blankToDefault(
            properties.getPathPrefix(), OpenApiSecurityConstants.OPEN_API_PATH_PREFIX
        );
        return !properties.isEnabled()
            || StrUtil.isBlank(uri)
            || !(uri.startsWith(configuredPrefix)
            || uri.startsWith(OpenApiSecurityConstants.OPEN_API_PATH_PREFIX)
            || uri.startsWith(OpenApiSecurityConstants.OPEN_API_V2_PATH_PREFIX));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        OpenApiAuthenticationResult authentication = null;
        String requestId = resolveRequestId(wrappedRequest);
        try {
            wrappedRequest.removeHeadersWithPrefix(OpenApiSecurityConstants.INTERNAL_HEADER_PREFIX);
            wrappedRequest.setAttribute(OpenApiSecurityConstants.ATTR_REQUEST_ID, requestId);

            OpenApiRuntimeContext runtimeContext = openApiSecurityService.resolveRuntimeContext(
                wrappedRequest.getRequestURI(), wrappedRequest.getMethod()
            );
            EffectiveOpenApiSecurityProfile profile = profileResolver.resolveInitial(wrappedRequest, runtimeContext);
            if (profile.getSecurityMode() == ApiSecurityMode.NONE) {
                validatePublicEndpoint(runtimeContext);
                exposePublicRuntimeContext(wrappedRequest, profile, requestId);
                filterChain.doFilter(wrappedRequest, wrappedResponse);
                wrappedResponse.copyBodyToResponse();
                return;
            }

            byte[] rawBody = wrappedRequest.getCachedBody();
            authentication = authenticate(wrappedRequest, rawBody, runtimeContext, profile, requestId);
            if (authentication.getCredential() != null) {
                profile = profileResolver.resolve(
                    wrappedRequest, runtimeContext, authentication.getCredential(), authentication.getApp()
                );
                authentication.setSecurityMode(profile.getSecurityMode());
                validateAppAndCredential(authentication, profile, wrappedRequest);
                decryptRequestIfNeeded(wrappedRequest, rawBody, authentication, profile);
                validateAuthorization(wrappedRequest, authentication);
                exposeTrustedRuntimeContext(wrappedRequest, authentication);
                credentialService.touchLastUsedTime(authentication.getCredential().getCredentialId());
            }

            filterChain.doFilter(wrappedRequest, wrappedResponse);
            encryptResponseIfNeeded(wrappedResponse, authentication, profile, wrappedRequest);
            wrappedResponse.copyBodyToResponse();
        } catch (BusinessException ex) {
            auditFailure(wrappedRequest, authentication, requestId, ex.getMessage());
            writeError(response, ex.getCode(), ex.getMessage());
        } catch (OpenApiSecurityException ex) {
            auditFailure(wrappedRequest, authentication, requestId, ex.getMessage());
            writeError(response, ResultCode.BAD_REQUEST.getCode(), ex.getMessage());
        } catch (Exception ex) {
            log.error("开放API过滤器异常: requestId={}, path={}", requestId, request.getRequestURI(), ex);
            writeError(response, ResultCode.INTERNAL_SERVER_ERROR.getCode(), "开放API安全校验失败");
        }
    }

    private OpenApiAuthenticationResult authenticate(CachedBodyHttpServletRequest request, byte[] rawBody,
                                                     OpenApiRuntimeContext runtimeContext,
                                                     EffectiveOpenApiSecurityProfile profile, String requestId) {
        OpenApiAuthenticationResult result = new OpenApiAuthenticationResult()
            .setSecurityMode(profile.getSecurityMode())
            .setRequestId(requestId);
        if (profile.getSecurityMode().requiresToken()) {
            validateToken(request);
        }
        SysOpenAppCredential credential = null;
        if (profile.getSecurityMode().requiresAppKey()) {
            credential = validateAppKeyRequest(request, runtimeContext);
        } else if (profile.getSecurityMode().requiresAksk()) {
            credential = validateAkskRequest(request, rawBody, runtimeContext);
        }
        if (credential == null) {
            return result;
        }
        SysOpenApp app = requireActiveApp(credential.getAppId());
        return result
            .setCredential(credential)
            .setApp(app)
            .setTenantId(resolveTenantId(request, app))
            .setPrincipalAttributes(buildPrincipalAttributes(app, credential));
    }

    private SysOpenAppCredential validateAkskRequest(CachedBodyHttpServletRequest request, byte[] rawBody,
                                                     OpenApiRuntimeContext runtimeContext) {
        String accessKey = requireHeader(request, OpenApiSecurityConstants.HEADER_ACCESS_KEY);
        String timestamp = requireHeader(request, OpenApiSecurityConstants.HEADER_TIMESTAMP);
        String signature = requireHeader(request, OpenApiSecurityConstants.HEADER_SIGNATURE);
        SysOpenAppCredential credential = credentialService.getActiveCredentialByAccessKey(accessKey);
        if (credential == null) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "AccessKey 无效或已停用");
        }

        EffectiveOpenApiSecurityProfile profile = profileResolver.resolve(request, runtimeContext, credential, null);
        if (!profile.getSecurityMode().requiresAksk()) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "当前接口不允许 AK/SK 认证");
        }
        String nonce = profile.isNonceEnabled()
            ? requireHeader(request, OpenApiSecurityConstants.HEADER_NONCE)
            : StrUtil.blankToDefault(request.getHeader(OpenApiSecurityConstants.HEADER_NONCE), "");
        LocalDateTime requestTime = LocalDateTime.ofInstant(
            OpenApiSecurityKit.parseTimestamp(timestamp), ZoneOffset.UTC
        );
        long delta = Math.abs(Duration.between(requestTime, LocalDateTime.now(ZoneOffset.UTC)).getSeconds());
        if (delta > profile.getTimestampWindowSeconds()) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "请求已过期");
        }
        if (profile.isNonceEnabled()) {
            validateNonce(accessKey, nonce, profile.getNonceTtlSeconds());
        }

        String tenantId = request.getHeader(OpenApiSecurityConstants.HEADER_TENANT_ID);
        String canonical = OpenApiRequestCanonicalizer.canonicalize(
            request, rawBody, timestamp, nonce, tenantId, accessKey
        );
        boolean verified = OpenApiSecurityKit.verify(
            canonical.getBytes(StandardCharsets.UTF_8),
            signature,
            profile.getSignatureAlgorithm(),
            new OpenApiCredentialMaterial()
                .setSecretKey(credentialService.revealSecretKey(credential))
                .setSignaturePublicKey(credential.getSignaturePublicKey())
        ).isVerified();
        if (!verified) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "签名校验失败");
        }
        return credential;
    }

    private SysOpenAppCredential validateAppKeyRequest(HttpServletRequest request,
                                                       OpenApiRuntimeContext runtimeContext) {
        String appKey = requireHeader(request, OpenApiSecurityConstants.HEADER_APP_KEY);
        SysOpenAppCredential credential = credentialService.getActiveCredentialByAppKey(appKey);
        if (credential == null) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "AppKey 无效或已停用");
        }
        EffectiveOpenApiSecurityProfile profile = profileResolver.resolve(request, runtimeContext, credential, null);
        if (!profile.getSecurityMode().requiresAppKey()) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "当前接口不允许 AppKey 认证");
        }
        return credential;
    }

    private void validateToken(HttpServletRequest request) {
        String authorization = request.getHeader(OpenApiSecurityConstants.HEADER_AUTHORIZATION);
        if (StrUtil.isBlank(authorization)
            || !authorization.startsWith(OpenApiSecurityConstants.AUTHORIZATION_PREFIX)) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "缺少 Bearer Token");
        }
        String token = authorization.substring(OpenApiSecurityConstants.AUTHORIZATION_PREFIX.length()).trim();
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "Token 无效或已过期");
        }
        StpUtil.setTokenValue(token);
    }

    private void validateNonce(String accessKey, String nonce, int ttlSeconds) {
        String cacheKey = OpenApiSecurityKit.buildNonceKey(
            OpenApiSecurityConstants.NONCE_CACHE_PREFIX, accessKey, nonce
        );
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        boolean absent = bucket.setIfAbsent("1", Duration.ofSeconds(ttlSeconds));
        if (!absent) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "重复请求已被拒绝");
        }
    }

    private SysOpenApp requireActiveApp(Long appId) {
        SysOpenApp app = openAppService.getById(appId);
        if (app == null || !"1".equals(app.getStatus())) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "开放应用已停用或不存在");
        }
        return app;
    }

    private void validateAppAndCredential(OpenApiAuthenticationResult authentication,
                                          EffectiveOpenApiSecurityProfile profile,
                                          HttpServletRequest request) {
        SysOpenApp app = authentication.getApp();
        SysOpenAppCredential credential = authentication.getCredential();
        LocalDateTime now = LocalDateTime.now();
        if (credential.getActiveFrom() != null && credential.getActiveFrom().isAfter(now)) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "凭证尚未生效");
        }
        if (credential.getActiveTo() != null && credential.getActiveTo().isBefore(now)) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "凭证已过期");
        }
        validateTenant(profile, request, app, authentication);
        validateIpWhitelist(request, profile);
    }

    private void validateTenant(EffectiveOpenApiSecurityProfile profile, HttpServletRequest request,
                                SysOpenApp app, OpenApiAuthenticationResult authentication) {
        String requestTenantId = request.getHeader(OpenApiSecurityConstants.HEADER_TENANT_ID);
        if (profile.isTenantRequired() && StrUtil.isBlank(requestTenantId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "缺少租户标识");
        }
        if (StrUtil.isNotBlank(requestTenantId)
            && StrUtil.isNotBlank(app.getTenantId())
            && !StrUtil.equals(app.getTenantId(), requestTenantId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户标识不匹配");
        }
        authentication.setTenantId(StrUtil.blankToDefault(requestTenantId, app.getTenantId()));
    }

    private void validateIpWhitelist(HttpServletRequest request, EffectiveOpenApiSecurityProfile profile) {
        if (StrUtil.isBlank(profile.getIpWhitelist())) {
            return;
        }
        String remoteIp = request.getRemoteAddr();
        boolean allowed = StrUtil.splitTrim(profile.getIpWhitelist(), ',').stream()
            .anyMatch(item -> matchIp(remoteIp, item));
        if (!allowed) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "当前 IP 不在白名单中");
        }
    }

    private void decryptRequestIfNeeded(CachedBodyHttpServletRequest request, byte[] rawBody,
                                        OpenApiAuthenticationResult authentication,
                                        EffectiveOpenApiSecurityProfile profile) throws IOException {
        if (!isPayloadEncryptionActive(profile, request)) {
            return;
        }
        if (profile.getSecurityMode() == ApiSecurityMode.APP_KEY) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "AppKey 模式不支持报文加密");
        }
        if (rawBody.length == 0) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "请求体缺少加密报文");
        }
        OpenApiEncryptedPayload encryptedPayload = parseEncryptedPayload(rawBody, profile);
        String plainSecret = credentialService.revealSecretKey(authentication.getCredential());
        String serverPrivateKey = credentialService.revealServerPrivateKey(authentication.getCredential());
        byte[] plainBody = OpenApiSecurityKit.decrypt(
            encryptedPayload,
            profile.getEncryptionAlgorithm(),
            new OpenApiCredentialMaterial()
                .setSecretKey(plainSecret)
                .setServerPrivateKey(serverPrivateKey)
                .setEncryptionPrivateKey(serverPrivateKey)
        );
        request.replaceBody(plainBody);
    }

    private void validateAuthorization(HttpServletRequest request, OpenApiAuthenticationResult authentication) {
        SysOpenApp app = authentication.getApp();
        String path = request.getRequestURI();
        String action = request.getMethod() + ":" + path;
        AuthorizationDecision decision = authorizationService.can(new AuthorizationRequest()
            .setSubject(new AuthorizationSubject().setType("app").setId(String.valueOf(app.getAppId())))
            .setAction(action)
            .setResource(new AuthorizationResource().setType("api").setId(path))
            .setContext(new AuthorizationContext()
                .setTenantId(authentication.getTenantId())
                .setIp(request.getRemoteAddr())
                .setAppId(app.getAppId())
                .setHeaders(readHeaders(request))
                .setAttributes(authentication.getPrincipalAttributes())));
        if (!decision.isAllowed()) {
            throw BusinessException.of(ResultCode.FORBIDDEN, decision.getReason());
        }
    }

    private void exposeTrustedRuntimeContext(CachedBodyHttpServletRequest request,
                                             OpenApiAuthenticationResult authentication) {
        SysOpenApp app = authentication.getApp();
        SysOpenAppCredential credential = authentication.getCredential();
        List<String> scopes = authorizationQueryMapper.selectAppGrantedScopeCodes(
            app.getAppId(), authentication.getTenantId()
        );
        String scopeHeader = String.join(",", scopes);

        request.setAttribute(OpenApiSecurityConstants.ATTR_APP_ID, app.getAppId());
        request.setAttribute(OpenApiSecurityConstants.ATTR_CREDENTIAL_ID, credential.getCredentialId());
        request.setAttribute(OpenApiSecurityConstants.ATTR_TENANT_ID, authentication.getTenantId());
        request.setAttribute(OpenApiSecurityConstants.ATTR_CLIENT_ID, app.getClientId());
        request.setAttribute(OpenApiSecurityConstants.ATTR_SECURITY_MODE, authentication.getSecurityMode().name());
        request.setAttribute(OpenApiSecurityConstants.ATTR_SCOPES, scopes);
        request.setAttribute(OpenApiSecurityConstants.ATTR_REQUEST_ID, authentication.getRequestId());

        request.setHeader(OpenApiSecurityConstants.HEADER_INTERNAL_SUBJECT_TYPE, "app");
        request.setHeader(OpenApiSecurityConstants.HEADER_INTERNAL_APP_ID, String.valueOf(app.getAppId()));
        request.setHeader(
            OpenApiSecurityConstants.HEADER_INTERNAL_CREDENTIAL_ID, String.valueOf(credential.getCredentialId())
        );
        request.setHeader(
            OpenApiSecurityConstants.HEADER_INTERNAL_TENANT_ID, nullToEmpty(authentication.getTenantId())
        );
        request.setHeader(OpenApiSecurityConstants.HEADER_INTERNAL_CLIENT_ID, nullToEmpty(app.getClientId()));
        request.setHeader(OpenApiSecurityConstants.HEADER_INTERNAL_REQUEST_ID, authentication.getRequestId());
        request.setHeader(OpenApiSecurityConstants.HEADER_INTERNAL_SCOPES, scopeHeader);
        request.setHeader(
            OpenApiSecurityConstants.HEADER_INTERNAL_SECURITY_MODE, authentication.getSecurityMode().name()
        );
    }

    private void exposePublicRuntimeContext(CachedBodyHttpServletRequest request,
                                            EffectiveOpenApiSecurityProfile profile, String requestId) {
        request.setAttribute(OpenApiSecurityConstants.ATTR_SECURITY_MODE, profile.getSecurityMode().name());
        request.setAttribute(OpenApiSecurityConstants.ATTR_REQUEST_ID, requestId);
        request.setHeader(OpenApiSecurityConstants.HEADER_INTERNAL_REQUEST_ID, requestId);
        request.setHeader(OpenApiSecurityConstants.HEADER_INTERNAL_SECURITY_MODE, profile.getSecurityMode().name());
    }

    private void encryptResponseIfNeeded(ContentCachingResponseWrapper response,
                                         OpenApiAuthenticationResult authentication,
                                         EffectiveOpenApiSecurityProfile profile,
                                         HttpServletRequest request) throws IOException {
        if (!isPayloadEncryptionActive(profile, request) || response.getStatus() >= 400) {
            return;
        }
        if (authentication == null || authentication.getCredential() == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "当前安全模式不支持报文加密");
        }
        byte[] responseBody = response.getContentAsByteArray();
        if (responseBody.length == 0) {
            return;
        }
        String plainSecret = credentialService.revealSecretKey(authentication.getCredential());
        OpenApiEncryptedPayload payload = OpenApiSecurityKit.encrypt(
            responseBody,
            profile.getEncryptionAlgorithm(),
            new OpenApiCredentialMaterial()
                .setSecretKey(plainSecret)
                .setServerPublicKey(authentication.getCredential().getEncryptionPublicKey())
                .setEncryptionPublicKey(authentication.getCredential().getEncryptionPublicKey())
        );
        response.resetBuffer();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), payload);
    }

    private OpenApiEncryptedPayload parseEncryptedPayload(byte[] body,
                                                          EffectiveOpenApiSecurityProfile profile) throws IOException {
        OpenApiEncryptedPayload payload;
        try {
            payload = objectMapper.readValue(body, OpenApiEncryptedPayload.class);
        } catch (Exception ex) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "请求体不是合法的加密报文");
        }
        if (payload == null || StrUtil.isBlank(payload.getEncrypted())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "请求体缺少加密报文");
        }
        if (StrUtil.isNotBlank(payload.getAlgorithm())
            && !StrUtil.equalsIgnoreCase(payload.getAlgorithm(), profile.getEncryptionAlgorithm().name())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "加密算法与服务端配置不一致");
        }
        return payload;
    }

    private boolean isPayloadEncryptionActive(EffectiveOpenApiSecurityProfile profile, HttpServletRequest request) {
        return profile != null
            && profile.getEncryptionAlgorithm() != ApiEncryptionAlgorithm.NONE
            && (profile.isEncryptionRequired()
            || StrUtil.isNotBlank(request.getHeader(OpenApiSecurityConstants.HEADER_ENCRYPT_ALGORITHM)));
    }

    private void validatePublicEndpoint(OpenApiRuntimeContext runtimeContext) {
        boolean explicitPublicPolicy = runtimeContext != null
            && runtimeContext.isPolicyMatched()
            && runtimeContext.getPolicy() != null
            && ApiSecurityMode.NONE.name().equalsIgnoreCase(runtimeContext.getPolicy().getSecurityMode());
        if (!explicitPublicPolicy) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "开放接口未配置公开放行策略");
        }
    }

    private String resolveTenantId(HttpServletRequest request, SysOpenApp app) {
        String requestTenantId = request.getHeader(OpenApiSecurityConstants.HEADER_TENANT_ID);
        return StrUtil.blankToDefault(requestTenantId, app.getTenantId());
    }

    private Map<String, Object> buildPrincipalAttributes(SysOpenApp app, SysOpenAppCredential credential) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("appId", app.getAppId());
        attributes.put("credentialId", credential.getCredentialId());
        attributes.put("clientId", app.getClientId());
        attributes.put("regionCode", app.getRegionCode());
        attributes.put("regionName", app.getRegionName());
        attributes.put("ownerUserId", app.getOwnerUserId());
        attributes.put("ownerDeptId", app.getOwnerDeptId());
        return attributes;
    }

    private Map<String, String> readHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

    private boolean matchIp(String remoteIp, String rule) {
        if (StrUtil.isBlank(rule)) {
            return false;
        }
        String candidate = rule.trim();
        if ("*".equals(candidate)) {
            return true;
        }
        if (candidate.contains("*")) {
            String regex = candidate.replace(".", "\\.").replace("*", ".*");
            return remoteIp.matches(regex);
        }
        if (candidate.contains("/")) {
            return matchCidr(remoteIp, candidate);
        }
        return StrUtil.equals(candidate, remoteIp);
    }

    private boolean matchCidr(String remoteIp, String cidrRule) {
        try {
            String[] parts = cidrRule.split("/");
            InetAddress remoteAddress = InetAddress.getByName(remoteIp);
            InetAddress networkAddress = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);
            byte[] remoteBytes = remoteAddress.getAddress();
            byte[] networkBytes = networkAddress.getAddress();
            if (remoteBytes.length != networkBytes.length) {
                return false;
            }
            BigInteger remote = new BigInteger(1, remoteBytes);
            BigInteger network = new BigInteger(1, networkBytes);
            int size = remoteBytes.length * 8;
            BigInteger mask = BigInteger.ONE.shiftLeft(size).subtract(BigInteger.ONE)
                .shiftRight(prefixLength)
                .not()
                .and(BigInteger.ONE.shiftLeft(size).subtract(BigInteger.ONE));
            return remote.and(mask).equals(network.and(mask));
        } catch (Exception ex) {
            log.warn("解析IP白名单规则失败: rule={}", cidrRule, ex);
            return false;
        }
    }

    private String requireHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (StrUtil.isBlank(value)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "缺少请求头: " + headerName);
        }
        return value.trim();
    }

    private String resolveRequestId(HttpServletRequest request) {
        return StrUtil.blankToDefault(
            request.getHeader(OpenApiSecurityConstants.HEADER_REQUEST_ID),
            UUID.randomUUID().toString().replace("-", "")
        );
    }

    private void auditFailure(HttpServletRequest request, OpenApiAuthenticationResult authentication,
                              String requestId, String reason) {
        Long appId = authentication == null || authentication.getApp() == null
            ? null : authentication.getApp().getAppId();
        Long credentialId = authentication == null || authentication.getCredential() == null
            ? null : authentication.getCredential().getCredentialId();
        log.warn(
            "开放API拒绝: requestId={}, appId={}, credentialId={}, path={}, reason={}",
            requestId, appId, credentialId, request.getRequestURI(), reason
        );
    }

    private void writeError(HttpServletResponse response, Integer code, String message) throws IOException {
        int httpStatus = code != null && code >= 400 && code <= 599 ? code : HttpServletResponse.SC_BAD_REQUEST;
        response.resetBuffer();
        response.setStatus(httpStatus);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), R.fail(code, message));
        response.flushBuffer();
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
