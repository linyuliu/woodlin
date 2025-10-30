package com.mumu.woodlin.file.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.enums.StorageType;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地存储服务实现
 * 
 * @author mumu
 * @description 本地文件系统存储实现，文件存储在服务器本地磁盘
 * @since 2025-01-30
 */
@Slf4j
@Service
public class LocalStorageService implements StorageService {
    
    @Override
    public String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream,
                            String contentType, long fileSize) {
        try {
            // 构建完整的文件路径
            String basePath = config.getBasePath();
            if (basePath == null || basePath.isEmpty()) {
                basePath = "./uploads/";
            }
            
            // 验证并规范化路径，防止路径遍历攻击
            Path basePathObj = Paths.get(basePath).toAbsolutePath().normalize();
            Path fullPath = basePathObj.resolve(objectKey).normalize();
            
            // 确保解析后的路径仍在基础路径内，防止路径遍历
            if (!fullPath.startsWith(basePathObj)) {
                throw new SecurityException("非法的文件路径: " + objectKey);
            }
            
            // 创建目录
            Files.createDirectories(fullPath.getParent());
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(fullPath.toFile())) {
                IoUtil.copy(inputStream, outputStream);
            }
            
            log.info("本地存储上传成功: {}", fullPath);
            
            // 返回访问URL
            String domain = config.getDomain();
            if (domain != null && !domain.isEmpty()) {
                return domain + "/" + objectKey;
            }
            return basePath + objectKey;
            
        } catch (Exception e) {
            log.error("本地存储上传失败: objectKey={}", objectKey, e);
            throw new RuntimeException("本地存储上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream downloadFile(SysStorageConfig config, String objectKey) {
        try {
            String basePath = config.getBasePath();
            if (basePath == null || basePath.isEmpty()) {
                basePath = "./uploads/";
            }
            
            // 验证并规范化路径，防止路径遍历攻击
            Path basePathObj = Paths.get(basePath).toAbsolutePath().normalize();
            Path fullPath = basePathObj.resolve(objectKey).normalize();
            
            // 确保解析后的路径仍在基础路径内，防止路径遍历
            if (!fullPath.startsWith(basePathObj)) {
                throw new SecurityException("非法的文件路径: " + objectKey);
            }
            
            if (!Files.exists(fullPath)) {
                throw new RuntimeException("文件不存在: " + objectKey);
            }
            
            return new FileInputStream(fullPath.toFile());
            
        } catch (Exception e) {
            log.error("本地存储下载失败: objectKey={}", objectKey, e);
            throw new RuntimeException("本地存储下载失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(SysStorageConfig config, String objectKey) {
        try {
            String basePath = config.getBasePath();
            if (basePath == null || basePath.isEmpty()) {
                basePath = "./uploads/";
            }
            
            // 验证并规范化路径，防止路径遍历攻击
            Path basePathObj = Paths.get(basePath).toAbsolutePath().normalize();
            Path fullPath = basePathObj.resolve(objectKey).normalize();
            
            // 确保解析后的路径仍在基础路径内，防止路径遍历
            if (!fullPath.startsWith(basePathObj)) {
                throw new SecurityException("非法的文件路径: " + objectKey);
            }
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("本地存储删除成功: {}", fullPath);
            }
            
        } catch (Exception e) {
            log.error("本地存储删除失败: objectKey={}", objectKey, e);
            throw new RuntimeException("本地存储删除失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean fileExists(SysStorageConfig config, String objectKey) {
        try {
            String basePath = config.getBasePath();
            if (basePath == null || basePath.isEmpty()) {
                basePath = "./uploads/";
            }
            
            // 验证并规范化路径，防止路径遍历攻击
            Path basePathObj = Paths.get(basePath).toAbsolutePath().normalize();
            Path fullPath = basePathObj.resolve(objectKey).normalize();
            
            // 确保解析后的路径仍在基础路径内，防止路径遍历
            if (!fullPath.startsWith(basePathObj)) {
                return false;
            }
            
            return Files.exists(fullPath);
            
        } catch (Exception e) {
            log.error("本地存储检查文件存在失败: objectKey={}", objectKey, e);
            return false;
        }
    }
    
    @Override
    public String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime) {
        // 本地存储不需要预签名URL，直接返回访问路径
        String domain = config.getDomain();
        if (domain != null && !domain.isEmpty()) {
            return domain + "/" + objectKey;
        }
        return config.getBasePath() + objectKey;
    }
    
    @Override
    public String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime) {
        // 本地存储不需要上传凭证
        return "{}";
    }
    
    @Override
    public String getStorageType() {
        return StorageType.LOCAL.getCode();
    }
}
