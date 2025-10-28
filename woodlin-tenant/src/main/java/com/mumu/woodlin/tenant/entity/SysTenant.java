package com.mumu.woodlin.tenant.entity;

import java.io.Serial;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 租户信息实体
 * 
 * @author mumu
 * @description 租户信息实体类，用于多租户数据隔离
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
@Schema(description = "租户信息")
public class SysTenant extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户ID
     */
    @TableId(value = "tenant_id", type = IdType.ASSIGN_ID)
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 租户名称
     */
    @TableField("tenant_name")
    @Schema(description = "租户名称")
    private String tenantName;
    
    /**
     * 租户编码
     */
    @TableField("tenant_code")
    @Schema(description = "租户编码")
    private String tenantCode;
    
    /**
     * 联系人
     */
    @TableField("contact_name")
    @Schema(description = "联系人")
    private String contactName;
    
    /**
     * 联系电话
     */
    @TableField("contact_phone")
    @Schema(description = "联系电话")
    private String contactPhone;
    
    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    @Schema(description = "联系邮箱")
    private String contactEmail;
    
    /**
     * 租户状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "租户状态", example = "1")
    private String status;
    
    /**
     * 过期时间
     */
    @TableField("expire_time")
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
    
    /**
     * 用户数量限制
     */
    @TableField("user_limit")
    @Schema(description = "用户数量限制")
    private Integer userLimit;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}