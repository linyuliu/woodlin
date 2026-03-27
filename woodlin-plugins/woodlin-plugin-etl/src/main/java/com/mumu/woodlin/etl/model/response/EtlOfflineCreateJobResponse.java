package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线任务批量创建结果。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线任务批量创建结果")
public class EtlOfflineCreateJobResponse {

    @Schema(description = "任务ID")
    private List<Long> createdJobIds = new ArrayList<>();

    @Schema(description = "创建成功的任务数量")
    private Integer createdJobCount;
}
