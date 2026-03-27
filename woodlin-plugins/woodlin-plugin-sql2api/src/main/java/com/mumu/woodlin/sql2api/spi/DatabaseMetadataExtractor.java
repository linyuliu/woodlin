package com.mumu.woodlin.sql2api.spi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;

/**
 * 数据库元数据提取器接口 (SPI)
 * <p>
 * 此接口已迁移到公共模块。为了向后兼容，保留此接口。
 * 新代码请使用 {@link com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor}
 * </p>
 * 
 * @author mumu
 * @see com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor
 * @since 2025-01-01
 */
public interface DatabaseMetadataExtractor {
    
    /**
     * 获取支持的数据库类型
     */
    String getDatabaseType();
    
    /**
     * 判断是否支持该数据源
     */
    boolean supports(Connection connection) throws SQLException;
    
    /**
     * 提取数据库元数据
     */
    DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException;
    
    /**
     * 提取Schema信息
     */
    List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException;
    
    /**
     * 提取表信息
     */
    List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException;
    
    /**
     * 提取列信息
     */
    List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException;
    
    /**
     * 获取表注释
     */
    String getTableComment(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException;
    
    /**
     * 获取优先级
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * 获取默认驱动类
     */
    default String getDefaultDriverClass() {
        return null;
    }
}
