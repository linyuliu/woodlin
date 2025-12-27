# 动态字典系统使用指南

## 概述

Woodlin 系统采用全新的动态字典管理方案，实现了"先查类型，再查数据"的设计模式，支持数据库驱动的字典管理和前端缓存优化。

## 核心特性

### 1. 数据库驱动
- 字典类型和数据完全存储在数据库中
- 支持动态添加、修改字典而无需修改代码
- 遵循国家标准（GB/T系列标准）

### 2. 统一的查询接口
- **查询字典类型**: `/common/dict/types`
- **查询字典数据**: `/common/dict/data/{type}`
- **查询行政区划树**: `/common/dict/region/tree`
- **查询子区划**: `/common/dict/region/children?parentCode={code}`

### 3. 多级缓存
- **后端缓存**: Spring Cache + Redis，按类型缓存
- **前端缓存**: 客户端内存缓存，支持TTL过期控制
  - 字典类型: 30分钟
  - 字典数据: 5分钟
  - 行政区划: 30分钟

### 4. 树形结构支持
- 行政区划采用树形结构存储
- 支持省市区县多级联动
- 按需加载子节点，减少数据传输

## 数据库结构

### sys_dict_type - 字典类型表

存储字典的分类信息。

```sql
CREATE TABLE `sys_dict_type` (
    `dict_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `dict_name` varchar(100) NOT NULL,        -- 字典名称，如"性别"
    `dict_type` varchar(100) NOT NULL,        -- 字典类型，如"gender"
    `dict_category` varchar(50) DEFAULT 'system',  -- 分类: system/business/custom
    `status` char(1) DEFAULT '1',
    ...
);
```

**字典分类**:
- `system`: 系统字典（符合国家标准）
- `business`: 业务字典（业务相关）
- `custom`: 自定义字典（客户定制）

### sys_dict_data - 字典数据表

存储具体的字典项。

```sql
CREATE TABLE `sys_dict_data` (
    `data_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `dict_type` varchar(100) NOT NULL,        -- 关联字典类型
    `dict_label` varchar(100) NOT NULL,       -- 显示标签
    `dict_value` varchar(100) NOT NULL,       -- 存储值
    `dict_desc` varchar(500),                 -- 描述
    `dict_sort` int(11) DEFAULT 0,            -- 排序
    `extra_data` text,                        -- 扩展数据(JSON)
    ...
);
```

### sys_region - 行政区划表

树形结构存储行政区划。

```sql
CREATE TABLE `sys_region` (
    `region_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `region_code` varchar(20) NOT NULL,       -- GB/T 2260代码
    `region_name` varchar(100) NOT NULL,      -- 区划名称
    `parent_code` varchar(20),                -- 父区划代码
    `region_level` int(11) DEFAULT 1,         -- 层级: 1-省 2-市 3-区县
    ...
);
```

## 后端使用

### 1. 查询字典类型列表

```java
@Autowired
private SystemDictionaryService dictionaryService;

// 获取所有字典类型
List<SysDictType> types = dictionaryService.getAllDictTypes();
```

### 2. 根据类型查询字典数据

```java
// 查询性别字典
List<Map<String, Object>> genderDict = dictionaryService.getDictDataByType("gender");

// 返回格式:
// [
//   {"value": "0", "label": "未知的性别", "desc": "GB/T 2261.1-2003标准"},
//   {"value": "1", "label": "男性", "desc": "GB/T 2261.1-2003标准"},
//   ...
// ]
```

### 3. 查询行政区划

```java
// 查询省级行政区划
List<Map<String, Object>> provinces = dictionaryService.getProvinces();

// 查询某省下的市级区划
List<Map<String, Object>> cities = dictionaryService.getCitiesByProvince("110000");

// 查询完整树形结构
List<Map<String, Object>> tree = dictionaryService.getRegionTree();
```

### 4. 兼容性方法

为保证向后兼容，保留了原有的方法：

```java
// 旧方法仍然可用
List<Map<String, Object>> genderDict = dictionaryService.getGenderDict();
```

## 前端使用

### 1. 导入API

```typescript
import { 
  getDictTypes,       // 查询字典类型
  getDictData,        // 查询字典数据
  getRegionTree,      // 查询行政区划树
  getRegionChildren,  // 查询子区划
  clearDictCache      // 清空缓存
} from '@/api/dict'
```

### 2. 查询字典类型

```typescript
// 查询所有字典类型（使用缓存）
const types = await getDictTypes()
// 返回: [
//   { dictType: 'gender', dictName: '性别', dictCategory: 'system' },
//   { dictType: 'ethnicity', dictName: '民族', dictCategory: 'system' },
//   ...
// ]

// 强制刷新，不使用缓存
const freshTypes = await getDictTypes(false)
```

### 3. 查询字典数据

```typescript
// 查询性别字典（使用缓存）
const genderDict = await getDictData('gender')
// 返回: [
//   { value: '0', label: '未知的性别', desc: 'GB/T 2261.1-2003标准' },
//   { value: '1', label: '男性', desc: 'GB/T 2261.1-2003标准' },
//   ...
// ]

// 强制刷新
const freshGenderDict = await getDictData('gender', false)
```

### 4. 便捷方法

```typescript
// 直接获取常用字典
import { 
  getGenderDict,      // 性别
  getEthnicityDict,   // 民族
  getEducationDict,   // 学历
  getMaritalDict,     // 婚姻状况
  getPoliticalDict,   // 政治面貌
  getIdTypeDict       // 证件类型
} from '@/api/dict'

const genderOptions = await getGenderDict()
```

### 5. 行政区划查询

```typescript
// 查询省级行政区划
const provinces = await getProvinces()

// 查询市级行政区划
const cities = await getCities('110000')  // 北京市下的区县

// 查询区县级行政区划
const districts = await getDistricts('110100')  // 北京市市辖区

// 查询完整树形结构
const regionTree = await getRegionTree()
```

### 6. 缓存控制

```typescript
// 查看缓存统计
import dictCache from '@/utils/dictCache'
const stats = dictCache.stats()
console.log(`总缓存数: ${stats.total}, 已过期: ${stats.expired}`)

// 清空所有缓存
clearDictCache()

// 清空特定缓存
import { CACHE_KEY } from '@/utils/dictCache'
dictCache.delete(CACHE_KEY.DICT_DATA('gender'))
```

### 7. 应用初始化时预加载

```typescript
import { preloadCommonDicts } from '@/api/dict'

// 在应用启动时预加载常用字典
await preloadCommonDicts()
```

## 数据导入

### 1. 执行SQL脚本

**MySQL**:
```bash
mysql -u root -p woodlin < sql/mysql/dynamic_dictionary_schema.sql
mysql -u root -p woodlin < sql/mysql/region_data.sql
```

**PostgreSQL**:
```bash
psql -U postgres -d woodlin < sql/postgresql/dynamic_dictionary_schema.sql
psql -U postgres -d woodlin < sql/postgresql/region_data.sql
```

### 2. 初始数据

脚本会自动导入：
- 7种系统字典类型
- 基础字典数据（性别、用户状态等）
- 34个省级行政区划

**注意**: 完整的市级和区县级数据量较大（约3000+条记录），需要根据实际需求单独导入。

## 自定义字典

### 1. 添加新的字典类型

```sql
INSERT INTO sys_dict_type (dict_name, dict_type, dict_category, status, remark)
VALUES ('客户类型', 'customer_type', 'business', '1', '业务字典-客户类型分类');
```

### 2. 添加字典数据

```sql
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES 
  ('customer_type', '个人客户', '1', '个人用户', 1, '1'),
  ('customer_type', '企业客户', '2', '企业用户', 2, '1'),
  ('customer_type', 'VIP客户', '3', 'VIP级别客户', 3, '1');
```

### 3. 在前端使用

```typescript
// 查询自定义字典
const customerTypes = await getDictData('customer_type')
```

## 多租户支持

字典系统支持多租户隔离：

```sql
-- 创建租户专属字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, dict_category, tenant_id, status)
VALUES ('部门类型', 'dept_type', 'custom', 'tenant_001', '1');

-- 创建租户专属字典数据
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, tenant_id, status)
VALUES ('dept_type', '研发部', '1', 'tenant_001', '1');
```

## 缓存策略

### 后端缓存

使用 Spring Cache 注解：

```java
@Cacheable(value = "dict:types", unless = "#result == null || #result.isEmpty()")
public List<SysDictType> getAllDictTypes() { ... }

@Cacheable(value = "dict:data", key = "#dictType", unless = "#result == null || #result.isEmpty()")
public List<Map<String, Object>> getDictDataByType(String dictType) { ... }
```

**缓存失效**:
- 通过 Redis TTL 自动过期
- 数据更新时手动清除缓存

### 前端缓存

使用内存缓存 + TTL：

```typescript
// 缓存时间配置
export const TTL = {
  SHORT: 1 * 60 * 1000,      // 1分钟
  MEDIUM: 5 * 60 * 1000,     // 5分钟（默认）
  LONG: 30 * 60 * 1000,      // 30分钟
}
```

**缓存策略**:
- 字典类型: 30分钟（变化不频繁）
- 字典数据: 5分钟（中等频率）
- 行政区划: 30分钟（基本不变）
- 页面级别: 直到页面刷新

## 最佳实践

### 1. 命名规范

- **字典类型**: 使用小写+下划线，如 `user_status`, `customer_type`
- **字典值**: 遵循国标或业务规则
- **字典标签**: 简洁明了，便于理解

### 2. 性能优化

```typescript
// ✅ 推荐：在应用启动时预加载常用字典
await preloadCommonDicts()

// ✅ 推荐：使用缓存，避免重复请求
const gender = await getDictData('gender', true)

// ❌ 不推荐：频繁强制刷新
const gender = await getDictData('gender', false)  // 每次都请求服务器
```

### 3. 错误处理

```typescript
try {
  const dictData = await getDictData('gender')
  // 使用字典数据
} catch (error) {
  console.error('加载字典失败:', error)
  // 使用默认值或显示错误提示
}
```

### 4. 国际化支持

可在 `extra_data` 字段存储多语言信息：

```sql
UPDATE sys_dict_data 
SET extra_data = '{"en": "Male", "zh": "男性", "jp": "男性"}'
WHERE dict_type = 'gender' AND dict_value = '1';
```

## 迁移指南

### 从旧系统迁移

1. **执行SQL脚本** - 创建新表结构
2. **导入基础数据** - 运行初始化脚本
3. **更新前端代码** - 使用新的API方法
4. **测试验证** - 确保所有字典功能正常
5. **清理旧代码** - 移除硬编码的字典枚举（可选）

### 兼容性说明

- 旧的API接口仍然可用，内部调用新方法
- 逐步迁移，不影响现有功能
- 建议新功能直接使用新API

## 常见问题

### Q: 如何添加新的字典类型？
A: 在 `sys_dict_type` 表中插入记录，然后在 `sys_dict_data` 表中添加对应的数据项。

### Q: 缓存什么时候失效？
A: 前端缓存根据TTL自动过期，后端缓存由Redis管理。可以手动调用 `clearDictCache()` 清空。

### Q: 如何导入完整的行政区划数据？
A: 可以从民政部官网下载最新的GB/T 2260数据，按照表结构导入。

### Q: 支持哪些数据库？
A: 目前支持 MySQL、PostgreSQL，Oracle版本的SQL脚本可根据需要提供。

### Q: 如何实现字典的权限控制？
A: 可以在查询时根据 `tenant_id` 字段过滤，实现多租户隔离。

## 相关标准

- GB/T 2261.1-2003：性别代码
- GB/T 3304-1991：中国各民族名称的罗马字母拼写法和代码
- GB/T 4658-2006：学历代码
- GB/T 2261.2-2003：婚姻状况代码
- GB/T 4762-1984：政治面貌代码
- GB/T 2260：中华人民共和国行政区划代码

## 总结

新的动态字典系统提供了：
- ✅ 灵活的数据库驱动方案
- ✅ 统一的API接口
- ✅ 多级缓存优化
- ✅ 树形结构支持
- ✅ 多租户隔离
- ✅ 向后兼容
- ✅ 国家标准遵循

建议在新项目中直接使用新API，老项目可以逐步迁移。
