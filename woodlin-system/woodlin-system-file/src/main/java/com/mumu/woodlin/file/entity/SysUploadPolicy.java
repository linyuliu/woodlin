package com.mumu.woodlin.file.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 上传策略实体
 * 
 * @author mumu
 * @description 上传策略实体类，定义文件上传的各种策略和规则
 * @since 2025-01-30
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_upload_policy")
@Schema(description = "上传策略")
public class SysUploadPolicy extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 策略ID
     */
    @TableId(value = "policy_id", type = IdType.ASSIGN_ID)
    @Schema(description = "策略ID")
    private Long policyId;
    
    /**
     * 策略名称
     */
    @TableField("policy_name")
    @Schema(description = "策略名称")
    private String policyName;
    
    /**
     * 策略编码
     */
    @TableField("policy_code")
    @Schema(description = "策略编码")
    private String policyCode;
    
    /**
     * 存储配置ID
     */
    @TableField("storage_config_id")
    @Schema(description = "存储配置ID")
    private Long storageConfigId;
    
    /**
     * 是否检测文件真实类型（0-否，1-是，使用Apache Tika）
     */
    @TableField("detect_file_type")
    @Schema(description = "是否检测文件真实类型")
    private String detectFileType;
    
    /**
     * 是否检查文件大小（0-否，1-是）
     */
    @TableField("check_file_size")
    @Schema(description = "是否检查文件大小")
    private String checkFileSize;
    
    /**
     * 最大文件大小（字节）
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
     * 允许的MIME类型
     */
    @TableField("allowed_mime_types")
    @Schema(description = "允许的MIME类型")
    private String allowedMimeTypes;
    
    /**
     * 是否校验MD5（0-否，1-是）
     */
    @TableField("check_md5")
    @Schema(description = "是否校验MD5")
    private String checkMd5;
    
    /**
     * 是否允许重复上传（0-否，1-是）
     */
    @TableField("allow_duplicate")
    @Schema(description = "是否允许重复上传")
    private String allowDuplicate;
    
    /**
     * 是否生成缩略图（0-否，1-是）
     */
    @TableField("generate_thumbnail")
    @Schema(description = "是否生成缩略图")
    private String generateThumbnail;
    
    /**
     * 缩略图宽度
     */
    @TableField("thumbnail_width")
    @Schema(description = "缩略图宽度")
    private Integer thumbnailWidth;
    
    /**
     * 缩略图高度
     */
    @TableField("thumbnail_height")
    @Schema(description = "缩略图高度")
    private Integer thumbnailHeight;
    
    /**
     * 文件路径模式
     */
    @TableField("path_pattern")
    @Schema(description = "文件路径模式")
    private String pathPattern;
    
    /**
     * 文件名模式
     */
    @TableField("file_name_pattern")
    @Schema(description = "文件名模式")
    private String fileNamePattern;
    
    /**
     * 签名有效期（秒）
     */
    @TableField("signature_expires")
    @Schema(description = "签名有效期")
    private Integer signatureExpires;
    
    /**
     * 上传回调地址
     */
    @TableField("callback_url")
    @Schema(description = "上传回调地址")
    private String callbackUrl;
    
    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    @Schema(description = "状态")
    private String status;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
