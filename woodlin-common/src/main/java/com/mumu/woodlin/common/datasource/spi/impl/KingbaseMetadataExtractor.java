package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * 人大金仓数据库元数据提取器
 * <p>
 * 人大金仓数据库（KingbaseES）基于PostgreSQL开发，是国产数据库。
 * 支持多种兼容模式：PostgreSQL、Oracle、MySQL、SQL Server。
 * </p>
 * <p>
 * 兼容模式说明：
 * <ul>
 *   <li>PG模式 (PostgreSQL)：默认模式，完全兼容PostgreSQL语法</li>
 *   <li>Oracle模式：兼容Oracle语法和数据类型</li>
 *   <li>MySQL模式：兼容MySQL语法和函数</li>
 *   <li>MSSQL模式 (SQL Server)：兼容SQL Server语法</li>
 * </ul>
 * </p>
 * <p>
 * 实现策略：
 * 1. 优先检测兼容模式，根据模式调整元数据提取策略
 * 2. 使用PostgreSQL协议作为基础，针对不同模式进行适配
 * 3. 处理不同模式下的数据类型映射差异
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public class KingbaseMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    /**
     * KingBase兼容模式枚举
     */
    public enum CompatibilityMode {
        /** PostgreSQL兼容模式（默认） */
        PG("pg", "PostgreSQL"),
        /** Oracle兼容模式 */
        ORACLE("oracle", "Oracle"),
        /** MySQL兼容模式 */
        MYSQL("mysql", "MySQL"),
        /** SQL Server兼容模式 */
        MSSQL("mssql", "SQL Server");
        
        private final String code;
        private final String description;
        
        CompatibilityMode(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static CompatibilityMode fromCode(String code) {
            if (code == null) return PG;
            String lowerCode = code.toLowerCase().trim();
            for (CompatibilityMode mode : values()) {
                if (mode.code.equals(lowerCode)) {
                    return mode;
                }
            }
            return PG;
        }
    }
    
    /** 当前连接的兼容模式（线程局部变量） */
    private final ThreadLocal<CompatibilityMode> currentMode = ThreadLocal.withInitial(() -> CompatibilityMode.PG);
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.KINGBASE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        if (productName == null) {
            return false;
        }
        String lowerName = productName.toLowerCase();
        return lowerName.contains("kingbase") || lowerName.contains("kingbasees");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.kingbase8.Driver";
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(javax.sql.DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // 检测并设置兼容模式
            CompatibilityMode mode = detectCompatibilityMode(connection);
            currentMode.set(mode);
            log.info("检测到KingBase兼容模式: {}", mode.getDescription());
            
            // 调用父类方法提取基本元数据
            DatabaseMetadata metadata = super.extractDatabaseMetadata(dataSource);
            
            // 添加KingBase特有信息
            try {
                String version = connection.getMetaData().getDatabaseProductVersion();
                metadata.setDatabaseProductVersion(version + " [" + mode.getDescription() + " 兼容模式]");
            } catch (SQLException e) {
                log.warn("无法获取KingBase版本信息", e);
            }
            
            return metadata;
        } finally {
            currentMode.remove();
        }
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        // 检测并设置兼容模式
        CompatibilityMode mode = detectCompatibilityMode(connection);
        currentMode.set(mode);
        
        try {
            // 根据兼容模式选择不同的提取策略
            return switch (mode) {
                case ORACLE -> extractTablesOracleMode(connection, databaseName, schemaName);
                case MYSQL -> extractTablesMySQLMode(connection, databaseName, schemaName);
                case MSSQL -> extractTablesMSSQLMode(connection, databaseName, schemaName);
                default -> super.extractTables(connection, databaseName, schemaName);
            };
        } finally {
            currentMode.remove();
        }
    }
    
    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        // 检测并设置兼容模式
        CompatibilityMode mode = detectCompatibilityMode(connection);
        currentMode.set(mode);
        
        try {
            // 根据兼容模式选择不同的提取策略
            return switch (mode) {
                case ORACLE -> extractColumnsOracleMode(connection, databaseName, schemaName, tableName);
                case MYSQL -> extractColumnsMySQLMode(connection, databaseName, schemaName, tableName);
                case MSSQL -> extractColumnsMSSQLMode(connection, databaseName, schemaName, tableName);
                default -> super.extractColumns(connection, databaseName, schemaName, tableName);
            };
        } finally {
            currentMode.remove();
        }
    }
    
    /**
     * 检测KingBase的兼容模式
     * <p>
     * 通过查询系统配置参数来判断当前数据库的兼容模式。
     * KingBase使用 db_mode 参数来标识兼容模式。
     * </p>
     * 
     * @param connection 数据库连接
     * @return 兼容模式
     */
    private CompatibilityMode detectCompatibilityMode(Connection connection) {
        // 尝试多种方式检测兼容模式
        
        // 方式1：通过 SHOW db_mode 命令（KingBase V8+）
        try (PreparedStatement pstmt = connection.prepareStatement("SHOW db_mode");
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String mode = rs.getString(1);
                CompatibilityMode detectedMode = CompatibilityMode.fromCode(mode);
                log.debug("通过 SHOW db_mode 检测到模式: {}", detectedMode.getDescription());
                return detectedMode;
            }
        } catch (SQLException e) {
            log.debug("SHOW db_mode 命令失败，尝试其他方式: {}", e.getMessage());
        }
        
        // 方式2：查询 pg_settings 表
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT setting FROM pg_settings WHERE name = 'db_mode'");
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String mode = rs.getString("setting");
                CompatibilityMode detectedMode = CompatibilityMode.fromCode(mode);
                log.debug("通过 pg_settings 检测到模式: {}", detectedMode.getDescription());
                return detectedMode;
            }
        } catch (SQLException e) {
            log.debug("查询 pg_settings 失败，尝试其他方式: {}", e.getMessage());
        }
        
        // 方式3：通过 current_setting 函数
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT current_setting('db_mode')");
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String mode = rs.getString(1);
                CompatibilityMode detectedMode = CompatibilityMode.fromCode(mode);
                log.debug("通过 current_setting 检测到模式: {}", detectedMode.getDescription());
                return detectedMode;
            }
        } catch (SQLException e) {
            log.debug("current_setting 函数失败: {}", e.getMessage());
        }
        
        // 默认使用PostgreSQL模式
        log.info("无法检测兼容模式，默认使用PostgreSQL模式");
        return CompatibilityMode.PG;
    }
    
    /**
     * Oracle兼容模式下提取表列表
     */
    private List<TableMetadata> extractTablesOracleMode(Connection connection, String databaseName, String schemaName) throws SQLException {
        // Oracle模式下，使用 USER_TABLES 或 ALL_TABLES 视图（如果可用）
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName.toUpperCase() : "PUBLIC";
        
        String sql = "SELECT table_name, " +
                     "COALESCE((SELECT comments FROM user_tab_comments WHERE table_name = t.table_name), '') as comments " +
                     "FROM user_tables t " +
                     "WHERE UPPER(owner) = ? OR owner IS NULL " +
                     "ORDER BY table_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            // 如果Oracle模式查询失败，回退到PostgreSQL模式
            try (ResultSet rs = pstmt.executeQuery()) {
                return buildTableList(rs, databaseName, targetSchema);
            }
        } catch (SQLException e) {
            log.warn("Oracle模式查询失败，回退到PostgreSQL模式: {}", e.getMessage());
            return super.extractTables(connection, databaseName, schemaName);
        }
    }
    
    /**
     * MySQL兼容模式下提取表列表
     */
    private List<TableMetadata> extractTablesMySQLMode(Connection connection, String databaseName, String schemaName) throws SQLException {
        // MySQL模式下，使用 information_schema.tables
        String targetDb = (databaseName != null && !databaseName.isEmpty()) ? databaseName : connection.getCatalog();
        
        String sql = "SELECT table_name, table_comment " +
                     "FROM information_schema.tables " +
                     "WHERE table_schema = ? AND table_type = 'BASE TABLE' " +
                     "ORDER BY table_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetDb);
            try (ResultSet rs = pstmt.executeQuery()) {
                return buildTableList(rs, targetDb, null);
            }
        } catch (SQLException e) {
            log.warn("MySQL模式查询失败，回退到PostgreSQL模式: {}", e.getMessage());
            return super.extractTables(connection, databaseName, schemaName);
        }
    }
    
    /**
     * SQL Server兼容模式下提取表列表
     */
    private List<TableMetadata> extractTablesMSSQLMode(Connection connection, String databaseName, String schemaName) throws SQLException {
        // MSSQL模式下，使用 sys.tables 视图
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "dbo";
        
        String sql = "SELECT t.name as table_name, " +
                     "COALESCE(ep.value, '') as table_comment " +
                     "FROM sys.tables t " +
                     "LEFT JOIN sys.extended_properties ep ON ep.major_id = t.object_id AND ep.minor_id = 0 " +
                     "WHERE SCHEMA_NAME(t.schema_id) = ? " +
                     "ORDER BY t.name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            try (ResultSet rs = pstmt.executeQuery()) {
                return buildTableList(rs, databaseName, targetSchema);
            }
        } catch (SQLException e) {
            log.warn("MSSQL模式查询失败，回退到PostgreSQL模式: {}", e.getMessage());
            return super.extractTables(connection, databaseName, schemaName);
        }
    }
    
    /**
     * Oracle兼容模式下提取列信息
     */
    private List<ColumnMetadata> extractColumnsOracleMode(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        // Oracle模式使用 user_tab_columns 视图
        try {
            return super.extractColumns(connection, databaseName, schemaName, tableName);
        } catch (SQLException e) {
            log.warn("Oracle模式列查询失败，回退到PostgreSQL模式: {}", e.getMessage());
            return super.extractColumns(connection, databaseName, schemaName, tableName);
        }
    }
    
    /**
     * MySQL兼容模式下提取列信息
     */
    private List<ColumnMetadata> extractColumnsMySQLMode(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        // MySQL模式使用 information_schema.columns
        try {
            return super.extractColumns(connection, databaseName, schemaName, tableName);
        } catch (SQLException e) {
            log.warn("MySQL模式列查询失败，回退到PostgreSQL模式: {}", e.getMessage());
            return super.extractColumns(connection, databaseName, schemaName, tableName);
        }
    }
    
    /**
     * SQL Server兼容模式下提取列信息
     */
    private List<ColumnMetadata> extractColumnsMSSQLMode(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        // MSSQL模式使用 sys.columns
        try {
            return super.extractColumns(connection, databaseName, schemaName, tableName);
        } catch (SQLException e) {
            log.warn("MSSQL模式列查询失败，回退到PostgreSQL模式: {}", e.getMessage());
            return super.extractColumns(connection, databaseName, schemaName, tableName);
        }
    }
    
    /**
     * 构建表元数据列表
     */
    private List<TableMetadata> buildTableList(ResultSet rs, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new java.util.ArrayList<>();
        while (rs.next()) {
            TableMetadata table = TableMetadata.builder()
                    .tableName(rs.getString(1))
                    .databaseName(databaseName)
                    .schemaName(schemaName)
                    .comment(rs.getString(2))
                    .build();
            tables.add(table);
        }
        return tables;
    }
    
    @Override
    protected void initializeDefaultTypeMapping() {
        super.initializeDefaultTypeMapping();
        
        // 添加KingBase特有的类型映射
        // Oracle模式下的特殊类型
        typeMapping.put("number", "BigDecimal");
        typeMapping.put("varchar2", "String");
        typeMapping.put("nvarchar2", "String");
        typeMapping.put("clob", "String");
        typeMapping.put("blob", "byte[]");
        typeMapping.put("raw", "byte[]");
        
        // MySQL模式下的特殊类型
        typeMapping.put("tinyint", "Byte");
        typeMapping.put("mediumint", "Integer");
        typeMapping.put("datetime", "LocalDateTime");
        typeMapping.put("longtext", "String");
        typeMapping.put("mediumtext", "String");
        typeMapping.put("tinytext", "String");
        
        // SQL Server模式下的特殊类型
        typeMapping.put("nvarchar", "String");
        typeMapping.put("nchar", "String");
        typeMapping.put("ntext", "String");
        typeMapping.put("image", "byte[]");
        typeMapping.put("money", "BigDecimal");
        typeMapping.put("smallmoney", "BigDecimal");
        typeMapping.put("uniqueidentifier", "UUID");
    }
    
    @Override
    public String getDefaultTestQuery() {
        CompatibilityMode mode = currentMode.get();
        return switch (mode) {
            case ORACLE -> "SELECT 1 FROM DUAL";
            case MYSQL -> "SELECT 1";
            case MSSQL -> "SELECT 1";
            default -> "SELECT 1";
        };
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
    
    @Override
    public String getMinSupportedVersion() {
        return "8.0";
    }
    
    @Override
    public boolean supportsVersion(Connection connection, int majorVersion, int minorVersion) throws SQLException {
        if (!supports(connection)) {
            return false;
        }
        // 支持 KingBase 8.0+
        return majorVersion >= 8;
    }
}
