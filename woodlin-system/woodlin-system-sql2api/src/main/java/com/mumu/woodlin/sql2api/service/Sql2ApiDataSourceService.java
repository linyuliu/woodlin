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

/**
 * SQL2API 动态数据源管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Sql2ApiDataSourceService {

    private final DataSource dataSource;

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
            dynamicRoutingDataSource.addDataSource(request.getName(), target);
        }
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
