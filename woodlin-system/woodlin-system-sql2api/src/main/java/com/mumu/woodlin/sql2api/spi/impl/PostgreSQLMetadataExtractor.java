package com.mumu.woodlin.sql2api.spi.impl;

/**
 * PostgreSQL数据库元数据提取器
 * <p>
 * 此类已迁移到公共模块。
 * 请使用 {@link com.mumu.woodlin.common.datasource.spi.impl.PostgreSQLMetadataExtractor}
 * </p>
 */
public class PostgreSQLMetadataExtractor 
        extends com.mumu.woodlin.common.datasource.spi.impl.PostgreSQLMetadataExtractor 
        implements com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor {
    // 继承自公共模块的类，保持向后兼容
}
