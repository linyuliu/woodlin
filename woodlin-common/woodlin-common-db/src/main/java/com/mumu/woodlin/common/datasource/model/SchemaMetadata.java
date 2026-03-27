package com.mumu.woodlin.common.datasource.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Schema元数据模型
 * 
 * @author mumu
 * @description Schema（模式）信息，某些数据库如MySQL不支持此概念
 * @since 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Schema名称
     */
    private String schemaName;
    
    /**
     * 所属数据库名称
     */
    private String databaseName;
    
    /**
     * Schema注释/描述
     */
    private String comment;
    
    /**
     * 表列表
     */
    private List<TableMetadata> tables;
}
