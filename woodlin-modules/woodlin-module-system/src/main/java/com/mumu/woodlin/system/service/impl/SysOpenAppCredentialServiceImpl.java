package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.openapi.OpenApiSecurityKit;
import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSecurityMode;
import com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.common.openapi.model.OpenApiAsymmetricKeyPair;
import com.mumu.woodlin.system.config.OpenApiSecurityProperties;
import com.mumu.woodlin.system.dto.OpenApiCredentialIssueResponse;
import com.mumu.woodlin.system.dto.OpenApiCredentialRequest;
import com.mumu.woodlin.system.dto.OpenApiCredentialView;
import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.entity.SysOpenAppCredential;
import com.mumu.woodlin.system.mapper.SysOpenAppCredentialMapper;
import com.mumu.woodlin.system.service.ISysConfigService;
import com.mumu.woodlin.system.service.ISysOpenAppCredentialService;
import com.mumu.woodlin.system.service.ISysOpenAppService;
import com.mumu.woodlin.system.util.OpenApiSecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 开放应用凭证服务实现。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Service
@RequiredArgsConstructor
public class SysOpenAppCredentialServiceImpl extends ServiceImpl<SysOpenAppCredentialMapper, SysOpenAppCredential>
    implements ISysOpenAppCredentialService {

    private final ISysOpenAppService openAppService;
    private final ISysConfigService configService;
    private final OpenApiSecurityProperties properties;

    @Override
    public List<OpenApiCredentialView> listByAppId(Long appId) {
        LambdaQueryWrapper<SysOpenAppCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenAppCredential::getAppId, appId);
        wrapper.eq(SysOpenAppCredential::getDeleted, "0");
        wrapper.orderByDesc(SysOpenAppCredential::getLastRotatedTime)
            .orderByDesc(SysOpenAppCredential::getCreateTime);
        return list(wrapper).stream().map(this::toView).toList();
    }

    @Override
    public OpenApiCredentialIssueResponse issueCredential(Long appId, OpenApiCredentialRequest request) {
        SysOpenApp app = requireActiveApp(appId);
        SysOpenAppCredential credential = new SysOpenAppCredential();
        credential.setAppId(app.getAppId());
        CredentialIssueMaterial material = fillCredential(credential, request);
        ensureCredentialNameUnique(appId, request.getCredentialName(), null);
        save(credential);
        return buildIssueResponse(credential, material);
    }

    @Override
    public OpenApiCredentialIssueResponse rotateCredential(Long credentialId, OpenApiCredentialRequest request) {
        SysOpenAppCredential existing = requireCredential(credentialId);
        ensureCredentialNameUnique(existing.getAppId(), request.getCredentialName(), credentialId);
        CredentialIssueMaterial material = fillCredential(existing, request);
        updateById(existing);
        return buildIssueResponse(existing, material);
    }

    @Override
    public boolean revokeCredential(Long credentialId) {
        SysOpenAppCredential credential = requireCredential(credentialId);
        credential.setStatus("0");
        credential.setActiveTo(LocalDateTime.now());
        return updateById(credential);
    }

    @Override
    public SysOpenAppCredential getActiveCredentialByAccessKey(String accessKey) {
        LambdaQueryWrapper<SysOpenAppCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenAppCredential::getAccessKey, accessKey);
        wrapper.eq(SysOpenAppCredential::getStatus, "1");
        wrapper.eq(SysOpenAppCredential::getDeleted, "0");
        wrapper.last("LIMIT 1");
        return getOne(wrapper, false);
    }

    @Override
    public String revealSecretKey(SysOpenAppCredential credential) {
        return OpenApiSecurityKit.reveal(credential.getSecretKeyEncrypted(), resolveMasterKey());
    }

    @Override
    public String revealServerPrivateKey(SysOpenAppCredential credential) {
        return OpenApiSecurityKit.reveal(credential.getServerPrivateKeyEncrypted(), resolveMasterKey());
    }

    private CredentialIssueMaterial fillCredential(SysOpenAppCredential credential, OpenApiCredentialRequest request) {
        String masterKey = resolveMasterKey();
        ApiSignatureAlgorithm signatureAlgorithm = ApiSignatureAlgorithm.of(
            request.getSignatureAlgorithm(), ApiSignatureAlgorithm.HMAC_SHA256
        );
        ApiEncryptionAlgorithm encryptionAlgorithm = ApiEncryptionAlgorithm.of(
            request.getEncryptionAlgorithm(), ApiEncryptionAlgorithm.NONE
        );
        ApiSecurityMode securityMode = ApiSecurityMode.of(request.getSecurityMode(), ApiSecurityMode.AKSK);
        validateAlgorithmCombination(signatureAlgorithm, encryptionAlgorithm);
        ensureGuoMiAllowed(signatureAlgorithm, encryptionAlgorithm);

        CredentialIssueMaterial material = new CredentialIssueMaterial();
        String secretKey = OpenApiSecurityKit.generateSecretKey();
        credential.setCredentialName(request.getCredentialName());
        credential.setAccessKey(OpenApiSecurityKit.generateAccessKey());
        credential.setSecretKeyEncrypted(OpenApiSecurityKit.protect(secretKey, masterKey));
        credential.setSecretKeyFingerprint(OpenApiSecurityKit.fingerprint(secretKey));
        credential.setSignatureAlgorithm(signatureAlgorithm.name());
        credential.setEncryptionAlgorithm(encryptionAlgorithm.name());
        credential.setSecurityMode(securityMode.name());
        credential.setActiveFrom(request.getActiveFrom() == null ? LocalDateTime.now() : request.getActiveFrom());
        credential.setActiveTo(request.getActiveTo());
        credential.setLastRotatedTime(LocalDateTime.now());
        credential.setStatus("1");
        credential.setRemark(request.getRemark());
        credential.setSignaturePublicKey(null);
        credential.setEncryptionPublicKey(null);
        credential.setServerPublicKey(null);
        credential.setServerPrivateKeyEncrypted(null);
        material.setSecretKey(secretKey);

        if (!signatureAlgorithm.isHmacFamily()) {
            OpenApiAsymmetricKeyPair signingKeyPair = signatureAlgorithm == ApiSignatureAlgorithm.RSA_SHA256
                ? OpenApiSecurityKit.generateRsaKeyPair()
                : OpenApiSecurityKit.generateSm2KeyPair();
            credential.setSignaturePublicKey(signingKeyPair.getPublicKey());
            material.setSignaturePrivateKey(signingKeyPair.getPrivateKey());
        }

        if (encryptionAlgorithm == ApiEncryptionAlgorithm.RSA_OAEP_SHA256) {
            OpenApiAsymmetricKeyPair clientKeyPair = OpenApiSecurityKit.generateRsaKeyPair();
            OpenApiAsymmetricKeyPair serverKeyPair = OpenApiSecurityKit.generateRsaKeyPair();
            credential.setEncryptionPublicKey(clientKeyPair.getPublicKey());
            credential.setServerPublicKey(serverKeyPair.getPublicKey());
            credential.setServerPrivateKeyEncrypted(OpenApiSecurityKit.protect(serverKeyPair.getPrivateKey(), masterKey));
            material.setEncryptionPrivateKey(clientKeyPair.getPrivateKey());
        }
        return material;
    }

    private OpenApiCredentialIssueResponse buildIssueResponse(SysOpenAppCredential credential,
                                                              CredentialIssueMaterial material) {
        OpenApiCredentialIssueResponse response = new OpenApiCredentialIssueResponse();
        response.setCredential(toView(credential));
        response.setSecretKey(material.getSecretKey());
        response.setSignaturePrivateKey(material.getSignaturePrivateKey());
        response.setEncryptionPrivateKey(material.getEncryptionPrivateKey());
        return response;
    }

    private OpenApiCredentialView toView(SysOpenAppCredential entity) {
        OpenApiCredentialView view = BeanUtil.copyProperties(entity, OpenApiCredentialView.class);
        view.setServerPublicKey(entity.getServerPublicKey());
        return view;
    }

    private SysOpenApp requireActiveApp(Long appId) {
        SysOpenApp app = openAppService.getById(appId);
        if (app == null || "1".equals(app.getDeleted())) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "开放应用不存在");
        }
        if (!"1".equals(app.getStatus())) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "开放应用已停用");
        }
        return app;
    }

    private SysOpenAppCredential requireCredential(Long credentialId) {
        SysOpenAppCredential credential = getById(credentialId);
        if (credential == null || "1".equals(credential.getDeleted())) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "开放应用凭证不存在");
        }
        return credential;
    }

    private void ensureCredentialNameUnique(Long appId, String credentialName, Long excludeId) {
        LambdaQueryWrapper<SysOpenAppCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenAppCredential::getAppId, appId);
        wrapper.eq(SysOpenAppCredential::getCredentialName, credentialName);
        wrapper.eq(SysOpenAppCredential::getDeleted, "0");
        wrapper.ne(excludeId != null, SysOpenAppCredential::getCredentialId, excludeId);
        if (count(wrapper) > 0) {
            throw BusinessException.of(ResultCode.CONFLICT, "凭证名称已存在");
        }
    }

    private void validateAlgorithmCombination(ApiSignatureAlgorithm signatureAlgorithm,
                                              ApiEncryptionAlgorithm encryptionAlgorithm) {
        if (signatureAlgorithm == ApiSignatureAlgorithm.SM2_SM3
            && encryptionAlgorithm == ApiEncryptionAlgorithm.RSA_OAEP_SHA256) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "SM2 签名与 RSA_OAEP 加密不能共用同一凭证");
        }
    }

    private void ensureGuoMiAllowed(ApiSignatureAlgorithm signatureAlgorithm,
                                    ApiEncryptionAlgorithm encryptionAlgorithm) {
        String gmEnabled = configService.getConfigValueByKey(OpenApiSecurityConstants.CONFIG_GM_ENABLED);
        boolean enabled = !"false".equalsIgnoreCase(StrUtil.blankToDefault(gmEnabled, "true"));
        if (!enabled && (signatureAlgorithm.isGuoMi() || encryptionAlgorithm.isGuoMi())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "当前环境未开启国密能力");
        }
    }

    private String resolveMasterKey() {
        if (StrUtil.isNotBlank(properties.getMasterKey())) {
            return properties.getMasterKey();
        }
        SysConfig config = configService.getByKeyWithCache(OpenApiSecurityConstants.CONFIG_MASTER_KEY);
        if (config != null && StrUtil.isNotBlank(config.getConfigValue())) {
            return config.getConfigValue();
        }
        throw BusinessException.of(ResultCode.SYSTEM_CONFIG_ERROR,
            "未配置开放API主密钥，请设置 woodlin.openapi.master-key 或 api.security.master-key");
    }

    /**
     * 凭证签发材料，仅在内存中保留。
     */
    @lombok.Data
    private static class CredentialIssueMaterial {
        private String secretKey;
        private String signaturePrivateKey;
        private String encryptionPrivateKey;
    }
}
