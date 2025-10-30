package com.mumu.woodlin.file.entity;

import java.io.Serial;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 上传令牌实体
 * 
 * @author mumu
 * @description 上传令牌实体类，用于签名验证和安全上传控制
 * @since 2025-01-30
 */
@Data
@Accessors(chain = true)
@TableName("sys_upload_token")
@Schema(description = "上传令牌")
public class SysUploadToken {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 令牌ID
     */
    @TableId(value = "token_id", type = IdType.ASSIGN_ID)
    @Schema(description = "令牌ID")
    private Long tokenId;
    
    /**
     * 上传令牌
     */
    @TableField("token")
    @Schema(description = "上传令牌")
    private String token;
    
    /**
     * 上传策略ID
     */
    @TableField("policy_id")
    @Schema(description = "上传策略ID")
    private Long policyId;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 签名
     */
    @TableField("signature")
    @Schema(description = "签名")
    private String signature;
    
    /**
     * 最大文件大小
     */
    @TableField("max_file_size")
    @Schema(description = "最大文件大小")
    private Long maxFileSize;
    
    /**
     * 允许的文件扩展名
     */
    @TableField("allowed_extensions")
    @Schema(description = "允许的文件扩展名")
    private String allowedExtensions;
    
    /**
     * 过期时间
     */
    @TableField("expire_time")
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
    
    /**
     * 是否已使用（0-否，1-是）
     */
    @TableField("is_used")
    @Schema(description = "是否已使用")
    private String isUsed;
    
    /**
     * 使用时间
     */
    @TableField("used_time")
    @Schema(description = "使用时间")
    private LocalDateTime usedTime;
    
    /**
     * 关联文件ID
     */
    @TableField("file_id")
    @Schema(description = "关联文件ID")
    private Long fileId;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
