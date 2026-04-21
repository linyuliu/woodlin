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
 * 开放应用实体。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_open_app")
@Schema(description = "开放应用")
public class SysOpenApp extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID。
     */
    @TableId(value = "app_id", type = IdType.ASSIGN_ID)
    @Schema(description = "应用ID")
    private Long appId;

    /**
     * 应用编码。
     */
    @NotBlank(message = "应用编码不能为空")
    @TableField("app_code")
    @Schema(description = "应用编码")
    private String appCode;

    /**
     * 应用名称。
     */
    @NotBlank(message = "应用名称不能为空")
    @TableField("app_name")
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用状态。
     */
    @TableField("status")
    @Schema(description = "应用状态", example = "1")
    private String status;

    /**
     * 租户ID。
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 负责人。
     */
    @TableField("owner_name")
    @Schema(description = "负责人")
    private String ownerName;

    /**
     * IP 白名单。
     */
    @TableField("ip_whitelist")
    @Schema(description = "IP白名单")
    private String ipWhitelist;

    /**
     * 备注。
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
