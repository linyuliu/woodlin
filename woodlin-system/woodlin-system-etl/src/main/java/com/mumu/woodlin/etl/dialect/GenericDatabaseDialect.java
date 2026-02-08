package com.mumu.woodlin.etl.dialect;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * 通用数据库方言实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Component
public class GenericDatabaseDialect extends AbstractDatabaseDialect {

    @Override
    public DatabaseDialectType getDialectType() {
        return DatabaseDialectType.GENERIC;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String buildUpsertSql(String qualifiedTableName, List<String> columns, List<String> primaryKeyColumns) {
        String insertColumns = joinQuotedColumns(columns);
        String insertValues = placeholders(columns.size());
        return "INSERT INTO " + qualifiedTableName + " (" + insertColumns + ") VALUES (" + insertValues + ")";
    }
}
