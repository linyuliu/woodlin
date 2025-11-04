# SQL2API数据库支持架构设计

## 概述

本文档描述SQL2API模块的数据库继承架构设计，用于支持MySQL、PostgreSQL及其兼容数据库（如TiDB、达梦、人大金仓、瀚高等）。

## 设计目标

1. **代码复用**: 通过继承减少重复代码
2. **灵活扩展**: 轻松添加新的数据库支持
3. **版本兼容**: 处理同一数据库不同版本间的差异
4. **类型映射**: 支持数据库特定的类型映射覆盖

## 架构层次

```
DatabaseMetadataExtractor (接口)
    │
    ├── AbstractMySQLCompatibleExtractor (抽象基类)
    │   ├── MySQLMetadataExtractor
    │   ├── TiDBMetadataExtractor
    │   ├── DM8MetadataExtractor (达梦)
    │   └── MariaDBMetadataExtractor (未来扩展)
    │
    ├── AbstractPostgreSQLCompatibleExtractor (抽象基类)
    │   ├── PostgreSQLMetadataExtractor
    │   ├── KingbaseESMetadataExtractor (人大金仓)
    │   ├── VastbaseMetadataExtractor (瀚高)
    │   └── OpenGaussMetadataExtractor (未来扩展)
    │
    └── OracleMetadataExtractor (独立实现)
```

## 核心抽象类

### AbstractMySQLCompatibleExtractor

为所有MySQL兼容数据库提供基础实现:

**核心功能:**
- 基于information_schema的元数据提取
- Catalog为主的数据库结构（不使用Schema）
- MySQL标准类型映射
- 可覆盖的查询方法和类型映射

**可覆盖方法:**
```java
protected String getTablesQuery()           // 自定义表查询SQL
protected String getColumnsQuery()          // 自定义列查询SQL
protected void extractCharsetInfo(...)     // 字符集信息提取
protected String mapToJavaType(...)        // 类型映射
protected boolean supportsSchemas()        // 是否支持Schema
```

**类型映射机制:**
```java
// 基础类型映射（所有版本共享）
protected Map<String, String> typeMapping

// 版本特定类型映射（key: 最小版本, value: 类型映射表）
protected Map<String, Map<String, String>> versionSpecificTypeMappings
```

### AbstractPostgreSQLCompatibleExtractor

为所有PostgreSQL兼容数据库提供基础实现:

**核心功能:**
- 基于pg_catalog的元数据提取
- Schema为主的数据库结构
- PostgreSQL标准类型映射
- 可覆盖的查询方法

**可覆盖方法:**
```java
protected String getSchemasQuery()         // Schema查询
protected String getTablesQuery()          // 表查询
protected String getColumnsQuery()         // 列查询
protected void extractCharsetInfo(...)     // 字符集信息
```

## 版本处理机制

### 版本比较

```java
protected boolean isVersionGreaterOrEqual(String currentVersion, String requiredVersion)
```

简单的版本号比较，支持 x.y.z 格式。

### 版本特定类型映射

```java
public TiDBMetadataExtractor() {
    super();
    // TiDB 5.0+ 支持JSON类型
    Map<String, String> tidb5Types = new HashMap<>();
    tidb5Types.put("json", "String");
    versionSpecificTypeMappings.put("5.0", tidb5Types);
}
```

查找顺序：
1. 检查所有符合版本要求的映射（从高版本到低版本）
2. 使用第一个匹配的映射
3. 如果没有找到，使用基础类型映射

## 已支持数据库

### MySQL兼容系列

| 数据库 | 继承自 | 优先级 | 特点 |
|--------|--------|--------|------|
| MySQL | AbstractMySQLCompatibleExtractor | 10 | 基础实现 |
| TiDB | AbstractMySQLCompatibleExtractor | 20 | 分布式，支持JSON |
| DM8 (达梦) | AbstractMySQLCompatibleExtractor | 20 | 支持Schema，CLOB/BLOB |

### PostgreSQL兼容系列

| 数据库 | 继承自 | 优先级 | 特点 |
|--------|--------|--------|------|
| PostgreSQL | AbstractPostgreSQLCompatibleExtractor | 10 | 基础实现 |
| KingbaseES (人大金仓) | AbstractPostgreSQLCompatibleExtractor | 20 | 支持nvarchar/nchar |
| Vastbase (瀚高) | AbstractPostgreSQLCompatibleExtractor | 20 | 完全兼容PostgreSQL |

### 独立实现

| 数据库 | 说明 |
|--------|------|
| Oracle | 使用ALL_*视图，Schema即用户 |

## 添加新数据库支持

### 步骤1: 确定兼容性

确定新数据库是MySQL兼容还是PostgreSQL兼容：
- MySQL兼容：使用information_schema，Catalog为主
- PostgreSQL兼容：使用pg_catalog，Schema为主
- 其他：需要独立实现

### 步骤2: 创建提取器类

**MySQL兼容示例:**
```java
public class XxxMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    public XxxMetadataExtractor() {
        super();
        // 添加特定类型映射
        Map<String, String> v1Types = new HashMap<>();
        v1Types.put("custom_type", "String");
        versionSpecificTypeMappings.put("1.0", v1Types);
    }
    
    @Override
    public String getDatabaseType() {
        return "Xxx";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("xxx");
    }
    
    @Override
    public int getPriority() {
        return 20; // 高于通用MySQL
    }
    
    // 可选：覆盖特定方法
    @Override
    protected String getTablesQuery() {
        // 返回数据库特定的查询
        return super.getTablesQuery();
    }
}
```

**PostgreSQL兼容示例:**
```java
public class YyyMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "Yyy";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("yyy");
    }
    
    @Override
    public int getPriority() {
        return 20;
    }
}
```

### 步骤3: 注册提取器

在`Sql2ApiConfiguration.java`中注册:

```java
extractors.add(new XxxMetadataExtractor());
```

或使用SPI机制：创建文件 `META-INF/services/com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor`

## 类型映射最佳实践

### 基础类型映射

在构造函数中调用`super()`后，基础映射已初始化。

### 添加数据库特定类型

```java
public MyDBExtractor() {
    super();
    // 直接添加到基础映射（所有版本共享）
    typeMapping.put("custom_type", "CustomJavaType");
}
```

### 添加版本特定类型

```java
public MyDBExtractor() {
    super();
    
    // 版本1.0的特定类型
    Map<String, String> v1Types = new HashMap<>();
    v1Types.put("new_type", "String");
    versionSpecificTypeMappings.put("1.0", v1Types);
    
    // 版本2.0的特定类型（覆盖v1.0的映射）
    Map<String, String> v2Types = new HashMap<>();
    v2Types.put("new_type", "JsonNode");
    v2Types.put("another_new_type", "UUID");
    versionSpecificTypeMappings.put("2.0", v2Types);
}
```

## 优先级系统

优先级数字越小，优先级越高：

- `10`: 基础数据库（MySQL, PostgreSQL, Oracle）
- `20`: 兼容数据库（TiDB, KingbaseES等）
- `100`: 默认优先级

当多个提取器都支持某个连接时，使用优先级最高的（数字最小的）。

## 处理数据库差异

### Schema支持差异

```java
@Override
protected boolean supportsSchemas() {
    return true; // DM8支持Schema，覆盖MySQL默认行为
}
```

### SQL语法差异

```java
@Override
protected String getColumnsQuery() {
    // 使用数据库特定的SQL
    return "SELECT ... FROM my_custom_schema.columns ...";
}
```

### 字符集信息差异

```java
@Override
protected void extractCharsetInfo(Connection connection, String databaseName, 
                                   DatabaseMetadata dbMetadata) throws SQLException {
    // 数据库特定的字符集查询
    try (Statement stmt = connection.createStatement()) {
        ResultSet rs = stmt.executeQuery("SELECT charset FROM db_info");
        if (rs.next()) {
            dbMetadata.setCharset(rs.getString(1));
        }
    }
}
```

## 测试建议

### 单元测试

```java
@Test
void testDatabaseTypeIdentification() {
    XxxMetadataExtractor extractor = new XxxMetadataExtractor();
    assertEquals("Xxx", extractor.getDatabaseType());
}

@Test
void testSupportsCorrectDatabase() throws SQLException {
    Connection mockConn = mock(Connection.class);
    DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
    
    when(mockConn.getMetaData()).thenReturn(mockMetaData);
    when(mockMetaData.getDatabaseProductName()).thenReturn("Xxx Database");
    
    XxxMetadataExtractor extractor = new XxxMetadataExtractor();
    assertTrue(extractor.supports(mockConn));
}

@Test
void testVersionSpecificTypeMapping() {
    XxxMetadataExtractor extractor = new XxxMetadataExtractor();
    
    // 测试版本1.0的类型映射
    String javaType = extractor.mapToJavaType("custom_type", "1.0.0");
    assertEquals("String", javaType);
    
    // 测试版本2.0的类型映射
    javaType = extractor.mapToJavaType("custom_type", "2.0.0");
    assertEquals("JsonNode", javaType);
}
```

### 集成测试

使用Testcontainers或真实数据库进行测试：

```java
@Test
void testRealDatabaseConnection() {
    DataSource dataSource = createDataSource();
    XxxMetadataExtractor extractor = new XxxMetadataExtractor();
    
    DatabaseMetadata metadata = extractor.extractDatabaseMetadata(dataSource);
    assertNotNull(metadata);
    assertEquals("Xxx", metadata.getDatabaseProductName());
}
```

## 性能考虑

1. **缓存元数据**: 使用Spring Cache缓存提取结果
2. **批量查询**: 尽量减少数据库往返次数
3. **延迟加载**: 只在需要时提取详细信息
4. **连接池**: 使用数据库连接池

## 未来扩展

计划支持的数据库：

- **MySQL兼容**: 
  - OceanBase (MySQL模式)
  - PolarDB (MySQL兼容)
  
- **PostgreSQL兼容**:
  - OpenGauss
  - GaussDB
  - Greenplum

- **其他**:
  - SQL Server
  - DB2
  - ClickHouse
  - 神通数据库

## 最佳实践总结

1. ✅ **优先使用继承**: 减少重复代码
2. ✅ **最小化覆盖**: 只覆盖必要的方法
3. ✅ **版本感知**: 使用版本特定映射处理差异
4. ✅ **优先级明确**: 兼容数据库优先级高于基础数据库
5. ✅ **充分测试**: 单元测试 + 集成测试
6. ✅ **文档完善**: 记录特殊处理和已知限制

## 参考资料

- [MySQL Information Schema](https://dev.mysql.com/doc/refman/8.0/en/information-schema.html)
- [PostgreSQL System Catalogs](https://www.postgresql.org/docs/current/catalogs.html)
- [TiDB MySQL Compatibility](https://docs.pingcap.com/tidb/stable/mysql-compatibility)
- [KingbaseES Documentation](https://www.kingbase.com.cn/)
- [DM8 Documentation](https://www.dameng.com/)
