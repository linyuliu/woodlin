package com.mumu.woodlin.etl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 表结构元数据模型。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "表结构元数据")
public class TableSchemaMetadata {

    /**
     * Schema 名称。
     */
    @Schema(description = "Schema 名称")
    private String schemaName;

    /**
     * 表名称。
     */
    @Schema(description = "表名称")
    private String tableName;

    /**
     * 字段列表。
     */
    @Builder.Default
    @Schema(description = "字段列表")
    private List<TableColumnMetadata> columns = new ArrayList<>();

    /**
     * 主键字段列表。
     */
    @Builder.Default
    @Schema(description = "主键字段列表")
    private List<String> primaryKeyColumns = new ArrayList<>();

    /**
     * 结构摘要。
     */
    @Schema(description = "结构摘要")
    private String structureDigest;

    /**
     * 根据字段名查找字段定义。
     *
     * @param columnName 字段名
     * @return 字段定义
     */
    public Optional<TableColumnMetadata> findColumn(String columnName) {
        if (columnName == null) {
            return Optional.empty();
        }
        return columns.stream().filter(item -> item.getColumnName().equalsIgnoreCase(columnName)).findFirst();
    }

    /**
     * 获取不可变字段列表。
     *
     * @return 不可变字段列表
     */
    public List<TableColumnMetadata> immutableColumns() {
        return Collections.unmodifiableList(columns);
    }
}
