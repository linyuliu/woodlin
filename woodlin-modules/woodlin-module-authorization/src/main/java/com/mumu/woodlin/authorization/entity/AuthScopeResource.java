package com.mumu.woodlin.authorization.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Scope 与开放接口资源关联。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Data
@Accessors(chain = true)
@TableName("auth_scope_resource")
public class AuthScopeResource {

    @TableField("scope_id")
    private Long scopeId;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("create_time")
    private LocalDateTime createTime;
}
