package com.mumu.woodlin.sql2api.controller;

import java.util.List;

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
import com.mumu.woodlin.sql2api.entity.SqlDatasourceConfig;
import com.mumu.woodlin.sql2api.mapper.Sql2ApiDatasourceMapper;
import com.mumu.woodlin.sql2api.model.request.AddDatasourceRequest;
import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import com.mumu.woodlin.sql2api.service.Sql2ApiDataSourceService;
import com.mumu.woodlin.sql2api.service.DatabaseMetadataService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * 平台侧数据源 CRUD + 校验（基础设施）
 */
@RestController
@RequestMapping("/admin/infra/datasource")
@RequiredArgsConstructor
public class InfraDatasourceAdminController {

    private final Sql2ApiDatasourceMapper datasourceMapper;
    private final Sql2ApiDataSourceService dataSourceService;
    private final DatabaseMetadataService metadataService;

    @GetMapping
    public R<List<SqlDatasourceConfig>> list() {
        return R.ok(datasourceMapper.selectList(new QueryWrapper<>()));
    }

    @GetMapping("/{code}")
    public R<SqlDatasourceConfig> detail(@PathVariable("code") String code) {
        SqlDatasourceConfig config = datasourceMapper.selectOne(new QueryWrapper<SqlDatasourceConfig>()
                .eq("datasource_code", code));
        return config == null ? R.fail("数据源不存在") : R.ok(config);
    }

    @PostMapping
    public R<Void> create(@Valid @RequestBody AddDatasourceRequest request) {
        saveOrUpdate(request, false);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@Valid @RequestBody AddDatasourceRequest request) {
        saveOrUpdate(request, true);
        return R.ok();
    }

    @DeleteMapping("/{code}")
    public R<Void> delete(@PathVariable("code") String code) {
        datasourceMapper.delete(new QueryWrapper<SqlDatasourceConfig>().eq("datasource_code", code));
        return R.ok();
    }

    @PostMapping("/test")
    public R<Void> test(@Valid @RequestBody AddDatasourceRequest request) {
        dataSourceService.validateConnectivity(
                dataSourceService.buildTemporaryDataSource(request),
                request.getTestSql(),
                request.getDatasourceType()
        );
        return R.ok();
    }

    @GetMapping("/{code}/metadata")
    public R<DatabaseMetadata> metadata(@PathVariable("code") String code) {
        return R.ok(metadataService.getDatabaseMetadata(code));
    }

    @GetMapping("/{code}/tables")
    public R<List<TableMetadata>> tables(@PathVariable("code") String code) {
        return R.ok(metadataService.getTables(code));
    }

    @GetMapping("/{code}/tables/{table}/columns")
    public R<List<ColumnMetadata>> columns(@PathVariable("code") String code, @PathVariable("table") String table) {
        return R.ok(metadataService.getColumns(code, table));
    }

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
