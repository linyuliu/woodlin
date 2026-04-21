package com.mumu.woodlin.system.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.openapi.OpenApiSecurityKit;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.config.OpenApiSecurityProperties;
import com.mumu.woodlin.system.dto.OpenApiEncryptedPayload;
import com.mumu.woodlin.system.dto.OpenApiGlobalSettingsDto;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.entity.SysOpenAppCredential;
import com.mumu.woodlin.system.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.system.enums.ApiSecurityMode;
import com.mumu.woodlin.system.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.system.model.OpenApiRuntimeContext;
import com.mumu.woodlin.system.service.IOpenApiSecurityService;
import com.mumu.woodlin.system.service.ISysOpenAppCredentialService;
import com.mumu.woodlin.system.service.ISysOpenAppService;
import com.mumu.woodlin.system.util.OpenApiCryptoUtil;
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !properties.isEnabled()
            || StrUtil.isBlank(uri)
            || !uri.startsWith(StrUtil.blankToDefault(properties.getPathPrefix(), OpenApiSecurityConstants.OPEN_API_PATH_PREFIX));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            OpenApiRuntimeContext runtimeContext = openApiSecurityService.resolveRuntimeContext(
                request.getRequestURI(), request.getMethod()
            );
            OpenApiGlobalSettingsDto globalSettings = runtimeContext.getGlobalSettings();
            SysOpenApiPolicy policy = runtimeContext.getPolicy();
            ApiSecurityMode securityMode = ApiSecurityMode.of(
                policy != null ? policy.getSecurityMode() : globalSettings.getDefaultMode(),
                ApiSecurityMode.AKSK
            );

            byte[] rawBody = wrappedRequest.getCachedBody();
            SysOpenAppCredential credential = null;
            SysOpenApp app = null;

            if (securityMode.requiresToken()) {
                validateToken(request);
            }
            if (securityMode.requiresAksk()) {
                credential = validateAkskRequest(wrappedRequest, rawBody, policy, globalSettings);
                app = requireActiveApp(credential.getAppId());
                validateAppAndCredentialWindow(app, credential);
                validateTenant(policy, request, app);
                validateIpWhitelist(request, app);
            }

            ApiEncryptionAlgorithm encryptionAlgorithm = resolveEncryptionAlgorithm(policy, credential, globalSettings, request);
            boolean encryptionRequired = isEncryptionRequired(policy, globalSettings, encryptionAlgorithm)
                || StrUtil.isNotBlank(request.getHeader(OpenApiSecurityConstants.HEADER_ENCRYPT_ALGORITHM));

            if (encryptionAlgorithm != ApiEncryptionAlgorithm.NONE && rawBody.length > 0) {
                OpenApiEncryptedPayload encryptedPayload = tryParseEncryptedPayload(rawBody, encryptionRequired);
                if (encryptedPayload != null) {
                    String plainSecret = credential == null ? null : credentialService.revealSecretKey(credential);
                    String serverPrivateKey = credential == null ? null : credentialService.revealServerPrivateKey(credential);
                    byte[] plainBody = OpenApiCryptoUtil.decryptPayload(
                        encryptedPayload, encryptionAlgorithm, plainSecret, serverPrivateKey
                    );
                    wrappedRequest.replaceBody(plainBody);
                }
            }

            filterChain.doFilter(wrappedRequest, wrappedResponse);

            if (encryptionAlgorithm != ApiEncryptionAlgorithm.NONE && wrappedResponse.getStatus() < 400) {
                byte[] responseBody = wrappedResponse.getContentAsByteArray();
                if (responseBody.length > 0) {
                    if (credential == null) {
                        throw BusinessException.of(ResultCode.BAD_REQUEST, "当前安全模式不支持报文加密");
                    }
                    String plainSecret = credentialService.revealSecretKey(credential);
                    OpenApiEncryptedPayload payload = OpenApiCryptoUtil.encryptPayload(
                        responseBody, encryptionAlgorithm, plainSecret, credential.getEncryptionPublicKey()
                    );
                    wrappedResponse.resetBuffer();
                    wrappedResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    wrappedResponse.setContentType("application/json;charset=UTF-8");
                    objectMapper.writeValue(wrappedResponse.getOutputStream(), payload);
                }
            }
        } catch (BusinessException ex) {
            log.warn("开放API校验失败: path={}, message={}", request.getRequestURI(), ex.getMessage());
            writeError(response, ex.getCode(), ex.getMessage());
            return;
        } catch (Exception ex) {
            log.error("开放API过滤器异常: path={}", request.getRequestURI(), ex);
            writeError(response, ResultCode.INTERNAL_SERVER_ERROR.getCode(), "开放API安全校验失败");
            return;
        }
        wrappedResponse.copyBodyToResponse();
    }

    private SysOpenAppCredential validateAkskRequest(CachedBodyHttpServletRequest request, byte[] rawBody,
                                                     SysOpenApiPolicy policy, OpenApiGlobalSettingsDto globalSettings) {
        String accessKey = requireHeader(request, OpenApiSecurityConstants.HEADER_ACCESS_KEY);
        String timestamp = requireHeader(request, OpenApiSecurityConstants.HEADER_TIMESTAMP);
        String nonce = requireHeader(request, OpenApiSecurityConstants.HEADER_NONCE);
        String signature = requireHeader(request, OpenApiSecurityConstants.HEADER_SIGNATURE);
        SysOpenAppCredential credential = credentialService.getActiveCredentialByAccessKey(accessKey);
        if (credential == null) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "AccessKey 无效或已停用");
        }

        ApiSignatureAlgorithm expectedSignatureAlgorithm = resolveSignatureAlgorithm(policy, credential, globalSettings, request);
        LocalDateTime requestTime = OpenApiCryptoUtil.parseTimestamp(timestamp);
        int windowSeconds = policy != null && policy.getTimestampWindowSeconds() != null
            ? policy.getTimestampWindowSeconds()
            : globalSettings.getTimestampWindowSeconds();
        long delta = Math.abs(Duration.between(requestTime, LocalDateTime.now(ZoneOffset.UTC)).getSeconds());
        if (delta > windowSeconds) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "请求已过期");
        }

        boolean nonceEnabled = policy != null && policy.getNonceEnabled() != null
            ? "1".equals(policy.getNonceEnabled())
            : Boolean.TRUE.equals(globalSettings.getNonceEnabled());
        int nonceTtl = policy != null && policy.getNonceTtlSeconds() != null
            ? policy.getNonceTtlSeconds()
            : globalSettings.getNonceTtlSeconds();
        if (nonceEnabled) {
            validateNonce(accessKey, nonce, nonceTtl);
        }

        String tenantId = request.getHeader(OpenApiSecurityConstants.HEADER_TENANT_ID);
        String canonical = OpenApiRequestCanonicalizer.canonicalize(
            request, rawBody, timestamp, nonce, tenantId, accessKey
        );
        boolean verified = OpenApiCryptoUtil.verify(
            canonical.getBytes(StandardCharsets.UTF_8),
            signature,
            expectedSignatureAlgorithm,
            credentialService.revealSecretKey(credential),
            credential.getSignaturePublicKey()
        );
        if (!verified) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "签名校验失败");
        }
        return credential;
    }

    private void validateToken(HttpServletRequest request) {
        String authorization = request.getHeader(OpenApiSecurityConstants.HEADER_AUTHORIZATION);
        if (StrUtil.isBlank(authorization) || !authorization.startsWith(OpenApiSecurityConstants.AUTHORIZATION_PREFIX)) {
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
        String cacheKey = OpenApiSecurityKit.buildNonceKey(OpenApiSecurityConstants.NONCE_CACHE_PREFIX, accessKey, nonce);
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

    private void validateAppAndCredentialWindow(SysOpenApp app, SysOpenAppCredential credential) {
        LocalDateTime now = LocalDateTime.now();
        if (credential.getActiveFrom() != null && credential.getActiveFrom().isAfter(now)) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "凭证尚未生效");
        }
        if (credential.getActiveTo() != null && credential.getActiveTo().isBefore(now)) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "凭证已过期");
        }
    }

    private void validateTenant(SysOpenApiPolicy policy, HttpServletRequest request, SysOpenApp app) {
        boolean tenantRequired = policy != null && "1".equals(policy.getTenantRequired());
        if (!tenantRequired) {
            return;
        }
        String tenantId = request.getHeader(OpenApiSecurityConstants.HEADER_TENANT_ID);
        if (StrUtil.isBlank(tenantId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "缺少租户标识");
        }
        if (StrUtil.isNotBlank(app.getTenantId()) && !StrUtil.equals(app.getTenantId(), tenantId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户标识不匹配");
        }
    }

    private void validateIpWhitelist(HttpServletRequest request, SysOpenApp app) {
        if (StrUtil.isBlank(app.getIpWhitelist())) {
            return;
        }
        String remoteIp = request.getRemoteAddr();
        boolean allowed = StrUtil.splitTrim(app.getIpWhitelist(), ',').stream()
            .anyMatch(item -> matchIp(remoteIp, item));
        if (!allowed) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "当前 IP 不在白名单中");
        }
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

    private ApiSignatureAlgorithm resolveSignatureAlgorithm(SysOpenApiPolicy policy, SysOpenAppCredential credential,
                                                            OpenApiGlobalSettingsDto globalSettings, HttpServletRequest request) {
        String configured = firstNonBlank(
            policy != null ? policy.getSignatureAlgorithm() : null,
            credential != null ? credential.getSignatureAlgorithm() : null,
            globalSettings.getDefaultSignatureAlgorithm()
        );
        String header = request.getHeader(OpenApiSecurityConstants.HEADER_SIGNATURE_ALGORITHM);
        if (StrUtil.isNotBlank(header) && !StrUtil.equalsIgnoreCase(header, configured)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "签名算法与服务端配置不一致");
        }
        return ApiSignatureAlgorithm.of(configured, ApiSignatureAlgorithm.HMAC_SHA256);
    }

    private ApiEncryptionAlgorithm resolveEncryptionAlgorithm(SysOpenApiPolicy policy, SysOpenAppCredential credential,
                                                              OpenApiGlobalSettingsDto globalSettings, HttpServletRequest request) {
        boolean policyExplicit = policy != null
            && StrUtil.isNotBlank(policy.getEncryptionAlgorithm())
            && !StrUtil.equalsIgnoreCase(policy.getEncryptionAlgorithm(), ApiEncryptionAlgorithm.NONE.name());
        boolean globalRequired = Boolean.TRUE.equals(globalSettings.getEncryptionRequired());
        String configured = firstNonBlank(
            policy != null ? policy.getEncryptionAlgorithm() : null,
            credential != null ? credential.getEncryptionAlgorithm() : null,
            globalSettings.getDefaultEncryptionAlgorithm()
        );
        ApiEncryptionAlgorithm configuredAlgorithm = ApiEncryptionAlgorithm.of(configured, ApiEncryptionAlgorithm.NONE);
        String header = request.getHeader(OpenApiSecurityConstants.HEADER_ENCRYPT_ALGORITHM);
        if (StrUtil.isBlank(header) && !policyExplicit && !globalRequired) {
            return ApiEncryptionAlgorithm.NONE;
        }
        if (StrUtil.isBlank(header)) {
            return configuredAlgorithm;
        }
        if (!StrUtil.equalsIgnoreCase(header, configuredAlgorithm.name())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "加密算法与服务端配置不一致");
        }
        return configuredAlgorithm;
    }

    private boolean isEncryptionRequired(SysOpenApiPolicy policy, OpenApiGlobalSettingsDto globalSettings,
                                         ApiEncryptionAlgorithm algorithm) {
        boolean policyExplicit = policy != null
            && StrUtil.isNotBlank(policy.getEncryptionAlgorithm())
            && !StrUtil.equalsIgnoreCase(policy.getEncryptionAlgorithm(), ApiEncryptionAlgorithm.NONE.name());
        return algorithm != ApiEncryptionAlgorithm.NONE && (policyExplicit || Boolean.TRUE.equals(globalSettings.getEncryptionRequired()));
    }

    private OpenApiEncryptedPayload tryParseEncryptedPayload(byte[] body, boolean required) throws IOException {
        try {
            OpenApiEncryptedPayload payload = objectMapper.readValue(body, OpenApiEncryptedPayload.class);
            if (payload != null && StrUtil.isNotBlank(payload.getEncrypted())) {
                return payload;
            }
        } catch (Exception ex) {
            if (required) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "请求体不是合法的加密报文");
            }
        }
        if (required) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "请求体缺少加密报文");
        }
        return null;
    }

    private String requireHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (StrUtil.isBlank(value)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "缺少请求头: " + headerName);
        }
        return value.trim();
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
