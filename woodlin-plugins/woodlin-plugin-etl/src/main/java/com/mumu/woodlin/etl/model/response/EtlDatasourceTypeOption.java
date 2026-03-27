package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据源类型选项。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "数据源类型选项")
public class EtlDatasourceTypeOption {

    @Schema(description = "数据源类型编码")
    private String datasourceType;

    @Schema(description = "展示名称")
    private String displayName;

    @Schema(description = "是否有可用数据源")
    private Boolean available;

    @Schema(description = "启用状态的数据源数量")
    private Long enabledCount;

    @Schema(description = "总数据源数量")
    private Long totalCount;
}
