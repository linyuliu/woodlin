package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.service.RedisCacheService;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import com.mumu.woodlin.system.mapper.SysOpenApiPolicyMapper;
import com.mumu.woodlin.system.service.ISysOpenApiPolicyService;
import com.mumu.woodlin.system.util.OpenApiSecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 开放 API 策略服务实现。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Service
@RequiredArgsConstructor
public class SysOpenApiPolicyServiceImpl extends ServiceImpl<SysOpenApiPolicyMapper, SysOpenApiPolicy>
    implements ISysOpenApiPolicyService {

    private final RedisCacheService redisCacheService;

    @Override
    public List<SysOpenApiPolicy> listPolicies(String keyword) {
        LambdaQueryWrapper<SysOpenApiPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOpenApiPolicy::getDeleted, "0");
        wrapper.and(StrUtil.isNotBlank(keyword), nested -> nested
            .like(SysOpenApiPolicy::getPolicyName, keyword)
            .or()
            .like(SysOpenApiPolicy::getPathPattern, keyword)
            .or()
            .like(SysOpenApiPolicy::getHttpMethod, keyword)
        );
        wrapper.orderByDesc(SysOpenApiPolicy::getUpdateTime).orderByDesc(SysOpenApiPolicy::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<SysOpenApiPolicy> listEnabledPoliciesWithCache() {
        return redisCacheService.getConfigCache(OpenApiSecurityConstants.POLICY_CACHE_KEY, () -> {
            LambdaQueryWrapper<SysOpenApiPolicy> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysOpenApiPolicy::getDeleted, "0");
            wrapper.eq(SysOpenApiPolicy::getEnabled, "1");
            return list(wrapper).stream()
                .sorted(Comparator
                    .comparingInt((SysOpenApiPolicy item) -> item.getPathPattern() == null ? 0 : item.getPathPattern().length())
                    .reversed()
                    .thenComparing(SysOpenApiPolicy::getHttpMethod, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        });
    }

    @Override
    public void evictPolicyCache() {
        redisCacheService.evictConfigCache(OpenApiSecurityConstants.POLICY_CACHE_KEY);
    }
}
