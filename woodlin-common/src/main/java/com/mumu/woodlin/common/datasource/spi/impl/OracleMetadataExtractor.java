package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;

/**
 * Oracle数据库元数据提取器
 * <p>
 * Oracle数据库的元数据提取实现，支持11g及以上版本。
 * </p>
 * <p>
 * 版本差异支持：
 * <ul>
 *   <li>Oracle 11g: 基础功能</li>
 *   <li>Oracle 12c+: IDENTITY列、JSON类型、Row Limiting子句</li>
 *   <li>Oracle 18c+: 私有临时表</li>
 *   <li>Oracle 19c+: JSON_TABLE增强</li>
 *   <li>Oracle 21c+: JSON数据类型原生支持</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public class OracleMetadataExtractor implements DatabaseMetadataExtractor {
    
    // 静态常量：数据类型集合，避免重复创建
    private static final Set<String> INTEGER_TYPES = Set.of("INTEGER", "INT");
    private static final Set<String> FLOAT_TYPES = Set.of("FLOAT", "BINARY_FLOAT");
    private static final Set<String> DOUBLE_TYPES = Set.of("DOUBLE", "BINARY_DOUBLE");
    private static final Set<String> STRING_TYPES = Set.of(
        "VARCHAR2", "VARCHAR", "NVARCHAR2", "CHAR", "NCHAR", "CLOB", "NCLOB", "LONG"
    );
    private static final Set<String> BINARY_TYPES = Set.of("BLOB", "RAW", "LONG RAW", "BFILE");
    private static final Set<String> ROWID_TYPES = Set.of("ROWID", "UROWID");
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.ORACLE;
    }
    
    @Override
    public String getMinSupportedVersion() {
        return "11.2";
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "oracle.jdbc.OracleDriver";
    }
    
    @Override
    public String getDefaultTestQuery() {
        return "SELECT 1 FROM DUAL";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("oracle");
    }
    
    @Override
    public boolean supportsVersion(Connection connection, int majorVersion, int minorVersion) throws SQLException {
        if (!supports(connection)) {
            return false;
        }
        // 支持 Oracle 11g+
        return majorVersion >= 11;
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取当前用户名（Oracle中用户即Schema）
            String schemaName = metaData.getUserName();
            String version = metaData.getDatabaseProductVersion();
            
            DatabaseMetadata dbMetadata = DatabaseMetadata.builder()
                    .databaseName(schemaName) // Oracle中通常使用Schema名称
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(version)
                    .majorVersion(getMajorVersion(version))
                    .minorVersion(getMinorVersion(version))
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(true) // Oracle支持Schema
                    .build();
            
            // 获取数据库字符集
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT VALUE FROM NLS_DATABASE_PARAMETERS WHERE PARAMETER = 'NLS_CHARACTERSET'")) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dbMetadata.setCharset(rs.getString("VALUE"));
                    }
                }
            }
            
            // 获取国家字符集
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT VALUE FROM NLS_DATABASE_PARAMETERS WHERE PARAMETER = 'NLS_NCHAR_CHARACTERSET'")) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dbMetadata.setCollation(rs.getString("VALUE"));
                    }
                }
            }
            
            return dbMetadata;
        }
    }
    
    private int getMajorVersion(String version) {
        if (version == null) return 0;
        try {
            // Oracle版本格式: "Oracle Database 19c Enterprise Edition Release 19.0.0.0.0 - Production"
            // 或者: "19.0.0.0.0"
            String[] parts = version.split("\\.");
            for (String part : parts) {
                String cleaned = part.replaceAll("\\D.*", "");
                if (!cleaned.isEmpty()) {
                    return Integer.parseInt(cleaned);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
    
    private int getMinorVersion(String version) {
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
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        List<SchemaMetadata> schemas = new ArrayList<>();
        
        // Oracle中的Schema即用户
        String sql = "SELECT USERNAME as schema_name, CREATED " +
                     "FROM ALL_USERS " +
                     "WHERE USERNAME NOT IN ('SYS', 'SYSTEM', 'OUTLN', 'DIP', 'ORACLE_OCM', " +
                     "'DBSNMP', 'APPQOSSYS', 'WMSYS', 'EXFSYS', 'CTXSYS', 'XDB', 'ANONYMOUS', " +
                     "'MDSYS', 'ORDSYS', 'ORDDATA', 'SI_INFORMTN_SCHEMA') " +
                     "ORDER BY USERNAME";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                SchemaMetadata schema = SchemaMetadata.builder()
                        .schemaName(rs.getString("schema_name"))
                        .databaseName(databaseName)
                        .build();
                schemas.add(schema);
            }
        }
        
        return schemas;
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        
        // 如果没有指定schema，使用当前用户
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) 
                ? schemaName.toUpperCase() 
                : connection.getMetaData().getUserName();
        
        String sql = "SELECT t.TABLE_NAME, " +
                     "CASE WHEN t.TABLE_TYPE = 'TABLE' THEN 'TABLE' ELSE t.TABLE_TYPE END as TABLE_TYPE, " +
                     "tc.COMMENTS " +
                     "FROM ALL_TABLES t " +
                     "LEFT JOIN ALL_TAB_COMMENTS tc ON t.OWNER = tc.OWNER AND t.TABLE_NAME = tc.TABLE_NAME " +
                     "WHERE t.OWNER = ? " +
                     "ORDER BY t.TABLE_NAME";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = TableMetadata.builder()
                            .tableName(rs.getString("TABLE_NAME"))
                            .databaseName(databaseName)
                            .schemaName(targetSchema)
                            .comment(rs.getString("COMMENTS"))
                            .tableType(rs.getString("TABLE_TYPE"))
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
        
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) 
                ? schemaName.toUpperCase() 
                : connection.getMetaData().getUserName();
        
        String sql = "SELECT " +
                     "c.COLUMN_NAME, " +
                     "c.DATA_TYPE, " +
                     "c.DATA_LENGTH, " +
                     "c.DATA_PRECISION, " +
                     "c.DATA_SCALE, " +
                     "c.NULLABLE, " +
                     "c.DATA_DEFAULT, " +
                     "c.COLUMN_ID, " +
                     "cc.COMMENTS, " +
                     "CASE WHEN pk.COLUMN_NAME IS NOT NULL THEN 'Y' ELSE 'N' END as IS_PRIMARY_KEY " +
                     "FROM ALL_TAB_COLUMNS c " +
                     "LEFT JOIN ALL_COL_COMMENTS cc ON c.OWNER = cc.OWNER AND c.TABLE_NAME = cc.TABLE_NAME AND c.COLUMN_NAME = cc.COLUMN_NAME " +
                     "LEFT JOIN ( " +
                     "    SELECT acc.OWNER, acc.TABLE_NAME, acc.COLUMN_NAME " +
                     "    FROM ALL_CONSTRAINTS ac " +
                     "    JOIN ALL_CONS_COLUMNS acc ON ac.OWNER = acc.OWNER AND ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME " +
                     "    WHERE ac.CONSTRAINT_TYPE = 'P' " +
                     ") pk ON c.OWNER = pk.OWNER AND c.TABLE_NAME = pk.TABLE_NAME AND c.COLUMN_NAME = pk.COLUMN_NAME " +
                     "WHERE c.OWNER = ? AND c.TABLE_NAME = ? " +
                     "ORDER BY c.COLUMN_ID";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            pstmt.setString(2, tableName.toUpperCase());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataType = rs.getString("DATA_TYPE");
                    String dataDefault = rs.getString("DATA_DEFAULT");
                    boolean isAutoIncrement = false;
                    
                    // Oracle 12c及以上支持IDENTITY列
                    if (dataDefault != null && dataDefault.trim().toUpperCase().contains("IDENTITY")) {
                        isAutoIncrement = true;
                    }
                    
                    boolean isPrimaryKey = "Y".equals(rs.getString("IS_PRIMARY_KEY"));
                    
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("COLUMN_NAME"))
                            .tableName(tableName)
                            .schemaName(targetSchema)
                            .databaseName(databaseName)
                            .comment(rs.getString("COMMENTS"))
                            .dataType(dataType)
                            .columnSize(rs.getInt("DATA_LENGTH"))
                            .decimalDigits(rs.getInt("DATA_SCALE"))
                            .nullable("Y".equalsIgnoreCase(rs.getString("NULLABLE")))
                            .defaultValue(dataDefault != null ? dataDefault.trim() : null)
                            .primaryKey(isPrimaryKey)
                            .autoIncrement(isAutoIncrement)
                            .ordinalPosition(rs.getInt("COLUMN_ID"))
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
        String targetSchema = (schemaName != null && !schemaName.isEmpty()) 
                ? schemaName.toUpperCase() 
                : connection.getMetaData().getUserName();
        
        String sql = "SELECT COMMENTS FROM ALL_TAB_COMMENTS WHERE OWNER = ? AND TABLE_NAME = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            pstmt.setString(2, tableName.toUpperCase());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("COMMENTS");
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
        String sql = "SELECT acc.COLUMN_NAME " +
                     "FROM ALL_CONSTRAINTS ac " +
                     "JOIN ALL_CONS_COLUMNS acc ON ac.OWNER = acc.OWNER AND ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME " +
                     "WHERE ac.OWNER = ? AND ac.TABLE_NAME = ? AND ac.CONSTRAINT_TYPE = 'P' " +
                     "ORDER BY acc.POSITION " +
                     "FETCH FIRST 1 ROW ONLY";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schemaName);
            pstmt.setString(2, tableName.toUpperCase());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("COLUMN_NAME");
                }
            }
        }
        
        return null;
    }
    
    /**
     * 将Oracle数据类型映射到Java类型
     */
    private String mapToJavaType(String dataType) {
        if (dataType == null) {
            return "Object";
        }
        
        String type = dataType.toUpperCase();
        
        // 使用静态常量Set优化多条件判断 - 避免多次equals调用和重复创建Set
        // 数字类型
        if (type.equals("NUMBER")) {
            return "BigDecimal"; // Oracle NUMBER可以表示任意精度的数字
        } else if (INTEGER_TYPES.contains(type)) {
            return "Integer";
        } else if (FLOAT_TYPES.contains(type)) {
            return "Float";
        } else if (DOUBLE_TYPES.contains(type)) {
            return "Double";
        }
        
        // 字符串类型
        else if (STRING_TYPES.contains(type)) {
            return "String";
        }
        
        // 日期时间类型
        else if (type.equals("DATE")) {
            return "LocalDateTime"; // Oracle DATE包含日期和时间
        } else if (type.equals("TIMESTAMP") || type.contains("TIMESTAMP")) {
            if (type.contains("WITH TIME ZONE")) {
                return "OffsetDateTime";
            } else {
                return "LocalDateTime";
            }
        }
        
        // 二进制类型
        else if (BINARY_TYPES.contains(type)) {
            return "byte[]";
        }
        
        // ROWID类型
        else if (ROWID_TYPES.contains(type)) {
            return "String";
        }
        
        // XML类型
        else if (type.equals("XMLTYPE")) {
            return "String";
        }
        
        // JSON类型 (Oracle 21c+)
        else if (type.equals("JSON")) {
            return "String";
        }
        
        return "Object";
    }
}
