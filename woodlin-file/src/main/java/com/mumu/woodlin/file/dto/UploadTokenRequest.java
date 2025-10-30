package com.mumu.woodlin.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 上传令牌请求DTO
 * 
 * @author mumu
 * @description 前端请求上传令牌的参数
 * @since 2025-01-30
 */
@Data
@Schema(description = "上传令牌请求")
public class UploadTokenRequest {
    
    /**
     * 上传策略编码
     */
    @NotBlank(message = "上传策略编码不能为空")
    @Schema(description = "上传策略编码", example = "default")
    private String policyCode;
    
    /**
     * 文件名（可选）
     */
    @Schema(description = "文件名", example = "example.jpg")
    private String fileName;
    
    /**
     * 文件大小（字节，可选）
     */
    @Schema(description = "文件大小", example = "1024000")
    private Long fileSize;
    
    /**
     * 文件扩展名（可选）
     */
    @Schema(description = "文件扩展名", example = "jpg")
    private String fileExtension;
    
    /**
     * 业务类型（可选）
     */
    @Schema(description = "业务类型", example = "avatar")
    private String businessType;
    
    /**
     * 业务ID（可选）
     */
    @Schema(description = "业务ID", example = "123456")
    private String businessId;
}
