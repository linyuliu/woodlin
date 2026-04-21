package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.dto.OpenApiCredentialIssueResponse;
import com.mumu.woodlin.system.dto.OpenApiCredentialRequest;
import com.mumu.woodlin.system.dto.OpenApiCredentialView;
import com.mumu.woodlin.system.entity.SysOpenAppCredential;

import java.util.List;

/**
 * 开放应用凭证服务。
 *
 * @author mumu
 * @since 2026-04-13
 */
public interface ISysOpenAppCredentialService extends IService<SysOpenAppCredential> {

    /**
     * 查询应用凭证。
     *
     * @param appId 应用ID
     * @return 凭证列表
     */
    List<OpenApiCredentialView> listByAppId(Long appId);

    /**
     * 签发凭证。
     *
     * @param appId   应用ID
     * @param request 请求
     * @return 凭证结果
     */
    OpenApiCredentialIssueResponse issueCredential(Long appId, OpenApiCredentialRequest request);

    /**
     * 轮换凭证。
     *
     * @param credentialId 凭证ID
     * @param request      请求
     * @return 凭证结果
     */
    OpenApiCredentialIssueResponse rotateCredential(Long credentialId, OpenApiCredentialRequest request);

    /**
     * 吊销凭证。
     *
     * @param credentialId 凭证ID
     * @return 是否成功
     */
    boolean revokeCredential(Long credentialId);

    /**
     * 根据 access key 查询有效凭证。
     *
     * @param accessKey access key
     * @return 凭证
     */
    SysOpenAppCredential getActiveCredentialByAccessKey(String accessKey);

    /**
     * 解密获取 SK。
     *
     * @param credential 凭证
     * @return 明文 SK
     */
    String revealSecretKey(SysOpenAppCredential credential);

    /**
     * 解密获取服务端私钥。
     *
     * @param credential 凭证
     * @return 服务端私钥
     */
    String revealServerPrivateKey(SysOpenAppCredential credential);
}
