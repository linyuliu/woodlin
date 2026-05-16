package com.mumu.woodlin.authorization.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 列表查询数据权限约束。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@Schema(description = "授权查询约束")
public class AuthorizationConstraint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否允许查询")
    private boolean allowed;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "允许部门ID列表")
    private List<Long> deptIds = new ArrayList<>();

    @Schema(description = "数据范围")
    private String dataScope;
}
