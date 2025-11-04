package com.mumu.woodlin.sql2api.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor;
import com.mumu.woodlin.sql2api.spi.impl.MySQLMetadataExtractor;
import com.mumu.woodlin.sql2api.spi.impl.PostgreSQLMetadataExtractor;
import com.mumu.woodlin.sql2api.spi.impl.OracleMetadataExtractor;

/**
 * SQL2API模块配置类
 * 
 * @author mumu
 * @description 配置SQL2API模块的核心组件和SPI机制
 * @since 2025-01-01
 */
@Slf4j
@Configuration
@EnableCaching
@ComponentScan(basePackages = "com.mumu.woodlin.sql2api")
public class Sql2ApiConfiguration {
    
    /**
     * 注册所有数据库元数据提取器
     * 支持SPI机制自动发现和手动注册
     */
    @Bean
    public List<DatabaseMetadataExtractor> databaseMetadataExtractors() {
        List<DatabaseMetadataExtractor> extractors = new ArrayList<>();
        
        // 1. 手动注册内置提取器
        extractors.add(new MySQLMetadataExtractor());
        extractors.add(new PostgreSQLMetadataExtractor());
        extractors.add(new OracleMetadataExtractor());
        log.info("已注册内置数据库元数据提取器: MySQL, PostgreSQL, Oracle");
        
        // 2. 通过SPI机制加载外部提取器
        ServiceLoader<DatabaseMetadataExtractor> serviceLoader = ServiceLoader.load(DatabaseMetadataExtractor.class);
        for (DatabaseMetadataExtractor extractor : serviceLoader) {
            log.info("通过SPI加载数据库元数据提取器: {}", extractor.getDatabaseType());
            extractors.add(extractor);
        }
        
        log.info("已注册 {} 个数据库元数据提取器", extractors.size());
        return extractors;
    }
}
