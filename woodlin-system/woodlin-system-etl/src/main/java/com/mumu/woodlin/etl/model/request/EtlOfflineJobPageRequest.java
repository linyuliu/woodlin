package com.mumu.woodlin.etl.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 离线ETL任务分页查询请求。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线ETL任务分页查询请求")
public class EtlOfflineJobPageRequest {

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 500, message = "每页大小不能超过500")
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "关键字（任务名/数据源/表名）")
    private String keyword;

    @Schema(description = "任务状态：1/0")
    private String status;

    @Schema(description = "同步模式：FULL/INCREMENTAL")
    private String syncMode;

    @Schema(description = "源数据源编码")
    private String sourceDatasource;

    @Schema(description = "目标数据源编码")
    private String targetDatasource;
}
