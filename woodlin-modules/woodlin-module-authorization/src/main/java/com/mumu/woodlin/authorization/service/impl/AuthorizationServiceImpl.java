package com.mumu.woodlin.authorization.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.authorization.entity.AuthPermission;
import com.mumu.woodlin.authorization.entity.AuthPolicy;
import com.mumu.woodlin.authorization.entity.AuthQuotaPolicy;
import com.mumu.woodlin.authorization.entity.AuthRole;
import com.mumu.woodlin.authorization.mapper.AuthPolicyMapper;
import com.mumu.woodlin.authorization.mapper.AuthorizationQueryMapper;
import com.mumu.woodlin.authorization.model.AuthorizationConstraint;
import com.mumu.woodlin.authorization.model.AuthorizationContext;
import com.mumu.woodlin.authorization.model.AuthorizationDecision;
import com.mumu.woodlin.authorization.model.AuthorizationEffect;
import com.mumu.woodlin.authorization.model.AuthorizationRequest;
import com.mumu.woodlin.authorization.model.AuthorizationResource;
import com.mumu.woodlin.authorization.model.AuthorizationSubject;
import com.mumu.woodlin.authorization.service.AuthorizationService;
import com.mumu.woodlin.common.constant.CommonConstant;

/**
 * 默认统一授权服务实现。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final String SUBJECT_USER = "user";
    private static final String SUBJECT_APP = "app";
    private static final String EFFECT_ALLOW = AuthorizationEffect.ALLOW.name();
    private static final String EFFECT_DENY = AuthorizationEffect.DENY.name();
    private static final String ENABLED = "1";
    private static final String DATA_SCOPE_ALL = "1";
    private static final String DATA_SCOPE_CUSTOM = "2";
    private static final String DATA_SCOPE_DEPT = "3";
    private static final String DATA_SCOPE_DEPT_AND_CHILD = "4";
    private static final String DATA_SCOPE_SELF = "5";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AuthorizationQueryMapper authorizationQueryMapper;
    private final AuthPolicyMapper authPolicyMapper;
    private final ObjectMapper objectMapper;

    private RedissonClient redissonClient;

    /**
     * 注入可选 Redisson 客户端。
     *
     * @param redissonClient Redisson 客户端
     */
    @Autowired(required = false)
    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public AuthorizationDecision can(AuthorizationRequest request) {
        AuthorizationDecision invalid = validate(request);
        if (invalid != null) {
            return invalid;
        }
        AuthorizationRequest normalized = normalize(request);
        AuthorizationDecision policyDecision = decideByJsonPolicy(normalized);
        if (policyDecision != null) {
            return policyDecision;
        }

        AuthorizationSubject subject = normalized.getSubject();
        if (SUBJECT_USER.equalsIgnoreCase(subject.getType())) {
            return decideUser(normalized);
        }
        if (SUBJECT_APP.equalsIgnoreCase(subject.getType())) {
            return decideApp(normalized);
        }
        return AuthorizationDecision.deny("unsupported subject type: " + subject.getType());
    }

    @Override
    public AuthorizationConstraint constraints(AuthorizationRequest request) {
        AuthorizationConstraint constraint = new AuthorizationConstraint().setAllowed(false);
        AuthorizationDecision invalid = validate(request);
        if (invalid != null) {
            return constraint;
        }
        AuthorizationRequest normalized = normalize(request);
        AuthorizationSubject subject = normalized.getSubject();
        if (!SUBJECT_USER.equalsIgnoreCase(subject.getType())) {
            return constraint;
        }

        Long userId = parseLong(subject.getId());
        if (userId == null) {
            return constraint;
        }
        List<AuthRole> roles = listUserRoles(userId);
        if (CollUtil.isEmpty(roles)) {
            return constraint;
        }
        AuthorizationContext context = normalized.getContext();
        constraint.setAllowed(true)
            .setTenantId(context.getTenantId())
            .setUserId(userId)
            .setDeptId(context.getDeptId());

        if (isSuperAdmin(roles)) {
            return constraint.setDataScope(DATA_SCOPE_ALL);
        }

        String dataScope = strongestDataScope(roles);
        constraint.setDataScope(dataScope);
        if (DATA_SCOPE_ALL.equals(dataScope)) {
            return constraint;
        }
        if (DATA_SCOPE_SELF.equals(dataScope)) {
            return constraint.setUserId(userId);
        }
        if (DATA_SCOPE_DEPT.equals(dataScope) || DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
            return constraint.setDeptId(context.getDeptId());
        }
        if (DATA_SCOPE_CUSTOM.equals(dataScope)) {
            List<Long> roleIds = roles.stream().map(AuthRole::getRoleId).filter(Objects::nonNull).toList();
            return constraint.setDeptIds(authorizationQueryMapper.selectCustomDeptIds(roleIds));
        }
        return constraint;
    }

    @Override
    public List<AuthRole> listUserRoles(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return authorizationQueryMapper.selectUserRoles(userId);
    }

    @Override
    public List<AuthPermission> listUserPermissions(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return authorizationQueryMapper.selectUserPermissions(userId);
    }

    @Override
    public List<String> listUserPermissionCodes(Long userId) {
        List<AuthPermission> permissions = listUserPermissions(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        return permissions.stream()
            .map(AuthPermission::getPermissionCode)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();
    }

    private AuthorizationDecision validate(AuthorizationRequest request) {
        if (request == null) {
            return AuthorizationDecision.deny("request is null");
        }
        if (request.getSubject() == null || StrUtil.isBlank(request.getSubject().getType())
            || StrUtil.isBlank(request.getSubject().getId())) {
            return AuthorizationDecision.deny("subject is required");
        }
        if (StrUtil.isBlank(request.getAction())) {
            return AuthorizationDecision.deny("action is required");
        }
        if (request.getResource() == null || StrUtil.isBlank(request.getResource().getType())) {
            return AuthorizationDecision.deny("resource is required");
        }
        return null;
    }

    private AuthorizationRequest normalize(AuthorizationRequest request) {
        if (request.getContext() == null) {
            request.setContext(new AuthorizationContext());
        }
        if (request.getContext().getTime() == null) {
            request.getContext().setTime(LocalDateTime.now());
        }
        return request;
    }

    private AuthorizationDecision decideUser(AuthorizationRequest request) {
        Long userId = parseLong(request.getSubject().getId());
        if (userId == null) {
            return AuthorizationDecision.deny("user subject id must be numeric");
        }

        List<AuthRole> roles = listUserRoles(userId);
        if (isSuperAdmin(roles)) {
            return AuthorizationDecision.allow("super admin").setMatchedPolicyCodes(List.of("RBAC_SUPER_ADMIN"));
        }

        List<AuthPermission> permissions = listUserPermissions(userId);
        for (AuthPermission permission : permissions) {
            if (matchesPermission(permission, request)) {
                return AuthorizationDecision.allow("rbac permission matched")
                    .setMatchedPolicyCodes(List.of(permission.getPermissionCode()));
            }
        }
        return AuthorizationDecision.deny("no rbac permission matched");
    }

    private AuthorizationDecision decideApp(AuthorizationRequest request) {
        Long appId = parseLong(request.getSubject().getId());
        if (appId == null && request.getContext() != null) {
            appId = request.getContext().getAppId();
        }
        if (appId == null) {
            return AuthorizationDecision.deny("app subject id must be numeric");
        }

        AuthorizationResource resource = request.getResource();
        String tenantId = request.getContext() == null ? null : request.getContext().getTenantId();
        int matches = authorizationQueryMapper.countAppScopeMatches(
            appId, request.getAction(), resource.getType(), resource.getId(), tenantId
        );
        if (matches <= 0) {
            return AuthorizationDecision.deny("capability or scope missing");
        }

        AuthorizationDecision quotaDecision = checkQuota(request);
        if (!quotaDecision.isAllowed()) {
            return quotaDecision;
        }
        return AuthorizationDecision.allow("capability scope matched").setMatchedPolicyCodes(List.of("CAPABILITY_SCOPE"));
    }

    private AuthorizationDecision checkQuota(AuthorizationRequest request) {
        AuthorizationSubject subject = request.getSubject();
        String tenantId = request.getContext() == null ? null : request.getContext().getTenantId();
        AuthorizationResource resource = request.getResource();
        List<AuthQuotaPolicy> policies = authorizationQueryMapper.selectQuotaPolicies(
            subject.getType(), subject.getId(), tenantId, request.getAction(), resource.getType(), resource.getId()
        );
        if (CollUtil.isEmpty(policies)) {
            return AuthorizationDecision.allow("quota not configured");
        }
        if (redissonClient == null) {
            return AuthorizationDecision.allow("quota skipped without redis");
        }
        for (AuthQuotaPolicy policy : policies) {
            AuthorizationDecision decision = consumeQuota(policy, subject);
            if (!decision.isAllowed()) {
                return decision;
            }
        }
        return AuthorizationDecision.allow("quota allowed");
    }

    private AuthorizationDecision consumeQuota(AuthQuotaPolicy policy, AuthorizationSubject subject) {
        if (!ENABLED.equals(policy.getEnabled()) || policy.getWindowSeconds() == null
            || policy.getLimitCount() == null || policy.getLimitCount() <= 0) {
            return AuthorizationDecision.allow("quota disabled");
        }
        long windowSeconds = policy.getWindowSeconds();
        long bucket = System.currentTimeMillis() / 1000 / windowSeconds;
        String key = "authz:quota:" + subject.getType() + ':' + subject.getId()
            + ':' + policy.getQuotaId() + ':' + bucket;
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        long value = counter.incrementAndGet();
        if (value == 1) {
            counter.expire(Duration.ofSeconds(windowSeconds + 1));
        }
        if (value > policy.getLimitCount()) {
            return AuthorizationDecision.deny("quota exceeded")
                .setMatchedPolicyCodes(List.of("QUOTA:" + policy.getQuotaId()));
        }
        return AuthorizationDecision.allow("quota allowed");
    }

    private AuthorizationDecision decideByJsonPolicy(AuthorizationRequest request) {
        List<AuthPolicy> policies = authPolicyMapper.selectList(new LambdaQueryWrapper<AuthPolicy>()
            .eq(AuthPolicy::getEnabled, ENABLED)
            .orderByDesc(AuthPolicy::getPriority)
            .orderByAsc(AuthPolicy::getPolicyId));
        AuthorizationDecision allowDecision = null;
        for (AuthPolicy policy : policies) {
            if (!policyTenantMatches(policy, request) || StrUtil.isBlank(policy.getPolicyJson())) {
                continue;
            }
            if (jsonPolicyMatches(policy, request)) {
                AuthorizationEffect effect = EFFECT_ALLOW.equalsIgnoreCase(policy.getEffect())
                    ? AuthorizationEffect.ALLOW
                    : AuthorizationEffect.DENY;
                AuthorizationDecision decision = new AuthorizationDecision()
                    .setEffect(effect)
                    .setReason("json policy matched")
                    .setMatchedPolicyCodes(List.of(policy.getPolicyCode()));
                if (AuthorizationEffect.DENY == effect) {
                    return decision;
                }
                if (allowDecision == null) {
                    allowDecision = decision;
                }
            }
        }
        return allowDecision;
    }

    private boolean policyTenantMatches(AuthPolicy policy, AuthorizationRequest request) {
        String policyTenantId = policy.getTenantId();
        String requestTenantId = request.getContext() == null ? null : request.getContext().getTenantId();
        return StrUtil.isBlank(policyTenantId) || StrUtil.equals(policyTenantId, requestTenantId);
    }

    @SuppressWarnings("unchecked")
    private boolean jsonPolicyMatches(AuthPolicy policy, AuthorizationRequest request) {
        try {
            Map<String, Object> json = objectMapper.readValue(policy.getPolicyJson(), MAP_TYPE);
            return subjectMatches((Map<String, Object>) json.get("subject"), request.getSubject())
                && actionMatches(json, request.getAction())
                && resourceMatches((Map<String, Object>) json.get("resource"), request.getResource())
                && contextMatches((Map<String, Object>) json.get("context"), request.getContext());
        } catch (Exception ex) {
            log.warn("授权策略 JSON 解析失败: policyCode={}", policy.getPolicyCode(), ex);
            return false;
        }
    }

    private boolean subjectMatches(Map<String, Object> expected, AuthorizationSubject actual) {
        if (expected == null || expected.isEmpty()) {
            return true;
        }
        return valueMatches(expected.get("type"), actual.getType()) && valueMatches(expected.get("id"), actual.getId());
    }

    private boolean actionMatches(Map<String, Object> json, String action) {
        Object actionValue = json.get("action");
        Object actionsValue = json.get("actions");
        if (actionValue == null && actionsValue == null) {
            return true;
        }
        return valueMatches(actionValue, action) || valueMatches(actionsValue, action);
    }

    private boolean resourceMatches(Map<String, Object> expected, AuthorizationResource actual) {
        if (expected == null || expected.isEmpty()) {
            return true;
        }
        return valueMatches(expected.get("type"), actual.getType()) && valueMatches(expected.get("id"), actual.getId());
    }

    private boolean contextMatches(Map<String, Object> expected, AuthorizationContext actual) {
        if (expected == null || expected.isEmpty()) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        boolean tenantMatches = valueMatches(expected.get("tenantId"), actual.getTenantId());
        boolean deptMatches = valueMatches(expected.get("deptId"), actual.getDeptId());
        boolean appMatches = valueMatches(expected.get("appId"), actual.getAppId());
        return tenantMatches && deptMatches && appMatches;
    }

    private boolean valueMatches(Object expected, Object actual) {
        if (expected == null) {
            return true;
        }
        if (expected instanceof Iterable<?> values) {
            for (Object value : values) {
                if (valueMatches(value, actual)) {
                    return true;
                }
            }
            return false;
        }
        String pattern = String.valueOf(expected);
        String value = actual == null ? null : String.valueOf(actual);
        return wildcardMatches(pattern, value);
    }

    private boolean matchesPermission(AuthPermission permission, AuthorizationRequest request) {
        if (permission == null || !ENABLED.equals(permission.getStatus())) {
            return false;
        }
        if (!wildcardMatches(permission.getPermissionCode(), request.getAction())) {
            return false;
        }
        AuthorizationResource resource = request.getResource();
        boolean typeMatches = StrUtil.isBlank(permission.getResourceType())
            || wildcardMatches(permission.getResourceType(), resource.getType());
        boolean idMatches = StrUtil.isBlank(permission.getResourceId())
            || wildcardMatches(permission.getResourceId(), resource.getId());
        return typeMatches && idMatches;
    }

    private boolean wildcardMatches(String pattern, String value) {
        if (StrUtil.isBlank(pattern)) {
            return false;
        }
        if ("*".equals(pattern) || "*:*:*".equals(pattern)) {
            return true;
        }
        if (value == null) {
            return false;
        }
        if (!pattern.contains("*")) {
            return StrUtil.equals(pattern, value);
        }
        String regex = Pattern.quote(pattern).replace("*", "\\E.*\\Q");
        return value.matches(regex);
    }

    private boolean isSuperAdmin(List<AuthRole> roles) {
        if (CollUtil.isEmpty(roles)) {
            return false;
        }
        return roles.stream()
            .map(AuthRole::getRoleCode)
            .filter(StrUtil::isNotBlank)
            .anyMatch(roleCode -> StrUtil.equals(roleCode, CommonConstant.SUPER_ADMIN_ROLE_CODE));
    }

    private String strongestDataScope(List<AuthRole> roles) {
        Set<String> scopes = new HashSet<>();
        roles.stream().map(AuthRole::getDataScope).filter(StrUtil::isNotBlank).forEach(scopes::add);
        if (scopes.contains(DATA_SCOPE_ALL)) {
            return DATA_SCOPE_ALL;
        }
        if (scopes.contains(DATA_SCOPE_DEPT_AND_CHILD)) {
            return DATA_SCOPE_DEPT_AND_CHILD;
        }
        if (scopes.contains(DATA_SCOPE_CUSTOM)) {
            return DATA_SCOPE_CUSTOM;
        }
        if (scopes.contains(DATA_SCOPE_DEPT)) {
            return DATA_SCOPE_DEPT;
        }
        return DATA_SCOPE_SELF;
    }

    private Long parseLong(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
