package com.mumu.woodlin.file.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mumu.woodlin.file.dto.UploadTokenRequest;
import com.mumu.woodlin.file.entity.SysUploadPolicy;
import com.mumu.woodlin.file.entity.SysUploadToken;
import com.mumu.woodlin.file.mapper.SysUploadPolicyMapper;
import com.mumu.woodlin.file.mapper.SysUploadTokenMapper;
import com.mumu.woodlin.file.service.ISysUploadTokenService;
import com.mumu.woodlin.file.storage.StorageServiceFactory;
import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.mapper.SysStorageConfigMapper;
import com.mumu.woodlin.file.storage.StorageService;
import com.mumu.woodlin.file.vo.UploadTokenVO;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.exception.BusinessException;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传令牌服务实现
 * 
 * @author mumu
 * @description 上传令牌服务实现，负责生成签名和验证上传请求
 * @since 2025-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUploadTokenServiceImpl implements ISysUploadTokenService {
    
    private final SysUploadTokenMapper uploadTokenMapper;
    private final SysUploadPolicyMapper uploadPolicyMapper;
    private final SysStorageConfigMapper storageConfigMapper;
    private final StorageServiceFactory storageServiceFactory;
    
    @Override
    public UploadTokenVO generateUploadToken(UploadTokenRequest request) {
        // 1. 查询上传策略
        SysUploadPolicy policy = uploadPolicyMapper.selectOne(
            new LambdaQueryWrapper<SysUploadPolicy>()
                .eq(SysUploadPolicy::getPolicyCode, request.getPolicyCode())
                .eq(SysUploadPolicy::getStatus, CommonConstant.STATUS_ENABLE)
        );
        
        if (policy == null) {
            log.warn("上传策略不存在或已禁用: policyCode={}", request.getPolicyCode());
            throw new BusinessException("上传策略不存在或已禁用: " + request.getPolicyCode());
        }
        
        // 2. 查询存储配置
        SysStorageConfig storageConfig = storageConfigMapper.selectById(policy.getStorageConfigId());
        if (storageConfig == null || !CommonConstant.STATUS_ENABLE.equals(storageConfig.getStatus())) {
            log.warn("存储配置不存在或已禁用: storageConfigId={}", policy.getStorageConfigId());
            throw new BusinessException("存储配置不存在或已禁用");
        }
        
        // 3. 生成对象键（文件路径）
        String objectKey = generateObjectKey(policy, request.getFileName());
        
        // 4. 生成令牌和签名
        String token = UUID.randomUUID().toString().replace("-", "");
        String signature = generateSignature(token, policy.getPolicyId(), objectKey);
        
        // 5. 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(policy.getSignatureExpires());
        
        // 6. 保存令牌到数据库
        SysUploadToken uploadToken = new SysUploadToken()
            .setToken(token)
            .setPolicyId(policy.getPolicyId())
            .setSignature(signature)
            .setMaxFileSize(policy.getMaxFileSize())
            .setAllowedExtensions(policy.getAllowedExtensions())
            .setExpireTime(expireTime)
            .setIsUsed(CommonConstant.STATUS_DISABLE);
        
        uploadTokenMapper.insert(uploadToken);
        
        // 7. 生成上传凭证（用于前端直传）
        StorageService storageService = storageServiceFactory.getStorageService(storageConfig.getStorageType());
        String credential = storageService.generateUploadCredential(
            storageConfig, 
            objectKey, 
            policy.getSignatureExpires()
        );
        
        // 8. 构建返回对象
        return new UploadTokenVO()
            .setToken(token)
            .setSignature(signature)
            .setCredential(credential)
            .setExpireTime(expireTime)
            .setMaxFileSize(policy.getMaxFileSize())
            .setAllowedExtensions(policy.getAllowedExtensions())
            .setStorageType(storageConfig.getStorageType())
            .setObjectKey(objectKey);
    }
    
    @Override
    public SysUploadToken validateToken(String token, String signature) {
        // 1. 查询令牌
        SysUploadToken uploadToken = uploadTokenMapper.selectOne(
            new LambdaQueryWrapper<SysUploadToken>()
                .eq(SysUploadToken::getToken, token)
        );
        
        if (uploadToken == null) {
            log.warn("上传令牌不存在: token={}", token);
            throw new BusinessException("上传令牌不存在");
        }
        
        // 2. 验证签名
        if (!signature.equals(uploadToken.getSignature())) {
            log.warn("上传令牌签名验证失败: token={}, expectedSignature={}, actualSignature={}", 
                token, uploadToken.getSignature(), signature);
            throw new BusinessException("上传令牌签名验证失败");
        }
        
        // 3. 检查是否过期
        if (LocalDateTime.now().isAfter(uploadToken.getExpireTime())) {
            log.warn("上传令牌已过期: token={}, expireTime={}", token, uploadToken.getExpireTime());
            throw new BusinessException("上传令牌已过期");
        }
        
        // 4. 检查是否已使用
        if (CommonConstant.STATUS_ENABLE.equals(uploadToken.getIsUsed())) {
            log.warn("上传令牌已使用: token={}", token);
            throw new BusinessException("上传令牌已使用");
        }
        
        return uploadToken;
    }
    
    @Override
    public void markTokenAsUsed(Long tokenId, Long fileId) {
        uploadTokenMapper.update(null,
            new LambdaUpdateWrapper<SysUploadToken>()
                .set(SysUploadToken::getIsUsed, CommonConstant.STATUS_ENABLE)
                .set(SysUploadToken::getUsedTime, LocalDateTime.now())
                .set(SysUploadToken::getFileId, fileId)
                .eq(SysUploadToken::getTokenId, tokenId)
        );
    }
    
    @Override
    public int cleanExpiredTokens() {
        int count = uploadTokenMapper.delete(
            new LambdaQueryWrapper<SysUploadToken>()
                .lt(SysUploadToken::getExpireTime, LocalDateTime.now())
        );
        
        log.info("清理过期上传令牌: count={}", count);
        return count;
    }
    
    /**
     * 生成对象键（文件路径）
     *
     * @param policy   上传策略
     * @param fileName 文件名
     * @return 对象键
     */
    private String generateObjectKey(SysUploadPolicy policy, String fileName) {
        LocalDateTime now = LocalDateTime.now();
        
        // 解析路径模式
        String path = policy.getPathPattern()
            .replace("{yyyy}", String.format("%04d", now.getYear()))
            .replace("{MM}", String.format("%02d", now.getMonthValue()))
            .replace("{dd}", String.format("%02d", now.getDayOfMonth()))
            .replace("{HH}", String.format("%02d", now.getHour()));
        
        // 解析文件名模式
        String namePattern = policy.getFileNamePattern();
        String generatedName = namePattern
            .replace("{timestamp}", String.valueOf(System.currentTimeMillis()))
            .replace("{random}", IdUtil.fastSimpleUUID().substring(0, 8))
            .replace("{uuid}", IdUtil.fastSimpleUUID());
        
        // 如果包含{original}占位符，需要文件名
        if (namePattern.contains("{original}") && fileName != null) {
            String originalNameWithoutExt = fileName.contains(".") 
                ? fileName.substring(0, fileName.lastIndexOf(".")) 
                : fileName;
            generatedName = generatedName.replace("{original}", originalNameWithoutExt);
        }
        
        // 获取文件扩展名
        String extension = "";
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        
        // 组合完整路径
        return path + generatedName + extension;
    }
    
    /**
     * 生成签名
     *
     * @param token     令牌
     * @param policyId  策略ID
     * @param objectKey 对象键
     * @return 签名
     */
    private String generateSignature(String token, Long policyId, String objectKey) {
        String data = token + ":" + policyId + ":" + objectKey;
        return SecureUtil.md5(data);
    }
}
