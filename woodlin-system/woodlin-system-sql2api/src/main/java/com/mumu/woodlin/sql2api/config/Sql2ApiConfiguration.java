package com.mumu.woodlin.sql2api.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractorFactory;

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
     * 使用 woodlin-common 中的统一 DatabaseMetadataExtractorFactory
     */
    @Bean
    public List<DatabaseMetadataExtractor> databaseMetadataExtractors() {
        // 使用统一的工厂获取所有已注册的提取器
        DatabaseMetadataExtractorFactory factory = DatabaseMetadataExtractorFactory.getInstance();
        List<DatabaseMetadataExtractor> extractors = factory.getAllExtractors();
        
        log.info("已从统一工厂加载 {} 个数据库元数据提取器", extractors.size());
        
        // 2. 通过SPI机制加载外部提取器（如果有的话）
        ServiceLoader<DatabaseMetadataExtractor> serviceLoader = ServiceLoader.load(DatabaseMetadataExtractor.class);
        for (DatabaseMetadataExtractor extractor : serviceLoader) {
            if (!extractors.contains(extractor)) {
                log.info("通过SPI加载额外的数据库元数据提取器: {}", extractor.getDatabaseType().name());
                extractors.add(extractor);
            }
        }
        
        return extractors;
    }
}
