package com.mumu.woodlin.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传凭证DTO
 * 
 * @author mumu
 * @description 上传凭证数据传输对象
 * @since 2025-01-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadCredentialDTO {
    
    /**
     * 上传URL
     */
    private String uploadUrl;
    
    /**
     * 存储桶名称
     */
    private String bucket;
    
    /**
     * 对象键
     */
    private String objectKey;
    
    /**
     * 过期时间（秒）
     */
    private int expiresIn;
}
