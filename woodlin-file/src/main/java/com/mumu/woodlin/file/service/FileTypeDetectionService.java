package com.mumu.woodlin.file.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件类型检测服务
 * 
 * @author mumu
 * @description 使用Apache Tika检测文件的真实类型，防止文件伪装
 * @since 2025-01-30
 */
@Slf4j
@Service
public class FileTypeDetectionService {
    
    /**
     * Tika实例（线程安全）
     */
    private final Tika tika = new Tika();
    
    /**
     * 检测文件的MIME类型
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return MIME类型
     */
    public String detectMimeType(InputStream inputStream, String fileName) {
        try {
            // 使用Tika检测MIME类型
            String mimeType = tika.detect(inputStream, fileName);
            log.debug("检测文件类型: fileName={}, detectedMimeType={}", fileName, mimeType);
            return mimeType;
        } catch (IOException e) {
            log.error("检测文件类型失败: fileName={}", fileName, e);
            return "application/octet-stream";
        }
    }
    
    /**
     * 检测文件的MIME类型（使用字节数组）
     *
     * @param bytes    文件字节数组
     * @param fileName 文件名
     * @return MIME类型
     */
    public String detectMimeType(byte[] bytes, String fileName) {
        try {
            String mimeType = tika.detect(bytes, fileName);
            log.debug("检测文件类型: fileName={}, detectedMimeType={}", fileName, mimeType);
            return mimeType;
        } catch (Exception e) {
            log.error("检测文件类型失败: fileName={}", fileName, e);
            return "application/octet-stream";
        }
    }
    
    /**
     * 检测文件的MIME类型（带元数据）
     *
     * @param inputStream 文件输入流
     * @param metadata    元数据
     * @return MIME类型
     */
    public String detectMimeTypeWithMetadata(InputStream inputStream, Metadata metadata) {
        try {
            String mimeType = tika.detect(inputStream, metadata);
            log.debug("检测文件类型: metadata={}, detectedMimeType={}", metadata, mimeType);
            return mimeType;
        } catch (IOException e) {
            log.error("检测文件类型失败: metadata={}", metadata, e);
            return "application/octet-stream";
        }
    }
    
    /**
     * 验证文件扩展名与真实类型是否匹配
     *
     * @param inputStream  文件输入流
     * @param fileName     文件名
     * @param expectedMime 期望的MIME类型
     * @return 是否匹配
     */
    public boolean validateFileType(InputStream inputStream, String fileName, String expectedMime) {
        try {
            String detectedMime = detectMimeType(inputStream, fileName);
            
            // 如果期望的MIME类型为空，则不校验
            if (expectedMime == null || expectedMime.isEmpty()) {
                return true;
            }
            
            // 检查是否匹配（支持通配符，如 image/*）
            if (expectedMime.endsWith("/*")) {
                String prefix = expectedMime.substring(0, expectedMime.length() - 2);
                return detectedMime.startsWith(prefix);
            }
            
            boolean isValid = detectedMime.equals(expectedMime);
            
            if (!isValid) {
                log.warn("文件类型不匹配: fileName={}, expected={}, detected={}", 
                    fileName, expectedMime, detectedMime);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("验证文件类型失败: fileName={}", fileName, e);
            return false;
        }
    }
    
    /**
     * 判断文件是否为图片
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 是否为图片
     */
    public boolean isImage(InputStream inputStream, String fileName) {
        String mimeType = detectMimeType(inputStream, fileName);
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    /**
     * 判断文件是否为视频
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 是否为视频
     */
    public boolean isVideo(InputStream inputStream, String fileName) {
        String mimeType = detectMimeType(inputStream, fileName);
        return mimeType != null && mimeType.startsWith("video/");
    }
    
    /**
     * 判断文件是否为音频
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 是否为音频
     */
    public boolean isAudio(InputStream inputStream, String fileName) {
        String mimeType = detectMimeType(inputStream, fileName);
        return mimeType != null && mimeType.startsWith("audio/");
    }
    
    /**
     * 判断文件是否为文档
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 是否为文档
     */
    public boolean isDocument(InputStream inputStream, String fileName) {
        String mimeType = detectMimeType(inputStream, fileName);
        if (mimeType == null) {
            return false;
        }
        
        return mimeType.startsWith("application/pdf") ||
               mimeType.startsWith("application/msword") ||
               mimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
               mimeType.startsWith("application/vnd.ms-") ||
               mimeType.startsWith("text/");
    }
}
