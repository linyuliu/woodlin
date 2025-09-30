package com.mumu.woodlin.sql2api.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 表元数据模型
 * 
 * @author mumu
 * @description 数据库表的详细信息，包括列信息
 * @since 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 表名称
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
     * 表注释/描述
     */
    private String comment;
    
    /**
     * 表类型（TABLE, VIEW, SYSTEM TABLE等）
     */
    private String tableType;
    
    /**
     * 列列表
     */
    private List<ColumnMetadata> columns;
    
    /**
     * 主键列名
     */
    private String primaryKey;
    
    /**
     * 表创建时间
     */
    private String createTime;
    
    /**
     * 表更新时间
     */
    private String updateTime;
    
    /**
     * 表引擎（MySQL特有）
     */
    private String engine;
    
    /**
     * 表字符集
     */
    private String charset;
    
    /**
     * 表排序规则
     */
    private String collation;
}
