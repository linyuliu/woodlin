package com.mumu.woodlin.etl.dialect;

import java.util.List;

/**
 * ETL同步数据库方言接口。
 *
 * @author mumu
 * @since 1.0.0
 */
public interface DatabaseDialect {

    /**
     * 获取方言类型。
     *
     * @return 方言类型
     */
    DatabaseDialectType getDialectType();

    /**
     * 引用标识符。
     *
     * @param identifier 标识符
     * @return 引用后的标识符
     */
    String quoteIdentifier(String identifier);

    /**
     * 拼装全限定表名。
     *
     * @param schemaName schema 名称
     * @param tableName 表名称
     * @return 全限定表名
     */
    String qualifyTable(String schemaName, String tableName);

    /**
     * 构建清空目标表 SQL。
     *
     * @param qualifiedTableName 全限定表名
     * @return SQL
     */
    String buildDeleteAllSql(String qualifiedTableName);

    /**
     * 构建 upsert SQL。
     *
     * @param qualifiedTableName 全限定表名
     * @param columns 写入列
     * @param primaryKeyColumns 主键列
     * @return upsert SQL
     */
    String buildUpsertSql(String qualifiedTableName, List<String> columns, List<String> primaryKeyColumns);

    /**
     * 构建按主键批量查询 SQL。
     *
     * @param qualifiedTableName 全限定表名
     * @param selectedColumns 查询列
     * @param primaryKeyColumn 主键列
     * @param keySize 主键值数量
     * @return SQL
     */
    String buildSelectByPrimaryKeyInSql(
            String qualifiedTableName,
            List<String> selectedColumns,
            String primaryKeyColumn,
            int keySize
    );

    /**
     * 构建新增字段 SQL。
     *
     * @param qualifiedTableName 全限定表名
     * @param columnName 字段名
     * @param columnTypeDefinition 字段类型定义
     * @return SQL
     */
    String buildAddColumnSql(String qualifiedTableName, String columnName, String columnTypeDefinition);
}
