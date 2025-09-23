package com.mumu.woodlin.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

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
     * 文件MD5
     */
    @TableField("file_md5")
    @Schema(description = "文件MD5")
    private String fileMd5;
    
    /**
     * 存储位置（local-本地，minio-MinIO，oss-阿里云OSS）
     */
    @TableField("storage_type")
    @Schema(description = "存储位置")
    private String storageType;
    
    /**
     * 是否为图片（1-是，0-否）
     */
    @TableField("is_image")
    @Schema(description = "是否为图片")
    private String isImage;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}