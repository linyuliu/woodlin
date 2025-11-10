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
 * @description 拦截API请求以记录用户活动
 * @since 2025-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "woodlin.security.activity-monitoring.enabled", havingValue = "true")
public class UserActivityInterceptor implements HandlerInterceptor {

    private final UserActivityMonitoringService activityMonitoringService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只监控已登录用户的活动
        if (StpUtil.isLogin()) {
            String userId = StpUtil.getLoginIdAsString();

            // 检查用户是否超时
            if (activityMonitoringService.isUserTimeout(userId)) {
                activityMonitoringService.forceLogout(userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户长时间未活动，已自动登出\",\"data\":null}");
                return false;
            }

            // 记录API请求活动
            activityMonitoringService.recordActivity(userId, "api");
        }

        return true;
    }
}
