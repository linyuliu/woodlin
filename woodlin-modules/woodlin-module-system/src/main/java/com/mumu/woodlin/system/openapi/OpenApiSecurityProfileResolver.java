package com.mumu.woodlin.system.openapi;

import cn.hutool.core.util.StrUtil;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSecurityMode;
import com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.system.dto.OpenApiGlobalSettingsDto;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.entity.SysOpenAppCredential;
import com.mumu.woodlin.system.model.EffectiveOpenApiSecurityProfile;
import com.mumu.woodlin.system.model.OpenApiRuntimeContext;
import com.mumu.woodlin.system.util.OpenApiSecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 开放 API 生效安全配置解析器。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Component
public class OpenApiSecurityProfileResolver {

    /**
     * 解析认证前安全配置。
     *
     * @param request 请求
     * @param context 运行时上下文
     * @return 生效安全配置
     */
    public EffectiveOpenApiSecurityProfile resolveInitial(HttpServletRequest request, OpenApiRuntimeContext context) {
        return resolve(request, context, null, null, false);
    }

    /**
     * 解析认证后的完整安全配置。
     *
     * @param request    请求
     * @param context    运行时上下文
     * @param credential 凭证
     * @param app        开放应用
     * @return 生效安全配置
     */
    public EffectiveOpenApiSecurityProfile resolve(HttpServletRequest request, OpenApiRuntimeContext context,
                                                   SysOpenAppCredential credential, SysOpenApp app) {
        return resolve(request, context, credential, app, true);
    }

    private EffectiveOpenApiSecurityProfile resolve(HttpServletRequest request, OpenApiRuntimeContext context,
                                                    SysOpenAppCredential credential, SysOpenApp app,
                                                    boolean validateClientHeaders) {
        SysOpenApiPolicy policy = context == null ? null : context.getPolicy();
        OpenApiGlobalSettingsDto globalSettings = context == null ? null : context.getGlobalSettings();
        boolean policySecurityModeExplicit = policy != null && StrUtil.isNotBlank(policy.getSecurityMode());
        String modeValue = firstNonBlank(
            policySecurityModeExplicit ? policy.getSecurityMode() : null,
            credential == null ? null : credential.getSecurityMode(),
            globalSettings == null ? null : globalSettings.getDefaultMode()
        );
        ApiSecurityMode securityMode = ApiSecurityMode.of(modeValue, ApiSecurityMode.AKSK);
        if (!policySecurityModeExplicit && credential == null
            && securityMode == ApiSecurityMode.AKSK
            && StrUtil.isNotBlank(request.getHeader(OpenApiSecurityConstants.HEADER_APP_KEY))) {
            securityMode = ApiSecurityMode.APP_KEY;
        }

        ApiSignatureAlgorithm signatureAlgorithm = resolveSignatureAlgorithm(policy, credential, globalSettings);
        ApiEncryptionAlgorithm encryptionAlgorithm = resolveEncryptionAlgorithm(policy, credential, globalSettings);
        if (validateClientHeaders) {
            validateDeclaredAlgorithm(
                request.getHeader(OpenApiSecurityConstants.HEADER_SIGNATURE_ALGORITHM),
                signatureAlgorithm.name(),
                "签名算法与服务端配置不一致"
            );
            validateDeclaredAlgorithm(
                request.getHeader(OpenApiSecurityConstants.HEADER_ENCRYPT_ALGORITHM),
                encryptionAlgorithm.name(),
                "加密算法与服务端配置不一致"
            );
        }

        return new EffectiveOpenApiSecurityProfile()
            .setSecurityMode(securityMode)
            .setSignatureAlgorithm(signatureAlgorithm)
            .setEncryptionAlgorithm(encryptionAlgorithm)
            .setEncryptionRequired(isEncryptionRequired(policy, globalSettings, encryptionAlgorithm))
            .setTimestampWindowSeconds(resolveTimestampWindow(policy, globalSettings))
            .setNonceEnabled(resolveNonceEnabled(policy, globalSettings))
            .setNonceTtlSeconds(resolveNonceTtl(policy, globalSettings))
            .setTenantRequired(policy != null && "1".equals(policy.getTenantRequired()))
            .setIpWhitelist(app == null ? null : app.getIpWhitelist())
            .setQuotaEnabled(true)
            .setPolicy(policy);
    }

    private ApiSignatureAlgorithm resolveSignatureAlgorithm(SysOpenApiPolicy policy,
                                                            SysOpenAppCredential credential,
                                                            OpenApiGlobalSettingsDto globalSettings) {
        String configured = firstNonBlank(
            policy == null ? null : policy.getSignatureAlgorithm(),
            credential == null ? null : credential.getSignatureAlgorithm(),
            globalSettings == null ? null : globalSettings.getDefaultSignatureAlgorithm()
        );
        return ApiSignatureAlgorithm.of(configured, ApiSignatureAlgorithm.HMAC_SHA256);
    }

    private ApiEncryptionAlgorithm resolveEncryptionAlgorithm(SysOpenApiPolicy policy,
                                                              SysOpenAppCredential credential,
                                                              OpenApiGlobalSettingsDto globalSettings) {
        String configured = firstNonBlank(
            policy == null ? null : policy.getEncryptionAlgorithm(),
            credential == null ? null : credential.getEncryptionAlgorithm(),
            globalSettings == null ? null : globalSettings.getDefaultEncryptionAlgorithm()
        );
        return ApiEncryptionAlgorithm.of(configured, ApiEncryptionAlgorithm.NONE);
    }

    private boolean isEncryptionRequired(SysOpenApiPolicy policy, OpenApiGlobalSettingsDto globalSettings,
                                         ApiEncryptionAlgorithm algorithm) {
        if (algorithm == ApiEncryptionAlgorithm.NONE) {
            return false;
        }
        if (policy != null && StrUtil.isNotBlank(policy.getEncryptionAlgorithm())) {
            return !StrUtil.equalsIgnoreCase(policy.getEncryptionAlgorithm(), ApiEncryptionAlgorithm.NONE.name());
        }
        return globalSettings != null && Boolean.TRUE.equals(globalSettings.getEncryptionRequired());
    }

    private int resolveTimestampWindow(SysOpenApiPolicy policy, OpenApiGlobalSettingsDto globalSettings) {
        Integer policyWindow = policy == null ? null : policy.getTimestampWindowSeconds();
        Integer globalWindow = globalSettings == null ? null : globalSettings.getTimestampWindowSeconds();
        return policyWindow != null ? policyWindow : defaultInt(globalWindow, 300);
    }

    private boolean resolveNonceEnabled(SysOpenApiPolicy policy, OpenApiGlobalSettingsDto globalSettings) {
        if (policy != null && StrUtil.isNotBlank(policy.getNonceEnabled())) {
            return "1".equals(policy.getNonceEnabled()) || Boolean.parseBoolean(policy.getNonceEnabled());
        }
        return globalSettings == null || !Boolean.FALSE.equals(globalSettings.getNonceEnabled());
    }

    private int resolveNonceTtl(SysOpenApiPolicy policy, OpenApiGlobalSettingsDto globalSettings) {
        Integer policyTtl = policy == null ? null : policy.getNonceTtlSeconds();
        Integer globalTtl = globalSettings == null ? null : globalSettings.getNonceTtlSeconds();
        return policyTtl != null ? policyTtl : defaultInt(globalTtl, 300);
    }

    private void validateDeclaredAlgorithm(String declared, String configured, String message) {
        if (StrUtil.isNotBlank(declared) && !StrUtil.equalsIgnoreCase(declared, configured)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, message);
        }
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
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
