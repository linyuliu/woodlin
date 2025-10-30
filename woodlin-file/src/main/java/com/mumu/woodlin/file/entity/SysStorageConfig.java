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
 * 存储平台配置实体
 * 
 * @author mumu
 * @description 存储平台配置实体类，支持多种对象存储平台配置
 * @since 2025-01-30
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_storage_config")
@Schema(description = "存储平台配置")
public class SysStorageConfig extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
    @Schema(description = "配置ID")
    private Long configId;
    
    /**
     * 配置名称
     */
    @TableField("config_name")
    @Schema(description = "配置名称")
    private String configName;
    
    /**
     * 存储类型（local-本地，minio-MinIO，s3-AWS S3，oss-阿里云OSS，cos-腾讯云COS，obs-华为云OBS）
     */
    @TableField("storage_type")
    @Schema(description = "存储类型")
    private String storageType;
    
    /**
     * 访问密钥
     */
    @TableField("access_key")
    @Schema(description = "访问密钥")
    private String accessKey;
    
    /**
     * 密钥（加密存储）
     */
    @TableField("secret_key")
    @Schema(description = "密钥")
    private String secretKey;
    
    /**
     * 终端节点地址
     */
    @TableField("endpoint")
    @Schema(description = "终端节点地址")
    private String endpoint;
    
    /**
     * 存储桶名称
     */
    @TableField("bucket_name")
    @Schema(description = "存储桶名称")
    private String bucketName;
    
    /**
     * 区域
     */
    @TableField("region")
    @Schema(description = "区域")
    private String region;
    
    /**
     * 基础路径
     */
    @TableField("base_path")
    @Schema(description = "基础路径")
    private String basePath;
    
    /**
     * 自定义域名
     */
    @TableField("domain")
    @Schema(description = "自定义域名")
    private String domain;
    
    /**
     * 是否为默认配置（0-否，1-是）
     */
    @TableField("is_default")
    @Schema(description = "是否为默认配置")
    private String isDefault;
    
    /**
     * 是否公开访问（0-私有，1-公开）
     */
    @TableField("is_public")
    @Schema(description = "是否公开访问")
    private String isPublic;
    
    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    @Schema(description = "状态")
    private String status;
    
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
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
