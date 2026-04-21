package com.mumu.woodlin.system.service;

import com.mumu.woodlin.system.dto.OpenApiGlobalSettingsDto;
import com.mumu.woodlin.system.dto.OpenApiOverviewDto;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import com.mumu.woodlin.system.model.OpenApiRuntimeContext;

import java.util.List;

/**
 * 开放 API 安全服务。
 *
 * @author mumu
 * @since 2026-04-13
 */
public interface IOpenApiSecurityService {

    /**
     * 获取全局配置。
     *
     * @return 全局配置
     */
    OpenApiGlobalSettingsDto getGlobalSettings();

    /**
     * 更新全局配置。
     *
     * @param settings 全局配置
     */
    void updateGlobalSettings(OpenApiGlobalSettingsDto settings);

    /**
     * 查询概览。
     *
     * @return 概览
     */
    OpenApiOverviewDto getOverview();

    /**
     * 解析请求上下文。
     *
     * @param requestPath 请求路径
     * @param httpMethod  请求方法
     * @return 运行时上下文
     */
    OpenApiRuntimeContext resolveRuntimeContext(String requestPath, String httpMethod);

    /**
     * 匹配策略列表。
     *
     * @return 启用策略
     */
    List<SysOpenApiPolicy> listEnabledPolicies();
}
