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
 * ETL 离线任务批量预校验请求。
 *
 * <p>用于在保存前一次性校验多个表映射的可行性。</p>
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务批量预校验请求")
public class EtlOfflineValidationRequest {

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
}
