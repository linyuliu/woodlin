package com.mumu.woodlin.sql2api.spi.impl;

import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.SchemaMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL数据库元数据提取器
 * 
 * @author mumu
 * @description MySQL/MariaDB数据库的元数据提取实现，支持5.x和8.x版本
 * @since 2025-01-01
 */
@Slf4j
public class MySQLMetadataExtractor implements DatabaseMetadataExtractor {
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && 
               (productName.toLowerCase().contains("mysql") || 
                productName.toLowerCase().contains("mariadb"));
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
                    .supportsSchemas(false) // MySQL不支持Schema概念，使用Catalog
                    .build();
            
            // 获取数据库字符集和排序规则
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME " +
                         "FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'")) {
                if (rs.next()) {
                    dbMetadata.setCharset(rs.getString("DEFAULT_CHARACTER_SET_NAME"));
                    dbMetadata.setCollation(rs.getString("DEFAULT_COLLATION_NAME"));
                }
            }
            
            return dbMetadata;
        }
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        // MySQL没有Schema概念，返回空列表
        return new ArrayList<>();
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        
        String sql = "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE, ENGINE, " +
                     "TABLE_COLLATION, CREATE_TIME, UPDATE_TIME " +
                     "FROM information_schema.TABLES " +
                     "WHERE TABLE_SCHEMA = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = TableMetadata.builder()
                            .tableName(rs.getString("TABLE_NAME"))
                            .databaseName(databaseName)
                            .comment(rs.getString("TABLE_COMMENT"))
                            .tableType(rs.getString("TABLE_TYPE"))
                            .engine(rs.getString("ENGINE"))
                            .collation(rs.getString("TABLE_COLLATION"))
                            .createTime(rs.getString("CREATE_TIME"))
                            .updateTime(rs.getString("UPDATE_TIME"))
                            .build();
                    
                    // 提取列信息
                    table.setColumns(extractColumns(connection, databaseName, null, table.getTableName()));
                    
                    // 查找主键
                    table.setPrimaryKey(findPrimaryKey(connection, databaseName, table.getTableName()));
                    
                    tables.add(table);
                }
            }
        }
        
        return tables;
    }
    
    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        
        String sql = "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE, " +
                     "CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, " +
                     "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION " +
                     "FROM information_schema.COLUMNS " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                     "ORDER BY ORDINAL_POSITION";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataType = rs.getString("DATA_TYPE");
                    boolean isPrimaryKey = "PRI".equals(rs.getString("COLUMN_KEY"));
                    boolean isAutoIncrement = rs.getString("EXTRA") != null && 
                                             rs.getString("EXTRA").toLowerCase().contains("auto_increment");
                    
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("COLUMN_NAME"))
                            .tableName(tableName)
                            .databaseName(databaseName)
                            .comment(rs.getString("COLUMN_COMMENT"))
                            .dataType(dataType)
                            .columnSize(rs.getInt("CHARACTER_MAXIMUM_LENGTH"))
                            .decimalDigits(rs.getInt("NUMERIC_SCALE"))
                            .nullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")))
                            .defaultValue(rs.getString("COLUMN_DEFAULT"))
                            .primaryKey(isPrimaryKey)
                            .autoIncrement(isAutoIncrement)
                            .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                            .javaType(mapToJavaType(dataType))
                            .build();
                    
                    columns.add(column);
                }
            }
        }
        
        return columns;
    }
    
    @Override
    public String getTableComment(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        String sql = "SELECT TABLE_COMMENT FROM information_schema.TABLES " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TABLE_COMMENT");
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
    private String findPrimaryKey(Connection connection, String databaseName, String tableName) throws SQLException {
        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = 'PRI' " +
                     "ORDER BY ORDINAL_POSITION LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("COLUMN_NAME");
                }
            }
        }
        
        return null;
    }
    
    /**
     * 将MySQL数据类型映射到Java类型
     */
    private String mapToJavaType(String sqlType) {
        if (sqlType == null) {
            return "Object";
        }
        
        String type = sqlType.toLowerCase();
        
        if (type.contains("int") || type.equals("integer")) {
            if (type.contains("bigint")) {
                return "Long";
            } else if (type.contains("tinyint")) {
                return "Byte";
            } else if (type.contains("smallint")) {
                return "Short";
            }
            return "Integer";
        } else if (type.contains("decimal") || type.contains("numeric")) {
            return "BigDecimal";
        } else if (type.contains("float")) {
            return "Float";
        } else if (type.contains("double")) {
            return "Double";
        } else if (type.contains("date")) {
            return "LocalDate";
        } else if (type.contains("time")) {
            if (type.contains("datetime") || type.contains("timestamp")) {
                return "LocalDateTime";
            }
            return "LocalTime";
        } else if (type.contains("char") || type.contains("text") || type.contains("varchar")) {
            return "String";
        } else if (type.contains("blob") || type.contains("binary")) {
            return "byte[]";
        } else if (type.contains("bit") || type.contains("boolean")) {
            return "Boolean";
        }
        
        return "Object";
    }
}
