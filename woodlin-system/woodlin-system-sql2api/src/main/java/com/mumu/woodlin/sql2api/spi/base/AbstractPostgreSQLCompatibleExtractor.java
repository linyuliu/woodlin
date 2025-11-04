package com.mumu.woodlin.sql2api.spi.base;

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

import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.SchemaMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor;

/**
 * PostgreSQL兼容数据库的抽象提取器基类
 * 
 * @author mumu
 * @description 为PostgreSQL兼容的数据库（如KingbaseES、Vastbase等）提供可继承的基础实现
 * @since 2025-01-04
 */
@Slf4j
public abstract class AbstractPostgreSQLCompatibleExtractor implements DatabaseMetadataExtractor {
    
    protected Map<String, String> typeMapping = new HashMap<>();
    protected Map<String, Map<String, String>> versionSpecificTypeMappings = new HashMap<>();
    
    public AbstractPostgreSQLCompatibleExtractor() {
        initializeDefaultTypeMapping();
    }
    
    protected void initializeDefaultTypeMapping() {
        typeMapping.put("smallint", "Short");
        typeMapping.put("integer", "Integer");
        typeMapping.put("bigint", "Long");
        typeMapping.put("real", "Float");
        typeMapping.put("double precision", "Double");
        typeMapping.put("numeric", "BigDecimal");
        typeMapping.put("varchar", "String");
        typeMapping.put("text", "String");
        typeMapping.put("date", "LocalDate");
        typeMapping.put("timestamp", "LocalDateTime");
        typeMapping.put("boolean", "Boolean");
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseName = connection.getCatalog();
            
            DatabaseMetadata dbMetadata = DatabaseMetadata.builder()
                    .databaseName(databaseName)
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(metaData.getDatabaseProductVersion())
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(true)
                    .build();
            
            extractCharsetInfo(connection, databaseName, dbMetadata);
            return dbMetadata;
        }
    }
    
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        // 默认实现，子类可覆盖
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        List<SchemaMetadata> schemas = new ArrayList<>();
        String sql = getSchemasQuery();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                schemas.add(SchemaMetadata.builder()
                        .schemaName(rs.getString("schema_name"))
                        .databaseName(databaseName)
                        .comment(rs.getString("comment"))
                        .build());
            }
        }
        return schemas;
    }
    
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
    
    protected String getTablesQuery() {
        return "SELECT t.table_name, pg_catalog.obj_description(pgc.oid, 'pg_class') as comment " +
               "FROM information_schema.tables t " +
               "LEFT JOIN pg_catalog.pg_class pgc ON pgc.relname = t.table_name " +
               "WHERE t.table_catalog = ? AND t.table_schema = ?";
    }
    
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
    
    protected String getColumnsQuery() {
        return "SELECT c.column_name, c.data_type, c.is_nullable, c.column_default, c.ordinal_position, " +
               "pgd.description as comment " +
               "FROM information_schema.columns c " +
               "LEFT JOIN pg_catalog.pg_description pgd ON pgd.objsubid = c.ordinal_position " +
               "WHERE c.table_catalog = ? AND c.table_schema = ? AND c.table_name = ? " +
               "ORDER BY c.ordinal_position";
    }
    
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
    
    protected String findPrimaryKey(Connection connection, String schemaName, String tableName) throws SQLException {
        String sql = "SELECT a.attname as column_name " +
                     "FROM pg_index i " +
                     "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                     "WHERE i.indrelid = (? || '.' || ?)::regclass AND i.indisprimary LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schemaName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("column_name");
                }
            }
        }
        return null;
    }
    
    protected String getDatabaseVersion(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductVersion();
    }
    
    protected String mapToJavaType(String sqlType, String version) {
        if (sqlType == null) return "Object";
        String type = sqlType.toLowerCase();
        
        for (Map.Entry<String, Map<String, String>> entry : versionSpecificTypeMappings.entrySet()) {
            if (isVersionGreaterOrEqual(version, entry.getKey())) {
                String javaType = entry.getValue().get(type);
                if (javaType != null) return javaType;
            }
        }
        
        return typeMapping.getOrDefault(type, "Object");
    }
    
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
