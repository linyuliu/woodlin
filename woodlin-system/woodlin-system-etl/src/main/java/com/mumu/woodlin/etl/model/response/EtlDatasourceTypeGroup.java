package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 数据源类型分组。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "数据源类型分组")
public class EtlDatasourceTypeGroup {

    @Schema(description = "分组编码")
    private String groupCode;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组下的数据源类型")
    private List<EtlDatasourceTypeOption> options;
}
