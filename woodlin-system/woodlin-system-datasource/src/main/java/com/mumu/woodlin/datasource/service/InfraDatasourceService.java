package com.mumu.woodlin.datasource.service;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import com.mumu.woodlin.datasource.mapper.InfraDatasourceMapper;
import com.mumu.woodlin.datasource.model.request.DatasourceRequest;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PreDestroy;

/**
 * 基础设施数据源管理服务
 * <p>
 * 提供数据源的动态管理、连接测试等功能，为各业务模块提供统一的数据源访问能力
 * </p>
 * 
 * @author mumu
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InfraDatasourceService {

    private final InfraDatasourceMapper datasourceMapper;
    
    /**
     * 数据源注册表（缓存已创建的数据源）
     */
    private final Map<String, HikariDataSource> datasourceRegistry = new ConcurrentHashMap<>();

    /**
     * 通过 datasourceCode 获取数据源
     * 
     * @param datasourceCode 数据源编码
     * @return 数据源实例
     */
    public DataSource getDataSourceByCode(String datasourceCode) {
        Assert.hasText(datasourceCode, "数据源编码不能为空");
        
        // 从缓存获取
        if (datasourceRegistry.containsKey(datasourceCode)) {
            return datasourceRegistry.get(datasourceCode);
        }
        
        // 从数据库加载配置
        InfraDatasourceConfig config = datasourceMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InfraDatasourceConfig>()
                        .eq("datasource_code", datasourceCode)
        );
        
        if (config == null) {
            throw new BusinessException("数据源不存在: " + datasourceCode);
        }
        
        if (config.getStatus() != null && config.getStatus() == 0) {
            throw new BusinessException("数据源已禁用: " + datasourceCode);
        }
        
        // 创建数据源
        String driverClassName = resolveDriver(config.getDriverClass(), config.getJdbcUrl());
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(driverClassName)
                .url(config.getJdbcUrl())
                .username(config.getUsername())
                .password(config.getPassword())
                .build();
        
        // 验证连接
        validateConnectivity(dataSource, config.getTestSql(), config.getDatasourceType());
        
        // 缓存数据源
        datasourceRegistry.put(datasourceCode, dataSource);
        
        return dataSource;
    }

    /**
     * 获取已缓存的数据源
     * 
     * @param datasourceCode 数据源编码
     * @return 数据源实例，不存在则返回null
     */
    public DataSource getCachedDataSource(String datasourceCode) {
        return datasourceRegistry.get(datasourceCode);
    }

    /**
     * 构建临时数据源（用于测试连接）
     * 
     * @param request 数据源配置请求
     * @return 临时数据源实例
     */
    public DataSource buildTemporaryDataSource(DatasourceRequest request) {
        String driverClassName = resolveDriver(request.getDriverClass(), request.getJdbcUrl());
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(driverClassName)
                .url(request.getJdbcUrl())
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
    }

    /**
     * 验证数据源连接
     * 
     * @param dataSource 数据源
     * @param testSql 测试SQL
     * @param databaseType 数据库类型
     */
    public void validateConnectivity(DataSource dataSource, String testSql, String databaseType) {
        String sql = StrUtil.emptyToDefault(testSql, defaultTestQuery(databaseType));
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.execute();
            log.info("数据源连接验证成功");
        } catch (Exception e) {
            log.error("数据源连接验证失败", e);
            throw new BusinessException("数据源连接验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取默认测试SQL
     * 
     * @param databaseType 数据库类型
     * @return 测试SQL
     */
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

    /**
     * 解析驱动类名
     * 
     * @param driverClassName 配置的驱动类名
     * @param jdbcUrl JDBC URL
     * @return 驱动类全限定名
     */
    private String resolveDriver(String driverClassName, String jdbcUrl) {
        if (StrUtil.isNotBlank(driverClassName)) {
            return driverClassName;
        }
        if (StrUtil.startWithIgnoreCase(jdbcUrl, "jdbc:mysql")) {
            return "com.mysql.cj.jdbc.Driver";
        }
        if (StrUtil.startWithIgnoreCase(jdbcUrl, "jdbc:postgresql")) {
            return "org.postgresql.Driver";
        }
        if (StrUtil.startWithIgnoreCase(jdbcUrl, "jdbc:oracle")) {
            return "oracle.jdbc.OracleDriver";
        }
        throw new BusinessException("无法识别数据源驱动，请指定 driverClassName");
    }

    /**
     * 关闭所有数据源连接
     */
    @PreDestroy
    public void closeAll() {
        log.info("关闭所有数据源连接，共 {} 个", datasourceRegistry.size());
        datasourceRegistry.values().forEach(HikariDataSource::close);
        datasourceRegistry.clear();
    }
}
