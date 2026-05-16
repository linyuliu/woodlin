package com.mumu.woodlin.system.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 开放平台客户。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_open_client")
@Schema(description = "开放平台客户")
public class SysOpenClient extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "client_id", type = IdType.ASSIGN_ID)
    @Schema(description = "客户ID")
    private Long clientId;

    @NotBlank(message = "客户编码不能为空")
    @TableField("client_code")
    @Schema(description = "客户编码")
    private String clientCode;

    @NotBlank(message = "客户名称不能为空")
    @TableField("client_name")
    @Schema(description = "客户名称")
    private String clientName;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    @TableField("owner_user_id")
    @Schema(description = "负责人用户ID")
    private Long ownerUserId;

    @TableField("owner_dept_id")
    @Schema(description = "负责部门ID")
    private Long ownerDeptId;

    @TableField("owner_name")
    @Schema(description = "负责人")
    private String ownerName;

    @TableField("status")
    @Schema(description = "状态")
    private String status;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
