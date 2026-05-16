package com.mumu.woodlin.sql2api.controller;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.sql2api.entity.SqlApiConfig;
import com.mumu.woodlin.sql2api.mapper.SqlApiConfigMapper;
import com.mumu.woodlin.sql2api.service.DynamicSqlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.util.StringUtils;

/**
 * SQL2API 配置控制器
 *
 * @author yulin
 * @since 2026-05
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/sql2api")
@Tag(name = "SQL2API配置", description = "SQL2API 配置管理与在线测试接口")
public class SqlApiConfigController {

    private final SqlApiConfigMapper sqlApiConfigMapper;
    private final DynamicSqlService dynamicSqlService;

    @GetMapping
    @Operation(summary = "分页查询 SQL2API 配置")
    public R<PageResult<SqlApiConfig>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) String datasourceName,
            @RequestParam(required = false) Integer status) {
        requirePermission("sql2api:design:list");
        LambdaQueryWrapper<SqlApiConfig> wrapper = new LambdaQueryWrapper<SqlApiConfig>()
                .like(StringUtils.hasText(apiName), SqlApiConfig::getApiName, apiName)
                .eq(StringUtils.hasText(datasourceName), SqlApiConfig::getDatasourceName, datasourceName)
                .eq(status != null, SqlApiConfig::getStatus, status)
                .orderByDesc(SqlApiConfig::getCreateTime);
        Page<SqlApiConfig> page = sqlApiConfigMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.success(pageNum, pageSize, page.getTotal(), page.getRecords()));
    }

    @GetMapping("/{apiId}")
    @Operation(summary = "查询 SQL2API 配置详情")
    public R<SqlApiConfig> detail(
            @Parameter(description = "接口ID", required = true) @PathVariable Long apiId) {
        requirePermission("sql2api:design:list");
        return R.ok(requireConfig(apiId));
    }

    @PostMapping
    @Operation(summary = "新增 SQL2API 配置")
    public R<Void> create(@Valid @RequestBody SqlApiConfig config) {
        requirePermission("sql2api:design:add");
        ensureSuccess(sqlApiConfigMapper.insert(config) > 0, "新增 SQL2API 配置失败");
        return R.ok();
    }

    @PutMapping("/{apiId}")
    @Operation(summary = "修改 SQL2API 配置")
    public R<Void> update(
            @Parameter(description = "接口ID", required = true) @PathVariable Long apiId,
            @Valid @RequestBody SqlApiConfig config) {
        requirePermission("sql2api:design:add");
        requireConfig(apiId);
        config.setApiId(apiId);
        ensureSuccess(sqlApiConfigMapper.updateById(config) > 0, "修改 SQL2API 配置失败");
        return R.ok();
    }

    @DeleteMapping("/{apiId}")
    @Operation(summary = "删除 SQL2API 配置")
    public R<Void> delete(
            @Parameter(description = "接口ID", required = true) @PathVariable Long apiId) {
        requirePermission("sql2api:design:remove");
        ensureSuccess(sqlApiConfigMapper.deleteById(apiId) > 0, "删除 SQL2API 配置失败");
        return R.ok();
    }

    @PostMapping("/{apiId}/test")
    @Operation(summary = "在线测试 SQL2API 配置")
    public R<Object> test(
            @Parameter(description = "接口ID", required = true) @PathVariable Long apiId,
            @RequestBody(required = false) Map<String, Object> params) {
        requirePermission("sql2api:design:list");
        SqlApiConfig config = requireConfig(apiId);
        Map<String, Object> runtimeParams = params == null ? Map.of() : params;

        if ("SELECT".equalsIgnoreCase(config.getSqlType())) {
            if ("single".equalsIgnoreCase(config.getResultType())) {
                return R.ok(dynamicSqlService.executeSingleQuery(config, runtimeParams));
            }
            if ("page".equalsIgnoreCase(config.getResultType())) {
                int pageNum = parseInt(runtimeParams.get("pageNum"), 1);
                int pageSize = parseInt(runtimeParams.get("pageSize"), 10);
                return R.ok(dynamicSqlService.executePageQuery(config, runtimeParams, pageNum, pageSize));
            }
            List<Map<String, Object>> result = dynamicSqlService.executeListQuery(config, runtimeParams);
            return R.ok(result);
        }

        int affectedRows = dynamicSqlService.executeUpdate(config, runtimeParams);
        return R.ok(Map.of("affectedRows", affectedRows));
    }

    private SqlApiConfig requireConfig(Long apiId) {
        SqlApiConfig config = sqlApiConfigMapper.selectById(apiId);
        if (config == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "SQL2API 配置不存在");
        }
        return config;
    }

    private void requirePermission(String permission) {
        if (!SecurityUtil.hasPermission(permission)) {
            throw BusinessException.of(ResultCode.PERMISSION_DENIED, "权限不足: " + permission);
        }
    }

    private void ensureSuccess(boolean result, String failureMessage) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, failureMessage);
        }
    }

    private int parseInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
