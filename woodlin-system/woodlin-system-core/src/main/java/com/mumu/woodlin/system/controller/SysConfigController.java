package com.mumu.woodlin.system.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.common.entity.BuildInfo;
import com.mumu.woodlin.common.service.BuildInfoService;
import com.mumu.woodlin.system.dto.ConfigBatchUpdateDto;
import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.service.ISysConfigService;

/**
 * 系统配置控制器
 * 
 * @author mumu
 * @description 系统配置管理控制器，提供配置的增删改查和批量更新功能
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统配置管理", description = "系统配置管理相关接口，包括配置的增删改查、批量更新等功能")
public class SysConfigController {
    
    private final ISysConfigService configService;
    private final BuildInfoService buildInfoService;
    
    /**
     * 获取所有配置列表
     */
    @GetMapping("/list")
    @Operation(
        summary = "获取所有配置列表",
        description = "获取系统中所有的配置项，使用缓存优化查询性能"
    )
    public R<List<SysConfig>> list() {
        List<SysConfig> list = configService.listWithCache();
        return R.ok(list);
    }
    
    /**
     * 根据配置键名获取配置
     */
    @GetMapping("/key/{configKey}")
    @Operation(
        summary = "根据配置键名获取配置",
        description = "通过配置键名获取配置对象，使用缓存优化查询性能"
    )
    public R<SysConfig> getByKey(
            @Parameter(description = "配置键名", required = true, example = "api.encryption.enabled") 
            @PathVariable String configKey) {
        SysConfig config = configService.getByKeyWithCache(configKey);
        if (config == null) {
            return R.fail("配置不存在");
        }
        return R.ok(config);
    }
    
    /**
     * 根据配置键名获取配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(
        summary = "根据配置键名获取配置值",
        description = "通过配置键名获取配置值字符串，使用缓存优化查询性能"
    )
    public R<String> getValueByKey(
            @Parameter(description = "配置键名", required = true, example = "api.encryption.enabled") 
            @PathVariable String configKey) {
        String value = configService.getConfigValueByKey(configKey);
        if (value == null) {
            return R.fail("配置不存在");
        }
        return R.ok(value);
    }
    
    /**
     * 根据配置分类获取配置列表
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "根据配置分类获取配置列表",
        description = "通过配置分类获取该分类下所有配置项，返回Map格式便于前端使用"
    )
    public R<Map<String, String>> getByCategory(
            @Parameter(description = "配置分类", required = true, example = "api.encryption") 
            @PathVariable String category) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysConfig::getConfigKey, category);
        wrapper.eq(SysConfig::getDeleted, "0");
        wrapper.orderByAsc(SysConfig::getConfigId);
        
        List<SysConfig> configs = configService.list(wrapper);
        Map<String, String> result = new HashMap<>();
        for (SysConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return R.ok(result);
    }
    
    /**
     * 根据配置ID获取详细信息
     */
    @GetMapping("/{configId}")
    @Operation(
        summary = "根据配置ID获取详细信息",
        description = "通过配置ID获取配置的详细信息"
    )
    public R<SysConfig> getInfo(
            @Parameter(description = "配置ID", required = true, example = "1") 
            @PathVariable Long configId) {
        SysConfig config = configService.getById(configId);
        if (config == null) {
            return R.fail("配置不存在");
        }
        return R.ok(config);
    }
    
    /**
     * 新增配置
     */
    @PostMapping
    @Operation(
        summary = "新增配置",
        description = "创建新的配置项，配置键名必须唯一"
    )
    public R<Void> add(@Valid @RequestBody SysConfig config) {
        // 检查配置键名唯一性
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
        wrapper.eq(SysConfig::getDeleted, "0");
        long count = configService.count(wrapper);
        if (count > 0) {
            return R.fail("配置键名已存在");
        }
        
        boolean result = configService.save(config);
        if (result) {
            // 清除缓存
            configService.evictCache();
            return R.ok("新增配置成功");
        }
        return R.fail("新增配置失败");
    }
    
    /**
     * 修改配置
     */
    @PutMapping
    @Operation(
        summary = "修改配置",
        description = "更新配置信息，配置键名必须唯一"
    )
    public R<Void> update(@Valid @RequestBody SysConfig config) {
        if (config.getConfigId() == null) {
            return R.fail("配置ID不能为空");
        }
        
        // 检查配置是否存在
        SysConfig existConfig = configService.getById(config.getConfigId());
        if (existConfig == null) {
            return R.fail("配置不存在");
        }
        
        // 检查配置键名唯一性（排除自己）
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
        wrapper.ne(SysConfig::getConfigId, config.getConfigId());
        wrapper.eq(SysConfig::getDeleted, "0");
        long count = configService.count(wrapper);
        if (count > 0) {
            return R.fail("配置键名已存在");
        }
        
        boolean result = configService.updateById(config);
        if (result) {
            // 清除缓存
            configService.evictCache();
            return R.ok("修改配置成功");
        }
        return R.fail("修改配置失败");
    }
    
    /**
     * 根据配置键名更新配置值
     */
    @PutMapping("/key/{configKey}")
    @Operation(
        summary = "根据配置键名更新配置值",
        description = "通过配置键名更新配置值"
    )
    public R<Void> updateByKey(
            @Parameter(description = "配置键名", required = true, example = "api.encryption.enabled") 
            @PathVariable String configKey,
            @Parameter(description = "配置值", required = true) 
            @RequestParam String configValue) {
        
        SysConfig config = configService.getByKeyWithCache(configKey);
        if (config == null) {
            return R.fail("配置不存在");
        }
        
        config.setConfigValue(configValue);
        boolean result = configService.updateById(config);
        if (result) {
            // 清除缓存
            configService.evictCache();
            return R.ok("更新配置成功");
        }
        return R.fail("更新配置失败");
    }
    
    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    @Operation(
        summary = "批量更新配置",
        description = "批量更新同一分类下的多个配置项，如果配置不存在则自动创建"
    )
    public R<Void> batchUpdate(@Valid @RequestBody ConfigBatchUpdateDto dto) {
        if (dto.getConfigs() == null || dto.getConfigs().isEmpty()) {
            return R.fail("配置项不能为空");
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Map.Entry<String, String> entry : dto.getConfigs().entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue();
            
            try {
                // 查询配置是否存在
                SysConfig config = configService.getByKeyWithCache(configKey);
                
                if (config != null) {
                    // 更新现有配置
                    config.setConfigValue(configValue);
                    boolean result = configService.updateById(config);
                    if (result) {
                        successCount++;
                    } else {
                        failCount++;
                        log.warn("更新配置失败: {}", configKey);
                    }
                } else {
                    // 创建新配置
                    SysConfig newConfig = new SysConfig();
                    newConfig.setConfigKey(configKey);
                    newConfig.setConfigValue(configValue);
                    newConfig.setConfigName(configKey);
                    newConfig.setConfigType("N"); // 用户自定义
                    newConfig.setRemark("批量创建");
                    
                    boolean result = configService.save(newConfig);
                    if (result) {
                        successCount++;
                    } else {
                        failCount++;
                        log.warn("创建配置失败: {}", configKey);
                    }
                }
            } catch (Exception e) {
                failCount++;
                log.error("处理配置失败: {}", configKey, e);
            }
        }
        
        // 清除缓存
        configService.evictCache();
        
        if (failCount == 0) {
            return R.ok(String.format("批量更新成功，共更新%d项配置", successCount));
        } else {
            return R.ok(String.format("批量更新完成，成功%d项，失败%d项", successCount, failCount));
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{configIds}")
    @Operation(
        summary = "删除配置",
        description = "删除一个或多个配置项，多个ID用逗号分隔"
    )
    public R<Void> delete(
            @Parameter(description = "配置ID，多个用逗号分隔", required = true, example = "1,2,3") 
            @PathVariable String configIds) {
        
        List<String> idList = Arrays.asList(configIds.split(","));
        boolean result = configService.removeByIds(idList);
        
        if (result) {
            // 清除缓存
            configService.evictCache();
            return R.ok("删除配置成功");
        }
        return R.fail("删除配置失败");
    }
    
    /**
     * 清除配置缓存
     */
    @PostMapping("/cache/evict")
    @Operation(
        summary = "清除配置缓存",
        description = "手动清除所有配置缓存，用于配置更新后强制刷新"
    )
    public R<Void> evictCache() {
        configService.evictCache();
        return R.ok("清除缓存成功");
    }
    
    /**
     * 预热配置缓存
     */
    @PostMapping("/cache/warmup")
    @Operation(
        summary = "预热配置缓存",
        description = "预热配置缓存，提前加载配置到缓存中"
    )
    public R<Void> warmupCache() {
        configService.warmupCache();
        return R.ok("缓存预热成功");
    }
    
    /**
     * 获取构建信息
     */
    @GetMapping("/build-info")
    @Operation(
        summary = "获取构建信息",
        description = "获取应用的构建信息，包括Git提交信息、构建时间等，用于版本追踪和问题诊断"
    )
    public R<BuildInfo> getBuildInfo() {
        BuildInfo buildInfo = buildInfoService.getBuildInfo();
        return R.ok(buildInfo);
    }
}
