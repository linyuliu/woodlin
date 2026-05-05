package com.mumu.woodlin.file.controller;

import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.enums.StorageType;
import com.mumu.woodlin.file.mapper.SysStorageConfigMapper;
import com.mumu.woodlin.file.storage.StorageServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 存储配置管理控制器。
 *
 * @author yulin
 * @since 2026-05-05
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file/storage")
@Tag(name = "文件存储配置", description = "文件存储配置管理接口")
public class FileStorageController {

    private final SysStorageConfigMapper storageConfigMapper;
    private final StorageServiceFactory storageServiceFactory;

    /**
     * 分页查询存储配置。
     */
    @GetMapping
    @Operation(summary = "分页查询存储配置")
    public R<Page<SysStorageConfig>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Long size,
            @Parameter(description = "存储名称") @RequestParam(required = false) String storageName,
            @Parameter(description = "存储类型") @RequestParam(required = false) String storageType,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        LambdaQueryWrapper<SysStorageConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(storageName), SysStorageConfig::getConfigName, storageName);
        wrapper.eq(StrUtil.isNotBlank(storageType), SysStorageConfig::getStorageType, normalizeStorageType(storageType));
        wrapper.eq(StrUtil.isNotBlank(status), SysStorageConfig::getStatus, status);
        wrapper.orderByDesc(SysStorageConfig::getIsDefault).orderByDesc(SysStorageConfig::getCreateTime);
        return R.ok(storageConfigMapper.selectPage(new Page<>(page, size), wrapper));
    }

    /**
     * 新增存储配置。
     */
    @PostMapping
    @Operation(summary = "新增存储配置")
    public R<Void> add(@RequestBody SysStorageConfig config) {
        normalizeConfig(config, null);
        ensureSuccess(storageConfigMapper.insert(config) > 0, "新增存储配置失败");
        return R.ok("新增成功");
    }

    /**
     * 更新存储配置。
     */
    @PutMapping("/{configId}")
    @Operation(summary = "更新存储配置")
    public R<Void> edit(
            @Parameter(description = "配置ID", required = true) @PathVariable Long configId,
            @RequestBody SysStorageConfig config) {
        SysStorageConfig current = requireConfig(configId);
        normalizeConfig(config, current);
        config.setConfigId(configId);
        ensureSuccess(storageConfigMapper.updateById(config) > 0, "更新存储配置失败");
        return R.ok("更新成功");
    }

    /**
     * 删除存储配置。
     */
    @DeleteMapping("/{configId}")
    @Operation(summary = "删除存储配置")
    public R<Void> remove(@Parameter(description = "配置ID", required = true) @PathVariable Long configId) {
        requireConfig(configId);
        ensureSuccess(storageConfigMapper.deleteById(configId) > 0, "删除存储配置失败");
        return R.ok("删除成功");
    }

    /**
     * 设为默认存储配置。
     */
    @PutMapping("/{configId}/default")
    @Operation(summary = "设为默认存储配置")
    public R<Void> setDefault(@Parameter(description = "配置ID", required = true) @PathVariable Long configId) {
        requireConfig(configId);
        SysStorageConfig reset = new SysStorageConfig();
        reset.setIsDefault("0");
        storageConfigMapper.update(reset, new LambdaQueryWrapper<SysStorageConfig>().eq(SysStorageConfig::getIsDefault, "1"));

        SysStorageConfig current = new SysStorageConfig();
        current.setConfigId(configId);
        current.setIsDefault("1");
        ensureSuccess(storageConfigMapper.updateById(current) > 0, "设置默认存储失败");
        return R.ok("设置成功");
    }

    /**
     * 测试存储配置。
     */
    @PostMapping("/{configId}/test")
    @Operation(summary = "测试存储配置")
    public R<ConnectionTestResult> test(@Parameter(description = "配置ID", required = true) @PathVariable Long configId) {
        SysStorageConfig config = requireConfig(configId);
        try {
            storageServiceFactory.getStorageService(config.getStorageType())
                .fileExists(config, "woodlin-connect-test/" + UUID.randomUUID());
            return R.ok(new ConnectionTestResult(true, "连接成功"));
        } catch (Exception e) {
            return R.ok(new ConnectionTestResult(false, "连接失败: " + e.getMessage()));
        }
    }

    private SysStorageConfig requireConfig(Long configId) {
        SysStorageConfig config = storageConfigMapper.selectById(configId);
        if (config == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "存储配置不存在");
        }
        return config;
    }

    private void normalizeConfig(SysStorageConfig target, SysStorageConfig current) {
        target.setStorageType(normalizeStorageType(target.getStorageType()));
        if (StrUtil.isBlank(target.getSecretKey()) && current != null) {
            target.setSecretKey(current.getSecretKey());
        }
        if (target.getIsDefault() == null) {
            target.setIsDefault(current == null ? "0" : current.getIsDefault());
        }
        if (target.getStatus() == null) {
            target.setStatus(current == null ? "1" : current.getStatus());
        }
    }

    private String normalizeStorageType(String storageType) {
        return StorageType.fromCode(storageType).getCode();
    }

    private void ensureSuccess(boolean result, String message) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, message);
        }
    }

    public record ConnectionTestResult(boolean success, String message) {
    }
}
