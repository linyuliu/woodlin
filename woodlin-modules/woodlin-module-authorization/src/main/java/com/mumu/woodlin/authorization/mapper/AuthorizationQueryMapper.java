package com.mumu.woodlin.authorization.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mumu.woodlin.authorization.entity.AuthPermission;
import com.mumu.woodlin.authorization.entity.AuthOpenApiResource;
import com.mumu.woodlin.authorization.entity.AuthQuotaPolicy;
import com.mumu.woodlin.authorization.entity.AuthRole;

/**
 * 授权决策查询 Mapper。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Mapper
public interface AuthorizationQueryMapper {

    /**
     * 查询用户所有角色。
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<AuthRole> selectUserRoles(@Param("userId") Long userId);

    /**
     * 查询用户所有权限。
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<AuthPermission> selectUserPermissions(@Param("userId") Long userId);

    /**
     * 查询角色所有权限。
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<AuthPermission> selectRolePermissions(@Param("roleId") Long roleId);

    /**
     * 查询角色自定义数据权限部门。
     *
     * @param roleIds 角色ID列表
     * @return 部门ID列表
     */
    List<Long> selectCustomDeptIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 查询应用能力授权匹配数量。
     *
     * @param appId 应用ID
     * @param action 动作
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param tenantId 租户ID
     * @return 匹配数量
     */
    int countAppScopeMatches(@Param("appId") Long appId,
                             @Param("action") String action,
                             @Param("resourceType") String resourceType,
                             @Param("resourceId") String resourceId,
                             @Param("tenantId") String tenantId);

    /**
     * 查询主体限额策略。
     *
     * @param subjectType 主体类型
     * @param subjectId 主体ID
     * @param tenantId 租户ID
     * @return 限额策略
     */
    List<AuthQuotaPolicy> selectQuotaPolicies(@Param("subjectType") String subjectType,
                                              @Param("subjectId") String subjectId,
                                              @Param("tenantId") String tenantId,
                                              @Param("action") String action,
                                              @Param("resourceType") String resourceType,
                                              @Param("resourceId") String resourceId);

    /**
     * 查询应用已授权开放接口资源。
     *
     * @param appId 应用ID
     * @param tenantId 租户ID
     * @return 资源列表
     */
    List<AuthOpenApiResource> selectAppAuthorizedResources(@Param("appId") Long appId,
                                                           @Param("tenantId") String tenantId);

    /**
     * 查询应用已授权 Scope 编码。
     *
     * @param appId 应用ID
     * @param tenantId 租户ID
     * @return Scope 编码列表
     */
    List<String> selectAppGrantedScopeCodes(@Param("appId") Long appId,
                                            @Param("tenantId") String tenantId);
}
