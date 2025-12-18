package com.mumu.woodlin.sql2api.service;

import javax.sql.DataSource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.sql2api.entity.SqlDatasourceConfig;
import com.mumu.woodlin.sql2api.mapper.Sql2ApiDatasourceMapper;
import com.mumu.woodlin.sql2api.model.request.AddDatasourceRequest;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PreDestroy;

/**
 * SQL2API 动态数据源管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Sql2ApiDataSourceService {

    private final DataSource dataSource;
    private final Sql2ApiDatasourceMapper datasourceMapper;
    /**
     * 独立的数据源注册表（不影响系统动态数据源）
     */
    private final Map<String, HikariDataSource> sql2ApiDataSources = new ConcurrentHashMap<>();

    /**
     * 动态新增数据源
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDataSource(AddDatasourceRequest request) {
        if (!(dataSource instanceof DynamicRoutingDataSource dynamicRoutingDataSource)) {
            throw new BusinessException("未启用动态数据源，无法新增数据源");
        }

        Assert.hasText(request.getCode(), "数据源编码不能为空");
        String key = request.getCode();

        String driverClassName = resolveDriver(request.getDriverClassName(), request.getUrl());

        HikariDataSource target = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(driverClassName)
                .url(request.getUrl())
                .username(request.getUsername())
                .password(request.getPassword())
                .build();

        validateConnectivity(target, request);

        synchronized (dynamicRoutingDataSource) {
            if (dynamicRoutingDataSource.getDataSources().containsKey(request.getName())) {
                throw new BusinessException("数据源已存在: " + request.getName());
            }
            if (sql2ApiDataSources.containsKey(key) || sql2ApiDataSources.containsKey(request.getName())) {
                throw new BusinessException("SQL2API 独立数据源已存在: " + key);
            }
            dynamicRoutingDataSource.addDataSource(request.getName(), target);
        }

        sql2ApiDataSources.put(key, target);
        if (!StrUtil.equalsIgnoreCase(key, request.getName())) {
            sql2ApiDataSources.put(request.getName(), target);
        }

        persistDatasource(request, driverClassName);
    }

    /**
     * 获取独立 SQL2API 数据源
     */
    public DataSource getSql2ApiDataSource(String name) {
        return sql2ApiDataSources.get(name);
    }

    @PreDestroy
    public void closeAll() {
        sql2ApiDataSources.values().forEach(HikariDataSource::close);
    }

    private void persistDatasource(AddDatasourceRequest request, String driverClassName) {
        SqlDatasourceConfig record = new SqlDatasourceConfig();
        record.setDatasourceName(request.getName());
        record.setCode(request.getCode());
        record.setDatabaseType(request.getDatabaseType());
        record.setDriverClass(driverClassName);
        record.setJdbcUrl(request.getUrl());
        record.setUsername(request.getUsername());
        record.setPassword(request.getPassword());
        record.setTestQuery(StrUtil.emptyToDefault(request.getTestQuery(), defaultTestQuery(request.getDatabaseType())));
        record.setEnabled(Boolean.TRUE);
        record.setDatasourceDesc(request.getDescription());

        Long existed = datasourceMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SqlDatasourceConfig>()
                        .eq("code", request.getCode())
                        .or()
                        .eq("datasource_name", request.getName())
        );
        if (existed != null && existed > 0) {
            throw new BusinessException("数据源编码或名称已存在");
        }
        datasourceMapper.insert(record);
    }

    private void validateConnectivity(DataSource target, AddDatasourceRequest request) {
        String testSql = StrUtil.emptyToDefault(request.getTestQuery(), defaultTestQuery(request.getDatabaseType()));
        try (var conn = target.getConnection();
             var ps = conn.prepareStatement(testSql)) {
            ps.execute();
        } catch (Exception e) {
            throw new BusinessException("数据源连接验证失败: " + e.getMessage(), e);
        }
    }

    private String defaultTestQuery(String databaseType) {
        if (StrUtil.isBlank(databaseType)) {
            return "SELECT 1";
        }
        String type = databaseType.toLowerCase();
        if (type.contains("oracle")) {
            return "SELECT 1 FROM dual";
        }
        if (type.contains("postgres")) {
            return "SELECT 1";
        }
        if (type.contains("sqlserver") || type.contains("mssql")) {
            return "SELECT 1";
        }
        return "SELECT 1";
    }

    private String resolveDriver(String driverClassName, String url) {
        if (StrUtil.isNotBlank(driverClassName)) {
            return driverClassName;
        }
        if (StrUtil.startWithIgnoreCase(url, "jdbc:mysql")) {
            return "com.mysql.cj.jdbc.Driver";
        }
        if (StrUtil.startWithIgnoreCase(url, "jdbc:postgresql")) {
            return "org.postgresql.Driver";
        }
        if (StrUtil.startWithIgnoreCase(url, "jdbc:oracle")) {
            return "oracle.jdbc.OracleDriver";
        }
        throw new BusinessException("无法识别数据源驱动，请指定 driverClassName");
    }
}
