package com.mumu.woodlin.file.service;

import com.mumu.woodlin.file.dto.UploadTokenRequest;
import com.mumu.woodlin.file.entity.SysUploadToken;
import com.mumu.woodlin.file.vo.UploadTokenVO;

/**
 * 上传令牌服务接口
 * 
 * @author mumu
 * @description 上传令牌服务，用于生成和验证上传签名
 * @since 2025-01-30
 */
public interface ISysUploadTokenService {
    
    /**
     * 生成上传令牌
     *
     * @param request 上传令牌请求
     * @return 上传令牌信息
     */
    UploadTokenVO generateUploadToken(UploadTokenRequest request);
    
    /**
     * 验证上传令牌
     *
     * @param token     令牌
     * @param signature 签名
     * @return 上传令牌实体
     */
    SysUploadToken validateToken(String token, String signature);
    
    /**
     * 标记令牌为已使用
     *
     * @param tokenId 令牌ID
     * @param fileId  文件ID
     */
    void markTokenAsUsed(Long tokenId, Long fileId);
    
    /**
     * 清理过期令牌
     *
     * @return 清理数量
     */
    int cleanExpiredTokens();
}
