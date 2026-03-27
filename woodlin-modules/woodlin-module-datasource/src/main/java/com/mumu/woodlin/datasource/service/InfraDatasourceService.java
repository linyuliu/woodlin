package com.mumu.woodlin.datasource.service;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractorFactory;
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
    private final DatabaseMetadataExtractorFactory extractorFactory = DatabaseMetadataExtractorFactory.getInstance();
    
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
            // 设置查询超时为5秒，避免连接挂起
            ps.setQueryTimeout(5);
            ps.execute();
            log.info("数据源连接验证成功");
        } catch (Exception e) {
            log.error("数据源连接验证失败", e);
            // 使用通用错误消息，避免泄露敏感信息
            String safeMessage = e.getMessage() != null && e.getMessage().length() < 200 
                ? sanitizeErrorMessage(e.getMessage()) 
                : "连接失败，请检查数据源配置";
            throw new BusinessException("数据源连接验证失败: " + safeMessage, e);
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

    /**
     * 关闭所有数据源连接
     */
    @PreDestroy
    public void closeAll() {
        log.info("关闭所有数据源连接，共 {} 个", datasourceRegistry.size());
        datasourceRegistry.values().forEach(HikariDataSource::close);
        datasourceRegistry.clear();
    }

    /**
     * 清理错误消息中的敏感信息
     * 
     * @param message 原始错误消息
     * @return 清理后的错误消息
     */
    private String sanitizeErrorMessage(String message) {
        if (message == null) {
            return "连接失败";
        }
        // 移除可能包含的连接字符串、密码等敏感信息
        return message
                .replaceAll("jdbc:[^\\s]+", "jdbc:***")
                .replaceAll("password[=:]\\S+", "password=***")
                .replaceAll("user[=:]\\S+", "user=***");
    }
}
