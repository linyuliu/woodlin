package com.mumu.woodlin.common.datasource.spi.base;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;

/**
 * MySQL兼容数据库的抽象提取器基类
 * <p>
 * 为MySQL兼容的数据库（如MySQL、MariaDB、TiDB、DM8等）提供可继承的基础实现。
 * 支持版本特定的类型映射和SQL差异处理。
 * </p>
 * <p>
 * 版本差异支持示例：
 * <ul>
 *   <li>MySQL 5.7 vs 8.0: DATETIME精度、JSON类型支持等</li>
 *   <li>MariaDB: SEQUENCE支持、JSON处理差异</li>
 *   <li>TiDB: 分布式特性相关元数据</li>
 * </ul>
 * </p>
 * <p>
 * 性能优化：
 * <ul>
 *   <li>默认使用 SHOW TABLES / SHOW FULL COLUMNS 等原生命令提取元数据，性能更好</li>
 *   <li>可通过 useNativeCommands 配置切换到 information_schema 查询方式</li>
 * </ul>
 * </p>
 *
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public abstract class AbstractMySQLCompatibleExtractor implements DatabaseMetadataExtractor {

    /** 基础类型映射 */
    protected Map<String, String> typeMapping = new HashMap<>();

    /** 版本特定的类型映射，key为最低版本号 */
    protected Map<String, Map<String, String>> versionSpecificTypeMappings = new HashMap<>();

    /** 是否使用原生命令（SHOW/DESC）提取元数据，默认为true以获得更好的性能
     * -- SETTER --
     *  设置是否使用原生MySQL命令（SHOW/DESC）提取元数据
     *  <p>
     *  原生命令（SHOW TABLES, SHOW FULL COLUMNS等）通常比查询 information_schema 更快，
     *  尤其是在表数量较多的数据库中。
     *  </p>
     *
     *
     * -- GETTER --
     *  获取是否使用原生命令
     *
     */
    @Getter
    @Setter
    protected boolean useNativeCommands = true;

    public AbstractMySQLCompatibleExtractor() {
        initializeDefaultTypeMapping();
        initializeVersionSpecificMappings();
    }

    /**
     * 初始化默认类型映射
     */
    protected void initializeDefaultTypeMapping() {
        // 数字类型
        typeMapping.put("tinyint", "Byte");
        typeMapping.put("smallint", "Short");
        typeMapping.put("mediumint", "Integer");
        typeMapping.put("int", "Integer");
        typeMapping.put("integer", "Integer");
        typeMapping.put("bigint", "Long");
        typeMapping.put("float", "Float");
        typeMapping.put("double", "Double");
        typeMapping.put("decimal", "BigDecimal");
        typeMapping.put("numeric", "BigDecimal");

        // 字符串类型
        typeMapping.put("char", "String");
        typeMapping.put("varchar", "String");
        typeMapping.put("tinytext", "String");
        typeMapping.put("text", "String");
        typeMapping.put("mediumtext", "String");
        typeMapping.put("longtext", "String");

        // 日期时间类型
        typeMapping.put("date", "LocalDate");
        typeMapping.put("time", "LocalTime");
        typeMapping.put("datetime", "LocalDateTime");
        typeMapping.put("timestamp", "LocalDateTime");
        typeMapping.put("year", "Integer");

        // 二进制类型
        typeMapping.put("binary", "byte[]");
        typeMapping.put("varbinary", "byte[]");
        typeMapping.put("tinyblob", "byte[]");
        typeMapping.put("blob", "byte[]");
        typeMapping.put("mediumblob", "byte[]");
        typeMapping.put("longblob", "byte[]");

        // 布尔类型
        typeMapping.put("bit", "Boolean");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("bool", "Boolean");

        // 枚举和集合
        typeMapping.put("enum", "String");
        typeMapping.put("set", "String");
    }

    /**
     * 初始化版本特定的类型映射
     * 子类可覆盖此方法添加版本特定映射
     */
    protected void initializeVersionSpecificMappings() {
        // MySQL 5.7+ JSON支持
        Map<String, String> mysql57Mappings = new HashMap<>();
        mysql57Mappings.put("json", "String");
        versionSpecificTypeMappings.put("5.7", mysql57Mappings);

        // MySQL 8.0+ 增强特性
        Map<String, String> mysql80Mappings = new HashMap<>();
        mysql80Mappings.put("json", "String");
        // 8.0支持更精确的datetime
        versionSpecificTypeMappings.put("8.0", mysql80Mappings);
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
        if (version == null) {
            return 0;
        }
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
        if (version == null) {
            return 0;
        }
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
        return "com.mysql.cj.jdbc.Driver";
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
                    .supportsSchemas(supportsSchemas())
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
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) {
        // MySQL不支持Schema概念，返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        if (useNativeCommands) {
            return extractTablesNative(connection, databaseName);
        }
        return extractTablesFromInfoSchema(connection, databaseName);
    }

    /**
     * 使用原生SHOW命令提取表列表（性能更好）
     * <p>
     * 使用 SHOW TABLE STATUS 命令获取表信息，相比查询 information_schema.TABLES 更快。
     * </p>
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @return 表元数据列表
     * @throws SQLException SQL异常
     */
    protected List<TableMetadata> extractTablesNative(Connection connection, String databaseName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        String version = getDatabaseVersion(connection);

        // 使用 SHOW TABLE STATUS 获取表的详细信息（包括注释、引擎等）
        String sql = "SHOW TABLE STATUS FROM `" + escapeSqlIdentifier(databaseName) + "`";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String tableName = rs.getString("Name");
                String tableComment = rs.getString("Comment");
                String engine = rs.getString("Engine");
                String collation = rs.getString("Collation");
                String createTime = rs.getString("Create_time");
                String updateTime = rs.getString("Update_time");

                // 判断表类型：如果Engine为null，可能是VIEW
                String tableType = (engine != null) ? "BASE TABLE" : "VIEW";

                TableMetadata table = TableMetadata.builder()
                        .tableName(tableName)
                        .databaseName(databaseName)
                        .comment(tableComment)
                        .tableType(tableType)
                        .engine(engine)
                        .collation(collation)
                        .createTime(createTime)
                        .updateTime(updateTime)
                        .build();

                // 提取列信息，同时获取主键（主键信息在SHOW FULL COLUMNS结果的Key字段中）
                List<ColumnMetadata> columns = extractColumnsNative(connection, databaseName, tableName, version);
                table.setColumns(columns);
                // 从列信息中找出主键列
                table.setPrimaryKey(findPrimaryKeyFromColumns(columns));
                tables.add(table);
            }
        }

        return tables;
    }

    /**
     * 从列列表中查找主键列名
     * <p>
     * 优化：直接从已提取的列元数据中获取主键信息，避免额外的数据库查询。
     * </p>
     *
     * @param columns 列元数据列表
     * @return 主键列名，如果没有主键则返回null
     */
    protected String findPrimaryKeyFromColumns(List<ColumnMetadata> columns) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        return columns.stream()
                .filter(col -> Boolean.TRUE.equals(col.getPrimaryKey()))
                .map(ColumnMetadata::getColumnName)
                .findFirst()
                .orElse(null);
    }

    /**
     * 使用 information_schema 提取表列表（兼容模式）
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @return 表元数据列表
     * @throws SQLException SQL异常
     */
    protected List<TableMetadata> extractTablesFromInfoSchema(Connection connection, String databaseName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        String sql = getTablesQuery();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = buildTableMetadata(rs, databaseName);
                    table.setColumns(extractColumns(connection, databaseName, null, table.getTableName()));
                    table.setPrimaryKey(findPrimaryKey(connection, databaseName, table.getTableName()));
                    tables.add(table);
                }
            }
        }

        return tables;
    }

    /**
     * 获取查询表列表的SQL，子类可覆盖以支持不同版本
     */
    protected String getTablesQuery() {
        return "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE " +
               "FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?";
    }

    /**
     * 构建表元数据
     */
    protected TableMetadata buildTableMetadata(ResultSet rs, String databaseName) throws SQLException {
        return TableMetadata.builder()
                .tableName(rs.getString("TABLE_NAME"))
                .databaseName(databaseName)
                .comment(rs.getString("TABLE_COMMENT"))
                .tableType(rs.getString("TABLE_TYPE"))
                .build();
    }

    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        if (useNativeCommands) {
            String version = getDatabaseVersion(connection);
            return extractColumnsNative(connection, databaseName, tableName, version);
        }
        return extractColumnsFromInfoSchema(connection, databaseName, tableName);
    }

    /**
     * 使用原生SHOW命令提取列信息（性能更好）
     * <p>
     * 使用 SHOW FULL COLUMNS FROM table 命令获取列信息，相比查询 information_schema.COLUMNS 更快。
     * </p>
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param version 数据库版本
     * @return 列元数据列表
     * @throws SQLException SQL异常
     */
    protected List<ColumnMetadata> extractColumnsNative(Connection connection, String databaseName, String tableName, String version) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();

        // 使用 SHOW FULL COLUMNS 获取列的详细信息（包括注释）
        String sql = "SHOW FULL COLUMNS FROM `" + escapeSqlIdentifier(tableName) + "` FROM `" + escapeSqlIdentifier(databaseName) + "`";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int ordinalPosition = 1;
            while (rs.next()) {
                String columnName = rs.getString("Field");
                String columnType = rs.getString("Type");
                String dataType = parseDataType(columnType);
                String collation = rs.getString("Collation");
                String isNullable = rs.getString("Null");
                String columnKey = rs.getString("Key");
                String defaultValue = rs.getString("Default");
                String extra = rs.getString("Extra");
                String comment = rs.getString("Comment");

                boolean isPrimaryKey = "PRI".equals(columnKey);
                boolean isAutoIncrement = extra != null && extra.toLowerCase().contains("auto_increment");

                ColumnMetadata column = ColumnMetadata.builder()
                        .columnName(columnName)
                        .tableName(tableName)
                        .databaseName(databaseName)
                        .comment(comment)
                        .dataType(dataType)
                        .nullable("YES".equalsIgnoreCase(isNullable))
                        .defaultValue(defaultValue)
                        .primaryKey(isPrimaryKey)
                        .autoIncrement(isAutoIncrement)
                        .ordinalPosition(ordinalPosition++)
                        .javaType(mapToJavaType(dataType, version))
                        .build();

                columns.add(column);
            }
        }

        return columns;
    }

    /**
     * 使用 information_schema 提取列信息（兼容模式）
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 列元数据列表
     * @throws SQLException SQL异常
     */
    protected List<ColumnMetadata> extractColumnsFromInfoSchema(Connection connection, String databaseName, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        String sql = getColumnsQuery();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                String version = getDatabaseVersion(connection);
                while (rs.next()) {
                    columns.add(buildColumnMetadata(rs, databaseName, tableName, version));
                }
            }
        }

        return columns;
    }

    /**
     * 解析列类型，从完整类型中提取基础数据类型
     * 例如: varchar(255) -> varchar, int(11) -> int, decimal(10,2) -> decimal
     *
     * @param columnType 完整列类型
     * @return 基础数据类型
     */
    protected String parseDataType(String columnType) {
        if (columnType == null) {
            return null;
        }
        // 移除括号及其内容，以及unsigned等修饰符
        int parenIndex = columnType.indexOf('(');
        if (parenIndex > 0) {
            return columnType.substring(0, parenIndex).trim().toLowerCase();
        }
        // 移除 unsigned, zerofill 等修饰符
        String type = columnType.split("\\s+")[0].toLowerCase();
        return type;
    }

    /**
     * 转义SQL标识符，防止SQL注入
     *
     * @param identifier SQL标识符（表名、数据库名等）
     * @return 转义后的标识符
     */
    protected String escapeSqlIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        // 替换反引号为双反引号以防止SQL注入
        return identifier.replace("`", "``");
    }

    /**
     * 获取查询列信息的SQL，子类可覆盖以支持不同版本
     */
    protected String getColumnsQuery() {
        return "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, " +
               "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION " +
               "FROM information_schema.COLUMNS " +
               "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
    }

    /**
     * 构建列元数据
     */
    protected ColumnMetadata buildColumnMetadata(ResultSet rs, String databaseName, String tableName, String version) throws SQLException {
        String dataType = rs.getString("DATA_TYPE");
        boolean isPrimaryKey = "PRI".equals(rs.getString("COLUMN_KEY"));
        String extra = rs.getString("EXTRA");
        boolean isAutoIncrement = extra != null && extra.toLowerCase().contains("auto_increment");

        return ColumnMetadata.builder()
                .columnName(rs.getString("COLUMN_NAME"))
                .tableName(tableName)
                .databaseName(databaseName)
                .comment(rs.getString("COLUMN_COMMENT"))
                .dataType(dataType)
                .nullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")))
                .defaultValue(rs.getString("COLUMN_DEFAULT"))
                .primaryKey(isPrimaryKey)
                .autoIncrement(isAutoIncrement)
                .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                .javaType(mapToJavaType(dataType, version))
                .build();
    }

    @Override
    public String getTableComment(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        if (useNativeCommands) {
            return getTableCommentNative(connection, databaseName, tableName);
        }
        return getTableCommentFromInfoSchema(connection, databaseName, tableName);
    }

    /**
     * 使用原生SHOW命令获取表注释（性能更好）
     * <p>
     * 使用 SHOW TABLE STATUS LIKE 'tablename' 获取表注释，比查询 information_schema 更快。
     * </p>
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 表注释
     * @throws SQLException SQL异常
     */
    protected String getTableCommentNative(Connection connection, String databaseName, String tableName) throws SQLException {
        String sql = "SHOW TABLE STATUS FROM `" + escapeSqlIdentifier(databaseName) + "` LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Comment");
                }
            }
        }
        return null;
    }

    /**
     * 使用 information_schema 获取表注释（兼容模式）
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 表注释
     * @throws SQLException SQL异常
     */
    protected String getTableCommentFromInfoSchema(Connection connection, String databaseName, String tableName) throws SQLException {
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

    /**
     * 查找表的主键
     */
    protected String findPrimaryKey(Connection connection, String databaseName, String tableName) throws SQLException {
        if (useNativeCommands) {
            return findPrimaryKeyNative(connection, databaseName, tableName);
        }
        return findPrimaryKeyFromInfoSchema(connection, databaseName, tableName);
    }

    /**
     * 使用原生SHOW命令查找表的主键（性能更好）
     * <p>
     * 使用 SHOW KEYS FROM table WHERE Key_name = 'PRIMARY' 获取主键信息。
     * </p>
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 主键列名
     * @throws SQLException SQL异常
     */
    protected String findPrimaryKeyNative(Connection connection, String databaseName, String tableName) throws SQLException {
        String sql = "SHOW KEYS FROM `" + escapeSqlIdentifier(tableName) + "` FROM `" + escapeSqlIdentifier(databaseName) + "` WHERE Key_name = 'PRIMARY'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("Column_name");
            }
        }
        return null;
    }

    /**
     * 使用 information_schema 查找表的主键（兼容模式）
     *
     * @param connection 数据库连接
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 主键列名
     * @throws SQLException SQL异常
     */
    protected String findPrimaryKeyFromInfoSchema(Connection connection, String databaseName, String tableName) throws SQLException {
        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = 'PRI' LIMIT 1";

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
     * 将SQL类型映射到Java类型，支持版本特定映射
     */
    protected String mapToJavaType(String sqlType, String version) {
        if (sqlType == null) {
            return "Object";
        }
        String type = sqlType.toLowerCase();

        // 首先检查版本特定映射
        for (Map.Entry<String, Map<String, String>> entry : versionSpecificTypeMappings.entrySet()) {
            if (isVersionGreaterOrEqual(version, entry.getKey())) {
                String javaType = entry.getValue().get(type);
                if (javaType != null) {
                    return javaType;
                }
            }
        }

        // 然后使用默认映射
        return typeMapping.getOrDefault(type, "Object");
    }

    /**
     * 比较版本号
     */
    protected boolean isVersionGreaterOrEqual(String currentVersion, String requiredVersion) {
        if (currentVersion == null || requiredVersion == null) {
            return false;
        }

        try {
            String[] current = currentVersion.split("\\.");
            String[] required = requiredVersion.split("\\.");

            for (int i = 0; i < Math.min(current.length, required.length); i++) {
                int c = Integer.parseInt(current[i].replaceAll("\\D.*", ""));
                int r = Integer.parseInt(required[i].replaceAll("\\D.*", ""));
                if (c > r) {
                    return true;
                }
                if (c < r) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否支持Schema概念
     */
    protected boolean supportsSchemas() {
        return false;
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
