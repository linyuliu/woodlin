package com.mumu.woodlin.sql2api.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.sql2api.entity.SqlDatasourceConfig;
import com.mumu.woodlin.sql2api.mapper.Sql2ApiDatasourceMapper;
import com.mumu.woodlin.sql2api.model.request.AddDatasourceRequest;
import com.mumu.woodlin.sql2api.service.Sql2ApiDataSourceService;
import com.mumu.woodlin.sql2api.service.DatabaseMetadataService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * 基础设施数据源管理控制器
 * <p>
 * 提供数据源的完整生命周期管理，包括增删改查、连接测试和元数据提取等功能。
 * 该控制器是平台级基础设施能力，为sql2api、ETL、报表等业务模块提供统一的数据源管理入口。
 * </p>
 * 
 * @author mumu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/infra/datasource")
@RequiredArgsConstructor
@Tag(name = "数据源管理", description = "基础设施数据源的增删改查、测试连接和元数据提取接口")
public class InfraDatasourceAdminController {

    private final Sql2ApiDatasourceMapper datasourceMapper;
    private final Sql2ApiDataSourceService dataSourceService;
    private final DatabaseMetadataService metadataService;

    /**
     * 查询所有数据源列表
     * 
     * @return 数据源配置列表
     */
    @GetMapping
    @Operation(summary = "查询数据源列表", description = "获取系统中所有已配置的数据源信息")
    public R<List<SqlDatasourceConfig>> list() {
        return R.ok(datasourceMapper.selectList(new QueryWrapper<>()));
    }

    /**
     * 根据数据源编码查询详情
     * 
     * @param code 数据源唯一编码
     * @return 数据源配置详情
     */
    @GetMapping("/{code}")
    @Operation(summary = "查询数据源详情", description = "根据数据源编码获取详细配置信息")
    public R<SqlDatasourceConfig> detail(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code) {
        SqlDatasourceConfig config = datasourceMapper.selectOne(new QueryWrapper<SqlDatasourceConfig>()
                .eq("datasource_code", code));
        return config == null ? R.fail("数据源不存在") : R.ok(config);
    }

    /**
     * 创建新数据源
     * 
     * @param request 数据源配置信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "创建数据源", description = "添加新的数据源配置，系统会自动验证数据源编码的唯一性")
    public R<Void> create(
            @Parameter(description = "数据源配置信息", required = true)
            @Valid @RequestBody AddDatasourceRequest request) {
        saveOrUpdate(request, false);
        return R.ok();
    }

    /**
     * 更新数据源配置
     * 
     * @param request 数据源配置信息
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "更新数据源", description = "修改已有数据源的配置信息")
    public R<Void> update(
            @Parameter(description = "数据源配置信息", required = true)
            @Valid @RequestBody AddDatasourceRequest request) {
        saveOrUpdate(request, true);
        return R.ok();
    }

    /**
     * 删除数据源
     * 
     * @param code 数据源唯一编码
     * @return 操作结果
     */
    @DeleteMapping("/{code}")
    @Operation(summary = "删除数据源", description = "根据数据源编码删除指定的数据源配置")
    public R<Void> delete(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code) {
        datasourceMapper.delete(new QueryWrapper<SqlDatasourceConfig>().eq("datasource_code", code));
        return R.ok();
    }

    /**
     * 测试数据源连接
     * 
     * @param request 数据源配置信息
     * @return 测试结果
     */
    @PostMapping("/test")
    @Operation(summary = "测试数据源连接", description = "验证数据源配置是否正确，能否成功建立数据库连接")
    public R<Void> test(
            @Parameter(description = "数据源配置信息", required = true)
            @Valid @RequestBody AddDatasourceRequest request) {
        dataSourceService.validateConnectivity(
                dataSourceService.buildTemporaryDataSource(request),
                request.getTestSql(),
                request.getDatasourceType()
        );
        return R.ok();
    }

    /**
     * 获取数据源的完整元数据
     * <p>
     * 包括数据库基本信息、版本信息、字符集等完整元数据
     * </p>
     * 
     * @param code 数据源唯一编码
     * @return 数据库元数据
     */
    @GetMapping("/{code}/metadata")
    @Operation(summary = "获取数据库元数据", description = "获取数据源的完整元数据信息，包括数据库版本、字符集、支持的Schema等")
    public R<DatabaseMetadata> metadata(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code) {
        return R.ok(metadataService.getDatabaseMetadata(code));
    }

    /**
     * 获取数据源的Schema列表
     * <p>
     * 注意：某些数据库（如MySQL）不支持Schema概念，会返回空列表或默认值
     * </p>
     * 
     * @param code 数据源唯一编码
     * @return Schema列表
     */
    @GetMapping("/{code}/schemas")
    @Operation(summary = "获取Schema列表", description = "获取数据源中所有的Schema（模式）列表。注意：MySQL等数据库不支持Schema概念")
    public R<List<SchemaMetadata>> schemas(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code) {
        return R.ok(metadataService.getSchemas(code));
    }

    /**
     * 获取数据源的表列表
     * 
     * @param code 数据源唯一编码
     * @return 表元数据列表
     */
    @GetMapping("/{code}/tables")
    @Operation(summary = "获取表列表", description = "获取数据源中所有的表信息，包括表名、表类型、注释等")
    public R<List<TableMetadata>> tables(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code) {
        return R.ok(metadataService.getTables(code));
    }

    /**
     * 获取指定表的字段列表
     * 
     * @param code 数据源唯一编码
     * @param table 表名
     * @return 字段元数据列表
     */
    @GetMapping("/{code}/tables/{table}/columns")
    @Operation(summary = "获取表字段列表", description = "获取指定表的所有字段信息，包括字段名、类型、长度、约束等详细信息")
    public R<List<ColumnMetadata>> columns(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code,
            @Parameter(description = "表名", required = true)
            @PathVariable("table") String table) {
        return R.ok(metadataService.getColumns(code, table));
    }

    /**
     * 保存或更新数据源配置
     * 
     * @param request 数据源配置请求
     * @param isUpdate 是否为更新操作
     */
    private void saveOrUpdate(AddDatasourceRequest request, boolean isUpdate) {
        QueryWrapper<SqlDatasourceConfig> wrapper = new QueryWrapper<SqlDatasourceConfig>()
                .eq("datasource_code", request.getDatasourceCode());
        SqlDatasourceConfig exist = datasourceMapper.selectOne(wrapper);
        if (!isUpdate && exist != null) {
            throw new BusinessException("数据源编码已存在");
        }
        if (isUpdate && exist == null) {
            throw new BusinessException("数据源不存在，无法更新");
        }
        SqlDatasourceConfig record = exist == null ? new SqlDatasourceConfig() : exist;
        record.setDatasourceCode(request.getDatasourceCode());
        record.setDatasourceName(request.getDatasourceName());
        record.setDatasourceType(request.getDatasourceType());
        record.setDriverClass(request.getDriverClass());
        record.setJdbcUrl(request.getJdbcUrl());
        record.setUsername(request.getUsername());
        record.setPassword(request.getPassword());
        record.setTestSql(StrUtil.emptyToDefault(request.getTestSql(), dataSourceService.defaultTestQuery(request.getDatasourceType())));
        record.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        record.setOwner(request.getOwner());
        record.setBizTags(request.getBizTags());
        record.setRemark(request.getRemark());
        record.setExtConfig(request.getExtConfig());

        if (!isUpdate) {
            datasourceMapper.insert(record);
        } else {
            datasourceMapper.updateById(record);
        }
    }
}
