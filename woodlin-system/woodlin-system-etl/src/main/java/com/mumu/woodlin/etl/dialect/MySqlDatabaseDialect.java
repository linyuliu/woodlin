package com.mumu.woodlin.etl.dialect;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * MySQL 数据库方言实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Component
public class MySqlDatabaseDialect extends AbstractDatabaseDialect {

    @Override
    public DatabaseDialectType getDialectType() {
        return DatabaseDialectType.MYSQL;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public String buildUpsertSql(String qualifiedTableName, List<String> columns, List<String> primaryKeyColumns) {
        String insertColumns = joinQuotedColumns(columns);
        String insertValues = placeholders(columns.size());
        String updateClause = columns.stream()
                .filter(column -> !isPrimaryKey(column, primaryKeyColumns))
                .map(column -> quoteIdentifier(column) + " = VALUES(" + quoteIdentifier(column) + ")")
                .collect(Collectors.joining(", "));
        if (updateClause.isEmpty()) {
            updateClause = quoteIdentifier(primaryKeyColumns.get(0)) + " = " + quoteIdentifier(primaryKeyColumns.get(0));
        }
        return "INSERT INTO " + qualifiedTableName + " (" + insertColumns + ") VALUES (" + insertValues + ")"
                + " ON DUPLICATE KEY UPDATE " + updateClause;
    }
}
