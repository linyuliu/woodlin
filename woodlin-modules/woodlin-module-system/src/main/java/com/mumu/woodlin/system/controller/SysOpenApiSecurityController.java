package com.mumu.woodlin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.authorization.entity.AuthOpenApiResource;
import com.mumu.woodlin.authorization.entity.AuthQuotaPolicy;
import com.mumu.woodlin.authorization.entity.AuthScope;
import com.mumu.woodlin.authorization.entity.AuthScopeResource;
import com.mumu.woodlin.authorization.entity.AuthSubjectGrant;
import com.mumu.woodlin.authorization.mapper.AuthOpenApiResourceMapper;
import com.mumu.woodlin.authorization.mapper.AuthQuotaPolicyMapper;
import com.mumu.woodlin.authorization.mapper.AuthScopeMapper;
import com.mumu.woodlin.authorization.mapper.AuthScopeResourceMapper;
import com.mumu.woodlin.authorization.mapper.AuthSubjectGrantMapper;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.dto.*;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.entity.SysOpenClient;
import com.mumu.woodlin.system.service.IOpenApiSecurityService;
import com.mumu.woodlin.system.service.ISysOpenApiPolicyService;
import com.mumu.woodlin.system.service.ISysOpenAppCredentialService;
import com.mumu.woodlin.system.service.ISysOpenAppService;
import com.mumu.woodlin.system.service.ISysOpenClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 开放 API 安全中心控制器。
 *
 * @author mumu
 * @since 2026-04-13
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/open-api")
@Tag(name = "开放API安全中心", description = "开放API安全中心管理接口")
public class SysOpenApiSecurityController {

    private final IOpenApiSecurityService openApiSecurityService;
    private final ISysOpenAppService openAppService;
    private final ISysOpenClientService openClientService;
    private final ISysOpenAppCredentialService credentialService;
    private final ISysOpenApiPolicyService policyService;
    private final AuthScopeMapper authScopeMapper;
    private final AuthSubjectGrantMapper authSubjectGrantMapper;
    private final AuthQuotaPolicyMapper authQuotaPolicyMapper;
    private final AuthOpenApiResourceMapper authOpenApiResourceMapper;
    private final AuthScopeResourceMapper authScopeResourceMapper;

    /**
     * 查询客户列表。
     *
     * @param keyword 关键字
     * @return 客户列表
     */
    @GetMapping("/clients")
    @Operation(summary = "查询开放客户列表")
    public R<List<SysOpenClient>> listClients(@RequestParam(required = false) String keyword) {
        requirePermission("openapi:app:list");
        return R.ok(openClientService.listClients(keyword));
    }

    /**
     * 新增客户。
     *
     * @param client 客户
     * @return 响应
     */
    @PostMapping("/clients")
    @Operation(summary = "新增开放客户")
    public R<Void> addClient(@Valid @RequestBody SysOpenClient client) {
        requirePermission("openapi:app:add");
        ensureSuccess(openClientService.createClient(client), "新增开放客户失败");
        return R.ok("新增成功");
    }

    /**
     * 更新客户。
     *
     * @param client 客户
     * @return 响应
     */
    @PutMapping("/clients")
    @Operation(summary = "更新开放客户")
    public R<Void> updateClient(@Valid @RequestBody SysOpenClient client) {
        requirePermission("openapi:app:edit");
        ensureSuccess(openClientService.updateClient(client), "修改开放客户失败");
        return R.ok("修改成功");
    }

    /**
     * 删除客户。
     *
     * @param clientIds 客户ID
     * @return 响应
     */
    @DeleteMapping("/clients/{clientIds}")
    @Operation(summary = "删除开放客户")
    public R<Void> removeClients(@PathVariable String clientIds) {
        requirePermission("openapi:app:remove");
        Arrays.stream(clientIds.split(","))
            .filter(item -> !item.isBlank())
            .map(Long::valueOf)
            .forEach(openClientService::removeById);
        return R.ok("删除成功");
    }

    /**
     * 概览。
     *
     * @return 概览
     */
    @GetMapping("/overview")
    @Operation(summary = "查询开放API概览")
    public R<OpenApiOverviewDto> overview() {
        requirePermission("openapi:overview:view");
        return R.ok(openApiSecurityService.getOverview());
    }

    /**
     * 获取全局配置。
     *
     * @return 全局配置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取开放API全局配置")
    public R<OpenApiGlobalSettingsDto> getSettings() {
        requirePermission("system:openapi:settings");
        return R.ok(openApiSecurityService.getGlobalSettings());
    }

    /**
     * 更新全局配置。
     *
     * @param settings 全局配置
     * @return 响应
     */
    @PutMapping("/settings")
    @Operation(summary = "更新开放API全局配置")
    public R<Void> updateSettings(@Valid @RequestBody OpenApiGlobalSettingsDto settings) {
        requirePermission("system:openapi:settings");
        openApiSecurityService.updateGlobalSettings(settings);
        return R.ok("保存成功");
    }

    /**
     * 查询应用列表。
     *
     * @param keyword 关键字
     * @return 应用列表
     */
    @GetMapping("/apps")
    @Operation(summary = "查询开放应用列表")
    public R<List<SysOpenApp>> listApps(@RequestParam(required = false) String keyword) {
        requirePermission("openapi:app:list");
        return R.ok(openAppService.listApps(keyword));
    }

    /**
     * 新增应用。
     *
     * @param app 应用
     * @return 响应
     */
    @PostMapping("/apps")
    @Operation(summary = "新增开放应用")
    public R<Void> addApp(@Valid @RequestBody SysOpenApp app) {
        requirePermission("openapi:app:add");
        ensureSuccess(openAppService.createApp(app), "新增开放应用失败");
        return R.ok("新增成功");
    }

    /**
     * 更新应用。
     *
     * @param app 应用
     * @return 响应
     */
    @PutMapping("/apps")
    @Operation(summary = "更新开放应用")
    public R<Void> updateApp(@Valid @RequestBody SysOpenApp app) {
        requirePermission("openapi:app:edit");
        ensureSuccess(openAppService.updateApp(app), "修改开放应用失败");
        return R.ok("修改成功");
    }

    /**
     * 删除应用。
     *
     * @param appIds 应用ID
     * @return 响应
     */
    @DeleteMapping("/apps/{appIds}")
    @Operation(summary = "删除开放应用")
    public R<Void> removeApps(@PathVariable String appIds) {
        requirePermission("openapi:app:remove");
        Arrays.stream(appIds.split(","))
            .filter(item -> !item.isBlank())
            .map(Long::valueOf)
            .forEach(openAppService::removeAppCascade);
        return R.ok("删除成功");
    }

    /**
     * 查询应用授权。
     *
     * @param appId 应用ID
     * @return 授权列表
     */
    @GetMapping("/apps/{appId}/grants")
    @Operation(summary = "查询开放应用授权")
    public R<List<AuthSubjectGrant>> listAppGrants(@PathVariable Long appId) {
        requirePermission("openapi:app:list");
        LambdaQueryWrapper<AuthSubjectGrant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthSubjectGrant::getSubjectType, "app");
        wrapper.eq(AuthSubjectGrant::getSubjectId, String.valueOf(appId));
        wrapper.eq(AuthSubjectGrant::getDeleted, "0");
        return R.ok(authSubjectGrantMapper.selectList(wrapper));
    }

    /**
     * 保存应用授权。
     *
     * @param appId 应用ID
     * @param request 授权请求
     * @return 响应
     */
    @PutMapping("/apps/{appId}/grants")
    @Operation(summary = "保存开放应用授权")
    public R<Void> saveAppGrants(@PathVariable Long appId, @RequestBody AppGrantRequest request) {
        requirePermission("openapi:app:edit");
        SysOpenApp app = requireApp(appId);
        LambdaQueryWrapper<AuthSubjectGrant> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AuthSubjectGrant::getSubjectType, "app");
        deleteWrapper.eq(AuthSubjectGrant::getSubjectId, String.valueOf(appId));
        authSubjectGrantMapper.delete(deleteWrapper);
        List<Long> scopeIds = request == null || request.getScopeIds() == null ? List.of() : request.getScopeIds();
        for (Long scopeId : scopeIds.stream().filter(Objects::nonNull).distinct().toList()) {
            AuthScope scope = authScopeMapper.selectById(scopeId);
            if (scope == null || !"1".equals(scope.getEnabled()) || "1".equals(scope.getDeleted())) {
                continue;
            }
            authSubjectGrantMapper.insert(new AuthSubjectGrant()
                .setSubjectType("app")
                .setSubjectId(String.valueOf(appId))
                .setCapabilityId(scope.getCapabilityId())
                .setScopeId(scope.getScopeId())
                .setStatus("1")
                .setTenantId(app.getTenantId()));
        }
        return R.ok("保存成功");
    }

    /**
     * 查询应用限额。
     *
     * @param appId 应用ID
     * @return 限额列表
     */
    @GetMapping("/apps/{appId}/quotas")
    @Operation(summary = "查询开放应用限额")
    public R<List<AuthQuotaPolicy>> listAppQuotas(@PathVariable Long appId) {
        requirePermission("openapi:app:list");
        LambdaQueryWrapper<AuthQuotaPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthQuotaPolicy::getSubjectType, "app");
        wrapper.eq(AuthQuotaPolicy::getSubjectId, String.valueOf(appId));
        wrapper.eq(AuthQuotaPolicy::getDeleted, "0");
        return R.ok(authQuotaPolicyMapper.selectList(wrapper));
    }

    /**
     * 保存应用限额。
     *
     * @param appId 应用ID
     * @param quotas 限额列表
     * @return 响应
     */
    @PutMapping("/apps/{appId}/quotas")
    @Operation(summary = "保存开放应用限额")
    public R<Void> saveAppQuotas(@PathVariable Long appId, @RequestBody List<AuthQuotaPolicy> quotas) {
        requirePermission("openapi:app:edit");
        SysOpenApp app = requireApp(appId);
        LambdaQueryWrapper<AuthQuotaPolicy> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AuthQuotaPolicy::getSubjectType, "app");
        deleteWrapper.eq(AuthQuotaPolicy::getSubjectId, String.valueOf(appId));
        authQuotaPolicyMapper.delete(deleteWrapper);
        if (quotas != null) {
            for (AuthQuotaPolicy quota : quotas) {
                quota.setQuotaId(null);
                quota.setSubjectType("app");
                quota.setSubjectId(String.valueOf(appId));
                quota.setTenantId(app.getTenantId());
                quota.setEnabled(quota.getEnabled() == null ? "1" : quota.getEnabled());
                authQuotaPolicyMapper.insert(quota);
            }
        }
        return R.ok("保存成功");
    }

    /**
     * 查询凭证。
     *
     * @param appId 应用ID
     * @return 凭证列表
     */
    @GetMapping("/apps/{appId}/credentials")
    @Operation(summary = "查询开放应用凭证")
    public R<List<OpenApiCredentialView>> listCredentials(
        @Parameter(description = "应用ID") @PathVariable Long appId) {
        requirePermission("openapi:credential:list");
        return R.ok(credentialService.listByAppId(appId));
    }

    /**
     * 签发凭证。
     *
     * @param appId   应用ID
     * @param request 请求
     * @return 凭证
     */
    @PostMapping("/apps/{appId}/credentials")
    @Operation(summary = "签发开放应用凭证")
    public R<OpenApiCredentialIssueResponse> issueCredential(
        @Parameter(description = "应用ID") @PathVariable Long appId,
        @Valid @RequestBody OpenApiCredentialRequest request) {
        requirePermission("openapi:credential:issue");
        return R.ok(credentialService.issueCredential(appId, request));
    }

    /**
     * 轮换凭证。
     *
     * @param credentialId 凭证ID
     * @param request      请求
     * @return 凭证
     */
    @PostMapping("/credentials/{credentialId}/rotate")
    @Operation(summary = "轮换开放应用凭证")
    public R<OpenApiCredentialIssueResponse> rotateCredential(
        @Parameter(description = "凭证ID") @PathVariable Long credentialId,
        @Valid @RequestBody OpenApiCredentialRequest request) {
        requirePermission("openapi:credential:issue");
        return R.ok(credentialService.rotateCredential(credentialId, request));
    }

    /**
     * 吊销凭证。
     *
     * @param credentialId 凭证ID
     * @return 响应
     */
    @PostMapping("/credentials/{credentialId}/revoke")
    @Operation(summary = "吊销开放应用凭证")
    public R<Void> revokeCredential(
        @Parameter(description = "凭证ID") @PathVariable Long credentialId) {
        requirePermission("openapi:credential:revoke");
        ensureSuccess(credentialService.revokeCredential(credentialId), "吊销凭证失败");
        return R.ok("吊销成功");
    }

    /**
     * 查询策略。
     *
     * @param keyword 关键字
     * @return 策略列表
     */
    @GetMapping("/policies")
    @Operation(summary = "查询开放API策略")
    public R<List<SysOpenApiPolicy>> listPolicies(@RequestParam(required = false) String keyword) {
        requirePermission("openapi:policy:list");
        return R.ok(policyService.listPolicies(keyword));
    }

    /**
     * 新增策略。
     *
     * @param policy 策略
     * @return 响应
     */
    @PostMapping("/policies")
    @Operation(summary = "新增开放API策略")
    public R<Void> addPolicy(@Valid @RequestBody SysOpenApiPolicy policy) {
        requirePermission("openapi:policy:add");
        ensurePolicyUnique(policy.getPathPattern(), policy.getHttpMethod(), null);
        policy.setEnabled(policy.getEnabled() == null ? "1" : policy.getEnabled());
        ensureSuccess(policyService.save(policy), "新增策略失败");
        policyService.evictPolicyCache();
        return R.ok("新增成功");
    }

    /**
     * 更新策略。
     *
     * @param policy 策略
     * @return 响应
     */
    @PutMapping("/policies")
    @Operation(summary = "更新开放API策略")
    public R<Void> updatePolicy(@Valid @RequestBody SysOpenApiPolicy policy) {
        requirePermission("openapi:policy:edit");
        if (policy.getPolicyId() == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "策略ID不能为空");
        }
        ensurePolicyUnique(policy.getPathPattern(), policy.getHttpMethod(), policy.getPolicyId());
        ensureSuccess(policyService.updateById(policy), "修改策略失败");
        policyService.evictPolicyCache();
        return R.ok("修改成功");
    }

    /**
     * 删除策略。
     *
     * @param policyIds 策略ID
     * @return 响应
     */
    @DeleteMapping("/policies/{policyIds}")
    @Operation(summary = "删除开放API策略")
    public R<Void> removePolicies(@PathVariable String policyIds) {
        requirePermission("openapi:policy:remove");
        Arrays.stream(policyIds.split(","))
            .filter(item -> !item.isBlank())
            .map(Long::valueOf)
            .forEach(policyService::removeById);
        policyService.evictPolicyCache();
        return R.ok("删除成功");
    }

    /**
     * 查询开放接口资源。
     *
     * @param keyword 关键字
     * @return 资源列表
     */
    @GetMapping("/resources")
    @Operation(summary = "查询开放接口资源")
    public R<List<AuthOpenApiResource>> listResources(@RequestParam(required = false) String keyword) {
        requirePermission("openapi:resource:list");
        LambdaQueryWrapper<AuthOpenApiResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthOpenApiResource::getDeleted, "0");
        wrapper.and(keyword != null && !keyword.isBlank(), nested -> nested
            .like(AuthOpenApiResource::getResourceCode, keyword)
            .or()
            .like(AuthOpenApiResource::getResourceName, keyword)
            .or()
            .like(AuthOpenApiResource::getPathPattern, keyword));
        wrapper.orderByAsc(AuthOpenApiResource::getResourceCode);
        return R.ok(authOpenApiResourceMapper.selectList(wrapper));
    }

    /**
     * 新增开放接口资源。
     *
     * @param resource 资源
     * @return 响应
     */
    @PostMapping("/resources")
    @Operation(summary = "新增开放接口资源")
    public R<Void> addResource(@RequestBody AuthOpenApiResource resource) {
        requirePermission("openapi:resource:add");
        authOpenApiResourceMapper.insert(resource);
        syncScopeResource(resource);
        return R.ok("新增成功");
    }

    /**
     * 更新开放接口资源。
     *
     * @param resource 资源
     * @return 响应
     */
    @PutMapping("/resources")
    @Operation(summary = "更新开放接口资源")
    public R<Void> updateResource(@RequestBody AuthOpenApiResource resource) {
        requirePermission("openapi:resource:edit");
        if (resource.getResourceId() == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "资源ID不能为空");
        }
        authOpenApiResourceMapper.updateById(resource);
        syncScopeResource(resource);
        return R.ok("修改成功");
    }

    /**
     * 删除开放接口资源。
     *
     * @param resourceIds 资源ID
     * @return 响应
     */
    @DeleteMapping("/resources/{resourceIds}")
    @Operation(summary = "删除开放接口资源")
    public R<Void> removeResources(@PathVariable String resourceIds) {
        requirePermission("openapi:resource:remove");
        Arrays.stream(resourceIds.split(","))
            .filter(item -> !item.isBlank())
            .map(Long::valueOf)
            .forEach(resourceId -> {
                authOpenApiResourceMapper.deleteById(resourceId);
                LambdaQueryWrapper<AuthScopeResource> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(AuthScopeResource::getResourceId, resourceId);
                authScopeResourceMapper.delete(wrapper);
            });
        return R.ok("删除成功");
    }

    /**
     * 查询资源已授权应用。
     *
     * @param resourceId 资源ID
     * @return 应用列表
     */
    @GetMapping("/resources/{resourceId}/apps")
    @Operation(summary = "查询资源已授权应用")
    public R<List<SysOpenApp>> listResourceApps(@PathVariable Long resourceId) {
        requirePermission("openapi:resource:list");
        AuthOpenApiResource resource = authOpenApiResourceMapper.selectById(resourceId);
        if (resource == null) {
            return R.ok(List.of());
        }
        LambdaQueryWrapper<AuthSubjectGrant> grantWrapper = new LambdaQueryWrapper<>();
        grantWrapper.eq(AuthSubjectGrant::getSubjectType, "app");
        grantWrapper.eq(AuthSubjectGrant::getScopeId, resource.getScopeId());
        grantWrapper.eq(AuthSubjectGrant::getStatus, "1");
        grantWrapper.eq(AuthSubjectGrant::getDeleted, "0");
        List<Long> appIds = authSubjectGrantMapper.selectList(grantWrapper).stream()
            .map(AuthSubjectGrant::getSubjectId)
            .map(Long::valueOf)
            .distinct()
            .toList();
        return R.ok(appIds.isEmpty() ? List.of() : new ArrayList<>(openAppService.listByIds(appIds)));
    }

    private void ensurePolicyUnique(String pathPattern, String httpMethod, Long excludeId) {
        LambdaQueryWrapper<SysOpenApiPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenApiPolicy::getPathPattern, pathPattern);
        wrapper.eq(SysOpenApiPolicy::getHttpMethod, httpMethod);
        wrapper.eq(SysOpenApiPolicy::getDeleted, "0");
        wrapper.ne(excludeId != null, SysOpenApiPolicy::getPolicyId, excludeId);
        if (policyService.count(wrapper) > 0) {
            throw BusinessException.of(ResultCode.CONFLICT, "相同路径和方法的策略已存在");
        }
    }

    private void requirePermission(String permission) {
        if (!SecurityUtil.hasPermission(permission)) {
            throw BusinessException.of(ResultCode.PERMISSION_DENIED, "权限不足: " + permission);
        }
    }

    private void ensureSuccess(boolean result, String message) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, message);
        }
    }

    private SysOpenApp requireApp(Long appId) {
        SysOpenApp app = openAppService.getById(appId);
        if (app == null || "1".equals(app.getDeleted())) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "开放应用不存在");
        }
        return app;
    }

    private void syncScopeResource(AuthOpenApiResource resource) {
        if (resource.getScopeId() == null || resource.getResourceId() == null) {
            return;
        }
        LambdaQueryWrapper<AuthScopeResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthScopeResource::getResourceId, resource.getResourceId());
        authScopeResourceMapper.delete(wrapper);
        authScopeResourceMapper.insert(new AuthScopeResource()
            .setScopeId(resource.getScopeId())
            .setResourceId(resource.getResourceId()));
    }

    @lombok.Data
    public static class AppGrantRequest {
        private List<Long> scopeIds;
    }
}
