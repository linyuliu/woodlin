package com.mumu.woodlin.etl.model.request;

import com.mumu.woodlin.etl.model.EtlOfflineRuntimeConfig;
import com.mumu.woodlin.etl.model.EtlOfflineTableMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ETL 离线任务批量创建/更新请求。
 *
 * <p>一个请求可以包含多个表映射，后端会按单表任务拆分落库。</p>
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务批量创建请求")
public class EtlOfflineJobCreateRequest {

    /**
     * 任务名称前缀。
     */
    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称前缀", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jobName;

    /**
     * 任务分组。
     */
    @Schema(description = "任务分组", example = "OFFLINE_SYNC")
    private String jobGroup;

    /**
     * 任务描述。
     */
    @Schema(description = "任务描述")
    private String jobDescription;

    /**
     * 源数据源编码。
     */
    @NotBlank(message = "源数据源不能为空")
    @Schema(description = "源数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceDatasource;

    /**
     * 目标数据源编码。
     */
    @NotBlank(message = "目标数据源不能为空")
    @Schema(description = "目标数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetDatasource;

    /**
     * 运行配置。
     */
    @Valid
    @Schema(description = "运行配置")
    private EtlOfflineRuntimeConfig runtimeConfig = new EtlOfflineRuntimeConfig();

    /**
     * 表映射列表。
     */
    @Valid
    @NotEmpty(message = "表映射不能为空")
    @Schema(description = "表映射列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<EtlOfflineTableMapping> tableMappings = new ArrayList<>();

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
