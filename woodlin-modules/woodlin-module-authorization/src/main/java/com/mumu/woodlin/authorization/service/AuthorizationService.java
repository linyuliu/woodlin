package com.mumu.woodlin.authorization.service;

import java.util.List;

import com.mumu.woodlin.authorization.entity.AuthPermission;
import com.mumu.woodlin.authorization.entity.AuthRole;
import com.mumu.woodlin.authorization.model.AuthorizationConstraint;
import com.mumu.woodlin.authorization.model.AuthorizationDecision;
import com.mumu.woodlin.authorization.model.AuthorizationRequest;

/**
 * 统一授权服务。
 *
 * @author mumu
 * @since 2026-06-02
 */
public interface AuthorizationService {

    /**
     * 判断主体是否可以对资源执行动作。
     *
     * @param request 授权请求
     * @return 授权决策
     */
    AuthorizationDecision can(AuthorizationRequest request);

    /**
     * 生成列表查询数据权限约束。
     *
     * @param request 授权请求
     * @return 数据权限约束
     */
    AuthorizationConstraint constraints(AuthorizationRequest request);

    /**
     * 查询用户所有授权角色。
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<AuthRole> listUserRoles(Long userId);

    /**
     * 查询用户所有授权权限。
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<AuthPermission> listUserPermissions(Long userId);

    /**
     * 查询用户所有授权权限编码。
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> listUserPermissionCodes(Long userId);
}
