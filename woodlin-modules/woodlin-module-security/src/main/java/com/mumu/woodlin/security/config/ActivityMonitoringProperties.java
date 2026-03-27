package com.mumu.woodlin.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用户活动监控配置属性
 *
 * @author mumu
 * @description 用户活动监控的配置属性，支持配置监控超时、检查间隔等
 * @since 2025-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.security.activity-monitoring")
public class ActivityMonitoringProperties {

    /**
     * 是否启用用户活动监控
     */
    private Boolean enabled = false;

    /**
     * 用户无活动超时时间（秒），超时后自动登出，-1表示不限制
     */
    private Long timeoutSeconds = 1800L;

    /**
     * 监控检查间隔（秒）
     */
    private Long checkIntervalSeconds = 60L;

    /**
     * 是否监控API请求活动
     */
    private Boolean monitorApiRequests = false;

    /**
     * 是否监控前端用户交互（键盘、鼠标）
     */
    private Boolean monitorUserInteractions = true;

    /**
     * 活动监控警告提前时间（秒）
     */
    private Long warningBeforeTimeoutSeconds = 60 * 60 * 2L;
}
