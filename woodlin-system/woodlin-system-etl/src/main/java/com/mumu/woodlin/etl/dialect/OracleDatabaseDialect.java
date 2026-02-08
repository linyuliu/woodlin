package com.mumu.woodlin.etl.dialect;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Oracle 数据库方言实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Component
public class OracleDatabaseDialect extends AbstractDatabaseDialect {

    @Override
    public DatabaseDialectType getDialectType() {
        return DatabaseDialectType.ORACLE;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String buildUpsertSql(String qualifiedTableName, List<String> columns, List<String> primaryKeyColumns) {
        String sourceProjection = columns.stream()
                .map(column -> "? AS " + quoteIdentifier(column))
                .collect(Collectors.joining(", "));
        String onClause = primaryKeyColumns.stream()
                .map(column -> "target." + quoteIdentifier(column) + " = source." + quoteIdentifier(column))
                .collect(Collectors.joining(" AND "));
        String updateClause = columns.stream()
                .filter(column -> !isPrimaryKey(column, primaryKeyColumns))
                .map(column -> "target." + quoteIdentifier(column) + " = source." + quoteIdentifier(column))
                .collect(Collectors.joining(", "));
        if (updateClause.isEmpty()) {
            String pk = quoteIdentifier(primaryKeyColumns.get(0));
            updateClause = "target." + pk + " = source." + pk;
        }
        String insertColumns = joinQuotedColumns(columns);
        String insertValues = columns.stream()
                .map(column -> "source." + quoteIdentifier(column))
                .collect(Collectors.joining(", "));
        return "MERGE INTO " + qualifiedTableName + " target USING (SELECT " + sourceProjection + " FROM dual) source "
                + "ON (" + onClause + ") "
                + "WHEN MATCHED THEN UPDATE SET " + updateClause + " "
                + "WHEN NOT MATCHED THEN INSERT (" + insertColumns + ") VALUES (" + insertValues + ")";
    }
}
