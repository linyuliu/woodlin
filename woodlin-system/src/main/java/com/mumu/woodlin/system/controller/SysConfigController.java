package com.mumu.woodlin.system.controller;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.dto.ConfigUpdateDto;
import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置控制器
 * 
 * @author mumu
 * @description 系统配置管理控制器，提供配置的增删改查功能
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统配置管理", description = "系统配置管理相关接口，包括API加密配置、密码策略配置等")
public class SysConfigController {
    
    private final ISysConfigService configService;
    
    /**
     * 获取所有配置列表
     */
    @GetMapping("/list")
    @Operation(
        summary = "获取配置列表",
        description = "获取所有系统配置列表"
    )
    public R<List<SysConfig>> list() {
        List<SysConfig> configs = configService.list();
        return R.ok(configs);
    }
    
    /**
     * 根据配置键名获取配置
     */
    @GetMapping("/key/{configKey}")
    @Operation(
        summary = "根据配置键名获取配置",
        description = "通过配置键名获取配置的详细信息"
    )
    public R<SysConfig> getByKey(
            @Parameter(description = "配置键名", required = true, example = "sys.user.initPassword") 
            @PathVariable String configKey) {
        SysConfig config = configService.selectConfigByKey(configKey);
        return R.ok(config);
    }
    
    /**
     * 根据配置键名获取配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(
        summary = "根据配置键名获取配置值",
        description = "通过配置键名获取配置值"
    )
    public R<String> getValueByKey(
            @Parameter(description = "配置键名", required = true, example = "sys.user.initPassword") 
            @PathVariable String configKey) {
        String value = configService.selectConfigValueByKey(configKey);
        return R.ok(value);
    }
    
    /**
     * 根据配置分类获取配置列表
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "根据配置分类获取配置列表",
        description = "通过配置分类获取该分类下的所有配置，返回配置键值对"
    )
    public R<Map<String, String>> getByCategory(
            @Parameter(description = "配置分类", required = true, example = "api.encryption") 
            @PathVariable String category) {
        List<SysConfig> configs = configService.selectConfigListByCategory(category);
        Map<String, String> configMap = configs.stream()
                .collect(Collectors.toMap(SysConfig::getConfigKey, SysConfig::getConfigValue));
        return R.ok(configMap);
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
        return R.ok(config);
    }
    
    /**
     * 新增配置
     */
    @PostMapping
    @Operation(
        summary = "新增配置",
        description = "创建新的系统配置项"
    )
    public R<Void> add(@Valid @RequestBody SysConfig config) {
        boolean result = configService.insertConfig(config);
        return result ? R.ok("新增配置成功") : R.fail("新增配置失败");
    }
    
    /**
     * 修改配置
     */
    @PutMapping
    @Operation(
        summary = "修改配置",
        description = "更新系统配置信息"
    )
    public R<Void> edit(@Valid @RequestBody SysConfig config) {
        boolean result = configService.updateConfig(config);
        return result ? R.ok("修改配置成功") : R.fail("修改配置失败");
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
            @Parameter(description = "配置键名", required = true) 
            @PathVariable String configKey,
            @Parameter(description = "配置值", required = true) 
            @RequestParam String configValue) {
        boolean result = configService.updateConfigByKey(configKey, configValue);
        return result ? R.ok("更新配置成功") : R.fail("更新配置失败");
    }
    
    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    @Operation(
        summary = "批量更新配置",
        description = "批量更新系统配置，通常用于更新某个分类下的所有配置"
    )
    public R<Void> batchUpdate(@Valid @RequestBody ConfigUpdateDto updateDto) {
        // 先获取该分类下的所有配置
        List<SysConfig> configs = configService.selectConfigListByCategory(updateDto.getCategory());
        
        // 更新配置值
        Map<String, String> updateMap = updateDto.getConfigs();
        configs.forEach(config -> {
            String key = config.getConfigKey();
            if (updateMap.containsKey(key)) {
                config.setConfigValue(updateMap.get(key));
            }
        });
        
        boolean result = configService.batchUpdateConfig(configs);
        return result ? R.ok("批量更新配置成功") : R.fail("批量更新配置失败");
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{configIds}")
    @Operation(
        summary = "删除配置",
        description = "根据配置ID删除配置，支持批量删除，多个ID用逗号分隔"
    )
    public R<Void> remove(
            @Parameter(description = "配置ID，多个用逗号分隔", required = true, example = "1,2,3") 
            @PathVariable String configIds) {
        List<Long> ids = Arrays.stream(configIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        boolean result = configService.deleteConfigByIds(ids);
        return result ? R.ok("删除配置成功") : R.fail("删除配置失败");
    }
}
