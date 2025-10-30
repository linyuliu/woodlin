# 可搜索加密功能使用指南

## 概述

Woodlin 系统提供了强大的可搜索加密功能，允许对数据库中的敏感数据进行加密存储，同时支持**模糊查询**和**精确匹配**，在保证数据安全的同时不影响查询性能。

## 核心特性

- ✅ **确定性加密**：相同明文生成相同密文，支持精确匹配查询
- ✅ **N-gram 索引**：自动生成加密的 N-gram 索引，支持模糊搜索
- ✅ **注解驱动**：使用 `@SearchableField` 注解标记字段，自动加密/解密
- ✅ **高性能**：使用索引优化查询，支持缓存机制
- ✅ **易用性**：无需手动处理加密逻辑，框架自动完成
- ✅ **灵活配置**：支持多种加密参数和搜索策略

## 技术原理

### 确定性加密

使用 AES/CBC 算法，通过从明文和密钥派生的确定性 IV，确保：
- 相同的明文总是产生相同的密文
- 支持数据库的精确匹配查询（使用 `=` 运算符）
- 密文不可逆，保证数据安全

### N-gram 索引

将文本分割为固定长度的子串（默认2个字符）：
- 文本 "张三" → N-grams: ["张三"]
- 文本 "李四四" → N-grams: ["李四", "四四"]
- 每个 N-gram 单独加密后存储在搜索索引字段
- 查询时将关键字分割为 N-grams，加密后在索引中匹配

**示例：**
```
明文：张三丰
N-grams (size=2): ["张三", "三丰"]
加密后：["aGd8f3...", "kJh9s2..."]
存储：aGd8f3...,kJh9s2...

搜索："三" → N-grams: ["三"]
加密后：["aGd8..."]
SQL: WHERE real_name_search_index LIKE '%aGd8...%'
```

### 关于解密的重要说明

**设计理念：** 本系统的可搜索加密采用确定性加密方案（从明文派生IV），这意味着：

✅ **优势：**
- 相同明文总是产生相同密文，支持数据库精确匹配
- 结合N-gram索引，支持加密数据的模糊搜索
- 数据在数据库和应用层保持加密状态，提高安全性

⚠️ **限制：**
- 解密需要原始明文作为参数（用于重建IV）
- 这是有意的设计选择，鼓励数据在应用层保持加密

🔒 **安全说明：**
- 使用AES-256/CBC模式实现确定性加密
- CBC模式理论上存在padding oracle攻击风险，但在本实现中风险极低：
  - 没有公开的解密API端点
  - 解密很少使用（数据保持加密状态）
  - 没有暴露解密错误信息
  - 不支持批量解密尝试
- 为了可搜索性，无法使用GCM等AEAD模式（需要随机nonce）
- 适用于需要搜索功能的敏感数据加密场景

**实际应用建议：**

1. **数据展示脱敏**：对于需要显示的敏感数据，使用脱敏而非完全解密
   ```java
   // 姓名脱敏：张三 -> 张*
   String masked = name.charAt(0) + "*".repeat(name.length() - 1);
   ```

2. **混合加密方案**：对需要完全解密的字段，使用标准AES/CBC（随机IV）
   - 使用 `ApiEncryptionUtil` 进行可解密的字段加密
   - 使用 `SearchableEncryptionUtil` 进行可搜索的字段加密

3. **哈希验证**：保存明文的哈希值，用于验证和重建
   ```java
   // 保存时记录哈希
   data.setNameHash(DigestUtils.sha256Hex(name));
   // 解密验证时使用哈希匹配
   ```

## 快速开始

### 1. 配置启用

在 `application.yml` 中添加配置：

```yaml
woodlin:
  searchable-encryption:
    # 启用可搜索加密
    enabled: true
    # 加密密钥（256位，Base64编码）
    encryption-key: "your-base64-encoded-256-bit-key"
    # N-gram大小（默认2）
    ngram-size: 2
    # 自动加密标注字段
    auto-encrypt: true
    # 自动生成搜索索引
    auto-generate-index: true
    # 启用缓存
    enable-cache: true
    # 缓存过期时间（秒）
    cache-expire-seconds: 3600
```

### 2. 生成加密密钥

使用工具类生成密钥：

```java
import com.mumu.woodlin.common.util.SearchableEncryptionUtil;

// 生成256位密钥
String key = SearchableEncryptionUtil.generateKey(256);
System.out.println("加密密钥: " + key);
```

或使用命令行：

```bash
# 使用 OpenSSL 生成
openssl rand -base64 32
```

### 3. 创建数据表

创建包含加密字段和搜索索引字段的表：

```sql
CREATE TABLE `sys_sensitive_data` (
  `data_id` BIGINT(20) NOT NULL,
  `real_name` VARCHAR(500) COMMENT '真实姓名（加密）',
  `real_name_search_index` TEXT COMMENT '搜索索引',
  `mobile` VARCHAR(500) COMMENT '手机号（加密）',
  `mobile_search_index` TEXT COMMENT '搜索索引',
  `id_card` VARCHAR(500) COMMENT '身份证号（加密）',
  -- 其他字段...
  PRIMARY KEY (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4. 定义实体类

使用 `@SearchableField` 注解标记需要加密的字段：

```java
import com.mumu.woodlin.common.annotation.SearchableField;

@Data
@TableName("sys_sensitive_data")
public class SensitiveData extends BaseEntity {
    
    @TableId(value = "data_id", type = IdType.ASSIGN_ID)
    private Long dataId;
    
    // 支持模糊搜索的字段
    @TableField("real_name")
    @SearchableField(fuzzySearch = true)
    private String realName;
    
    // 搜索索引字段（自动生成，无需手动赋值）
    @TableField("real_name_search_index")
    private String realNameSearchIndex;
    
    // 仅支持精确匹配的字段
    @TableField("id_card")
    @SearchableField(fuzzySearch = false)
    private String idCard;
    
    // 支持模糊搜索，自定义 N-gram 大小
    @TableField("mobile")
    @SearchableField(fuzzySearch = true, ngramSize = 3)
    private String mobile;
    
    @TableField("mobile_search_index")
    private String mobileSearchIndex;
}
```

### 5. 实现服务层

```java
@Service
@RequiredArgsConstructor
public class SensitiveDataServiceImpl extends ServiceImpl<SensitiveDataMapper, SensitiveData> 
        implements ISensitiveDataService {
    
    private final SearchableEncryptionService encryptionService;
    
    /**
     * 模糊搜索示例
     */
    @Override
    public IPage<SensitiveData> searchByNameFuzzy(String keyword, Integer pageNum, Integer pageSize) {
        Page<SensitiveData> page = new Page<>(pageNum, pageSize);
        
        // 生成搜索令牌（加密的 N-grams）
        List<String> searchTokens = encryptionService.generateSearchTokens(keyword);
        
        // 构建查询条件
        LambdaQueryWrapper<SensitiveData> queryWrapper = new LambdaQueryWrapper<>();
        for (String token : searchTokens) {
            queryWrapper.or().like(SensitiveData::getRealNameSearchIndex, token);
        }
        
        return this.page(page, queryWrapper);
    }
    
    /**
     * 精确匹配示例
     */
    @Override
    public SensitiveData findByIdCardExact(String idCard) {
        // 加密身份证号
        String encryptedIdCard = encryptionService.encrypt(idCard);
        
        // 精确匹配
        return this.getOne(new LambdaQueryWrapper<SensitiveData>()
                .eq(SensitiveData::getIdCard, encryptedIdCard)
                .last("LIMIT 1"));
    }
    
    /**
     * 保存时自动加密
     */
    @Override
    public boolean save(SensitiveData entity) {
        // 自动加密实体
        encryptionService.encryptEntity(entity);
        return super.save(entity);
    }
}
```

### 6. 实现控制器

```java
@RestController
@RequestMapping("/api/system/sensitive-data")
@RequiredArgsConstructor
public class SensitiveDataController {
    
    private final ISensitiveDataService sensitiveDataService;
    
    /**
     * 模糊搜索接口
     */
    @GetMapping("/search/name")
    public Result<IPage<SensitiveData>> searchByName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        IPage<SensitiveData> page = sensitiveDataService.searchByNameFuzzy(keyword, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 精确查询接口
     */
    @GetMapping("/search/idcard")
    public Result<SensitiveData> searchByIdCard(@RequestParam String idCard) {
        SensitiveData data = sensitiveDataService.findByIdCardExact(idCard);
        return Result.success(data);
    }
    
    /**
     * 新增接口（自动加密）
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody SensitiveData data) {
        boolean success = sensitiveDataService.save(data);
        return success ? Result.success() : Result.error("新增失败");
    }
}
```

## 使用示例

### API 调用示例

**1. 新增敏感数据（自动加密）**

```bash
curl -X POST http://localhost:8080/api/system/sensitive-data/add \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "张三",
    "idCard": "110101199001011234",
    "mobile": "13800138000",
    "emailAddress": "zhangsan@example.com"
  }'
```

系统会自动：
- 加密 `realName`、`idCard`、`mobile`、`emailAddress`
- 为 `realName` 和 `mobile` 生成 N-gram 搜索索引

**2. 模糊搜索（搜索姓名包含"张"的记录）**

```bash
curl -X GET "http://localhost:8080/api/system/sensitive-data/search/name?keyword=张&pageNum=1&pageSize=10"
```

**3. 精确查询（通过身份证号查询）**

```bash
curl -X GET "http://localhost:8080/api/system/sensitive-data/search/idcard?idCard=110101199001011234"
```

### Java 代码示例

**基础加密/解密**

```java
import com.mumu.woodlin.common.util.SearchableEncryptionUtil;

String key = "your-base64-encoded-key";

// 加密
String plaintext = "张三";
String ciphertext = SearchableEncryptionUtil.encrypt(plaintext, key);
System.out.println("密文: " + ciphertext);

// 解密（需要原始明文用于生成IV）
String decrypted = SearchableEncryptionUtil.decrypt(ciphertext, key, plaintext);
System.out.println("明文: " + decrypted);
```

**生成搜索索引**

```java
import com.mumu.woodlin.common.util.SearchableEncryptionUtil;

String key = "your-base64-encoded-key";
String text = "张三丰";

// 生成 N-grams
Set<String> ngrams = SearchableEncryptionUtil.generateNGrams(text, 2);
System.out.println("N-grams: " + ngrams);
// 输出: ["张三", "三丰"]

// 生成搜索索引（加密的 N-grams）
String searchIndex = SearchableEncryptionUtil.generateSearchIndex(text, key);
System.out.println("搜索索引: " + searchIndex);
// 输出: "aGd8f3K9s...,kJh9s2Lm..."
```

**搜索令牌生成**

```java
import com.mumu.woodlin.common.util.SearchableEncryptionUtil;

String key = "your-base64-encoded-key";
String keyword = "张";

// 生成搜索令牌
List<String> tokens = SearchableEncryptionUtil.generateSearchTokens(keyword, key);
System.out.println("搜索令牌: " + tokens);

// 使用令牌构建 SQL 查询
// WHERE real_name_search_index LIKE '%token1%' OR real_name_search_index LIKE '%token2%'
```

## 高级用法

### 自定义 N-gram 大小

不同场景可以使用不同的 N-gram 大小：

```java
@TableField("home_address")
@SearchableField(fuzzySearch = true, ngramSize = 3)  // 使用3-gram
private String homeAddress;
```

- **小值（2）**：更细粒度，适合短文本（姓名、标题）
- **大值（3-4）**：更精确，适合长文本（地址、描述）

### 自定义索引字段名

```java
@TableField("real_name")
@SearchableField(fuzzySearch = true, indexField = "name_idx")
private String realName;

@TableField("name_idx")
private String nameIdx;
```

### 批量操作

```java
@Service
public class SensitiveDataServiceImpl {
    
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SensitiveData> dataList) {
        // 批量加密
        for (SensitiveData data : dataList) {
            encryptionService.encryptEntity(data);
        }
        
        // 批量插入
        return this.saveBatch(dataList) ? dataList.size() : 0;
    }
}
```

### 禁用自动加密

如果需要手动控制加密：

```yaml
woodlin:
  searchable-encryption:
    auto-encrypt: false
```

然后在需要的地方手动调用：

```java
// 手动加密
encryptionService.encryptEntity(entity);

// 或使用工具类
String encrypted = SearchableEncryptionUtil.encrypt(plaintext, key);
```

## 性能优化

### 1. 数据库索引

为搜索索引字段创建索引：

**MySQL：**
```sql
-- 创建全文索引（MySQL 5.7+）
ALTER TABLE sys_sensitive_data 
ADD FULLTEXT INDEX ft_real_name_search (real_name_search_index);

-- 查询时使用全文索引
SELECT * FROM sys_sensitive_data 
WHERE MATCH(real_name_search_index) AGAINST('encrypted_token');
```

**PostgreSQL：**
```sql
-- 创建 GIN 索引
CREATE INDEX idx_gin_real_name_search 
ON sys_sensitive_data 
USING gin(to_tsvector('simple', real_name_search_index));
```

### 2. 缓存配置

调整缓存参数以平衡性能和内存：

```yaml
woodlin:
  searchable-encryption:
    enable-cache: true
    cache-expire-seconds: 3600    # 1小时过期
    max-cache-size: 10000         # 最多缓存10000条
```

### 3. 查询优化

**使用分页避免全表扫描：**

```java
Page<SensitiveData> page = new Page<>(pageNum, pageSize);
// 限制最大页码
if (pageNum > 100) {
    pageNum = 100;
}
```

**限制返回字段：**

```java
LambdaQueryWrapper<SensitiveData> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.select(SensitiveData::getDataId, SensitiveData::getRealName);
```

## 安全建议

### 1. 密钥管理

- **不要硬编码密钥**：使用环境变量或配置中心
- **定期轮换密钥**：建议每3-6个月轮换一次
- **分离存储**：密钥与数据分开存储
- **访问控制**：限制密钥的访问权限

```bash
# 使用环境变量
export WOODLIN_SEARCHABLE_ENCRYPTION_ENCRYPTION_KEY="your-key"
```

```yaml
woodlin:
  searchable-encryption:
    encryption-key: ${WOODLIN_SEARCHABLE_ENCRYPTION_ENCRYPTION_KEY}
```

### 2. 数据脱敏

在日志和响应中脱敏：

```java
@Override
public String toString() {
    return "SensitiveData{" +
           "dataId=" + dataId +
           ", realName='***'" +  // 脱敏
           ", idCard='***'" +     // 脱敏
           "}";
}
```

### 3. 审计日志

记录敏感数据的访问：

```java
@Aspect
@Component
public class SensitiveDataAuditAspect {
    
    @Around("@annotation(operation)")
    public Object audit(ProceedingJoinPoint pjp, Operation operation) throws Throwable {
        // 记录访问日志
        log.info("访问敏感数据: user={}, operation={}", 
                 SecurityUtils.getCurrentUser(), operation.summary());
        return pjp.proceed();
    }
}
```

## 常见问题

### Q1: 加密后无法查询到数据？

**原因：** 可能是密钥不一致或未生成搜索索引

**解决：**
1. 检查配置的密钥是否正确
2. 确认 `auto-generate-index` 设置为 `true`
3. 检查实体中是否定义了索引字段

### Q2: 查询性能慢？

**原因：** 未创建数据库索引或 N-gram 过小

**解决：**
1. 为搜索索引字段创建全文索引或 GIN 索引
2. 适当增大 N-gram 大小（2→3）
3. 启用缓存
4. 使用分页限制结果数量

### Q3: 如何迁移现有数据？

**步骤：**
```java
// 1. 查询所有明文数据
List<SensitiveData> dataList = sensitiveDataService.list();

// 2. 加密并更新
for (SensitiveData data : dataList) {
    // 备份原始数据
    String originalName = data.getRealName();
    
    // 加密
    encryptionService.encryptEntity(data);
    
    // 更新
    sensitiveDataService.updateById(data);
}
```

### Q4: 如何解密显示？

**方案1：在服务层解密**
```java
public SensitiveData getWithDecryption(Long id) {
    SensitiveData data = this.getById(id);
    // 注意：解密需要原始明文，实际使用中需要特殊处理
    // 建议保存明文的哈希值用于验证
    return data;
}
```

**方案2：前端解密**
将解密逻辑放在前端，后端只返回密文

## 最佳实践总结

1. **合理选择加密字段**：只加密真正敏感的字段
2. **使用强密钥**：至少256位的随机密钥
3. **定期备份**：加密后数据无法恢复，务必备份
4. **监控性能**：定期检查查询性能，优化索引
5. **审计合规**：记录所有敏感数据访问日志
6. **灵活配置**：根据场景选择合适的 N-gram 大小
7. **测试验证**：充分测试加密和搜索功能

## 相关文档

- [API 加密配置文档](./API_ENCRYPTION.md)
- [系统配置管理](./SYSTEM_CONFIG.md)
- [安全最佳实践](./SECURITY_BEST_PRACTICES.md)

## 技术支持

如有问题，请联系技术支持或提交 Issue。
