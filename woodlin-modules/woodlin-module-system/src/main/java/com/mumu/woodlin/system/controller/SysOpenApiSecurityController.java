package com.mumu.woodlin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.dto.*;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.service.IOpenApiSecurityService;
import com.mumu.woodlin.system.service.ISysOpenApiPolicyService;
import com.mumu.woodlin.system.service.ISysOpenAppCredentialService;
import com.mumu.woodlin.system.service.ISysOpenAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

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
    private final ISysOpenAppCredentialService credentialService;
    private final ISysOpenApiPolicyService policyService;

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
}
