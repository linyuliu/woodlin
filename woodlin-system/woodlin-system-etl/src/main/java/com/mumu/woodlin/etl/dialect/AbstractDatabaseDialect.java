package com.mumu.woodlin.etl.dialect;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

/**
 * ETL数据库方言抽象基类。
 *
 * @author mumu
 * @since 1.0.0
 */
public abstract class AbstractDatabaseDialect implements DatabaseDialect {

    @Override
    public String qualifyTable(String schemaName, String tableName) {
        if (!StringUtils.hasText(schemaName)) {
            return quoteIdentifier(tableName);
        }
        return quoteIdentifier(schemaName) + "." + quoteIdentifier(tableName);
    }

    @Override
    public String buildDeleteAllSql(String qualifiedTableName) {
        return "DELETE FROM " + qualifiedTableName;
    }

    @Override
    public String buildTruncateSql(String qualifiedTableName) {
        return "TRUNCATE TABLE " + qualifiedTableName;
    }

    @Override
    public String buildInsertSql(String qualifiedTableName, List<String> columns) {
        String insertColumns = joinQuotedColumns(columns);
        String insertValues = placeholders(columns.size());
        return "INSERT INTO " + qualifiedTableName + " (" + insertColumns + ") VALUES (" + insertValues + ")";
    }

    @Override
    public String buildSelectByPrimaryKeyInSql(
            String qualifiedTableName,
            List<String> selectedColumns,
            String primaryKeyColumn,
            int keySize
    ) {
        String selectClause = selectedColumns.stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));
        String placeholders = IntStream.range(0, keySize)
                .mapToObj(index -> "?")
                .collect(Collectors.joining(", "));
        return "SELECT " + selectClause + " FROM " + qualifiedTableName
                + " WHERE " + quoteIdentifier(primaryKeyColumn) + " IN (" + placeholders + ")";
    }

    @Override
    public String buildAddColumnSql(String qualifiedTableName, String columnName, String columnTypeDefinition) {
        return "ALTER TABLE " + qualifiedTableName + " ADD " + quoteIdentifier(columnName) + " " + columnTypeDefinition;
    }

    /**
     * 拼装列名片段。
     *
     * @param columns 列名集合
     * @return SQL 列片段
     */
    protected String joinQuotedColumns(List<String> columns) {
        return columns.stream().map(this::quoteIdentifier).collect(Collectors.joining(", "));
    }

    /**
     * 拼装问号占位符。
     *
     * @param size 数量
     * @return 占位符片段
     */
    protected String placeholders(int size) {
        return IntStream.range(0, size).mapToObj(index -> "?").collect(Collectors.joining(", "));
    }

    /**
     * 判断字段是否属于主键字段。
     *
     * @param column 字段名
     * @param primaryKeyColumns 主键字段集合
     * @return 是否主键字段
     */
    protected boolean isPrimaryKey(String column, List<String> primaryKeyColumns) {
        return primaryKeyColumns.stream().anyMatch(item -> item.equalsIgnoreCase(column));
    }
}
