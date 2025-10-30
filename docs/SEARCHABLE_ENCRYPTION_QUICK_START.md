# 可搜索加密 - 5分钟快速上手

## 🚀 快速开始

### 1️⃣ 生成密钥（5秒）

```bash
# 方法1：使用命令行生成256位密钥
openssl rand -base64 32

# 方法2：使用Java（见下方代码）
```

```java
import com.mumu.woodlin.common.util.SearchableEncryptionUtil;

String key = SearchableEncryptionUtil.generateKey(256);
System.out.println("密钥: " + key);
```

### 2️⃣ 配置启用（30秒）

在 `application.yml` 或 `.env` 中添加：

```yaml
woodlin:
  searchable-encryption:
    enabled: true
    encryption-key: "你的密钥"  # 使用步骤1生成的密钥
```

### 3️⃣ 创建表结构（1分钟）

```sql
CREATE TABLE `user_info` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(500),              -- 加密字段
  `name_search_index` TEXT,         -- 搜索索引
  `phone` VARCHAR(500),             -- 加密字段
  `phone_search_index` TEXT,        -- 搜索索引
  PRIMARY KEY (`id`)
);
```

### 4️⃣ 定义实体（2分钟）

```java
@Data
@TableName("user_info")
public class UserInfo extends BaseEntity {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    // 支持模糊搜索
    @SearchableField(fuzzySearch = true)
    private String name;
    
    // 自动生成，无需手动赋值
    private String nameSearchIndex;
    
    // 支持模糊搜索
    @SearchableField(fuzzySearch = true)
    private String phone;
    
    private String phoneSearchIndex;
}
```

### 5️⃣ 使用示例（2分钟）

```java
@Service
@RequiredArgsConstructor
public class UserInfoService extends ServiceImpl<UserInfoMapper, UserInfo> {
    
    private final SearchableEncryptionService encryptionService;
    
    // ✅ 插入数据（自动加密）
    public boolean addUser(UserInfo user) {
        return this.save(user);  // 框架自动加密
    }
    
    // ✅ 模糊搜索
    public List<UserInfo> searchByName(String keyword) {
        List<String> tokens = encryptionService.generateSearchTokens(keyword);
        
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        for (String token : tokens) {
            wrapper.or().like(UserInfo::getNameSearchIndex, token);
        }
        
        return this.list(wrapper);
    }
}
```

## 📋 完整工作流程

```
                    用户输入                   
                      ↓
              ┌──────────────┐
              │  "张三"      │  明文
              └──────────────┘
                      ↓
              ┌──────────────┐
              │   加密处理   │  @SearchableField 自动触发
              └──────────────┘
                      ↓
        ┌─────────────┴─────────────┐
        ↓                           ↓
┌──────────────┐          ┌──────────────┐
│ name字段     │          │ N-gram索引   │
│ "xF2g..."    │          │ "aB3d,cD4e"  │
│ (加密后)     │          │ (加密N-grams)│
└──────────────┘          └──────────────┘
        ↓                           ↓
    存储到数据库              存储到数据库
    name列                   name_search_index列
```

## 🔍 搜索工作原理

```
用户搜索："张"
    ↓
生成N-grams: ["张"]
    ↓
加密N-grams: ["aB3d..."]
    ↓
SQL查询: WHERE name_search_index LIKE '%aB3d...%'
    ↓
返回匹配结果（包含"张三"、"张四"等）
```

## 💡 核心概念

| 概念 | 说明 | 示例 |
|------|------|------|
| **确定性加密** | 相同明文 → 相同密文 | "张三" 每次加密都是 "xF2g..." |
| **N-gram** | 文本分割为固定长度片段 | "张三丰" → ["张三", "三丰"] |
| **搜索索引** | 加密的N-gram集合 | "aB3d,cD4e,fG6h" |
| **模糊搜索** | 匹配任意N-gram | 搜索"张"可找到"张三"、"张四" |
| **精确匹配** | 直接比较加密值 | 身份证号完全匹配 |

## 🎯 使用场景

### ✅ 适合场景

- 📝 姓名、地址等需要模糊搜索的文本字段
- 📱 手机号、邮箱等支持部分匹配的字段
- 🏥 医疗病历、患者信息等敏感数据
- 💳 客户资料、订单信息等业务数据

### ⚠️ 不适合场景

- 🔢 数值计算（加减乘除）
- 📊 范围查询（大于、小于）
- 🔄 排序操作（ORDER BY 加密字段）
- 🔗 JOIN 操作（关联加密字段）

## 🛠️ API 示例

### 插入数据

**请求：**
```bash
curl -X POST http://localhost:8080/api/system/sensitive-data/add \
  -H "Content-Type: application/json" \
  -d '{"realName":"张三","mobile":"13800138000"}'
```

**数据库存储：**
```
real_name: "xF2gH5jK8lM..."           （加密）
real_name_search_index: "aB3d,cD4e"   （N-gram索引）
mobile: "pQ7rT0vW3yZ..."              （加密）
mobile_search_index: "eF8g,hI9j,kL0m" （N-gram索引）
```

### 模糊搜索

**请求：**
```bash
curl "http://localhost:8080/api/system/sensitive-data/search/name?keyword=张"
```

**SQL 执行：**
```sql
SELECT * FROM sys_sensitive_data 
WHERE real_name_search_index LIKE '%aB3d%'  -- 加密后的"张"
```

## 🔐 安全提示

### ✅ 推荐做法

- 密钥使用环境变量，不要硬编码
- 定期轮换密钥（3-6个月）
- 密钥与代码分离存储
- 启用访问审计日志
- 备份加密密钥（安全存储）

### ❌ 避免错误

- ❌ 不要在日志中打印密钥
- ❌ 不要在客户端解密数据
- ❌ 不要使用弱密钥（少于128位）
- ❌ 不要共享加密密钥
- ❌ 不要忘记备份数据

## 📊 性能参考

| 操作 | 响应时间 | 说明 |
|------|---------|------|
| 加密单条 | 0.25ms | 包含N-gram生成（实测值） |
| 模糊搜索 | 15-50ms | 取决于数据量和索引 |
| 精确匹配 | 5ms | 直接索引查询 |
| 批量插入 | 0.25ms/条 | 1000条约255ms |

## 🆘 常见问题

### Q1: 加密后查不到数据？
**A:** 检查密钥是否一致，确认已生成搜索索引。

### Q2: 搜索速度慢？
**A:** 为搜索索引字段创建全文索引。

### Q3: 如何迁移现有数据？
**A:** 参考文档中的数据迁移示例代码。

## 📚 更多资源

- 📖 [详细使用指南](./SEARCHABLE_ENCRYPTION_GUIDE.md)
- 💼 [完整示例代码](./SEARCHABLE_ENCRYPTION_EXAMPLES.md)
- 🔗 [API 加密文档](./API_ENCRYPTION.md)

## 🎉 开始使用

现在你已经掌握了基础知识，开始使用可搜索加密保护你的敏感数据吧！

```java
// 1. 标记字段
@SearchableField(fuzzySearch = true)
private String name;

// 2. 保存数据（自动加密）
userService.save(user);

// 3. 搜索数据（自动生成加密查询）
List<User> results = userService.searchByName("张");

// 就是这么简单！✨
```
