package com.mumu.woodlin.etl.controller;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.Result;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.etl.constant.EtlPermissionConstants;
import com.mumu.woodlin.etl.model.response.EtlOfflineJobDetailResponse;
import com.mumu.woodlin.etl.model.response.EtlOfflineJobSummaryResponse;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobCreateRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobPageRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineValidationRequest;
import com.mumu.woodlin.etl.model.response.EtlOfflineCreateJobResponse;
import com.mumu.woodlin.etl.model.response.EtlOfflineDatasourceOption;
import com.mumu.woodlin.etl.model.response.EtlOfflineValidationResult;
import com.mumu.woodlin.etl.model.response.EtlOfflineWizardConfigResponse;
import com.mumu.woodlin.etl.service.IEtlOfflineService;
import com.mumu.woodlin.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ETL离线任务向导控制器。
 *
 * @author mumu
 * @since 1.0.0
 */
@Tag(name = "ETL离线任务", description = "离线同步任务创建向导相关接口")
@RestController
@RequestMapping("/etl/offline")
@RequiredArgsConstructor
@Validated
public class EtlOfflineController {

    private final IEtlOfflineService etlOfflineService;

    @Operation(summary = "获取离线向导配置")
    @GetMapping("/config")
    public Result<EtlOfflineWizardConfigResponse> getWizardConfig() {
        requirePermission(EtlPermissionConstants.OFFLINE_LIST);
        return Result.success(etlOfflineService.getWizardConfig());
    }

    @Operation(summary = "查询离线任务可用数据源")
    @GetMapping("/datasources")
    public Result<List<EtlOfflineDatasourceOption>> listDatasourceOptions(
        @RequestParam(name = "datasourceType", required = false) String datasourceType,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "enabledOnly", defaultValue = "true") boolean enabledOnly) {
        requirePermission(EtlPermissionConstants.OFFLINE_LIST);
        return Result.success(etlOfflineService.listDatasourceOptions(datasourceType, keyword, enabledOnly));
    }

    @Operation(summary = "查询离线任务分页")
    @GetMapping("/jobs/page")
    public PageResult<EtlOfflineJobSummaryResponse> pageJobs(@Valid EtlOfflineJobPageRequest request) {
        requirePermission(EtlPermissionConstants.OFFLINE_LIST);
        return etlOfflineService.pageJobs(request);
    }

    @Operation(summary = "查询离线任务详情")
    @GetMapping("/jobs/{jobId}")
    public Result<EtlOfflineJobDetailResponse> getJobDetail(@PathVariable("jobId") Long jobId) {
        requirePermission(EtlPermissionConstants.OFFLINE_DETAIL);
        return Result.success(etlOfflineService.getJobDetail(jobId));
    }

    @Operation(summary = "离线配置预校验")
    @PostMapping("/validate")
    public Result<EtlOfflineValidationResult> validate(
        @Valid @RequestBody EtlOfflineValidationRequest request) {
        requirePermission(EtlPermissionConstants.OFFLINE_VALIDATE);
        return Result.success(etlOfflineService.validate(request));
    }

    @Operation(summary = "创建离线任务")
    @PostMapping("/jobs")
    public Result<EtlOfflineCreateJobResponse> createOfflineJob(
        @Valid @RequestBody EtlOfflineJobCreateRequest request) {
        requirePermission(EtlPermissionConstants.OFFLINE_CREATE);
        return Result.success(etlOfflineService.createOfflineJob(request));
    }

    @Operation(summary = "更新离线任务")
    @PutMapping("/jobs/{jobId}")
    public Result<EtlOfflineCreateJobResponse> updateOfflineJob(
        @PathVariable("jobId") Long jobId,
        @Valid @RequestBody EtlOfflineJobCreateRequest request) {
        requirePermission(EtlPermissionConstants.OFFLINE_EDIT);
        return Result.success(etlOfflineService.updateOfflineJob(jobId, request));
    }

    @Operation(summary = "删除离线任务")
    @DeleteMapping("/jobs/{jobId}")
    public Result<Void> deleteOfflineJob(@PathVariable("jobId") Long jobId) {
        requirePermission(EtlPermissionConstants.OFFLINE_DELETE);
        etlOfflineService.deleteOfflineJob(jobId);
        return Result.success();
    }

    @Operation(summary = "查询表列表（支持关键字过滤）")
    @GetMapping("/tables")
    public Result<List<TableMetadata>> listTables(
        @RequestParam("datasourceCode") String datasourceCode,
        @RequestParam(name = "schemaName", required = false) String schemaName,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "limit", required = false) Integer limit) {
        requirePermission(EtlPermissionConstants.OFFLINE_TABLE_LIST);
        return Result.success(etlOfflineService.listTables(datasourceCode, schemaName, keyword, limit));
    }

    @Operation(summary = "查询字段列表（支持关键字过滤）")
    @GetMapping("/columns")
    public Result<List<ColumnMetadata>> listColumns(
        @RequestParam("datasourceCode") String datasourceCode,
        @RequestParam(name = "schemaName", required = false) String schemaName,
        @RequestParam("tableName") String tableName,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "limit", required = false) Integer limit) {
        requirePermission(EtlPermissionConstants.OFFLINE_COLUMN_LIST);
        return Result.success(etlOfflineService.listColumns(datasourceCode, schemaName, tableName, keyword, limit));
    }

    /**
     * 校验当前用户是否具备指定权限。
     *
     * @param permission 权限码
     */
    private void requirePermission(String permission) {
        if (!SecurityUtil.hasPermission(permission)) {
            throw new BusinessException("权限不足: " + permission);
        }
    }
}
