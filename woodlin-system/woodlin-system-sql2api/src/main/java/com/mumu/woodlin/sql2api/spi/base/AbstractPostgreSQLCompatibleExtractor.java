package com.mumu.woodlin.sql2api.spi.base;

/**
 * PostgreSQL兼容数据库的抽象提取器基类
 * <p>
 * 此类已迁移到公共模块。
 * 请使用 {@link com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor}
 * </p>
 */
public abstract class AbstractPostgreSQLCompatibleExtractor 
        extends com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor 
        implements com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor {
    // 继承自公共模块的类，保持向后兼容
}
