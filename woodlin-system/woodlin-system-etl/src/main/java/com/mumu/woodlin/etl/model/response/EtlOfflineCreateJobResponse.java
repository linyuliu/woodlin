package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 离线任务创建结果。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线任务创建结果")
public class EtlOfflineCreateJobResponse {

    @Schema(description = "任务ID")
    private Long jobId;

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "生效的Cron表达式")
    private String cronExpression;
}
