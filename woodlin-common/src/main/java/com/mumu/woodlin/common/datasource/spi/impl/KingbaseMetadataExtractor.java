package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
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
 * 实现策略（2025-01更新）：
 * 1. 使用 KingBase 原生系统表（sys_class, sys_namespace, sys_attribute, sys_type）进行元数据提取
 * 2. 虽然 KingBase 有兼容模式，但其内部实现与真正的数据库不同，不能直接使用 MySQL/Oracle 的元数据提取方式
 * 3. 提供 pg_catalog 作为后备方案，确保在不同 KingBase 版本中的兼容性
 * 4. 兼容模式检测保留用于连接测试和类型映射
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
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        // 使用 KingBase 原生系统表提取 Schema 列表
        List<SchemaMetadata> schemas = new ArrayList<>();
        
        // KingBase 使用 sys_namespace 系统表
        String sql = "SELECT " +
                     "  nspname AS schema_name, " +
                     "  COALESCE(pg_catalog.obj_description(oid, 'pg_namespace'), '') AS comment " +
                     "FROM sys_namespace " +
                     "WHERE nspname NOT IN ('sys_catalog', 'information_schema', 'sys_toast', 'pg_toast', 'pg_temp_1', 'pg_toast_temp_1') " +
                     "ORDER BY nspname";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SchemaMetadata schema = SchemaMetadata.builder()
                            .schemaName(rs.getString("schema_name"))
                            .databaseName(databaseName)
                            .comment(rs.getString("comment"))
                            .build();
                    schemas.add(schema);
                }
            }
        } catch (SQLException e) {
            // 如果 sys_namespace 不可用，使用 pg_namespace 作为后备
            log.warn("KingBase 原生系统表查询 Schema 失败，尝试使用 pg_namespace 作为后备: {}", e.getMessage());
            return extractSchemasViaPgCatalog(connection, databaseName);
        }
        
        return schemas;
    }
    
    /**
     * 使用 pg_catalog 提取 Schema 列表（后备方案）
     */
    private List<SchemaMetadata> extractSchemasViaPgCatalog(Connection connection, String databaseName) throws SQLException {
        List<SchemaMetadata> schemas = new ArrayList<>();
        
        String sql = "SELECT " +
                     "  nspname AS schema_name, " +
                     "  COALESCE(pg_catalog.obj_description(oid, 'pg_namespace'), '') AS comment " +
                     "FROM pg_catalog.pg_namespace " +
                     "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast', 'pg_temp_1', 'pg_toast_temp_1') " +
                     "ORDER BY nspname";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SchemaMetadata schema = SchemaMetadata.builder()
                            .schemaName(rs.getString("schema_name"))
                            .databaseName(databaseName)
                            .comment(rs.getString("comment"))
                            .build();
                    schemas.add(schema);
                }
            }
        }
        
        return schemas;
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        // 使用 KingBase 原生系统表提取元数据，不依赖兼容模式
        return extractTablesNative(connection, databaseName, schemaName);
    }
    
    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        // 使用 KingBase 原生系统表提取元数据，不依赖兼容模式
        return extractColumnsNative(connection, databaseName, schemaName, tableName);
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
        
        // 方式1：通过 SHOW database_mode 命令（KingBase V8+）
        try (PreparedStatement pstmt = connection.prepareStatement("SHOW database_mode");
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String mode = rs.getString(1);
                CompatibilityMode detectedMode = CompatibilityMode.fromCode(mode);
                log.debug("通过 SHOW database_mode 检测到模式: {}", detectedMode.getDescription());
                return detectedMode;
            }
        } catch (SQLException e) {
            log.debug("SHOW database_mode 命令失败，尝试其他方式: {}", e.getMessage());
        }
        
        // 方式2：查询 pg_settings 表
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT setting FROM pg_settings WHERE name = 'database_mode'");
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
                "SELECT current_setting('database_mode')");
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
     * 使用 KingBase 原生系统表提取表列表
     * <p>
     * KingBase 基于 PostgreSQL 开发，但其内部实现与真实的 PostgreSQL 有差异。
     * 使用 KingBase 自己的系统表（sys_class, sys_namespace）进行元数据提取。
     * </p>
     * 
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param schemaName Schema名称
     * @return 表元数据列表
     * @throws SQLException SQL异常
     */
    private List<TableMetadata> extractTablesNative(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        
        // KingBase 使用 sys_class 和 sys_namespace 系统表
        // 这些表是 KingBase 内部实现，不同于 PostgreSQL 的 pg_class 和 pg_namespace
        String sql;
        boolean hasSchemaFilter = schemaName != null && !schemaName.isEmpty();
        
        if (hasSchemaFilter) {
            // 指定 schema 时，只查询该 schema 的表
            sql = "SELECT " +
                  "  c.relname AS table_name, " +
                  "  n.nspname AS schema_name, " +
                  "  COALESCE(d.description, '') AS table_comment " +
                  "FROM sys_class c " +
                  "INNER JOIN sys_namespace n ON c.relnamespace = n.oid " +
                  "LEFT JOIN sys_description d ON d.objoid = c.oid AND d.objsubid = 0 " +
                  "WHERE n.nspname = ? " +
                  "  AND c.relkind = 'r' " +  // 'r' = 普通表
                  "  AND n.nspname NOT IN ('sys_catalog', 'information_schema', 'sys_toast') " +
                  "ORDER BY n.nspname, c.relname";
        } else {
            // 未指定 schema 时，查询所有非系统 schema 的表
            sql = "SELECT " +
                  "  c.relname AS table_name, " +
                  "  n.nspname AS schema_name, " +
                  "  COALESCE(d.description, '') AS table_comment " +
                  "FROM sys_class c " +
                  "INNER JOIN sys_namespace n ON c.relnamespace = n.oid " +
                  "LEFT JOIN sys_description d ON d.objoid = c.oid AND d.objsubid = 0 " +
                  "WHERE c.relkind = 'r' " +  // 'r' = 普通表
                  "  AND n.nspname NOT IN ('sys_catalog', 'information_schema', 'sys_toast', 'pg_catalog', 'pg_toast') " +
                  "ORDER BY n.nspname, c.relname";
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);
            if (hasSchemaFilter) {
                pstmt.setString(1, schemaName);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = TableMetadata.builder()
                            .tableName(rs.getString("table_name"))
                            .databaseName(databaseName)
                            .schemaName(rs.getString("schema_name"))
                            .comment(rs.getString("table_comment"))
                            .build();
                    tables.add(table);
                }
            }
        } catch (SQLException e) {
            // 如果 sys_* 系统表不可用，尝试使用 pg_* 系统表作为后备
            log.warn("KingBase 原生系统表查询失败，尝试使用 pg_* 系统表作为后备: {}", e.getMessage());
            return extractTablesViaPgCatalog(connection, databaseName, schemaName);
        }
        
        return tables;
    }
    
    /**
     * 使用 pg_catalog 提取表列表（后备方案）
     */
    private List<TableMetadata> extractTablesViaPgCatalog(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        
        String sql;
        boolean hasSchemaFilter = schemaName != null && !schemaName.isEmpty();
        
        if (hasSchemaFilter) {
            // 指定 schema 时，只查询该 schema 的表
            sql = "SELECT " +
                  "  c.relname AS table_name, " +
                  "  n.nspname AS schema_name, " +
                  "  COALESCE(pg_catalog.obj_description(c.oid, 'pg_class'), '') AS table_comment " +
                  "FROM pg_catalog.pg_class c " +
                  "INNER JOIN pg_catalog.pg_namespace n ON c.relnamespace = n.oid " +
                  "WHERE n.nspname = ? " +
                  "  AND c.relkind = 'r' " +
                  "  AND n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                  "ORDER BY n.nspname, c.relname";
        } else {
            // 未指定 schema 时，查询所有非系统 schema 的表
            sql = "SELECT " +
                  "  c.relname AS table_name, " +
                  "  n.nspname AS schema_name, " +
                  "  COALESCE(pg_catalog.obj_description(c.oid, 'pg_class'), '') AS table_comment " +
                  "FROM pg_catalog.pg_class c " +
                  "INNER JOIN pg_catalog.pg_namespace n ON c.relnamespace = n.oid " +
                  "WHERE c.relkind = 'r' " +
                  "  AND n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast', 'sys_catalog', 'sys_toast') " +
                  "ORDER BY n.nspname, c.relname";
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);
            if (hasSchemaFilter) {
                pstmt.setString(1, schemaName);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = TableMetadata.builder()
                            .tableName(rs.getString("table_name"))
                            .databaseName(databaseName)
                            .schemaName(rs.getString("schema_name"))
                            .comment(rs.getString("table_comment"))
                            .build();
                    tables.add(table);
                }
            }
        }
        
        return tables;
    }
    
    /**
     * 使用 KingBase 原生系统表提取列信息
     * <p>
     * 直接查询 KingBase 的系统表（sys_attribute, sys_class, sys_namespace）
     * 获取列的详细信息，不依赖兼容模式。
     * </p>
     * 
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param schemaName Schema名称
     * @param tableName 表名
     * @return 列元数据列表
     * @throws SQLException SQL异常
     */
    private List<ColumnMetadata> extractColumnsNative(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "public";
        
        // KingBase 使用 sys_attribute 和 sys_type 系统表
        String sql = "SELECT " +
                     "  a.attname AS column_name, " +
                     "  t.typname AS data_type, " +
                     "  a.attnotnull AS not_null, " +
                     "  a.attnum AS ordinal_position, " +
                     "  pg_catalog.format_type(a.atttypid, a.atttypmod) AS formatted_type, " +
                     "  COALESCE(pg_catalog.col_description(c.oid, a.attnum), '') AS column_comment, " +
                     "  pg_catalog.pg_get_expr(ad.adbin, ad.adrelid) AS column_default " +
                     "FROM sys_attribute a " +
                     "INNER JOIN sys_class c ON a.attrelid = c.oid " +
                     "INNER JOIN sys_namespace n ON c.relnamespace = n.oid " +
                     "INNER JOIN sys_type t ON a.atttypid = t.oid " +
                     "LEFT JOIN sys_attrdef ad ON a.attrelid = ad.adrelid AND a.attnum = ad.adnum " +
                     "WHERE n.nspname = ? " +
                     "  AND c.relname = ? " +
                     "  AND a.attnum > 0 " +  // 跳过系统列
                     "  AND NOT a.attisdropped " +  // 跳过已删除的列
                     "ORDER BY a.attnum";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);
            pstmt.setString(1, targetSchema);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataType = rs.getString("data_type");
                    String columnDefault = rs.getString("column_default");
                    boolean isAutoIncrement = columnDefault != null && 
                                             (columnDefault.contains("nextval") || 
                                              columnDefault.contains("seq_"));
                    
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("column_name"))
                            .tableName(tableName)
                            .schemaName(targetSchema)
                            .databaseName(databaseName)
                            .dataType(dataType)
                            .nullable(!rs.getBoolean("not_null"))
                            .ordinalPosition(rs.getInt("ordinal_position"))
                            .comment(rs.getString("column_comment"))
                            .defaultValue(columnDefault)
                            .autoIncrement(isAutoIncrement)
                            .javaType(mapKingBaseTypeToJava(dataType))
                            .build();
                    
                    columns.add(column);
                }
            }
        } catch (SQLException e) {
            // 如果 sys_* 系统表不可用，尝试使用 pg_* 系统表作为后备
            log.warn("KingBase 原生系统表查询列失败，尝试使用 pg_* 系统表作为后备: {}", e.getMessage());
            return extractColumnsViaPgCatalog(connection, databaseName, schemaName, tableName);
        }
        
        return columns;
    }
    
    /**
     * 使用 pg_catalog 提取列信息（后备方案）
     */
    private List<ColumnMetadata> extractColumnsViaPgCatalog(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "public";
        
        String sql = "SELECT " +
                     "  a.attname AS column_name, " +
                     "  t.typname AS data_type, " +
                     "  a.attnotnull AS not_null, " +
                     "  a.attnum AS ordinal_position, " +
                     "  pg_catalog.format_type(a.atttypid, a.atttypmod) AS formatted_type, " +
                     "  COALESCE(pg_catalog.col_description(c.oid, a.attnum), '') AS column_comment, " +
                     "  pg_catalog.pg_get_expr(ad.adbin, ad.adrelid) AS column_default " +
                     "FROM pg_catalog.pg_attribute a " +
                     "INNER JOIN pg_catalog.pg_class c ON a.attrelid = c.oid " +
                     "INNER JOIN pg_catalog.pg_namespace n ON c.relnamespace = n.oid " +
                     "INNER JOIN pg_catalog.pg_type t ON a.atttypid = t.oid " +
                     "LEFT JOIN pg_catalog.pg_attrdef ad ON a.attrelid = ad.adrelid AND a.attnum = ad.adnum " +
                     "WHERE n.nspname = ? " +
                     "  AND c.relname = ? " +
                     "  AND a.attnum > 0 " +
                     "  AND NOT a.attisdropped " +
                     "ORDER BY a.attnum";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);
            pstmt.setString(1, targetSchema);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataType = rs.getString("data_type");
                    String columnDefault = rs.getString("column_default");
                    boolean isAutoIncrement = columnDefault != null && 
                                             (columnDefault.contains("nextval") || 
                                              columnDefault.contains("seq_"));
                    
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("column_name"))
                            .tableName(tableName)
                            .schemaName(targetSchema)
                            .databaseName(databaseName)
                            .dataType(dataType)
                            .nullable(!rs.getBoolean("not_null"))
                            .ordinalPosition(rs.getInt("ordinal_position"))
                            .comment(rs.getString("column_comment"))
                            .defaultValue(columnDefault)
                            .autoIncrement(isAutoIncrement)
                            .javaType(mapKingBaseTypeToJava(dataType))
                            .build();
                    
                    columns.add(column);
                }
            }
        }
        
        return columns;
    }
    
    /**
     * 将 KingBase 数据类型映射到 Java 类型
     * <p>
     * KingBase 的类型系统基于 PostgreSQL，但在不同兼容模式下可能有所不同。
     * 这里提供一个统一的类型映射，不依赖兼容模式。
     * </p>
     * 
     * @param kingbaseType KingBase 数据类型
     * @return Java 类型
     */
    private String mapKingBaseTypeToJava(String kingbaseType) {
        if (kingbaseType == null) {
            return "Object";
        }
        
        String type = kingbaseType.toLowerCase();
        
        // 使用父类的类型映射
        String javaType = typeMapping.get(type);
        if (javaType != null) {
            return javaType;
        }
        
        // KingBase 特有类型映射（不在父类映射中）
        return switch (type) {
            // 数字类型
            case "number" -> "BigDecimal";
            case "tinyint" -> "Byte";
            case "mediumint" -> "Integer";
            
            // 字符类型
            case "varchar2", "nvarchar2", "nvarchar" -> "String";
            case "nchar" -> "String";
            case "clob", "ntext", "longtext", "mediumtext", "tinytext" -> "String";
            
            // 二进制类型
            case "blob", "raw", "image" -> "byte[]";
            
            // 日期时间类型
            case "datetime" -> "LocalDateTime";
            
            // 金额类型
            case "money", "smallmoney" -> "BigDecimal";
            
            // 唯一标识符
            case "uniqueidentifier" -> "UUID";
            
            default -> "Object";
        };
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
