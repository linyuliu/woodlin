package com.mumu.woodlin.authorization.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 主体授权。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("auth_subject_grant")
public class AuthSubjectGrant extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "grant_id", type = IdType.ASSIGN_ID)
    private Long grantId;

    @TableField("subject_type")
    private String subjectType;

    @TableField("subject_id")
    private String subjectId;

    @TableField("capability_id")
    private Long capabilityId;

    @TableField("scope_id")
    private Long scopeId;

    @TableField("status")
    private String status;

    @TableField("tenant_id")
    private String tenantId;
}
