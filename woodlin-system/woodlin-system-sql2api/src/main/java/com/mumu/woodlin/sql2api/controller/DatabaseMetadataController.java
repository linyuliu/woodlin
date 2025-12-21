package com.mumu.woodlin.sql2api.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseDocFormat;
import com.mumu.woodlin.sql2api.model.DatabaseStructureResponse;
import com.mumu.woodlin.sql2api.model.request.DatabaseDocExportRequest;
import com.mumu.woodlin.sql2api.service.DatabaseDocumentExportService;
import com.mumu.woodlin.sql2api.service.DatabaseMetadataService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * SQL2API 元数据接口
 */
@RestController
@RequestMapping("/sql2api/metadata")
@RequiredArgsConstructor
@Tag(name = "SQL2API元数据", description = "提供数据源动态管理、元数据获取及文档导出功能")
public class DatabaseMetadataController {

    private final DatabaseMetadataService databaseMetadataService;
    private final DatabaseDocumentExportService documentExportService;

    @GetMapping("/tables")
    @Operation(summary = "获取表结构", description = "返回指定数据源下的所有表及列信息。支持通过schema参数过滤特定模式的表")
    public R<List<TableMetadata>> getTables(
            @Parameter(description = "数据源名称", required = true)
            @RequestParam String datasourceName,
            @Parameter(description = "Schema名称（可选，用于过滤特定模式的表）", required = false)
            @RequestParam(required = false) String schema) {
        List<TableMetadata> tables = databaseMetadataService.getTables(datasourceName, schema);
        fillColumnsIfNeeded(datasourceName, tables);
        return R.ok(tables);
    }

    @GetMapping("/columns")
    @Operation(summary = "获取表字段", description = "返回指定表的字段结构信息，类似于 MySQL 的 DESCRIBE。支持通过schema参数指定表所在的模式")
    public R<List<ColumnMetadata>> getColumns(
            @Parameter(description = "数据源名称", required = true)
            @RequestParam String datasourceName,
            @Parameter(description = "表名", required = true)
            @RequestParam String tableName,
            @Parameter(description = "Schema名称（可选，指定表所在的模式）", required = false)
            @RequestParam(required = false) String schema) {
        return R.ok(databaseMetadataService.getColumns(datasourceName, tableName, schema));
    }

    @GetMapping("/structure")
    @Operation(summary = "获取数据库结构元数据", description = "返回数据库基础信息和表字段结构。支持通过schema参数过滤特定模式的表")
    public R<DatabaseStructureResponse> getStructure(
            @Parameter(description = "数据源名称", required = true)
            @RequestParam String datasourceName,
            @Parameter(description = "Schema名称（可选，用于过滤特定模式的表）", required = false)
            @RequestParam(required = false) String schema) {
        DatabaseMetadata metadata = databaseMetadataService.getDatabaseMetadata(datasourceName);
        List<TableMetadata> tables = databaseMetadataService.getTables(datasourceName, schema);
        fillColumnsIfNeeded(datasourceName, tables);
        return R.ok(new DatabaseStructureResponse(metadata, tables));
    }

    @PostMapping("/export")
    @Operation(summary = "导出数据库文档", description = "支持导出为 Excel、Word、PDF 格式的数据库结构文档")
    public void export(@Valid @RequestBody DatabaseDocExportRequest request, HttpServletResponse response) throws IOException {
        DatabaseMetadata metadata = databaseMetadataService.getDatabaseMetadata(request.getDatasourceName());
        List<TableMetadata> tables = databaseMetadataService.getTables(request.getDatasourceName());
        fillColumnsIfNeeded(request.getDatasourceName(), tables);

        if (CollUtil.isNotEmpty(request.getTables())) {
            tables = tables.stream()
                    .filter(t -> request.getTables().contains(t.getTableName()))
                    .collect(Collectors.toList());
        }

        byte[] content = documentExportService.export(metadata, tables, request.getFormat());
        writeResponse(response, content, request.getFormat(), metadata.getDatabaseName());
    }

    private void fillColumnsIfNeeded(String datasourceName, List<TableMetadata> tables) {
        if (CollUtil.isEmpty(tables)) {
            return;
        }
        for (TableMetadata table : tables) {
            if (CollUtil.isEmpty(table.getColumns())) {
                List<ColumnMetadata> columns = databaseMetadataService.getColumns(datasourceName, table.getTableName());
                table.setColumns(columns);
            }
        }
    }

    private void writeResponse(HttpServletResponse response, byte[] content, DatabaseDocFormat format, String databaseName) throws IOException {
        response.setContentType(format.getContentType());
        String safeBaseName = sanitizeFileName(databaseName);
        String fileName = safeBaseName + "-metadata." + format.getFileExtension();
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        response.getOutputStream().write(content);
        response.flushBuffer();
    }

    private String sanitizeFileName(String name) {
        String base = CharSequenceUtil.emptyToDefault(name, "database");
        return base.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
