package com.mumu.woodlin.sql2api.spi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.SchemaMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;

/**
 * 数据库元数据提取器接口 (SPI)
 * 
 * @author mumu
 * @description 定义数据库元数据提取的标准接口，支持多种数据库类型通过SPI机制扩展
 * @since 2025-01-01
 */
public interface DatabaseMetadataExtractor {
    
    /**
     * 获取支持的数据库类型
     * 
     * @return 数据库类型标识，如 "MySQL", "PostgreSQL", "Oracle", "DM8" 等
     */
    String getDatabaseType();
    
    /**
     * 判断是否支持该数据源
     * 
     * @param connection 数据库连接
     * @return 如果支持返回true，否则返回false
     */
    boolean supports(Connection connection) throws SQLException;
    
    /**
     * 提取数据库元数据
     * 
     * @param dataSource 数据源
     * @return 数据库元数据信息
     * @throws SQLException SQL异常
     */
    DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException;
    
    /**
     * 提取指定数据库的所有Schema信息
     * 注意：某些数据库（如MySQL）没有Schema概念，会返回空列表或默认值
     * 
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @return Schema列表
     * @throws SQLException SQL异常
     */
    List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException;
    
    /**
     * 提取指定Schema下的所有表信息
     * 
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param schemaName Schema名称，可以为null（对于不支持Schema的数据库）
     * @return 表列表
     * @throws SQLException SQL异常
     */
    List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException;
    
    /**
     * 提取指定表的所有列信息
     * 
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param schemaName Schema名称，可以为null
     * @param tableName 表名称
     * @return 列列表
     * @throws SQLException SQL异常
     */
    List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException;
    
    /**
     * 获取表注释
     * 
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param schemaName Schema名称，可以为null
     * @param tableName 表名称
     * @return 表注释
     * @throws SQLException SQL异常
     */
    String getTableComment(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException;
    
    /**
     * 获取优先级，用于多个提取器匹配时的选择
     * 数字越小优先级越高
     * 
     * @return 优先级值
     */
    default int getPriority() {
        return 100;
    }
}
