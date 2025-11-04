package com.mumu.woodlin.sql2api.spi.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.SchemaMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor;

/**
 * PostgreSQL数据库元数据提取器
 * 
 * @author mumu
 * @description PostgreSQL数据库的元数据提取实现，支持10.x及以上版本
 * @since 2025-01-04
 */
@Slf4j
public class PostgreSQLMetadataExtractor implements DatabaseMetadataExtractor {
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("postgresql");
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取当前数据库名
            String databaseName = connection.getCatalog();
            
            DatabaseMetadata dbMetadata = DatabaseMetadata.builder()
                    .databaseName(databaseName)
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(metaData.getDatabaseProductVersion())
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(true) // PostgreSQL支持Schema
                    .build();
            
            // 获取数据库编码
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT pg_encoding_to_char(encoding) as encoding, " +
                    "datcollate as collation " +
                    "FROM pg_database WHERE datname = ?")) {
                pstmt.setString(1, databaseName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dbMetadata.setCharset(rs.getString("encoding"));
                        dbMetadata.setCollation(rs.getString("collation"));
                    }
                }
            }
            
            return dbMetadata;
        }
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        List<SchemaMetadata> schemas = new ArrayList<>();
        
        String sql = "SELECT n.nspname as schema_name, " +
                     "pg_catalog.obj_description(n.oid, 'pg_namespace') as comment " +
                     "FROM pg_catalog.pg_namespace n " +
                     "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                     "AND n.nspname NOT LIKE 'pg_temp_%' " +
                     "AND n.nspname NOT LIKE 'pg_toast_temp_%' " +
                     "ORDER BY n.nspname";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                SchemaMetadata schema = SchemaMetadata.builder()
                        .schemaName(rs.getString("schema_name"))
                        .databaseName(databaseName)
                        .comment(rs.getString("comment"))
                        .build();
                schemas.add(schema);
            }
        }
        
        return schemas;
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        
        // 如果没有指定schema，使用public
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "public";
        
        String sql = "SELECT t.table_name, " +
                     "CASE WHEN t.table_type = 'BASE TABLE' THEN 'TABLE' ELSE t.table_type END as table_type, " +
                     "pg_catalog.obj_description(pgc.oid, 'pg_class') as comment, " +
                     "pgc.reltuples::bigint as row_count " +
                     "FROM information_schema.tables t " +
                     "LEFT JOIN pg_catalog.pg_class pgc ON pgc.relname = t.table_name " +
                     "LEFT JOIN pg_catalog.pg_namespace pgn ON pgn.oid = pgc.relnamespace AND pgn.nspname = t.table_schema " +
                     "WHERE t.table_catalog = ? AND t.table_schema = ? " +
                     "ORDER BY t.table_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, targetSchema);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = TableMetadata.builder()
                            .tableName(rs.getString("table_name"))
                            .databaseName(databaseName)
                            .schemaName(targetSchema)
                            .comment(rs.getString("comment"))
                            .tableType(rs.getString("table_type"))
                            .build();
                    
                    // 提取列信息
                    table.setColumns(extractColumns(connection, databaseName, targetSchema, table.getTableName()));
                    
                    // 查找主键
                    table.setPrimaryKey(findPrimaryKey(connection, targetSchema, table.getTableName()));
                    
                    tables.add(table);
                }
            }
        }
        
        return tables;
    }
    
    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) ? schemaName : "public";
        
        String sql = "SELECT " +
                     "c.column_name, " +
                     "c.data_type, " +
                     "c.udt_name, " +
                     "c.character_maximum_length, " +
                     "c.numeric_precision, " +
                     "c.numeric_scale, " +
                     "c.is_nullable, " +
                     "c.column_default, " +
                     "c.ordinal_position, " +
                     "pgd.description as comment " +
                     "FROM information_schema.columns c " +
                     "LEFT JOIN pg_catalog.pg_statio_all_tables st ON c.table_schema = st.schemaname AND c.table_name = st.relname " +
                     "LEFT JOIN pg_catalog.pg_description pgd ON pgd.objoid = st.relid AND pgd.objsubid = c.ordinal_position " +
                     "WHERE c.table_catalog = ? AND c.table_schema = ? AND c.table_name = ? " +
                     "ORDER BY c.ordinal_position";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, targetSchema);
            pstmt.setString(3, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataType = rs.getString("data_type");
                    String columnDefault = rs.getString("column_default");
                    boolean isAutoIncrement = columnDefault != null && 
                                             (columnDefault.contains("nextval") || columnDefault.contains("GENERATED"));
                    
                    // 检查是否为主键
                    boolean isPrimaryKey = isPrimaryKeyColumn(connection, targetSchema, tableName, rs.getString("column_name"));
                    
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("column_name"))
                            .tableName(tableName)
                            .schemaName(targetSchema)
                            .databaseName(databaseName)
                            .comment(rs.getString("comment"))
                            .dataType(dataType)
                            .columnSize(rs.getInt("character_maximum_length"))
                            .decimalDigits(rs.getInt("numeric_scale"))
                            .nullable("YES".equalsIgnoreCase(rs.getString("is_nullable")))
                            .defaultValue(columnDefault)
                            .primaryKey(isPrimaryKey)
                            .autoIncrement(isAutoIncrement)
                            .ordinalPosition(rs.getInt("ordinal_position"))
                            .javaType(mapToJavaType(dataType, rs.getString("udt_name")))
                            .build();
                    
                    columns.add(column);
                }
            }
        }
        
        return columns;
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
    
    @Override
    public int getPriority() {
        return 10; // 高优先级
    }
    
    /**
     * 查找表的主键
     */
    private String findPrimaryKey(Connection connection, String schemaName, String tableName) throws SQLException {
        String sql = "SELECT a.attname as column_name " +
                     "FROM pg_index i " +
                     "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                     "WHERE i.indrelid = (? || '.' || ?)::regclass " +
                     "AND i.indisprimary " +
                     "ORDER BY a.attnum LIMIT 1";
        
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
    
    /**
     * 检查列是否为主键
     */
    private boolean isPrimaryKeyColumn(Connection connection, String schemaName, String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) as is_pk " +
                     "FROM pg_index i " +
                     "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                     "WHERE i.indrelid = (? || '.' || ?)::regclass " +
                     "AND i.indisprimary " +
                     "AND a.attname = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schemaName);
            pstmt.setString(2, tableName);
            pstmt.setString(3, columnName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("is_pk") > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 将PostgreSQL数据类型映射到Java类型
     */
    private String mapToJavaType(String dataType, String udtName) {
        if (dataType == null && udtName == null) {
            return "Object";
        }
        
        // 优先使用dataType
        String type = (dataType != null ? dataType : udtName).toLowerCase();
        
        // 整数类型
        if (type.equals("smallint") || type.equals("int2")) {
            return "Short";
        } else if (type.equals("integer") || type.equals("int") || type.equals("int4")) {
            return "Integer";
        } else if (type.equals("bigint") || type.equals("int8")) {
            return "Long";
        }
        
        // 浮点数类型
        else if (type.equals("real") || type.equals("float4")) {
            return "Float";
        } else if (type.equals("double precision") || type.equals("float8")) {
            return "Double";
        } else if (type.equals("numeric") || type.equals("decimal")) {
            return "BigDecimal";
        }
        
        // 布尔类型
        else if (type.equals("boolean") || type.equals("bool")) {
            return "Boolean";
        }
        
        // 字符串类型
        else if (type.contains("char") || type.contains("text") || type.equals("varchar")) {
            return "String";
        }
        
        // 日期时间类型
        else if (type.equals("date")) {
            return "LocalDate";
        } else if (type.equals("time") || type.contains("time without")) {
            return "LocalTime";
        } else if (type.equals("timestamp") || type.contains("timestamp without")) {
            return "LocalDateTime";
        } else if (type.contains("timestamp with time zone") || type.equals("timestamptz")) {
            return "OffsetDateTime";
        }
        
        // 二进制类型
        else if (type.equals("bytea")) {
            return "byte[]";
        }
        
        // JSON类型
        else if (type.equals("json") || type.equals("jsonb")) {
            return "String"; // 可以使用JsonNode或自定义对象
        }
        
        // UUID类型
        else if (type.equals("uuid")) {
            return "UUID";
        }
        
        // 数组类型
        else if (type.equals("array") || type.contains("[]")) {
            return "Object[]"; // 可以根据具体情况使用List
        }
        
        return "Object";
    }
}
