package com.mumu.woodlin.sql2api.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;

/**
 * SQL构建器
 * 
 * @author mumu
 * @description 基于数据库元数据构建标准SQL语句（SELECT、INSERT、UPDATE、DELETE）
 * @since 2025-01-04
 */
public class SqlBuilder {
    
    /**
     * 构建SELECT查询SQL
     * 
     * @param table 表元数据
     * @param config 查询配置
     * @return SQL语句
     */
    public static String buildSelectSql(TableMetadata table, SelectConfig config) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // SELECT子句
        if (config.getColumns() != null && !config.getColumns().isEmpty()) {
            sql.append(String.join(", ", config.getColumns()));
        } else {
            // 使用所有列
            List<String> columnNames = table.getColumns().stream()
                    .map(ColumnMetadata::getColumnName)
                    .collect(Collectors.toList());
            sql.append(String.join(", ", columnNames));
        }
        
        // FROM子句
        sql.append("\nFROM ");
        if (table.getSchemaName() != null && !table.getSchemaName().isEmpty()) {
            sql.append(table.getSchemaName()).append(".");
        }
        sql.append(table.getTableName());
        
        // WHERE子句
        if (config.getWhereConditions() != null && !config.getWhereConditions().isEmpty()) {
            sql.append("\nWHERE ");
            sql.append(String.join(" AND ", config.getWhereConditions()));
        }
        
        // ORDER BY子句
        if (config.getOrderBy() != null && !config.getOrderBy().isEmpty()) {
            sql.append("\nORDER BY ").append(config.getOrderBy());
        }
        
        // LIMIT子句 (支持MySQL/PostgreSQL语法)
        if (config.getLimit() != null && config.getLimit() > 0) {
            sql.append("\nLIMIT ").append(config.getLimit());
            if (config.getOffset() != null && config.getOffset() > 0) {
                sql.append(" OFFSET ").append(config.getOffset());
            }
        }
        
        return sql.toString();
    }
    
    /**
     * 构建INSERT语句
     * 
     * @param table 表元数据
     * @param config 插入配置
     * @return SQL语句
     */
    public static String buildInsertSql(TableMetadata table, InsertConfig config) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        
        // 表名
        if (table.getSchemaName() != null && !table.getSchemaName().isEmpty()) {
            sql.append(table.getSchemaName()).append(".");
        }
        sql.append(table.getTableName());
        
        // 列名列表
        List<String> columns = config.getColumns() != null && !config.getColumns().isEmpty() 
                ? config.getColumns()
                : table.getColumns().stream()
                    .filter(col -> !Boolean.TRUE.equals(col.getAutoIncrement())) // 排除自增列
                    .map(ColumnMetadata::getColumnName)
                    .collect(Collectors.toList());
        
        sql.append(" (");
        sql.append(String.join(", ", columns));
        sql.append(")\nVALUES (");
        
        // 参数占位符
        String placeholders = columns.stream()
                .map(col -> "#{" + col + "}")
                .collect(Collectors.joining(", "));
        sql.append(placeholders);
        sql.append(")");
        
        return sql.toString();
    }
    
    /**
     * 构建UPDATE语句
     * 
     * @param table 表元数据
     * @param config 更新配置
     * @return SQL语句
     */
    public static String buildUpdateSql(TableMetadata table, UpdateConfig config) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        
        // 表名
        if (table.getSchemaName() != null && !table.getSchemaName().isEmpty()) {
            sql.append(table.getSchemaName()).append(".");
        }
        sql.append(table.getTableName());
        
        // SET子句
        sql.append("\nSET ");
        List<String> setExpressions = new ArrayList<>();
        
        if (config.getColumns() != null && !config.getColumns().isEmpty()) {
            for (String column : config.getColumns()) {
                setExpressions.add(column + " = #{" + column + "}");
            }
        } else {
            // 使用所有非主键、非自增列
            for (ColumnMetadata col : table.getColumns()) {
                if (!Boolean.TRUE.equals(col.getPrimaryKey()) && !Boolean.TRUE.equals(col.getAutoIncrement())) {
                    setExpressions.add(col.getColumnName() + " = #{" + col.getColumnName() + "}");
                }
            }
        }
        
        sql.append(String.join(", ", setExpressions));
        
        // WHERE子句（通常使用主键）
        sql.append("\nWHERE ");
        if (config.getWhereConditions() != null && !config.getWhereConditions().isEmpty()) {
            sql.append(String.join(" AND ", config.getWhereConditions()));
        } else if (table.getPrimaryKey() != null) {
            sql.append(table.getPrimaryKey()).append(" = #{").append(table.getPrimaryKey()).append("}");
        } else {
            throw new IllegalArgumentException("No WHERE condition specified and no primary key found");
        }
        
        return sql.toString();
    }
    
    /**
     * 构建DELETE语句
     * 
     * @param table 表元数据
     * @param config 删除配置
     * @return SQL语句
     */
    public static String buildDeleteSql(TableMetadata table, DeleteConfig config) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        
        // 表名
        if (table.getSchemaName() != null && !table.getSchemaName().isEmpty()) {
            sql.append(table.getSchemaName()).append(".");
        }
        sql.append(table.getTableName());
        
        // WHERE子句
        sql.append("\nWHERE ");
        if (config.getWhereConditions() != null && !config.getWhereConditions().isEmpty()) {
            sql.append(String.join(" AND ", config.getWhereConditions()));
        } else if (table.getPrimaryKey() != null) {
            sql.append(table.getPrimaryKey()).append(" = #{").append(table.getPrimaryKey()).append("}");
        } else {
            throw new IllegalArgumentException("No WHERE condition specified and no primary key found");
        }
        
        return sql.toString();
    }
    
    /**
     * 构建COUNT查询SQL
     * 
     * @param table 表元数据
     * @param whereConditions WHERE条件列表
     * @return SQL语句
     */
    public static String buildCountSql(TableMetadata table, List<String> whereConditions) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as total\nFROM ");
        
        if (table.getSchemaName() != null && !table.getSchemaName().isEmpty()) {
            sql.append(table.getSchemaName()).append(".");
        }
        sql.append(table.getTableName());
        
        if (whereConditions != null && !whereConditions.isEmpty()) {
            sql.append("\nWHERE ");
            sql.append(String.join(" AND ", whereConditions));
        }
        
        return sql.toString();
    }
    
    /**
     * SELECT查询配置
     */
    @Data
    @Builder
    public static class SelectConfig {
        /**
         * 要查询的列（如果为空则查询所有列）
         */
        @Singular
        private List<String> columns;
        
        /**
         * WHERE条件列表
         */
        @Singular
        private List<String> whereConditions;
        
        /**
         * ORDER BY子句
         */
        private String orderBy;
        
        /**
         * 查询限制数量
         */
        private Integer limit;
        
        /**
         * 查询偏移量
         */
        private Integer offset;
    }
    
    /**
     * INSERT配置
     */
    @Data
    @Builder
    public static class InsertConfig {
        /**
         * 要插入的列（如果为空则插入所有非自增列）
         */
        @Singular
        private List<String> columns;
    }
    
    /**
     * UPDATE配置
     */
    @Data
    @Builder
    public static class UpdateConfig {
        /**
         * 要更新的列（如果为空则更新所有非主键、非自增列）
         */
        @Singular
        private List<String> columns;
        
        /**
         * WHERE条件列表（如果为空则使用主键）
         */
        @Singular
        private List<String> whereConditions;
    }
    
    /**
     * DELETE配置
     */
    @Data
    @Builder
    public static class DeleteConfig {
        /**
         * WHERE条件列表（如果为空则使用主键）
         */
        @Singular
        private List<String> whereConditions;
    }
}
