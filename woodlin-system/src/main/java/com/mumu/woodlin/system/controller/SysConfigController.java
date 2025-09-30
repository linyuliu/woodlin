package com.mumu.woodlin.system.controller;

import com.mumu.woodlin.common.response.Result;
import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 * 
 * @author mumu
 * @description 系统配置管理接口，支持Redis二级缓存
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
@Tag(name = "系统配置管理", description = "系统配置字典管理接口，支持二级缓存")
public class SysConfigController {
    
    private final ISysConfigService configService;
    
    /**
     * 查询配置列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询配置列表", description = "查询所有系统配置，使用Redis二级缓存")
    public Result<List<SysConfig>> list() {
        log.info("查询配置列表");
        List<SysConfig> list = configService.listWithCache();
        return Result.success("查询成功", list);
    }
    
    /**
     * 根据配置键名查询配置值
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据配置键名查询", description = "根据配置键名查询配置信息，使用Redis二级缓存")
    public Result<SysConfig> getByKey(
            @Parameter(description = "配置键名") @PathVariable String configKey) {
        log.info("查询配置: {}", configKey);
        SysConfig config = configService.getByKeyWithCache(configKey);
        if (config != null) {
            return Result.success("查询成功", config);
        }
        return Result.fail("配置不存在");
    }
    
    /**
     * 根据配置键名查询配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(summary = "查询配置值", description = "根据配置键名查询配置值，使用Redis二级缓存")
    public Result<String> getValueByKey(
            @Parameter(description = "配置键名") @PathVariable String configKey) {
        log.info("查询配置值: {}", configKey);
        String value = configService.getConfigValueByKey(configKey);
        if (value != null) {
            return Result.success("查询成功", value);
        }
        return Result.fail("配置不存在");
    }
    
    /**
     * 新增或更新配置
     */
    @PostMapping
    @Operation(summary = "新增或更新配置", description = "新增或更新系统配置，自动清除缓存")
    public Result<Void> saveOrUpdate(@RequestBody SysConfig config) {
        log.info("保存配置: {}", config.getConfigKey());
        boolean result = configService.saveOrUpdateConfig(config);
        if (result) {
            return Result.success("操作成功");
        }
        return Result.fail("操作失败");
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{configId}")
    @Operation(summary = "删除配置", description = "删除系统配置，自动清除缓存")
    public Result<Void> delete(
            @Parameter(description = "配置ID") @PathVariable Long configId) {
        log.info("删除配置: {}", configId);
        boolean result = configService.deleteConfig(configId);
        if (result) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }
    
    /**
     * 清除配置缓存
     */
    @DeleteMapping("/cache")
    @Operation(summary = "清除配置缓存", description = "清除所有系统配置缓存")
    public Result<Void> evictCache() {
        log.info("清除配置缓存");
        configService.evictCache();
        return Result.success("缓存清除成功");
    }
    
    /**
     * 预热配置缓存
     */
    @PostMapping("/cache/warmup")
    @Operation(summary = "预热配置缓存", description = "预热系统配置缓存")
    public Result<Void> warmupCache() {
        log.info("预热配置缓存");
        configService.warmupCache();
        return Result.success("缓存预热成功");
    }
}
