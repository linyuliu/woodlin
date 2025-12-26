package com.mumu.woodlin.security.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.mumu.woodlin.security.service.UserActivityMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户活动监控拦截器
 *
 * @author mumu
 * @description 已废弃：后端不再监控API请求，改由前端监控用户交互
 *              保留此类用于超时检查，但不记录API活动
 * @since 2025-01-01
 * @deprecated 后端API监控已移除，改为前端用户交互监控
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "woodlin.security.activity-monitoring.enabled", havingValue = "true")
@Deprecated
public class UserActivityInterceptor implements HandlerInterceptor {

    private final UserActivityMonitoringService activityMonitoringService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只监控已登录用户的活动
        if (StpUtil.isLogin()) {
            String userId = StpUtil.getLoginIdAsString();

            // 检查用户是否超时（基于前端上报的用户交互数据）
            if (activityMonitoringService.isUserTimeout(userId)) {
                activityMonitoringService.forceLogout(userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户长时间未活动，已自动登出\",\"data\":null}");
                return false;
            }

            // 后端不再记录API活动 - 改为前端上报用户交互
            // activityMonitoringService.recordActivity(userId, "api");
        }

        return true;
    }
}
