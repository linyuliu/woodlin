package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 离线ETL使用的数据源选项。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线ETL数据源选项")
public class EtlOfflineDatasourceOption {

    @Schema(description = "数据源编码")
    private String datasourceCode;

    @Schema(description = "数据源名称")
    private String datasourceName;

    @Schema(description = "数据源类型")
    private String datasourceType;

    @Schema(description = "状态：1启用0禁用")
    private Integer status;

    @Schema(description = "负责人")
    private String owner;

    @Schema(description = "业务标签")
    private String bizTags;

    @Schema(description = "备注")
    private String remark;
}
