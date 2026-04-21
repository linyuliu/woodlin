package com.mumu.woodlin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 开放 API 策略实体。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_open_api_policy")
@Schema(description = "开放API安全策略")
public class SysOpenApiPolicy extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 策略ID。
     */
    @TableId(value = "policy_id", type = IdType.ASSIGN_ID)
    @Schema(description = "策略ID")
    private Long policyId;

    /**
     * 策略名称。
     */
    @NotBlank(message = "策略名称不能为空")
    @TableField("policy_name")
    @Schema(description = "策略名称")
    private String policyName;

    /**
     * 路径模式。
     */
    @NotBlank(message = "路径模式不能为空")
    @TableField("path_pattern")
    @Schema(description = "路径模式")
    private String pathPattern;

    /**
     * HTTP 方法。
     */
    @NotBlank(message = "HTTP方法不能为空")
    @TableField("http_method")
    @Schema(description = "HTTP方法")
    private String httpMethod;

    /**
     * 安全模式。
     */
    @TableField("security_mode")
    @Schema(description = "安全模式")
    private String securityMode;

    /**
     * 签名算法。
     */
    @TableField("signature_algorithm")
    @Schema(description = "签名算法")
    private String signatureAlgorithm;

    /**
     * 加密算法。
     */
    @TableField("encryption_algorithm")
    @Schema(description = "加密算法")
    private String encryptionAlgorithm;

    /**
     * 时间窗秒数。
     */
    @TableField("timestamp_window_seconds")
    @Schema(description = "时间窗秒数")
    private Integer timestampWindowSeconds;

    /**
     * 是否启用 nonce。
     */
    @TableField("nonce_enabled")
    @Schema(description = "是否启用nonce")
    private String nonceEnabled;

    /**
     * nonce TTL。
     */
    @TableField("nonce_ttl_seconds")
    @Schema(description = "nonce TTL 秒数")
    private Integer nonceTtlSeconds;

    /**
     * 是否要求租户。
     */
    @TableField("tenant_required")
    @Schema(description = "是否要求租户")
    private String tenantRequired;

    /**
     * 是否启用。
     */
    @TableField("enabled")
    @Schema(description = "是否启用")
    private String enabled;

    /**
     * 备注。
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
