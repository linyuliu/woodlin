package com.mumu.woodlin.etl.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 表字段元数据模型。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "表字段元数据")
public class TableColumnMetadata {

    /**
     * 字段名称。
     */
    @Schema(description = "字段名称")
    private String columnName;

    /**
     * JDBC 类型码。
     */
    @Schema(description = "JDBC 类型码")
    private Integer jdbcType;

    /**
     * 数据库类型名称。
     */
    @Schema(description = "数据库类型名称")
    private String typeName;

    /**
     * 字段长度。
     */
    @Schema(description = "字段长度")
    private Integer columnSize;

    /**
     * 小数位数。
     */
    @Schema(description = "小数位数")
    private Integer decimalDigits;

    /**
     * 是否可空。
     */
    @Schema(description = "是否可空")
    private boolean nullable;

    /**
     * 字段序号。
     */
    @Schema(description = "字段序号")
    private Integer ordinalPosition;
}
