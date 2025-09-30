package com.mumu.woodlin.admin.controller;

import com.mumu.woodlin.security.config.ActivityMonitoringProperties;
import com.mumu.woodlin.security.service.UserActivityMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * 系统安全配置控制器
 *
 * @author mumu
 * @description 系统安全相关的配置管理，包括用户活动监控等
 * @since 2025-01-01
 */
@Tag(name = "系统安全配置", description = "系统安全相关配置管理接口，包括用户活动监控、会话管理等功能")
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "woodlin.security.activity-monitoring.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfigController {

    private final UserActivityMonitoringService activityMonitoringService;

    /**
     * 获取用户活动监控配置
     */
    @Operation(
        summary = "获取用户活动监控配置",
        description = "获取当前系统的用户活动监控配置信息，包括超时时间、检查间隔等参数"
    )
    @GetMapping("/activity-monitoring/config")
    public ActivityMonitoringProperties getActivityMonitoringConfig() {
        return activityMonitoringService.getConfig();
    }

    /**
     * 记录用户交互活动
     */
    @Operation(
        summary = "记录用户交互活动",
        description = "前端调用此接口记录用户的键盘、鼠标等交互活动，用于更新用户活动时间，防止会话超时"
    )
    @PostMapping("/activity-monitoring/record-interaction")
    public void recordUserInteraction() {
        // 由拦截器自动记录API活动，这里只是提供一个明确的接口供前端调用
        // 实际的记录逻辑在拦截器中已经完成
    }

    /**
     * 检查当前用户活动状态
     */
    @Operation(
        summary = "检查用户活动状态",
        description = "检查当前登录用户的活动状态，包括是否需要警告、剩余活动时间等信息"
    )
    @GetMapping("/activity-monitoring/status")
    public ActivityStatusResponse getActivityStatus() {
        // 这个方法会由拦截器自动记录为API活动
        return new ActivityStatusResponse();
    }

    /**
     * 用户活动状态响应
     */
    public static class ActivityStatusResponse {
        public String status = "active";
        public String message = "用户活动正常";
    }
}