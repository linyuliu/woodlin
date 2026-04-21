package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;

import java.util.List;

/**
 * 开放 API 策略服务。
 *
 * @author mumu
 * @since 2026-04-13
 */
public interface ISysOpenApiPolicyService extends IService<SysOpenApiPolicy> {

    /**
     * 查询策略列表。
     *
     * @param keyword 关键字
     * @return 策略列表
     */
    List<SysOpenApiPolicy> listPolicies(String keyword);

    /**
     * 查询启用策略并缓存。
     *
     * @return 启用策略
     */
    List<SysOpenApiPolicy> listEnabledPoliciesWithCache();

    /**
     * 清理策略缓存。
     */
    void evictPolicyCache();
}
