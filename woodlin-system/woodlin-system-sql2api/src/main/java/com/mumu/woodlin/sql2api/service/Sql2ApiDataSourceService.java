package com.mumu.woodlin.sql2api.service;

import javax.sql.DataSource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.exception.BusinessException;
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
    /**
     * 独立的数据源注册表（不影响系统动态数据源）
     */
    private final Map<String, HikariDataSource> sql2ApiDataSources = new ConcurrentHashMap<>();

    /**
     * 动态新增数据源
     */
    public void addDataSource(AddDatasourceRequest request) {
        if (!(dataSource instanceof DynamicRoutingDataSource dynamicRoutingDataSource)) {
            throw new BusinessException("未启用动态数据源，无法新增数据源");
        }

        String driverClassName = resolveDriver(request.getDriverClassName(), request.getUrl());

        HikariDataSource target = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(driverClassName)
                .url(request.getUrl())
                .username(request.getUsername())
                .password(request.getPassword())
                .build();

        synchronized (dynamicRoutingDataSource) {
            if (dynamicRoutingDataSource.getDataSources().containsKey(request.getName())) {
                throw new BusinessException("数据源已存在: " + request.getName());
            }
            if (sql2ApiDataSources.containsKey(request.getName())) {
                throw new BusinessException("SQL2API 独立数据源已存在: " + request.getName());
            }
            dynamicRoutingDataSource.addDataSource(request.getName(), target);
        }

        sql2ApiDataSources.put(request.getName(), target);
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
