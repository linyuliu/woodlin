package com.mumu.woodlin.security.controller;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线用户管理接口
 *
 * @author mumu
 * @description 提供在线用户查询、统计等功能
 * @since 2025-12-26
 */
@Tag(name = "在线用户管理", description = "在线用户查询和统计")
@RestController
@RequestMapping("/security/online-users")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @Operation(summary = "获取在线用户数量")
    @GetMapping("/count")
    public R<Integer> getOnlineUserCount() {
        int count = onlineUserService.getOnlineUserCount();
        return R.ok(count);
    }

    @Operation(summary = "获取在线用户列表")
    @GetMapping("/list")
    public R<Map<String, Object>> getOnlineUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        List<Map<String, Object>> users = onlineUserService.getOnlineUsers(page, pageSize);
        int total = onlineUserService.getOnlineUserCount();
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", users);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("pages", (total + pageSize - 1) / pageSize);
        
        return R.ok(result);
    }

    @Operation(summary = "获取用户累计在线时长")
    @GetMapping("/duration/total/{userId}")
    public R<Long> getUserTotalDuration(@PathVariable String userId) {
        long duration = onlineUserService.getUserTotalDuration(userId);
        return R.ok(duration);
    }

    @Operation(summary = "获取用户当日在线时长")
    @GetMapping("/duration/daily/{userId}")
    public R<Long> getUserDailyDuration(@PathVariable String userId) {
        long duration = onlineUserService.getUserDailyDuration(userId);
        return R.ok(duration);
    }

    @Operation(summary = "获取用户指定日期在线时长")
    @GetMapping("/duration/{userId}/{date}")
    public R<Long> getUserDurationByDate(
            @PathVariable String userId,
            @PathVariable String date) {
        long duration = onlineUserService.getUserDurationByDate(userId, date);
        return R.ok(duration);
    }

    @Operation(summary = "强制下线用户")
    @PostMapping("/offline/{userId}")
    public R<Void> forceOffline(@PathVariable String userId) {
        onlineUserService.userOffline(userId);
        return R.ok();
    }

    @Operation(summary = "清理超时用户")
    @PostMapping("/cleanup")
    public R<Integer> cleanupInactiveUsers(@RequestParam(defaultValue = "1800") long timeoutSeconds) {
        int count = onlineUserService.cleanupInactiveUsers(timeoutSeconds);
        return R.ok(count);
    }
}
