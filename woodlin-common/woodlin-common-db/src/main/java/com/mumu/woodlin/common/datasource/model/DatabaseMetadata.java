package com.mumu.woodlin.common.datasource.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库元数据模型
 * 
 * @author mumu
 * @description 包含数据库的基本信息和Schema列表，作为平台级基础设施能力提供
 * @since 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 数据库名称
     */
    private String databaseName;
    
    /**
     * 数据库产品名称（如 MySQL, PostgreSQL, Oracle）
     */
    private String databaseProductName;
    
    /**
     * 数据库产品版本
     */
    private String databaseProductVersion;
    
    /**
     * 主版本号（如 MySQL 8.0.x 中的 8）
     */
    private Integer majorVersion;
    
    /**
     * 次版本号（如 MySQL 8.0.x 中的 0）
     */
    private Integer minorVersion;
    
    /**
     * 驱动名称
     */
    private String driverName;
    
    /**
     * 驱动版本
     */
    private String driverVersion;
    
    /**
     * 是否支持Schema概念
     */
    private Boolean supportsSchemas;
    
    /**
     * Schema列表
     */
    private List<SchemaMetadata> schemas;
    
    /**
     * 数据库字符集
     */
    private String charset;
    
    /**
     * 数据库排序规则
     */
    private String collation;
}
