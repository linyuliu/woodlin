package com.mumu.woodlin.authorization.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.mumu.woodlin.authorization.entity.AuthCapability;
import com.mumu.woodlin.authorization.entity.AuthPolicy;
import com.mumu.woodlin.authorization.entity.AuthQuotaPolicy;
import com.mumu.woodlin.authorization.entity.AuthScope;
import com.mumu.woodlin.authorization.entity.AuthSubjectGrant;
import com.mumu.woodlin.authorization.mapper.AuthCapabilityMapper;
import com.mumu.woodlin.authorization.mapper.AuthPolicyMapper;
import com.mumu.woodlin.authorization.mapper.AuthQuotaPolicyMapper;
import com.mumu.woodlin.authorization.mapper.AuthScopeMapper;
import com.mumu.woodlin.authorization.mapper.AuthSubjectGrantMapper;
import com.mumu.woodlin.authorization.model.AuthorizationConstraint;
import com.mumu.woodlin.authorization.model.AuthorizationDecision;
import com.mumu.woodlin.authorization.model.AuthorizationRequest;
import com.mumu.woodlin.authorization.service.AuthorizationService;
import com.mumu.woodlin.common.response.R;

/**
 * 统一授权决策接口。
 *
 * @author mumu
 * @since 2026-06-02
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/authorization")
public class AuthorizationController {

    private final AuthorizationService authorizationService;
    private final AuthPolicyMapper authPolicyMapper;
    private final AuthCapabilityMapper authCapabilityMapper;
    private final AuthScopeMapper authScopeMapper;
    private final AuthSubjectGrantMapper authSubjectGrantMapper;
    private final AuthQuotaPolicyMapper authQuotaPolicyMapper;

    /**
     * 执行授权决策。
     *
     * @param request 授权请求
     * @return 授权决策
     */
    @PostMapping("/decision")
    public R<AuthorizationDecision> decision(@Valid @RequestBody AuthorizationRequest request) {
        return R.ok(authorizationService.can(request));
    }

    /**
     * 生成列表数据权限约束。
     *
     * @param request 授权请求
     * @return 数据权限约束
     */
    @PostMapping("/constraints")
    public R<AuthorizationConstraint> constraints(@Valid @RequestBody AuthorizationRequest request) {
        return R.ok(authorizationService.constraints(request));
    }

    /**
     * 查询 JSON 策略列表。
     *
     * @return 策略列表
     */
    @GetMapping("/policies")
    public R<List<AuthPolicy>> listPolicies() {
        return R.ok(authPolicyMapper.selectList(null));
    }

    /**
     * 新增 JSON 策略。
     *
     * @param policy 策略
     * @return 结果
     */
    @PostMapping("/policies")
    public R<Void> createPolicy(@RequestBody AuthPolicy policy) {
        authPolicyMapper.insert(policy);
        return R.ok();
    }

    /**
     * 更新 JSON 策略。
     *
     * @param policy 策略
     * @return 结果
     */
    @PutMapping("/policies")
    public R<Void> updatePolicy(@RequestBody AuthPolicy policy) {
        authPolicyMapper.updateById(policy);
        return R.ok();
    }

    /**
     * 删除 JSON 策略。
     *
     * @param policyId 策略ID
     * @return 结果
     */
    @DeleteMapping("/policies/{policyId}")
    public R<Void> deletePolicy(@PathVariable Long policyId) {
        authPolicyMapper.deleteById(policyId);
        return R.ok();
    }

    /**
     * 查询 Capability 列表。
     *
     * @return Capability 列表
     */
    @GetMapping("/capabilities")
    public R<List<AuthCapability>> listCapabilities() {
        return R.ok(authCapabilityMapper.selectList(null));
    }

    /**
     * 保存 Capability。
     *
     * @param capability Capability
     * @return 结果
     */
    @PostMapping("/capabilities")
    public R<Void> createCapability(@RequestBody AuthCapability capability) {
        authCapabilityMapper.insert(capability);
        return R.ok();
    }

    /**
     * 更新 Capability。
     *
     * @param capability Capability
     * @return 结果
     */
    @PutMapping("/capabilities")
    public R<Void> updateCapability(@RequestBody AuthCapability capability) {
        authCapabilityMapper.updateById(capability);
        return R.ok();
    }

    /**
     * 删除 Capability。
     *
     * @param capabilityId Capability ID
     * @return 结果
     */
    @DeleteMapping("/capabilities/{capabilityId}")
    public R<Void> deleteCapability(@PathVariable Long capabilityId) {
        authCapabilityMapper.deleteById(capabilityId);
        return R.ok();
    }

    /**
     * 查询 Scope 列表。
     *
     * @return Scope 列表
     */
    @GetMapping("/scopes")
    public R<List<AuthScope>> listScopes() {
        return R.ok(authScopeMapper.selectList(null));
    }

    /**
     * 保存 Scope。
     *
     * @param scope Scope
     * @return 结果
     */
    @PostMapping("/scopes")
    public R<Void> createScope(@RequestBody AuthScope scope) {
        authScopeMapper.insert(scope);
        return R.ok();
    }

    /**
     * 更新 Scope。
     *
     * @param scope Scope
     * @return 结果
     */
    @PutMapping("/scopes")
    public R<Void> updateScope(@RequestBody AuthScope scope) {
        authScopeMapper.updateById(scope);
        return R.ok();
    }

    /**
     * 删除 Scope。
     *
     * @param scopeId Scope ID
     * @return 结果
     */
    @DeleteMapping("/scopes/{scopeId}")
    public R<Void> deleteScope(@PathVariable Long scopeId) {
        authScopeMapper.deleteById(scopeId);
        return R.ok();
    }

    /**
     * 查询主体授权列表。
     *
     * @return 授权列表
     */
    @GetMapping("/grants")
    public R<List<AuthSubjectGrant>> listGrants() {
        return R.ok(authSubjectGrantMapper.selectList(null));
    }

    /**
     * 保存主体授权。
     *
     * @param grant 主体授权
     * @return 结果
     */
    @PostMapping("/grants")
    public R<Void> createGrant(@RequestBody AuthSubjectGrant grant) {
        authSubjectGrantMapper.insert(grant);
        return R.ok();
    }

    /**
     * 更新主体授权。
     *
     * @param grant 主体授权
     * @return 结果
     */
    @PutMapping("/grants")
    public R<Void> updateGrant(@RequestBody AuthSubjectGrant grant) {
        authSubjectGrantMapper.updateById(grant);
        return R.ok();
    }

    /**
     * 删除主体授权。
     *
     * @param grantId 授权ID
     * @return 结果
     */
    @DeleteMapping("/grants/{grantId}")
    public R<Void> deleteGrant(@PathVariable Long grantId) {
        authSubjectGrantMapper.deleteById(grantId);
        return R.ok();
    }

    /**
     * 查询 Quota 列表。
     *
     * @return Quota 列表
     */
    @GetMapping("/quotas")
    public R<List<AuthQuotaPolicy>> listQuotas() {
        return R.ok(authQuotaPolicyMapper.selectList(null));
    }

    /**
     * 保存 Quota。
     *
     * @param quota Quota
     * @return 结果
     */
    @PostMapping("/quotas")
    public R<Void> createQuota(@RequestBody AuthQuotaPolicy quota) {
        authQuotaPolicyMapper.insert(quota);
        return R.ok();
    }

    /**
     * 更新 Quota。
     *
     * @param quota Quota
     * @return 结果
     */
    @PutMapping("/quotas")
    public R<Void> updateQuota(@RequestBody AuthQuotaPolicy quota) {
        authQuotaPolicyMapper.updateById(quota);
        return R.ok();
    }

    /**
     * 删除 Quota。
     *
     * @param quotaId Quota ID
     * @return 结果
     */
    @DeleteMapping("/quotas/{quotaId}")
    public R<Void> deleteQuota(@PathVariable Long quotaId) {
        authQuotaPolicyMapper.deleteById(quotaId);
        return R.ok();
    }
}
