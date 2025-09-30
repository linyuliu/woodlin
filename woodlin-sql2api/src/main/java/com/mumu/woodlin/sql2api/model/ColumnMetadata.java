package com.mumu.woodlin.sql2api.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 列元数据模型
 * 
 * @author mumu
 * @description 数据库表列的详细信息，包括类型、约束等
 * @since 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 列名称
     */
    private String columnName;
    
    /**
     * 所属表名称
     */
    private String tableName;
    
    /**
     * 所属Schema名称
     */
    private String schemaName;
    
    /**
     * 所属数据库名称
     */
    private String databaseName;
    
    /**
     * 列注释/描述
     */
    private String comment;
    
    /**
     * 数据类型（如 VARCHAR, INT, DATETIME）
     */
    private String dataType;
    
    /**
     * JDBC类型代码
     */
    private Integer jdbcType;
    
    /**
     * 列长度
     */
    private Integer columnSize;
    
    /**
     * 小数位数
     */
    private Integer decimalDigits;
    
    /**
     * 是否可为空
     */
    private Boolean nullable;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 是否为主键
     */
    private Boolean primaryKey;
    
    /**
     * 是否自增
     */
    private Boolean autoIncrement;
    
    /**
     * 列位置
     */
    private Integer ordinalPosition;
    
    /**
     * Java类型（映射后的）
     */
    private String javaType;
}
