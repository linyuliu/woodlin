package com.mumu.woodlin.etl.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * ETL 离线任务字段处理规则。
 *
 * <p>该对象既用于前端提交字段映射，也用于后端返回字段规则回填。</p>
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务字段处理规则")
public class EtlOfflineFieldRule {

    /**
     * 源字段名称。
     */
    @Schema(description = "源字段名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sourceColumnName;

    /**
     * 目标字段名称。
     */
    @NotBlank(message = "目标字段名称不能为空")
    @Schema(description = "目标字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetColumnName;

    /**
     * 源字段类型。
     */
    @Schema(description = "源字段类型")
    private String sourceColumnType;

    /**
     * 目标字段类型。
     */
    @Schema(description = "目标字段类型")
    private String targetColumnType;

    /**
     * 处理动作。
     */
    @Schema(description = "处理动作", example = "COPY")
    private String mappingAction;

    /**
     * 字段顺序。
     */
    @Schema(description = "字段顺序")
    private Integer ordinalPosition;

    /**
     * 是否启用。
     */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * 常量值。
     */
    @Schema(description = "常量值")
    private String constantValue;

    /**
     * 默认值。
     */
    @Schema(description = "默认值")
    private String defaultValue;

    /**
     * 空值处理策略。
     */
    @Schema(description = "空值处理策略", example = "KEEP")
    private String emptyValuePolicy;

    /**
     * 转换参数。
     */
    @Schema(description = "转换参数")
    private Map<String, Object> transformParams;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
