package com.mumu.woodlin.sql2api.service;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
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
     * sql2api 只读：通过 datasourceCode 引用基础设施数据源
     */
    public DataSource getDataSourceByCode(String datasourceCode) {
        Assert.hasText(datasourceCode, "数据源编码不能为空");
        if (sql2ApiDataSources.containsKey(datasourceCode)) {
            return sql2ApiDataSources.get(datasourceCode);
        }
        SqlDatasourceConfig config = datasourceMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SqlDatasourceConfig>()
                        .eq("datasource_code", datasourceCode)
        );
        if (config == null) {
            throw new BusinessException("数据源不存在: " + datasourceCode);
        }
        if (config.getStatus() != null && config.getStatus() == 0) {
            throw new BusinessException("数据源已禁用: " + datasourceCode);
        }
        String driverClassName = resolveDriver(config.getDriverClass(), config.getJdbcUrl());
        HikariDataSource target = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(driverClassName)
                .url(config.getJdbcUrl())
                .username(config.getUsername())
                .password(config.getPassword())
                .build();
        validateConnectivity(target, config.getTestSql(), config.getDatasourceType());
        sql2ApiDataSources.put(datasourceCode, target);
        return target;
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

    public DataSource buildTemporaryDataSource(AddDatasourceRequest request) {
        String driverClassName = resolveDriver(request.getDriverClass(), request.getJdbcUrl());
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(driverClassName)
                .url(request.getJdbcUrl())
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
    }

    public void validateConnectivity(DataSource target, String testSql, String databaseType) {
        String sql = StrUtil.emptyToDefault(testSql, defaultTestQuery(databaseType));
        try (var conn = target.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (Exception e) {
            throw new BusinessException("数据源连接验证失败: " + e.getMessage(), e);
        }
    }

    public String defaultTestQuery(String databaseType) {
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
