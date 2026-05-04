package com.mumu.woodlin.system.controller;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.service.OnlineUserService;
import com.mumu.woodlin.system.dto.ServerInfoDto;
import com.mumu.woodlin.system.entity.SysLoginLog;
import com.mumu.woodlin.system.entity.SysOperLog;
import com.mumu.woodlin.system.mapper.SysLoginLogMapper;
import com.mumu.woodlin.system.mapper.SysOperLogMapper;

/**
 * 系统监控控制器
 *
 * @author yulin
 * @description 系统监控相关接口，包含在线用户、登录日志、操作日志、服务器信息、缓存监控等
 * @since 2026-06
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统监控", description = "在线用户、登录日志、操作日志、服务器信息、缓存监控")
public class MonitorController {

    private final SysLoginLogMapper loginLogMapper;
    private final SysOperLogMapper operLogMapper;

    @Autowired(required = false)
    private OnlineUserService onlineUserService;

    @Autowired(required = false)
    private CacheManager cacheManager;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /* ---------------- 在线用户 ---------------- */

    /**
     * 分页查询在线用户
     */
    @GetMapping("/online")
    @Operation(summary = "在线用户分页", description = "分页查询当前在线用户列表")
    public R<PageResult<Map<String, Object>>> pageOnline(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "ipaddr", required = false) String ipaddr) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 10 : size;
        if (onlineUserService == null) {
            return R.ok(PageResult.empty((long) pageNum, (long) pageSize));
        }
        List<Map<String, Object>> records = onlineUserService.getOnlineUsers(pageNum, pageSize);
        if (StringUtils.hasText(username) || StringUtils.hasText(ipaddr)) {
            records = records.stream().filter(m -> matches(m, username, ipaddr)).toList();
        }
        long total = onlineUserService.getOnlineUserCount();
        return R.ok(PageResult.success((long) pageNum, (long) pageSize, total, records));
    }

    private boolean matches(Map<String, Object> entry, String username, String ipaddr) {
        if (StringUtils.hasText(username)) {
            Object value = entry.get("username");
            if (value == null || !value.toString().contains(username)) {
                return false;
            }
        }
        if (StringUtils.hasText(ipaddr)) {
            Object value = entry.get("ip");
            if (value == null || !value.toString().contains(ipaddr)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 强制下线
     */
    @DeleteMapping("/online/{tokenId}")
    @Operation(summary = "强制下线", description = "根据tokenId（即userId）强制用户下线")
    public R<Void> forceLogout(
            @Parameter(description = "tokenId", required = true) @PathVariable String tokenId) {
        if (onlineUserService != null) {
            onlineUserService.userOffline(tokenId);
        }
        return R.ok("下线成功");
    }

    /**
     * 批量强制下线
     */
    @DeleteMapping("/online/batch")
    @Operation(summary = "批量强制下线", description = "根据tokenId列表批量强制下线")
    public R<Void> batchForceLogout(@RequestBody BatchTokenRequest body) {
        if (onlineUserService != null && body != null && body.getTokenIds() != null) {
            for (String tokenId : body.getTokenIds()) {
                if (StringUtils.hasText(tokenId)) {
                    onlineUserService.userOffline(tokenId);
                }
            }
        }
        return R.ok("批量下线成功");
    }

    /**
     * 批量下线请求体
     */
    public static class BatchTokenRequest {
        private List<String> tokenIds;

        public List<String> getTokenIds() {
            return tokenIds;
        }

        public void setTokenIds(List<String> tokenIds) {
            this.tokenIds = tokenIds;
        }
    }

    /* ---------------- 登录日志 ---------------- */

    /**
     * 分页查询登录日志
     */
    @GetMapping("/loginLog")
    @Operation(summary = "登录日志分页", description = "按条件分页查询登录日志")
    public R<PageResult<SysLoginLog>> pageLoginLog(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "ipaddr", required = false) String ipaddr,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        long current = page == null || page < 1 ? 1L : page;
        long pageSize = size == null || size < 1 ? 10L : size;
        Page<SysLoginLog> pageParam = new Page<>(current, pageSize);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysLoginLog::getUsername, username);
        }
        if (StringUtils.hasText(ipaddr)) {
            wrapper.like(SysLoginLog::getIpaddr, ipaddr);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        LocalDateTime start = parseDateTime(startTime);
        if (start != null) {
            wrapper.ge(SysLoginLog::getLoginTime, start);
        }
        LocalDateTime end = parseDateTime(endTime);
        if (end != null) {
            wrapper.le(SysLoginLog::getLoginTime, end);
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        Page<SysLoginLog> result = loginLogMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.success(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    /**
     * 删除登录日志
     */
    @DeleteMapping("/loginLog/{id}")
    @Operation(summary = "删除登录日志", description = "根据ID删除登录日志")
    public R<Void> deleteLoginLog(
            @Parameter(description = "登录日志ID", required = true) @PathVariable Long id) {
        loginLogMapper.deleteById(id);
        return R.ok("删除成功");
    }

    /**
     * 清空登录日志
     */
    @DeleteMapping("/loginLog/clean")
    @Operation(summary = "清空登录日志", description = "清空所有登录日志")
    public R<Void> cleanLoginLog() {
        loginLogMapper.delete(new LambdaQueryWrapper<>());
        return R.ok("清空成功");
    }

    /* ---------------- 操作日志 ---------------- */

    /**
     * 分页查询操作日志
     */
    @GetMapping("/operLog")
    @Operation(summary = "操作日志分页", description = "按条件分页查询操作日志")
    public R<PageResult<SysOperLog>> pageOperLog(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        long current = page == null || page < 1 ? 1L : page;
        long pageSize = size == null || size < 1 ? 10L : size;
        Page<SysOperLog> pageParam = new Page<>(current, pageSize);
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(SysOperLog::getTitle, title);
        }
        if (StringUtils.hasText(status)) {
            try {
                wrapper.eq(SysOperLog::getStatus, Integer.parseInt(status));
            } catch (NumberFormatException ignored) {
                // ignore invalid status
            }
        }
        LocalDateTime start = parseDateTime(startTime);
        if (start != null) {
            wrapper.ge(SysOperLog::getOperTime, start);
        }
        LocalDateTime end = parseDateTime(endTime);
        if (end != null) {
            wrapper.le(SysOperLog::getOperTime, end);
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);
        Page<SysOperLog> result = operLogMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.success(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    /**
     * 操作日志详情
     */
    @GetMapping("/operLog/{id}")
    @Operation(summary = "操作日志详情", description = "根据ID查询操作日志详情")
    public R<SysOperLog> getOperLog(
            @Parameter(description = "操作日志ID", required = true) @PathVariable Long id) {
        return R.ok(operLogMapper.selectById(id));
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/operLog/{id}")
    @Operation(summary = "删除操作日志", description = "根据ID删除操作日志")
    public R<Void> deleteOperLog(
            @Parameter(description = "操作日志ID", required = true) @PathVariable Long id) {
        operLogMapper.deleteById(id);
        return R.ok("删除成功");
    }

    /**
     * 清空操作日志
     */
    @DeleteMapping("/operLog/clean")
    @Operation(summary = "清空操作日志", description = "清空所有操作日志")
    public R<Void> cleanOperLog() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
        return R.ok("清空成功");
    }

    /* ---------------- 服务器监控 ---------------- */

    /**
     * 服务器信息
     */
    @GetMapping("/server")
    @Operation(summary = "服务器信息", description = "获取CPU/内存/JVM/磁盘等服务器信息")
    public R<ServerInfoDto> server() {
        ServerInfoDto dto = new ServerInfoDto();

        Runtime runtime = Runtime.getRuntime();
        ServerInfoDto.CpuInfo cpu = new ServerInfoDto.CpuInfo();
        cpu.setCpuNum(runtime.availableProcessors());
        dto.setCpu(cpu);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        ServerInfoDto.MemInfo mem = new ServerInfoDto.MemInfo();
        double memTotalMb = bytesToMb(heap.getCommitted());
        double memUsedMb = bytesToMb(heap.getUsed());
        mem.setTotal(memTotalMb);
        mem.setUsed(memUsedMb);
        mem.setFree(round(memTotalMb - memUsedMb));
        mem.setUsage(memTotalMb > 0 ? round(memUsedMb / memTotalMb * 100) : 0.0);
        dto.setMem(mem);

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        ServerInfoDto.JvmInfo jvm = new ServerInfoDto.JvmInfo();
        double jvmTotalMb = bytesToMb(runtime.totalMemory());
        double jvmFreeMb = bytesToMb(runtime.freeMemory());
        double jvmMaxMb = bytesToMb(runtime.maxMemory());
        double jvmUsedMb = round(jvmTotalMb - jvmFreeMb);
        jvm.setTotal(jvmTotalMb);
        jvm.setMax(jvmMaxMb);
        jvm.setFree(jvmFreeMb);
        jvm.setUsed(jvmUsedMb);
        jvm.setUsage(jvmTotalMb > 0 ? round(jvmUsedMb / jvmTotalMb * 100) : 0.0);
        jvm.setVersion(System.getProperty("java.version"));
        jvm.setHome(System.getProperty("java.home"));
        jvm.setName(runtimeBean.getVmName());
        Instant startInstant = Instant.ofEpochMilli(runtimeBean.getStartTime());
        jvm.setStartTime(LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()).format(DATE_TIME_FORMATTER));
        jvm.setRunTime(formatDuration(Duration.ofMillis(runtimeBean.getUptime())));
        jvm.setInputArgs(String.join(" ", runtimeBean.getInputArguments()));
        dto.setJvm(jvm);

        ServerInfoDto.SysInfo sys = new ServerInfoDto.SysInfo();
        try {
            InetAddress local = InetAddress.getLocalHost();
            sys.setComputerName(local.getHostName());
            sys.setComputerIp(local.getHostAddress());
        } catch (UnknownHostException e) {
            sys.setComputerName("unknown");
            sys.setComputerIp("unknown");
        }
        sys.setUserDir(System.getProperty("user.dir"));
        sys.setOsName(System.getProperty("os.name"));
        sys.setOsArch(System.getProperty("os.arch"));
        dto.setSys(sys);

        List<ServerInfoDto.DiskInfo> disks = new ArrayList<>();
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                long total = root.getTotalSpace();
                if (total <= 0) {
                    continue;
                }
                long free = root.getFreeSpace();
                long used = total - free;
                ServerInfoDto.DiskInfo disk = new ServerInfoDto.DiskInfo();
                disk.setDirName(root.getAbsolutePath());
                disk.setSysTypeName(System.getProperty("os.name"));
                disk.setTypeName("local");
                disk.setTotal(formatBytes(total));
                disk.setFree(formatBytes(free));
                disk.setUsed(formatBytes(used));
                disk.setUsage(round((double) used / total * 100));
                disks.add(disk);
            }
        }
        dto.setDisk(disks);

        return R.ok(dto);
    }

    /* ---------------- 缓存监控 ---------------- */

    /**
     * 获取缓存名称列表
     */
    @GetMapping("/cache")
    @Operation(summary = "缓存概览", description = "获取所有缓存名称列表")
    public R<Map<String, Object>> cacheOverview() {
        List<String> names = cacheManager == null
                ? Collections.emptyList()
                : new ArrayList<>(cacheManager.getCacheNames());
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("cacheNames", names);
        return R.ok(data);
    }

    /**
     * 获取缓存键列表
     */
    @GetMapping("/cache/{cacheName}/keys")
    @Operation(summary = "缓存键列表", description = "获取指定缓存的键列表")
    public R<List<String>> cacheKeys(
            @Parameter(description = "缓存名称", required = true) @PathVariable String cacheName) {
        if (cacheManager == null) {
            return R.ok(Collections.emptyList());
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return R.ok(Collections.emptyList());
        }
        Object nativeCache = cache.getNativeCache();
        List<String> keys = new ArrayList<>();
        if (nativeCache instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) {
                keys.add(String.valueOf(key));
            }
        } else if (nativeCache instanceof Collection<?> collection) {
            for (Object key : collection) {
                keys.add(String.valueOf(key));
            }
        } else if (nativeCache instanceof Set<?> set) {
            keys = set.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return R.ok(keys);
    }

    /**
     * 获取缓存值
     */
    @GetMapping("/cache/{cacheName}/{cacheKey}")
    @Operation(summary = "缓存值", description = "获取指定缓存项的值")
    public R<Map<String, Object>> cacheValue(
            @Parameter(description = "缓存名称", required = true) @PathVariable String cacheName,
            @Parameter(description = "缓存键", required = true) @PathVariable String cacheKey) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("cacheName", cacheName);
        data.put("cacheKey", cacheKey);
        data.put("cacheValue", "");
        if (cacheManager == null) {
            return R.ok(data);
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            if (wrapper != null && wrapper.get() != null) {
                data.put("cacheValue", String.valueOf(wrapper.get()));
            }
        }
        return R.ok(data);
    }

    /**
     * 清空指定缓存
     */
    @DeleteMapping("/cache/{cacheName}")
    @Operation(summary = "清空缓存", description = "清空指定名称的缓存")
    public R<Void> clearCache(
            @Parameter(description = "缓存名称", required = true) @PathVariable String cacheName) {
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        return R.ok("清空成功");
    }

    /**
     * 删除缓存键
     */
    @DeleteMapping("/cache/{cacheName}/{cacheKey}")
    @Operation(summary = "删除缓存项", description = "删除指定缓存中的某个键")
    public R<Void> evictCacheKey(
            @Parameter(description = "缓存名称", required = true) @PathVariable String cacheName,
            @Parameter(description = "缓存键", required = true) @PathVariable String cacheKey) {
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(cacheKey);
            }
        }
        return R.ok("删除成功");
    }

    /* ---------------- 工具方法 ---------------- */

    private LocalDateTime parseDateTime(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            if (raw.length() <= 10) {
                return LocalDateTime.parse(raw + " 00:00:00", DATE_TIME_FORMATTER);
            }
            if (raw.contains("T")) {
                return LocalDateTime.parse(raw);
            }
            return LocalDateTime.parse(raw, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("parseDateTime failed: {}", raw);
            return null;
        }
    }

    private double bytesToMb(long bytes) {
        return round(bytes / 1024.0 / 1024.0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatBytes(long bytes) {
        double gb = bytes / 1024.0 / 1024.0 / 1024.0;
        if (gb >= 1) {
            return String.format("%.2f GB", gb);
        }
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.2f MB", mb);
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.toSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%d天%d时%d分%d秒", days, hours, minutes, secs);
    }
}
