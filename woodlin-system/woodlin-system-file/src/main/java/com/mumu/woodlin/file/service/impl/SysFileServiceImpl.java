package com.mumu.woodlin.file.service.impl;

import java.awt.image.BufferedImage;
import com.mumu.woodlin.common.exception.BusinessException;
import java.io.ByteArrayInputStream;
import com.mumu.woodlin.common.exception.BusinessException;
import java.io.InputStream;
import com.mumu.woodlin.common.exception.BusinessException;
import java.util.Arrays;
import com.mumu.woodlin.common.exception.BusinessException;
import java.util.List;
import com.mumu.woodlin.common.exception.BusinessException;

import javax.imageio.ImageIO;
import com.mumu.woodlin.common.exception.BusinessException;

import org.springframework.stereotype.Service;
import com.mumu.woodlin.common.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;
import com.mumu.woodlin.common.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;
import com.mumu.woodlin.common.exception.BusinessException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.common.exception.BusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.dto.UploadTokenRequest;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.entity.SysFile;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.entity.SysUploadPolicy;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.entity.SysUploadToken;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.mapper.SysFileMapper;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.mapper.SysStorageConfigMapper;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.mapper.SysUploadPolicyMapper;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.service.FileTypeDetectionService;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.service.ISysFileService;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.service.ISysUploadTokenService;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.storage.StorageService;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.storage.StorageServiceFactory;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.file.vo.UploadTokenVO;
import com.mumu.woodlin.common.exception.BusinessException;

import cn.hutool.core.io.IoUtil;
import com.mumu.woodlin.common.exception.BusinessException;
import cn.hutool.core.util.IdUtil;
import com.mumu.woodlin.common.exception.BusinessException;
import cn.hutool.crypto.digest.DigestUtil;
import com.mumu.woodlin.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import com.mumu.woodlin.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import com.mumu.woodlin.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import com.mumu.woodlin.common.exception.BusinessException;

/**
 * 文件服务实现
 * 
 * @author mumu
 * @description 文件服务实现，处理文件上传、下载、删除等核心业务逻辑
 * @since 2025-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {
    
    private final SysUploadPolicyMapper uploadPolicyMapper;
    private final SysStorageConfigMapper storageConfigMapper;
    private final ISysUploadTokenService uploadTokenService;
    private final FileTypeDetectionService fileTypeDetectionService;
    private final StorageServiceFactory storageServiceFactory;
    private final HttpServletRequest request;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile uploadFile(MultipartFile file, String token, String signature) {
        try {
            // 1. 验证上传令牌
            SysUploadToken uploadToken = uploadTokenService.validateToken(token, signature);
            
            // 2. 查询上传策略
            SysUploadPolicy policy = uploadPolicyMapper.selectById(uploadToken.getPolicyId());
            if (policy == null || !"1".equals(policy.getStatus())) {
                throw new BusinessException("上传策略不存在或已禁用");
            }
            
            // 3. 验证文件
            validateFile(file, policy, uploadToken);
            
            // 4. 读取文件内容
            byte[] fileBytes = IoUtil.readBytes(file.getInputStream());
            
            // 5. 检测文件类型（如果策略要求）
            String detectedMimeType = null;
            if ("1".equals(policy.getDetectFileType())) {
                detectedMimeType = fileTypeDetectionService.detectMimeType(fileBytes, file.getOriginalFilename());
                
                // 验证MIME类型
                if (policy.getAllowedMimeTypes() != null && !policy.getAllowedMimeTypes().isEmpty()) {
                    List<String> allowedMimes = Arrays.asList(policy.getAllowedMimeTypes().split(","));
                    if (!isAllowedMimeType(detectedMimeType, allowedMimes)) {
                        throw new BusinessException("不允许的文件类型: " + detectedMimeType);
                    }
                }
            }
            
            // 6. 计算文件哈希值
            String md5 = DigestUtil.md5Hex(fileBytes);
            String sha256 = DigestUtil.sha256Hex(fileBytes);
            
            // 7. 检查是否允许重复上传
            if ("0".equals(policy.getAllowDuplicate())) {
                SysFile existingFile = getFileByMd5(md5);
                if (existingFile != null) {
                    log.info("文件已存在，返回已有文件: md5={}, fileId={}", md5, existingFile.getFileId());
                    return existingFile;
                }
            }
            
            // 8. 查询存储配置
            SysStorageConfig storageConfig = storageConfigMapper.selectById(policy.getStorageConfigId());
            if (storageConfig == null || !"1".equals(storageConfig.getStatus())) {
                throw new BusinessException("存储配置不存在或已禁用");
            }
            
            // 9. 生成对象键
            String objectKey = generateObjectKey(policy, file.getOriginalFilename());
            
            // 10. 上传到存储服务
            StorageService storageService = storageServiceFactory.getStorageService(storageConfig.getStorageType());
            String fileUrl = storageService.uploadFile(
                storageConfig,
                objectKey,
                new ByteArrayInputStream(fileBytes),
                file.getContentType(),
                fileBytes.length
            );
            
            // 11. 判断是否为图片并获取尺寸
            Integer imageWidth = null;
            Integer imageHeight = null;
            String isImage = "0";
            
            if (detectedMimeType != null && detectedMimeType.startsWith("image/")) {
                isImage = "1";
                try {
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(fileBytes));
                    if (bufferedImage != null) {
                        imageWidth = bufferedImage.getWidth();
                        imageHeight = bufferedImage.getHeight();
                    }
                } catch (Exception e) {
                    log.warn("读取图片尺寸失败: {}", e.getMessage());
                }
            }
            
            // 12. 保存文件信息
            SysFile sysFile = new SysFile()
                .setFileName(generateFileName(file.getOriginalFilename()))
                .setOriginalName(file.getOriginalFilename())
                .setFilePath(objectKey)
                .setFileUrl(fileUrl)
                .setFileSize(file.getSize())
                .setFileExtension(getFileExtension(file.getOriginalFilename()))
                .setFileType(file.getContentType())
                .setMimeType(file.getContentType())
                .setDetectedMimeType(detectedMimeType)
                .setFileMd5(md5)
                .setFileSha256(sha256)
                .setStorageType(storageConfig.getStorageType())
                .setStorageConfigId(storageConfig.getConfigId())
                .setUploadPolicyId(policy.getPolicyId())
                .setBucketName(storageConfig.getBucketName())
                .setObjectKey(objectKey)
                .setIsImage(isImage)
                .setImageWidth(imageWidth)
                .setImageHeight(imageHeight)
                .setIsPublic(storageConfig.getIsPublic())
                .setUploadIp(getClientIp())
                .setUserAgent(request.getHeader("User-Agent"));
            
            save(sysFile);
            
            // 13. 标记令牌为已使用
            uploadTokenService.markTokenAsUsed(uploadToken.getTokenId(), sysFile.getFileId());
            
            log.info("文件上传成功: fileId={}, fileName={}, size={}", 
                sysFile.getFileId(), sysFile.getFileName(), sysFile.getFileSize());
            
            return sysFile;
            
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile uploadFileWithPolicy(MultipartFile file, String policyCode) {
        // 生成临时令牌
        UploadTokenRequest tokenRequest = new UploadTokenRequest();
        tokenRequest.setPolicyCode(policyCode);
        tokenRequest.setFileName(file.getOriginalFilename());
        tokenRequest.setFileSize(file.getSize());
        
        UploadTokenVO tokenVO = uploadTokenService.generateUploadToken(tokenRequest);
        
        // 使用令牌上传
        return uploadFile(file, tokenVO.getToken(), tokenVO.getSignature());
    }
    
    @Override
    public InputStream downloadFile(Long fileId) {
        // 1. 查询文件信息
        SysFile sysFile = getById(fileId);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 2. 查询存储配置
        SysStorageConfig storageConfig = storageConfigMapper.selectById(sysFile.getStorageConfigId());
        if (storageConfig == null) {
            throw new BusinessException("存储配置不存在");
        }
        
        // 3. 从存储服务下载
        StorageService storageService = storageServiceFactory.getStorageService(storageConfig.getStorageType());
        return storageService.downloadFile(storageConfig, sysFile.getObjectKey());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long fileId) {
        // 1. 查询文件信息
        SysFile sysFile = getById(fileId);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 2. 查询存储配置
        SysStorageConfig storageConfig = storageConfigMapper.selectById(sysFile.getStorageConfigId());
        if (storageConfig != null) {
            try {
                // 3. 从存储服务删除
                StorageService storageService = storageServiceFactory.getStorageService(storageConfig.getStorageType());
                storageService.deleteFile(storageConfig, sysFile.getObjectKey());
            } catch (Exception e) {
                log.error("从存储服务删除文件失败: {}", e.getMessage(), e);
            }
        }
        
        // 4. 删除数据库记录
        return removeById(fileId);
    }
    
    @Override
    public String getFileUrl(Long fileId, int expirationTime) {
        // 1. 查询文件信息
        SysFile sysFile = getById(fileId);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 2. 如果是公开文件，直接返回URL
        if ("1".equals(sysFile.getIsPublic())) {
            return sysFile.getFileUrl();
        }
        
        // 3. 查询存储配置
        SysStorageConfig storageConfig = storageConfigMapper.selectById(sysFile.getStorageConfigId());
        if (storageConfig == null) {
            throw new BusinessException("存储配置不存在");
        }
        
        // 4. 生成预签名URL
        if (expirationTime <= 0) {
            return sysFile.getFileUrl();
        }
        
        StorageService storageService = storageServiceFactory.getStorageService(storageConfig.getStorageType());
        return storageService.generatePresignedUrl(storageConfig, sysFile.getObjectKey(), expirationTime);
    }
    
    @Override
    public SysFile getFileByMd5(String md5) {
        return getOne(
            new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getFileMd5, md5)
                .last("LIMIT 1")
        );
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, SysUploadPolicy policy, SysUploadToken uploadToken) {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 2. 检查文件大小
        if ("1".equals(policy.getCheckFileSize())) {
            Long maxSize = uploadToken.getMaxFileSize() != null 
                ? uploadToken.getMaxFileSize() 
                : policy.getMaxFileSize();
            
            if (file.getSize() > maxSize) {
                throw new BusinessException("文件大小超过限制: " + formatFileSize(maxSize));
            }
        }
        
        // 3. 检查文件扩展名
        String allowedExtensions = uploadToken.getAllowedExtensions() != null 
            ? uploadToken.getAllowedExtensions() 
            : policy.getAllowedExtensions();
        
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            String fileExtension = getFileExtension(file.getOriginalFilename());
            if (fileExtension == null || fileExtension.isEmpty()) {
                throw new BusinessException("文件没有扩展名");
            }
            
            List<String> allowedExts = Arrays.asList(allowedExtensions.toLowerCase().split(","));
            if (!allowedExts.contains(fileExtension.toLowerCase())) {
                throw new BusinessException("不允许的文件扩展名: " + fileExtension);
            }
        }
    }
    
    /**
     * 判断MIME类型是否允许
     */
    private boolean isAllowedMimeType(String mimeType, List<String> allowedMimes) {
        for (String allowedMime : allowedMimes) {
            String trimmed = allowedMime.trim();
            if (trimmed.endsWith("/*")) {
                String prefix = trimmed.substring(0, trimmed.length() - 2);
                if (mimeType.startsWith(prefix)) {
                    return true;
                }
            } else if (trimmed.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 生成对象键
     */
    private String generateObjectKey(SysUploadPolicy policy, String originalFilename) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        String path = policy.getPathPattern()
            .replace("{yyyy}", String.format("%04d", now.getYear()))
            .replace("{MM}", String.format("%02d", now.getMonthValue()))
            .replace("{dd}", String.format("%02d", now.getDayOfMonth()))
            .replace("{HH}", String.format("%02d", now.getHour()));
        
        String namePattern = policy.getFileNamePattern();
        String generatedName = namePattern
            .replace("{timestamp}", String.valueOf(System.currentTimeMillis()))
            .replace("{random}", IdUtil.fastSimpleUUID().substring(0, 8))
            .replace("{uuid}", IdUtil.fastSimpleUUID());
        
        if (namePattern.contains("{original}") && originalFilename != null) {
            String originalNameWithoutExt = originalFilename.contains(".") 
                ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) 
                : originalFilename;
            generatedName = generatedName.replace("{original}", originalNameWithoutExt);
        }
        
        String extension = getFileExtension(originalFilename);
        if (extension != null && !extension.isEmpty()) {
            extension = "." + extension;
        } else {
            extension = "";
        }
        
        return path + generatedName + extension;
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String fileName = IdUtil.fastSimpleUUID();
        return extension != null && !extension.isEmpty() ? fileName + "." + extension : fileName;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
