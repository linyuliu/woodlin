package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSecurityMode;
import com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.system.dto.OpenApiGlobalSettingsDto;
import com.mumu.woodlin.system.dto.OpenApiOverviewDto;
import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import com.mumu.woodlin.system.model.OpenApiRuntimeContext;
import com.mumu.woodlin.system.service.*;
import com.mumu.woodlin.system.util.OpenApiSecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 开放 API 安全服务实现。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Service
@RequiredArgsConstructor
public class OpenApiSecurityServiceImpl implements IOpenApiSecurityService {

    private final ISysConfigService configService;
    private final ISysOpenAppService openAppService;
    private final ISysOpenAppCredentialService credentialService;
    private final ISysOpenApiPolicyService policyService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public OpenApiGlobalSettingsDto getGlobalSettings() {
        OpenApiGlobalSettingsDto dto = new OpenApiGlobalSettingsDto();
        dto.setDefaultMode(getConfigValue(OpenApiSecurityConstants.CONFIG_DEFAULT_MODE, ApiSecurityMode.AKSK.name()));
        dto.setDefaultSignatureAlgorithm(getConfigValue(
            OpenApiSecurityConstants.CONFIG_DEFAULT_SIGNATURE_ALGORITHM, ApiSignatureAlgorithm.HMAC_SHA256.name()
        ));
        dto.setTimestampWindowSeconds(Integer.parseInt(getConfigValue(
            OpenApiSecurityConstants.CONFIG_TIMESTAMP_WINDOW, "300"
        )));
        dto.setNonceEnabled(Boolean.parseBoolean(getConfigValue(
            OpenApiSecurityConstants.CONFIG_NONCE_ENABLED, "true"
        )));
        dto.setNonceTtlSeconds(Integer.parseInt(getConfigValue(
            OpenApiSecurityConstants.CONFIG_NONCE_TTL, "300"
        )));
        dto.setDefaultEncryptionAlgorithm(getConfigValue(
            OpenApiSecurityConstants.CONFIG_DEFAULT_ENCRYPTION_ALGORITHM, ApiEncryptionAlgorithm.AES_GCM.name()
        ));
        dto.setEncryptionRequired(Boolean.parseBoolean(getConfigValue(
            OpenApiSecurityConstants.CONFIG_ENCRYPTION_REQUIRED, "false"
        )));
        dto.setGmEnabled(Boolean.parseBoolean(getConfigValue(OpenApiSecurityConstants.CONFIG_GM_ENABLED, "true")));
        return dto;
    }

    @Override
    public void updateGlobalSettings(OpenApiGlobalSettingsDto settings) {
        Map<String, String> configs = new LinkedHashMap<>();
        configs.put(OpenApiSecurityConstants.CONFIG_DEFAULT_MODE, settings.getDefaultMode());
        configs.put(OpenApiSecurityConstants.CONFIG_DEFAULT_SIGNATURE_ALGORITHM, settings.getDefaultSignatureAlgorithm());
        configs.put(OpenApiSecurityConstants.CONFIG_TIMESTAMP_WINDOW, String.valueOf(settings.getTimestampWindowSeconds()));
        configs.put(OpenApiSecurityConstants.CONFIG_NONCE_ENABLED, String.valueOf(settings.getNonceEnabled()));
        configs.put(OpenApiSecurityConstants.CONFIG_NONCE_TTL, String.valueOf(settings.getNonceTtlSeconds()));
        configs.put(OpenApiSecurityConstants.CONFIG_DEFAULT_ENCRYPTION_ALGORITHM, settings.getDefaultEncryptionAlgorithm());
        configs.put(OpenApiSecurityConstants.CONFIG_ENCRYPTION_REQUIRED, String.valueOf(settings.getEncryptionRequired()));
        configs.put(OpenApiSecurityConstants.CONFIG_GM_ENABLED, String.valueOf(settings.getGmEnabled()));

        configs.forEach(this::upsertConfig);
        configService.evictCache();
    }

    @Override
    public OpenApiOverviewDto getOverview() {
        OpenApiOverviewDto dto = new OpenApiOverviewDto();
        dto.setAppCount(openAppService.count());
        dto.setCredentialCount(credentialService.count());
        dto.setPolicyCount(policyService.count());
        return dto;
    }

    @Override
    public OpenApiRuntimeContext resolveRuntimeContext(String requestPath, String httpMethod) {
        List<SysOpenApiPolicy> policies = listEnabledPolicies();
        SysOpenApiPolicy matched = policies.stream()
            .filter(item -> antPathMatcher.match(item.getPathPattern(), requestPath))
            .filter(item -> "*".equals(item.getHttpMethod()) || "ALL".equalsIgnoreCase(item.getHttpMethod())
                || StrUtil.equalsIgnoreCase(item.getHttpMethod(), httpMethod))
            .findFirst()
            .orElse(null);
        return OpenApiRuntimeContext.builder()
            .policyMatched(matched != null)
            .policy(matched)
            .globalSettings(getGlobalSettings())
            .build();
    }

    @Override
    public List<SysOpenApiPolicy> listEnabledPolicies() {
        return policyService.listEnabledPoliciesWithCache();
    }

    private String getConfigValue(String key, String defaultValue) {
        String value = configService.getConfigValueByKey(key);
        return StrUtil.isBlank(value) ? defaultValue : value;
    }

    private void upsertConfig(String key, String value) {
        SysConfig existing = configService.getByKeyWithCache(key);
        if (existing != null) {
            existing.setConfigValue(value);
            configService.updateById(existing);
            return;
        }
        SysConfig config = new SysConfig();
        config.setConfigName(key);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType("Y");
        config.setTenantId("default");
        config.setRemark("开放API安全中心配置");
        configService.save(config);
    }
}
