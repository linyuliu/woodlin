package com.mumu.woodlin.etl.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.datasource.service.DatabaseMetadataService;
import com.mumu.woodlin.etl.model.TableColumnMetadata;
import com.mumu.woodlin.etl.model.TableSchemaMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * ETL 表结构检查器。
 *
 * @author mumu
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Component
public class EtlTableMetadataInspector {

    private final DatabaseMetadataService databaseMetadataService;

    /**
     * 读取指定表结构元数据。
     *
     * @param datasourceCode 数据源编码
     * @param schemaName schema 名称
     * @param tableName 表名称
     * @return 表结构元数据
     */
    public TableSchemaMetadata inspect(String datasourceCode, String schemaName, String tableName) {
        if (!StringUtils.hasText(tableName)) {
            throw new BusinessException("表名不能为空");
        }
        String normalizedSchema = normalizeSchema(schemaName);
        List<ColumnMetadata> columns = databaseMetadataService.getColumns(datasourceCode, normalizedSchema, tableName, false);
        if (columns.isEmpty()) {
            throw new BusinessException("表不存在或字段为空: " + tableName);
        }
        List<TableColumnMetadata> normalizedColumns = toTableColumnMetadata(columns);
        List<String> primaryKeys = resolvePrimaryKeys(columns, datasourceCode, normalizedSchema, tableName);
        String digest = buildStructureDigest(normalizedColumns, primaryKeys);
        return TableSchemaMetadata.builder()
                .schemaName(normalizedSchema)
                .tableName(tableName)
                .columns(normalizedColumns)
                .primaryKeyColumns(primaryKeys)
                .structureDigest(digest)
                .build();
    }

    private List<TableColumnMetadata> toTableColumnMetadata(List<ColumnMetadata> columns) {
        return columns.stream()
                .sorted(Comparator.comparing(ColumnMetadata::getOrdinalPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(column -> TableColumnMetadata.builder()
                        .columnName(column.getColumnName())
                        .jdbcType(column.getJdbcType())
                        .typeName(column.getDataType())
                        .columnSize(column.getColumnSize())
                        .decimalDigits(column.getDecimalDigits())
                        .nullable(!Boolean.FALSE.equals(column.getNullable()))
                        .ordinalPosition(column.getOrdinalPosition())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> resolvePrimaryKeys(
            List<ColumnMetadata> columns,
            String datasourceCode,
            String schemaName,
            String tableName
    ) {
        Set<String> primaryKeys = columns.stream()
                .filter(column -> Boolean.TRUE.equals(column.getPrimaryKey()))
                .sorted(Comparator.comparing(ColumnMetadata::getOrdinalPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ColumnMetadata::getColumnName)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!primaryKeys.isEmpty()) {
            return new ArrayList<>(primaryKeys);
        }
        List<TableMetadata> tables = databaseMetadataService.getTables(datasourceCode, schemaName, false);
        return tables.stream()
                .filter(table -> table.getTableName() != null && table.getTableName().equalsIgnoreCase(tableName))
                .findFirst()
                .map(TableMetadata::getPrimaryKey)
                .map(this::parsePrimaryKeys)
                .orElseGet(ArrayList::new);
    }

    private List<String> parsePrimaryKeys(String primaryKey) {
        if (!StringUtils.hasText(primaryKey)) {
            return new ArrayList<>();
        }
        return Arrays.stream(primaryKey.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private String buildStructureDigest(List<TableColumnMetadata> columns, List<String> primaryKeys) {
        String payload = columns.stream()
                .sorted(Comparator.comparing(TableColumnMetadata::getOrdinalPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(item -> item.getColumnName() + "|" + item.getTypeName() + "|" + item.getColumnSize()
                        + "|" + item.getDecimalDigits() + "|" + item.isNullable())
                .collect(Collectors.joining("||")) + "##" + String.join(",", primaryKeys);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 算法不可用", exception);
        }
    }

    private String normalizeSchema(String schemaName) {
        if (!StringUtils.hasText(schemaName)) {
            return null;
        }
        return schemaName.trim();
    }
}
