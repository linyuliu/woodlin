package com.mumu.woodlin.common.datasource.spi.base;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;

/**
 * PostgreSQL兼容数据库的抽象提取器基类
 * <p>
 * 为PostgreSQL兼容的数据库（如PostgreSQL、KingbaseES、Vastbase等）提供可继承的基础实现。
 * 支持版本特定的类型映射和SQL差异处理。
 * </p>
 * <p>
 * 版本差异支持示例：
 * <ul>
 *   <li>PostgreSQL 10+ vs 12+: 分区表支持、JSON路径查询等</li>
 *   <li>PostgreSQL 14+: MULTIRANGE类型支持</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public abstract class AbstractPostgreSQLCompatibleExtractor implements DatabaseMetadataExtractor {
    
    /** 基础类型映射 */
    protected Map<String, String> typeMapping = new HashMap<>();
    
    /** 版本特定的类型映射，key为最低版本号 */
    protected Map<String, Map<String, String>> versionSpecificTypeMappings = new HashMap<>();
    
    public AbstractPostgreSQLCompatibleExtractor() {
        initializeDefaultTypeMapping();
        initializeVersionSpecificMappings();
    }
    
    /**
     * 初始化默认类型映射
     */
    protected void initializeDefaultTypeMapping() {
        // 数字类型
        typeMapping.put("smallint", "Short");
        typeMapping.put("int2", "Short");
        typeMapping.put("integer", "Integer");
        typeMapping.put("int", "Integer");
        typeMapping.put("int4", "Integer");
        typeMapping.put("bigint", "Long");
        typeMapping.put("int8", "Long");
        typeMapping.put("real", "Float");
        typeMapping.put("float4", "Float");
        typeMapping.put("double precision", "Double");
        typeMapping.put("float8", "Double");
        typeMapping.put("numeric", "BigDecimal");
        typeMapping.put("decimal", "BigDecimal");
        typeMapping.put("serial", "Integer");
        typeMapping.put("bigserial", "Long");
        typeMapping.put("smallserial", "Short");
        
        // 字符串类型
        typeMapping.put("character varying", "String");
        typeMapping.put("varchar", "String");
        typeMapping.put("character", "String");
        typeMapping.put("char", "String");
        typeMapping.put("text", "String");
        typeMapping.put("name", "String");
        
        // 日期时间类型
        typeMapping.put("date", "LocalDate");
        typeMapping.put("time", "LocalTime");
        typeMapping.put("time without time zone", "LocalTime");
        typeMapping.put("time with time zone", "OffsetTime");
        typeMapping.put("timestamp", "LocalDateTime");
        typeMapping.put("timestamp without time zone", "LocalDateTime");
        typeMapping.put("timestamp with time zone", "OffsetDateTime");
        typeMapping.put("interval", "String");
        
        // 布尔类型
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("bool", "Boolean");
        
        // 二进制类型
        typeMapping.put("bytea", "byte[]");
        
        // UUID类型
        typeMapping.put("uuid", "UUID");
        
        // 网络类型
        typeMapping.put("inet", "String");
        typeMapping.put("cidr", "String");
        typeMapping.put("macaddr", "String");
        typeMapping.put("macaddr8", "String");
        
        // JSON类型
        typeMapping.put("json", "String");
        typeMapping.put("jsonb", "String");
        
        // XML类型
        typeMapping.put("xml", "String");
        
        // 数组类型 (简化处理)
        typeMapping.put("array", "Object[]");
        typeMapping.put("_int4", "Integer[]");
        typeMapping.put("_int8", "Long[]");
        typeMapping.put("_text", "String[]");
        typeMapping.put("_varchar", "String[]");
    }
    
    /**
     * 初始化版本特定的类型映射
     * 子类可覆盖此方法添加版本特定映射
     */
    protected void initializeVersionSpecificMappings() {
        // PostgreSQL 10+ 支持 identity column
        Map<String, String> pg10Mappings = new HashMap<>();
        versionSpecificTypeMappings.put("10", pg10Mappings);
        
        // PostgreSQL 12+ JSON路径查询增强
        Map<String, String> pg12Mappings = new HashMap<>();
        versionSpecificTypeMappings.put("12", pg12Mappings);
        
        // PostgreSQL 14+ MULTIRANGE类型
        Map<String, String> pg14Mappings = new HashMap<>();
        pg14Mappings.put("int4multirange", "String");
        pg14Mappings.put("int8multirange", "String");
        pg14Mappings.put("nummultirange", "String");
        pg14Mappings.put("datemultirange", "String");
        pg14Mappings.put("tsmultirange", "String");
        pg14Mappings.put("tstzmultirange", "String");
        versionSpecificTypeMappings.put("14", pg14Mappings);
    }
    
    /**
     * 获取数据库版本信息
     */
    protected String getDatabaseVersion(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductVersion();
    }
    
    /**
     * 解析版本号中的主版本号
     */
    protected int getMajorVersion(String version) {
        if (version == null) return 0;
        try {
            String[] parts = version.split("\\.");
            return Integer.parseInt(parts[0].replaceAll("\\D.*", ""));
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 解析版本号中的次版本号
     */
    protected int getMinorVersion(String version) {
        if (version == null) return 0;
        try {
            String[] parts = version.split("\\.");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1].replaceAll("\\D.*", ""));
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.postgresql.Driver";
    }
    
    @Override
    public String getDefaultTestQuery() {
        return "SELECT 1";
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseName = connection.getCatalog();
            String version = getDatabaseVersion(connection);
            
            DatabaseMetadata dbMetadata = DatabaseMetadata.builder()
                    .databaseName(databaseName)
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(version)
                    .majorVersion(getMajorVersion(version))
                    .minorVersion(getMinorVersion(version))
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(true)
                    .build();
            
            extractCharsetInfo(connection, databaseName, dbMetadata);
            return dbMetadata;
        }
    }
    
    /**
     * 提取字符集信息，子类可覆盖
     */
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        // 默认实现，子类可覆盖
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        List<SchemaMetadata> schemas = new ArrayList<>();
        String sql = getSchemasQuery();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);  // 30秒查询超时
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schemas.add(SchemaMetadata.builder()
                            .schemaName(rs.getString("schema_name"))
                            .databaseName(databaseName)
                            .comment(rs.getString("comment"))
                            .build());
                }
            }
        }
        return schemas;
    }
    
    /**
     * 获取查询Schema列表的SQL
     */
    protected String getSchemasQuery() {
        return "SELECT n.nspname as schema_name, " +
               "pg_catalog.obj_description(n.oid, 'pg_namespace') as comment " +
               "FROM pg_catalog.pg_namespace n " +
               "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
               "ORDER BY n.nspname";
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "public";
        String sql = getTablesQuery();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);  // 30秒查询超时
            pstmt.setString(1, databaseName);
            pstmt.setString(2, targetSchema);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = buildTableMetadata(rs, databaseName, targetSchema);
                    table.setColumns(extractColumns(connection, databaseName, targetSchema, table.getTableName()));
                    table.setPrimaryKey(findPrimaryKey(connection, targetSchema, table.getTableName()));
                    tables.add(table);
                }
            }
        }
        return tables;
    }
    
    /**
     * 获取查询表列表的SQL
     */
    protected String getTablesQuery() {
        return "SELECT t.table_name, pg_catalog.obj_description(pgc.oid, 'pg_class') as comment " +
               "FROM information_schema.tables t " +
               "LEFT JOIN pg_catalog.pg_class pgc ON pgc.relname = t.table_name " +
               "WHERE t.table_catalog = ? AND t.table_schema = ?";
    }
    
    /**
     * 构建表元数据
     */
    protected TableMetadata buildTableMetadata(ResultSet rs, String databaseName, String schemaName) throws SQLException {
        return TableMetadata.builder()
                .tableName(rs.getString("table_name"))
                .databaseName(databaseName)
                .schemaName(schemaName)
                .comment(rs.getString("comment"))
                .build();
    }
    
    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "public";
        String sql = getColumnsQuery();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);  // 30秒查询超时
            pstmt.setString(1, databaseName);
            pstmt.setString(2, targetSchema);
            pstmt.setString(3, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                String version = getDatabaseVersion(connection);
                while (rs.next()) {
                    columns.add(buildColumnMetadata(rs, databaseName, targetSchema, tableName, version));
                }
            }
        }
        return columns;
    }
    
    /**
     * 获取查询列信息的SQL
     */
    protected String getColumnsQuery() {
        return "SELECT c.column_name, c.data_type, c.is_nullable, c.column_default, c.ordinal_position, " +
               "pgd.description as comment " +
               "FROM information_schema.columns c " +
               "LEFT JOIN pg_catalog.pg_statio_all_tables st ON st.relname = c.table_name AND st.schemaname = c.table_schema " +
               "LEFT JOIN pg_catalog.pg_description pgd ON pgd.objoid = st.relid AND pgd.objsubid = c.ordinal_position " +
               "WHERE c.table_catalog = ? AND c.table_schema = ? AND c.table_name = ? " +
               "ORDER BY c.ordinal_position";
    }
    
    /**
     * 构建列元数据
     */
    protected ColumnMetadata buildColumnMetadata(ResultSet rs, String databaseName, String schemaName, String tableName, String version) throws SQLException {
        String dataType = rs.getString("data_type");
        String columnDefault = rs.getString("column_default");
        boolean isAutoIncrement = columnDefault != null && columnDefault.contains("nextval");
        
        return ColumnMetadata.builder()
                .columnName(rs.getString("column_name"))
                .tableName(tableName)
                .schemaName(schemaName)
                .databaseName(databaseName)
                .comment(rs.getString("comment"))
                .dataType(dataType)
                .nullable("YES".equalsIgnoreCase(rs.getString("is_nullable")))
                .defaultValue(columnDefault)
                .autoIncrement(isAutoIncrement)
                .ordinalPosition(rs.getInt("ordinal_position"))
                .javaType(mapToJavaType(dataType, version))
                .build();
    }
    
    @Override
    public String getTableComment(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "public";
        String sql = "SELECT pg_catalog.obj_description(pgc.oid, 'pg_class') as comment " +
                     "FROM pg_catalog.pg_class pgc " +
                     "JOIN pg_catalog.pg_namespace pgn ON pgn.oid = pgc.relnamespace " +
                     "WHERE pgc.relname = ? AND pgn.nspname = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(10);  // 10秒查询超时
            pstmt.setString(1, tableName);
            pstmt.setString(2, targetSchema);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("comment");
                }
            }
        }
        return null;
    }
    
    /**
     * 查找表的主键
     */
    protected String findPrimaryKey(Connection connection, String schemaName, String tableName) throws SQLException {
        String sql = "SELECT a.attname as column_name " +
                     "FROM pg_index i " +
                     "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                     "WHERE i.indrelid = (? || '.' || ?)::regclass AND i.indisprimary LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(10);  // 10秒查询超时
            pstmt.setString(1, schemaName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("column_name");
                }
            }
        } catch (SQLException e) {
            // 部分兼容数据库可能不支持regclass，忽略错误
            log.debug("Failed to find primary key for {}.{}: {}", schemaName, tableName, e.getMessage());
        }
        return null;
    }
    
    /**
     * 将SQL类型映射到Java类型，支持版本特定映射
     */
    protected String mapToJavaType(String sqlType, String version) {
        if (sqlType == null) return "Object";
        String type = sqlType.toLowerCase();
        
        // 首先检查版本特定映射
        for (Map.Entry<String, Map<String, String>> entry : versionSpecificTypeMappings.entrySet()) {
            if (isVersionGreaterOrEqual(version, entry.getKey())) {
                String javaType = entry.getValue().get(type);
                if (javaType != null) return javaType;
            }
        }
        
        // 然后使用默认映射
        return typeMapping.getOrDefault(type, "Object");
    }
    
    /**
     * 比较版本号
     */
    protected boolean isVersionGreaterOrEqual(String currentVersion, String requiredVersion) {
        if (currentVersion == null || requiredVersion == null) return false;
        
        try {
            String[] current = currentVersion.split("\\.");
            String[] required = requiredVersion.split("\\.");
            
            for (int i = 0; i < Math.min(current.length, required.length); i++) {
                int c = Integer.parseInt(current[i].replaceAll("\\D.*", ""));
                int r = Integer.parseInt(required[i].replaceAll("\\D.*", ""));
                if (c > r) return true;
                if (c < r) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
}
