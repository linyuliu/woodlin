# Woodlin SQL2API Module

## 概述

SQL2API模块提供了将SQL查询转换为RESTful API的能力，支持多种数据库系统。本模块采用可扩展的SPI（Service Provider Interface）架构，便于添加新的数据库支持。

## 核心功能

1. **多数据库支持**：开箱即用支持MySQL、PostgreSQL、Oracle
2. **元数据提取**：自动提取数据库、表、列的元数据信息（包括注释）
3. **SQL构建器**：基于元数据动态生成CRUD SQL语句
4. **SQL DSL解析**：支持参数化SQL和动态条件
5. **元数据缓存**：基于Spring Cache的元数据缓存机制
6. **动态数据源**：支持多数据源动态切换

## 已支持的数据库

| 数据库 | 版本要求 | Schema支持 | 注释提取 | 实现类 |
|--------|---------|-----------|----------|--------|
| MySQL | 5.7+ | ❌ (使用Catalog) | ✅ | MySQLMetadataExtractor |
| PostgreSQL | 10+ | ✅ | ✅ | PostgreSQLMetadataExtractor |
| Oracle | 11g+ | ✅ | ✅ | OracleMetadataExtractor |

## 架构设计

### SPI接口：DatabaseMetadataExtractor

所有数据库元数据提取器必须实现此接口：

```java
public interface DatabaseMetadataExtractor {
    // 获取数据库类型标识
    String getDatabaseType();
    
    // 判断是否支持该数据源
    boolean supports(Connection connection) throws SQLException;
    
    // 提取数据库元数据
    DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException;
    
    // 提取Schema信息
    List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException;
    
    // 提取表信息
    List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException;
    
    // 提取列信息
    List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException;
    
    // 获取表注释
    String getTableComment(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException;
    
    // 获取优先级（数字越小优先级越高）
    default int getPriority() {
        return 100;
    }
}
```

## 添加新数据库支持

### 步骤1：创建元数据提取器

在 `com.mumu.woodlin.sql2api.spi.impl` 包下创建新的提取器类：

```java
package com.mumu.woodlin.sql2api.spi.impl;

import lombok.extern.slf4j.Slf4j;
import com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor;
// ... 其他导入

@Slf4j
public class DM8MetadataExtractor implements DatabaseMetadataExtractor {
    
    @Override
    public String getDatabaseType() {
        return "DM8"; // 达梦数据库
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("dm");
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            return DatabaseMetadata.builder()
                    .databaseName(connection.getCatalog())
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(metaData.getDatabaseProductVersion())
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(true) // 根据数据库特性设置
                    .build();
        }
    }
    
    // 实现其他方法...
}
```

### 步骤2：注册提取器

有两种方式注册新的提取器：

#### 方式1：在配置类中手动注册（推荐）

编辑 `Sql2ApiConfiguration.java`：

```java
@Bean
public List<DatabaseMetadataExtractor> databaseMetadataExtractors() {
    List<DatabaseMetadataExtractor> extractors = new ArrayList<>();
    
    // 注册内置提取器
    extractors.add(new MySQLMetadataExtractor());
    extractors.add(new PostgreSQLMetadataExtractor());
    extractors.add(new OracleMetadataExtractor());
    extractors.add(new DM8MetadataExtractor()); // 添加新的提取器
    
    // SPI加载外部提取器
    ServiceLoader<DatabaseMetadataExtractor> serviceLoader = ServiceLoader.load(DatabaseMetadataExtractor.class);
    for (DatabaseMetadataExtractor extractor : serviceLoader) {
        extractors.add(extractor);
    }
    
    return extractors;
}
```

#### 方式2：使用Java SPI机制

1. 创建文件：`src/main/resources/META-INF/services/com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor`
2. 在文件中添加实现类的全限定名：
```
com.mumu.woodlin.sql2api.spi.impl.DM8MetadataExtractor
```

### 步骤3：添加数据库驱动依赖

在 `woodlin-system-sql2api/pom.xml` 中添加驱动依赖：

```xml
<!-- DM8 Driver (example) -->
<dependency>
    <groupId>com.dameng</groupId>
    <artifactId>DmJdbcDriver18</artifactId>
    <version>8.1.2.192</version>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

## 关键实现要点

### 1. Schema处理

不同数据库对Schema的支持不同：

- **MySQL**：不支持Schema概念，使用Catalog（数据库名）
- **PostgreSQL**：完整支持Schema，默认使用public
- **Oracle**：Schema即用户，通常使用当前登录用户

实现时需要注意：
```java
// 处理Schema为null的情况
String targetSchema = (schemaName != null && !schemaName.isEmpty()) 
        ? schemaName 
        : getDefaultSchema(connection);
```

### 2. 注释提取

不同数据库存储注释的方式不同：

**MySQL:**
```sql
SELECT TABLE_COMMENT 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
```

**PostgreSQL:**
```sql
SELECT pg_catalog.obj_description(pgc.oid, 'pg_class') as comment
FROM pg_catalog.pg_class pgc
JOIN pg_catalog.pg_namespace pgn ON pgn.oid = pgc.relnamespace
WHERE pgc.relname = ? AND pgn.nspname = ?
```

**Oracle:**
```sql
SELECT COMMENTS 
FROM ALL_TAB_COMMENTS 
WHERE OWNER = ? AND TABLE_NAME = ?
```

### 3. 数据类型映射

实现 `mapToJavaType()` 方法，将数据库类型映射到Java类型：

```java
private String mapToJavaType(String dataType) {
    String type = dataType.toLowerCase();
    
    // 整数类型
    if (type.contains("int")) {
        if (type.contains("bigint")) return "Long";
        if (type.contains("smallint")) return "Short";
        return "Integer";
    }
    
    // 浮点数类型
    if (type.contains("decimal") || type.contains("numeric")) {
        return "BigDecimal";
    }
    
    // 日期时间类型
    if (type.contains("date")) return "LocalDate";
    if (type.contains("timestamp")) return "LocalDateTime";
    
    // 字符串类型
    if (type.contains("char") || type.contains("text")) {
        return "String";
    }
    
    return "Object";
}
```

### 4. 主键识别

不同数据库查询主键的方式：

**通用方式（使用JDBC元数据）:**
```java
DatabaseMetaData metaData = connection.getMetaData();
ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName);
```

**数据库特定查询（性能更好）:**

MySQL:
```sql
SELECT COLUMN_NAME 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = 'PRI'
```

PostgreSQL:
```sql
SELECT a.attname 
FROM pg_index i 
JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)
WHERE i.indrelid = ?::regclass AND i.indisprimary
```

Oracle:
```sql
SELECT acc.COLUMN_NAME
FROM ALL_CONSTRAINTS ac
JOIN ALL_CONS_COLUMNS acc ON ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME
WHERE ac.OWNER = ? AND ac.TABLE_NAME = ? AND ac.CONSTRAINT_TYPE = 'P'
```

### 5. 自增列识别

**MySQL:**
```sql
-- EXTRA字段包含 'auto_increment'
SELECT EXTRA FROM information_schema.COLUMNS
```

**PostgreSQL:**
```sql
-- column_default包含 'nextval' 或 使用GENERATED
SELECT column_default FROM information_schema.columns
```

**Oracle:**
```sql
-- Oracle 12c+ 支持IDENTITY列
SELECT data_default FROM all_tab_columns
```

## SQL构建器使用示例

### 生成SELECT语句

```java
// 构建SELECT查询
String sql = SqlBuilder.buildSelectSql(tableMetadata, 
    SqlBuilder.SelectConfig.builder()
        .column("id")
        .column("name")
        .column("email")
        .whereCondition("status = #{status}")
        .whereCondition("created_at > #{startDate}")
        .orderBy("created_at DESC")
        .limit(10)
        .offset(0)
        .build()
);

// 生成的SQL:
// SELECT id, name, email
// FROM schema.table_name
// WHERE status = #{status} AND created_at > #{startDate}
// ORDER BY created_at DESC
// LIMIT 10 OFFSET 0
```

### 生成INSERT语句

```java
String sql = SqlBuilder.buildInsertSql(tableMetadata,
    SqlBuilder.InsertConfig.builder()
        .column("name")
        .column("email")
        .column("status")
        .build()
);

// 生成的SQL:
// INSERT INTO schema.table_name (name, email, status)
// VALUES (#{name}, #{email}, #{status})
```

### 生成UPDATE语句

```java
String sql = SqlBuilder.buildUpdateSql(tableMetadata,
    SqlBuilder.UpdateConfig.builder()
        .column("name")
        .column("email")
        .whereCondition("id = #{id}")
        .build()
);

// 生成的SQL:
// UPDATE schema.table_name
// SET name = #{name}, email = #{email}
// WHERE id = #{id}
```

## 元数据缓存

模块使用Spring Cache进行元数据缓存：

```java
@Cacheable(value = "databaseMetadata", key = "#datasourceName")
public DatabaseMetadata getDatabaseMetadata(String datasourceName);

@Cacheable(value = "databaseTables", key = "#datasourceName")
public List<TableMetadata> getTables(String datasourceName);

@Cacheable(value = "tableColumns", key = "#datasourceName + ':' + #tableName")
public List<ColumnMetadata> getColumns(String datasourceName, String tableName);

@CacheEvict(value = {"databaseMetadata", "databaseTables", "tableColumns"}, allEntries = true)
public void refreshMetadataCache(String datasourceName);
```

缓存配置在 `application.yml` 中：

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
```

## 测试建议

### 单元测试

为每个提取器创建单元测试：

```java
@Test
void testExtractDatabaseMetadata() {
    DatabaseMetadata metadata = extractor.extractDatabaseMetadata(dataSource);
    
    assertNotNull(metadata);
    assertNotNull(metadata.getDatabaseName());
    assertNotNull(metadata.getDatabaseProductName());
}

@Test
void testExtractTables() {
    List<TableMetadata> tables = extractor.extractTables(connection, "testdb", "public");
    
    assertNotNull(tables);
    assertFalse(tables.isEmpty());
    
    TableMetadata table = tables.get(0);
    assertNotNull(table.getTableName());
    assertNotNull(table.getColumns());
}
```

### 集成测试

使用Testcontainers进行集成测试：

```java
@Testcontainers
class PostgreSQLMetadataExtractorTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void testWithRealDatabase() {
        DataSource dataSource = createDataSource(postgres);
        PostgreSQLMetadataExtractor extractor = new PostgreSQLMetadataExtractor();
        
        DatabaseMetadata metadata = extractor.extractDatabaseMetadata(dataSource);
        assertEquals("testdb", metadata.getDatabaseName());
    }
}
```

## 常见问题

### Q1: 如何处理数据库方言差异？

A: 通过DatabaseMetadataExtractor接口的实现，每个数据库可以有自己的SQL查询逻辑。使用数据库特定的系统表或信息模式。

### Q2: 如何支持LIMIT/OFFSET在不同数据库中的差异？

A: 目前SqlBuilder默认使用MySQL/PostgreSQL的LIMIT语法。对于Oracle，需要在具体实现中转换为ROWNUM或FETCH FIRST语法。

### Q3: 如何处理复杂的数据类型？

A: 在mapToJavaType方法中，对于复杂类型（如JSON、ARRAY、自定义类型），可以映射为String或Object，由应用层进一步处理。

### Q4: 性能优化建议？

A: 
1. 启用元数据缓存
2. 使用批量查询减少数据库往返
3. 只提取需要的字段和表
4. 考虑使用连接池

## 扩展资源

- [MySQL Information Schema](https://dev.mysql.com/doc/refman/8.0/en/information-schema.html)
- [PostgreSQL System Catalogs](https://www.postgresql.org/docs/current/catalogs.html)
- [Oracle Data Dictionary](https://docs.oracle.com/en/database/oracle/oracle-database/19/refrn/about-static-data-dictionary-views.html)

## 贡献指南

如果你添加了新的数据库支持，请：

1. 确保实现了所有DatabaseMetadataExtractor接口方法
2. 添加完整的注释和JavaDoc
3. 创建单元测试和集成测试
4. 更新此README文档
5. 在Sql2ApiConfiguration中注册新的提取器

## 许可证

本模块遵循项目整体许可证。
