package com.mumu.woodlin.file.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 上传令牌响应VO
 * 
 * @author mumu
 * @description 返回给前端的上传令牌信息
 * @since 2025-01-30
 */
@Data
@Accessors(chain = true)
@Schema(description = "上传令牌响应")
public class UploadTokenVO {
    
    /**
     * 上传令牌
     */
    @Schema(description = "上传令牌")
    private String token;
    
    /**
     * 签名
     */
    @Schema(description = "签名")
    private String signature;
    
    /**
     * 上传凭证（包含预签名URL等信息，JSON格式）
     */
    @Schema(description = "上传凭证")
    private String credential;
    
    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
    
    /**
     * 最大文件大小（字节）
     */
    @Schema(description = "最大文件大小")
    private Long maxFileSize;
    
    /**
     * 允许的文件扩展名
     */
    @Schema(description = "允许的文件扩展名")
    private String allowedExtensions;
    
    /**
     * 上传地址（用于后端上传）
     */
    @Schema(description = "上传地址")
    private String uploadUrl;
    
    /**
     * 存储类型
     */
    @Schema(description = "存储类型")
    private String storageType;
    
    /**
     * 对象键（文件路径）
     */
    @Schema(description = "对象键")
    private String objectKey;
}
