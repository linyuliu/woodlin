package com.mumu.woodlin.etl.dialect;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * PostgreSQL 数据库方言实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Component
public class PostgreSqlDatabaseDialect extends AbstractDatabaseDialect {

    @Override
    public DatabaseDialectType getDialectType() {
        return DatabaseDialectType.POSTGRESQL;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String buildUpsertSql(String qualifiedTableName, List<String> columns, List<String> primaryKeyColumns) {
        String insertColumns = joinQuotedColumns(columns);
        String insertValues = placeholders(columns.size());
        String conflictColumns = primaryKeyColumns.stream().map(this::quoteIdentifier).collect(Collectors.joining(", "));
        String updateClause = columns.stream()
                .filter(column -> !isPrimaryKey(column, primaryKeyColumns))
                .map(column -> quoteIdentifier(column) + " = EXCLUDED." + quoteIdentifier(column))
                .collect(Collectors.joining(", "));
        if (updateClause.isEmpty()) {
            updateClause = quoteIdentifier(primaryKeyColumns.get(0)) + " = EXCLUDED." + quoteIdentifier(primaryKeyColumns.get(0));
        }
        return "INSERT INTO " + qualifiedTableName + " (" + insertColumns + ") VALUES (" + insertValues + ")"
                + " ON CONFLICT (" + conflictColumns + ") DO UPDATE SET " + updateClause;
    }
}
