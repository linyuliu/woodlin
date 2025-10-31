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
 * 文件信息实体
 * 
 * @author mumu
 * @description 文件信息实体类，用于文件管理和预览
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
@Schema(description = "文件信息")
public class SysFile extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 文件ID
     */
    @TableId(value = "file_id", type = IdType.ASSIGN_ID)
    @Schema(description = "文件ID")
    private Long fileId;
    
    /**
     * 文件名称
     */
    @TableField("file_name")
    @Schema(description = "文件名称")
    private String fileName;
    
    /**
     * 原始文件名
     */
    @TableField("original_name")
    @Schema(description = "原始文件名")
    private String originalName;
    
    /**
     * 文件路径
     */
    @TableField("file_path")
    @Schema(description = "文件路径")
    private String filePath;
    
    /**
     * 文件URL
     */
    @TableField("file_url")
    @Schema(description = "文件URL")
    private String fileUrl;
    
    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    @Schema(description = "文件大小")
    private Long fileSize;
    
    /**
     * 文件扩展名
     */
    @TableField("file_extension")
    @Schema(description = "文件扩展名")
    private String fileExtension;
    
    /**
     * 文件类型
     */
    @TableField("file_type")
    @Schema(description = "文件类型")
    private String fileType;
    
    /**
     * MIME类型
     */
    @TableField("mime_type")
    @Schema(description = "MIME类型")
    private String mimeType;
    
    /**
     * 检测到的真实MIME类型（通过Apache Tika）
     */
    @TableField("detected_mime_type")
    @Schema(description = "检测到的真实MIME类型")
    private String detectedMimeType;
    
    /**
     * 文件MD5
     */
    @TableField("file_md5")
    @Schema(description = "文件MD5")
    private String fileMd5;
    
    /**
     * 文件SHA256
     */
    @TableField("file_sha256")
    @Schema(description = "文件SHA256")
    private String fileSha256;
    
    /**
     * 存储位置（local-本地，minio-MinIO，s3-AWS S3，oss-阿里云OSS，cos-腾讯云COS，obs-华为云OBS）
     */
    @TableField("storage_type")
    @Schema(description = "存储位置")
    private String storageType;
    
    /**
     * 存储配置ID
     */
    @TableField("storage_config_id")
    @Schema(description = "存储配置ID")
    private Long storageConfigId;
    
    /**
     * 上传策略ID
     */
    @TableField("upload_policy_id")
    @Schema(description = "上传策略ID")
    private Long uploadPolicyId;
    
    /**
     * 存储桶名称
     */
    @TableField("bucket_name")
    @Schema(description = "存储桶名称")
    private String bucketName;
    
    /**
     * 对象键
     */
    @TableField("object_key")
    @Schema(description = "对象键")
    private String objectKey;
    
    /**
     * 是否为图片（0-否，1-是）
     */
    @TableField("is_image")
    @Schema(description = "是否为图片")
    private String isImage;
    
    /**
     * 图片宽度
     */
    @TableField("image_width")
    @Schema(description = "图片宽度")
    private Integer imageWidth;
    
    /**
     * 图片高度
     */
    @TableField("image_height")
    @Schema(description = "图片高度")
    private Integer imageHeight;
    
    /**
     * 缩略图路径
     */
    @TableField("thumbnail_path")
    @Schema(description = "缩略图路径")
    private String thumbnailPath;
    
    /**
     * 缩略图URL
     */
    @TableField("thumbnail_url")
    @Schema(description = "缩略图URL")
    private String thumbnailUrl;
    
    /**
     * 是否公开（0-否，1-是）
     */
    @TableField("is_public")
    @Schema(description = "是否公开")
    private String isPublic;
    
    /**
     * 访问次数
     */
    @TableField("access_count")
    @Schema(description = "访问次数")
    private Integer accessCount;
    
    /**
     * 最后访问时间
     */
    @TableField("last_access_time")
    @Schema(description = "最后访问时间")
    private java.time.LocalDateTime lastAccessTime;
    
    /**
     * 过期时间
     */
    @TableField("expired_time")
    @Schema(description = "过期时间")
    private java.time.LocalDateTime expiredTime;
    
    /**
     * 上传IP
     */
    @TableField("upload_ip")
    @Schema(description = "上传IP")
    private String uploadIp;
    
    /**
     * 用户代理
     */
    @TableField("user_agent")
    @Schema(description = "用户代理")
    private String userAgent;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 业务类型
     */
    @TableField("business_type")
    @Schema(description = "业务类型")
    private String businessType;
    
    /**
     * 业务ID
     */
    @TableField("business_id")
    @Schema(description = "业务ID")
    private String businessId;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}