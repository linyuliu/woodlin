package com.mumu.woodlin.etl.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * ETL 离线任务分页查询请求。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务分页查询请求")
public class EtlOfflineJobPageRequest {

    /**
     * 当前页码。
     */
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    /**
     * 每页记录数。
     */
    @Min(value = 1, message = "每页记录数不能小于1")
    @Schema(description = "每页记录数", example = "20")
    private Long pageSize = 20L;

    /**
     * 关键字。
     */
    @Schema(description = "关键字")
    private String keyword;

    /**
     * 任务状态。
     */
    @Schema(description = "任务状态")
    private String status;

    /**
     * 同步模式。
     */
    @Schema(description = "同步模式")
    private String syncMode;

    /**
     * 源数据源编码。
     */
    @Schema(description = "源数据源编码")
    private String sourceDatasource;

    /**
     * 目标数据源编码。
     */
    @Schema(description = "目标数据源编码")
    private String targetDatasource;
}
