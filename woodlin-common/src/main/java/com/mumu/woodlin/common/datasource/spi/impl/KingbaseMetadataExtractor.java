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
import lombok.Getter;
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
    @Getter
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

        public static CompatibilityMode fromCode(String code) {
            if (code == null) {
                return PG;
            }
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
     * KingBase使用 database_mode 参数来标识兼容模式。
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
     * 使用 KingBase 原生系统表提取列信息（完整版）
     * <p>
     * 使用 information_schema.columns 结合 PostgreSQL 系统表进行元数据提取。
     * 这是参考 PostgreSQL 官方元数据查询方式改进后的 KingBase 兼容版本。
     * </p>
     * <p>
     * SQL查询说明：
     * 1. 基础信息：从 information_schema.columns 获取标准的列信息（表名、列名、数据类型、是否可空等）
     * 2. 类型详情：从 pg_attribute 和 pg_type 获取类型的详细信息（atttypmod, attndims, typelem, typlen, typtype）
     * 3. 真实类型：使用 format_type(b.atttypid, b.atttypmod) 获取完整的类型定义，包括 with time zone 等修饰符
     * 4. 数组类型：通过 typelem 关联 pg_type 获取数组元素类型信息（elem_schema, elem_name）
     * 5. 注释信息：使用 col_description 函数获取列注释
     * 6. 默认值：使用 column_default 获取列的默认值
     * 7. 排序规则：通过 attcollation 关联 pg_collation 获取排序规则信息
     * 8. 表类型：获取 relkind 判断是表、视图还是其他类型
     * </p>
     * <p>
     * 关于类型的说明：
     * - col.data_type: information_schema 中的高层抽象类型，可能显示为 USER-DEFINED
     * - et.typname: pg_type 中的原始类型名称，如 timestamptz, timestamp
     * - format_type(): 格式化后的完整类型，如 "timestamp with time zone", "character varying(255)"
     * 本实现使用 format_type() 作为主要类型，因为它最准确且包含所有类型修饰符
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

        // 使用 information_schema.columns 结合 pg_* 系统表的完整元数据查询
        // 这个 SQL 参考了 PostgreSQL 官方的元数据提取方式，并针对 KingBase 进行了优化
        String sql = "SELECT " +
                     // 基础列信息（来自 information_schema）
                     "  col.table_schema AS schema_name, " +              // Schema名称
                     "  col.table_name, " +                               // 表名
                     "  col.column_name, " +                              // 列名
                     "  col.character_maximum_length, " +                 // 字符类型最大长度
                     "  col.is_nullable, " +                              // 是否可空
                     "  col.numeric_precision, " +                        // 数值精度
                     "  col.numeric_scale, " +                            // 数值小数位数
                     "  col.datetime_precision, " +                       // 日期时间精度
                     "  col.ordinal_position, " +                         // 列顺序位置
                     // 类型信息（三种类型表示，用于兼容和精确性）
                     "  col.data_type AS info_data_type, " +              // information_schema 的标准类型（用于兼容）
                     "  et.typname AS pg_type_name, " +                   // PostgreSQL/KingBase 真实类型名（如 timestamptz）
                     "  format_type(b.atttypid, b.atttypmod) AS real_type, " +  // 格式化后的完整类型（最准确，如 "timestamp with time zone"）
                     // 类型详细信息（来自 pg_attribute 和 pg_type）
                     "  b.atttypmod, " +                                  // 类型修饰符（包含长度、精度等信息）
                     "  b.attndims, " +                                   // 数组维度数
                     "  et.typelem, " +                                   // 数组元素类型OID
                     "  et.typlen, " +                                    // 类型长度
                     "  et.typtype, " +                                   // 类型类别（b=基础类型, c=复合类型, e=枚举类型等）
                     // 数组元素类型信息
                     "  nbt.nspname AS elem_schema, " +                   // 数组元素类型所在schema
                     "  bt.typname AS elem_name, " +                      // 数组元素类型名称
                     "  b.atttypid, " +                                   // 列类型OID
                     // 注释和默认值
                     "  col_description(c.oid, col.ordinal_position) AS comment, " +  // 列注释
                     "  col.column_default AS col_default, " +            // 列默认值
                     "  col.generation_expression, " +                    // 生成列表达式
                     "  b.attacl, " +                                     // 列级别访问控制列表
                     // 排序规则信息
                     "  colnsp.nspname AS collation_schema_name, " +      // 排序规则schema
                     "  coll.collname, " +                                // 排序规则名称
                     // 表类型
                     "  c.relkind, " +                                    // 表类型（r=表, v=视图, m=物化视图等）
                     "  b.attfdwoptions AS foreign_options " +            // 外部表选项
                     "FROM information_schema.columns AS col " +
                     // 关联 pg_namespace 获取 schema 信息
                     "JOIN pg_namespace ns ON ns.nspname = col.table_schema " +
                     // 关联 pg_class 获取表信息
                     "JOIN pg_class c ON col.table_name = c.relname AND c.relnamespace = ns.oid " +
                     // 关联 pg_attribute 获取列属性详细信息（改为 JOIN 确保获取到类型信息）
                     "JOIN pg_attribute b ON b.attrelid = c.oid AND b.attname = col.column_name " +
                     // 关联 pg_type 获取列类型信息（改为 JOIN 确保获取到类型信息）
                     "JOIN pg_type et ON et.oid = b.atttypid " +
                     // 关联 pg_attrdef 获取默认值定义
                     "LEFT JOIN pg_attrdef a ON c.oid = a.adrelid AND col.ordinal_position = a.adnum " +
                     // 关联 pg_collation 获取排序规则
                     "LEFT JOIN pg_collation coll ON coll.oid = b.attcollation " +
                     "LEFT JOIN pg_namespace colnsp ON coll.collnamespace = colnsp.oid " +
                     // 关联序列依赖（用于判断自增列）
                     "LEFT JOIN (" +
                     "  pg_depend dep " +
                     "  JOIN pg_sequence seq ON dep.classid = 'pg_class'::regclass::oid " +
                     "    AND dep.objid = seq.seqrelid " +
                     "    AND dep.deptype = 'i'::\"char\" " +
                     ") ON dep.refclassid = 'pg_class'::regclass::oid " +
                     "  AND dep.refobjid = c.oid " +
                     "  AND dep.refobjsubid = b.attnum " +
                     // 关联数组元素类型（如果是数组类型）
                     "LEFT JOIN pg_type bt ON et.typelem = bt.oid " +
                     "LEFT JOIN pg_namespace nbt ON bt.typnamespace = nbt.oid " +
                     // WHERE 条件
                     "WHERE col.table_schema = ? " +
                     "  AND col.table_name = ? " +
                     // 排序
                     "ORDER BY col.ordinal_position";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setQueryTimeout(30);
            pstmt.setString(1, targetSchema);
            pstmt.setString(2, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 获取列的数据类型 - 优先使用 format_type 的结果，这是最准确的类型表示
                    // format_type 会返回完整的类型定义，如 "timestamp with time zone", "character varying(255)" 等
                    String realType = rs.getString("real_type");
                    String pgTypeName = rs.getString("pg_type_name");
                    String infoDataType = rs.getString("info_data_type");
                    
                    // 使用 format_type 的结果作为主要类型，如果为空则回退到 pg_type_name，最后是 info_data_type
                    String dataType = realType != null ? realType : (pgTypeName != null ? pgTypeName : infoDataType);
                    
                    String columnDefault = rs.getString("col_default");
                    
                    // 判断是否自增列：检查默认值是否包含 nextval 或 seq_
                    boolean isAutoIncrement = columnDefault != null &&
                                             (columnDefault.contains("nextval") ||
                                              columnDefault.contains("seq_"));

                    // 构建列元数据对象
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("column_name"))
                            .tableName(tableName)
                            .schemaName(targetSchema)
                            .databaseName(databaseName)
                            .dataType(dataType)  // 使用 format_type 的结果，包含完整的类型修饰符
                            .nullable("YES".equalsIgnoreCase(rs.getString("is_nullable")))
                            .ordinalPosition(rs.getInt("ordinal_position"))
                            .comment(rs.getString("comment"))
                            .defaultValue(columnDefault)
                            .autoIncrement(isAutoIncrement)
                            .javaType(mapKingBaseTypeToJava(dataType))
                            .build();

                    columns.add(column);
                }
            }
        } catch (SQLException e) {
            // 如果标准查询失败，尝试使用 pg_catalog 作为后备
            log.warn("KingBase 标准元数据查询失败，尝试使用 pg_catalog 作为后备: {}", e.getMessage());
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
                     "  t.typname AS pg_type_name, " +
                     "  a.attnotnull AS not_null, " +
                     "  a.attnum AS ordinal_position, " +
                     "  pg_catalog.format_type(a.atttypid, a.atttypmod) AS real_type, " +
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
                    // 优先使用 format_type 的结果
                    String realType = rs.getString("real_type");
                    String pgTypeName = rs.getString("pg_type_name");
                    String dataType = realType != null ? realType : pgTypeName;
                    
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
     * <p>
     * 注意：此方法现在接收 format_type() 的结果，格式如 "timestamp with time zone", "character varying(255)" 等。
     * 需要提取基础类型名称进行映射。
     * </p>
     *
     * @param kingbaseType KingBase 数据类型（可能包含修饰符，如 "timestamp with time zone"）
     * @return Java 类型
     */
    private String mapKingBaseTypeToJava(String kingbaseType) {
        if (kingbaseType == null) {
            return "Object";
        }

        String type = kingbaseType.toLowerCase();
        
        // 处理 format_type() 返回的格式化类型
        // 提取基础类型名称（去掉长度、精度等修饰符）
        String baseType = extractBaseType(type);

        // 使用父类的类型映射
        String javaType = typeMapping.get(baseType);
        if (javaType != null) {
            return javaType;
        }

        // KingBase 特有类型映射（不在父类映射中）
        return switch (baseType) {
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
    
    /**
     * 从 format_type() 返回的格式化类型中提取基础类型名称
     * <p>
     * 例如：
     * - "timestamp with time zone" → "timestamp with time zone" (保持完整)
     * - "timestamp without time zone" → "timestamp without time zone" (保持完整)
     * - "character varying(255)" → "character varying"
     * - "numeric(10,2)" → "numeric"
     * - "integer" → "integer"
     * </p>
     *
     * @param formattedType format_type() 返回的格式化类型
     * @return 基础类型名称
     */
    private String extractBaseType(String formattedType) {
        if (formattedType == null) {
            return null;
        }
        
        String type = formattedType.trim();
        
        // 对于带 with/without time zone 的类型，保持完整（这是重要的类型修饰符）
        if (type.contains("with time zone") || type.contains("without time zone")) {
            // 提取 "timestamp with time zone" 或 "time with time zone" 等
            if (type.startsWith("timestamp")) {
                return type.contains("with time zone") ? "timestamp with time zone" : "timestamp without time zone";
            } else if (type.startsWith("time")) {
                return type.contains("with time zone") ? "time with time zone" : "time without time zone";
            }
        }
        
        // 去掉括号中的长度/精度信息，如 "character varying(255)" → "character varying"
        int parenIndex = type.indexOf('(');
        if (parenIndex > 0) {
            type = type.substring(0, parenIndex).trim();
        }
        
        // 去掉数组标记 []
        if (type.endsWith("[]")) {
            type = type.substring(0, type.length() - 2).trim();
        }
        
        return type;
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
