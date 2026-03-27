package com.mumu.woodlin.sql2api.service;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractorFactory;
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
    private final DatabaseMetadataExtractorFactory extractorFactory = DatabaseMetadataExtractorFactory.getInstance();
    
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

    /**
     * 获取默认测试SQL
     * <p>
     * 使用DatabaseMetadataExtractorFactory根据数据库类型获取对应的测试SQL
     * </p>
     * 
     * @param databaseType 数据库类型
     * @return 测试SQL
     */
    public String defaultTestQuery(String databaseType) {
        if (StrUtil.isBlank(databaseType)) {
            return "SELECT 1";
        }
        
        String testSql = extractorFactory.getDefaultTestQuery(databaseType);
        if (testSql != null) {
            return testSql;
        }
        
        log.warn("无法获取数据库类型 {} 的默认测试SQL，使用默认值 'SELECT 1'", databaseType);
        return "SELECT 1";
    }

    /**
     * 解析驱动类名
     * <p>
     * 优先使用配置的驱动类名，如果未配置则通过DatabaseMetadataExtractorFactory
     * 根据JDBC URL推断数据库类型并获取对应的驱动类名
     * </p>
     * 
     * @param driverClassName 配置的驱动类名
     * @param jdbcUrl JDBC URL
     * @return 驱动类全限定名
     */
    private String resolveDriver(String driverClassName, String jdbcUrl) {
        // 如果已配置驱动类名，直接返回
        if (StrUtil.isNotBlank(driverClassName)) {
            return driverClassName;
        }
        
        // 通过JDBC URL推断数据库类型
        String databaseType = extractorFactory.inferDatabaseType(jdbcUrl);
        if (databaseType == null) {
            throw new BusinessException("无法识别JDBC URL的数据库类型，请指定 driverClassName");
        }
        
        // 获取该数据库类型的默认驱动类名
        String defaultDriver = extractorFactory.getDefaultDriverClass(databaseType);
        if (defaultDriver == null) {
            throw new BusinessException("无法获取数据库类型 " + databaseType + " 的驱动类名，请指定 driverClassName");
        }
        
        log.debug("根据JDBC URL推断数据库类型: {}, 使用驱动: {}", databaseType, defaultDriver);
        return defaultDriver;
    }
}
