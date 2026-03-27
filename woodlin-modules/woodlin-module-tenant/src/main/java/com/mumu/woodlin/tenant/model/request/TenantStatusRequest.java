package com.mumu.woodlin.tenant.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 租户状态变更请求
 *
 * @author mumu
 * @since 2026-03-18
 */
@Data
@Schema(description = "租户状态变更请求")
public class TenantStatusRequest {

    /**
     * 租户ID
     */
    @NotBlank(message = "租户ID不能为空")
    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantId;

    /**
     * 租户状态
     */
    @NotBlank(message = "租户状态不能为空")
    @Schema(description = "租户状态（1-启用，0-禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
}
