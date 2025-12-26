# 系统字典使用文档

## 概述

统一的系统字典管理，所有字典数据遵循中国国家标准（GB）和国际标准（ISO），提供前后端统一的数据字典接口。

## 标准遵循

| 字典类型 | 遵循标准 | 说明 |
|---------|---------|------|
| 性别 | GB/T 2261.1-2003 | 个人基本信息分类与代码 第1部分：人的性别代码 |
| 民族 | GB/T 3304-1991 | 中国各民族名称的罗马字母拼写法和代码（56个民族） |
| 婚姻状况 | GB/T 2261.2-2003 | 个人基本信息分类与代码 第2部分：婚姻状况代码 |
| 政治面貌 | GB/T 4762-1984 | 政治面貌代码 |
| 学历 | GB/T 4658-2006 | 学历代码 |
| 证件类型 | GB/T 2261.4 | 证件类型代码 |
| 行政区划 | GB/T 2260 | 中华人民共和国行政区划代码 |

## API 接口

### 基础路径
```
/common/dict
```

### 1. 获取所有字典（推荐用于前端初始化）
```http
GET /common/dict/all
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "gender": [...],
    "ethnicity": [...],
    "education": [...],
    "marital": [...],
    "political": [...],
    "idType": [...],
    "provinces": [...]
  }
}
```

### 2. 性别字典（GB/T 2261.1-2003）
```http
GET /common/dict/gender
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": 0,
      "label": "未知的性别",
      "desc": "未知的性别 / Unknown"
    },
    {
      "value": 1,
      "label": "男",
      "desc": "男 / Male"
    },
    {
      "value": 2,
      "label": "女",
      "desc": "女 / Female"
    },
    {
      "value": 9,
      "label": "未说明的性别",
      "desc": "未说明的性别 / Not Applicable"
    }
  ]
}
```

### 3. 民族字典（GB/T 3304-1991）
```http
GET /common/dict/ethnicity
```

包含56个民族，代码1-56：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": 1,
      "label": "汉族",
      "desc": "汉族 / Han"
    },
    {
      "value": 2,
      "label": "蒙古族",
      "desc": "蒙古族 / Mongol"
    },
    ...
  ]
}
```

### 4. 学历字典（GB/T 4658-2006）
```http
GET /common/dict/education
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": 10,
      "label": "博士研究生",
      "desc": "博士研究生 / Doctoral"
    },
    {
      "value": 11,
      "label": "硕士研究生",
      "desc": "硕士研究生 / Master"
    },
    {
      "value": 20,
      "label": "大学本科",
      "desc": "大学本科 / Bachelor"
    },
    ...
  ]
}
```

### 5. 婚姻状况字典（GB/T 2261.2-2003）
```http
GET /common/dict/marital
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": 10,
      "label": "未婚",
      "desc": "未婚 / Unmarried"
    },
    {
      "value": 20,
      "label": "已婚",
      "desc": "已婚 / Married"
    },
    ...
  ]
}
```

### 6. 政治面貌字典（GB/T 4762-1984）
```http
GET /common/dict/political
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": 1,
      "label": "中共党员",
      "desc": "中共党员 / CPC Member"
    },
    {
      "value": 2,
      "label": "中共预备党员",
      "desc": "中共预备党员 / CPC Probationary Member"
    },
    {
      "value": 3,
      "label": "共青团员",
      "desc": "共青团员 / CYLC Member"
    },
    ...
  ]
}
```

### 7. 证件类型字典（GB/T 2261.4）
```http
GET /common/dict/idtype
```

**响应示例**（包含验证正则）：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": 1,
      "label": "居民身份证",
      "desc": "居民身份证 / ID Card",
      "pattern": "^\\d{15}$|^\\d{17}[\\dXx]$"
    },
    {
      "value": 2,
      "label": "护照",
      "desc": "护照 / Passport",
      "pattern": "^[EeKkGgDdSsPpHh]\\d{8}$|..."
    },
    ...
  ]
}
```

### 8. 省级行政区划（GB/T 2260）
```http
GET /common/dict/region/provinces
```

**响应示例**（34个省级行政区）：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "code": "110000",
      "name": "北京市",
      "pinyin": "Beijing",
      "shortName": "BJ",
      "level": 1
    },
    {
      "code": "310000",
      "name": "上海市",
      "pinyin": "Shanghai",
      "shortName": "SH",
      "level": 1
    },
    ...
  ]
}
```

### 9. 市级行政区划
```http
GET /common/dict/region/cities/{provinceCode}
```

示例：`GET /common/dict/region/cities/110000` （获取北京市的区）

### 10. 区县级行政区划
```http
GET /common/dict/region/districts/{cityCode}
```

示例：`GET /common/dict/region/districts/110100` （获取北京市市辖区的县）

## 前端使用示例

### Vue 3 + TypeScript

```typescript
// 定义字典类型
interface DictItem {
  value: number | string;
  label: string;
  desc: string;
}

interface Dictionaries {
  gender: DictItem[];
  ethnicity: DictItem[];
  education: DictItem[];
  marital: DictItem[];
  political: DictItem[];
  idType: DictItem[];
  provinces: any[];
}

// 1. 一次性加载所有字典（推荐）
async function loadAllDictionaries(): Promise<Dictionaries> {
  const response = await fetch('/common/dict/all');
  const result = await response.json();
  return result.data;
}

// 2. 按需加载单个字典
async function loadGenderDict(): Promise<DictItem[]> {
  const response = await fetch('/common/dict/gender');
  const result = await response.json();
  return result.data;
}

// 3. 使用示例 - 在组件中
import { ref, onMounted } from 'vue';

export default {
  setup() {
    const dictionaries = ref<Dictionaries | null>(null);
    const selectedGender = ref(1);
    
    onMounted(async () => {
      dictionaries.value = await loadAllDictionaries();
    });
    
    return {
      dictionaries,
      selectedGender
    };
  }
};
```

### 在表单中使用
```vue
<template>
  <n-form>
    <!-- 性别选择 -->
    <n-form-item label="性别">
      <n-select
        v-model:value="formData.gender"
        :options="dictionaries.gender"
        label-field="label"
        value-field="value"
      />
    </n-form-item>
    
    <!-- 民族选择 -->
    <n-form-item label="民族">
      <n-select
        v-model:value="formData.ethnicity"
        :options="dictionaries.ethnicity"
        label-field="label"
        value-field="value"
        filterable
      />
    </n-form-item>
    
    <!-- 学历选择 -->
    <n-form-item label="学历">
      <n-select
        v-model:value="formData.education"
        :options="dictionaries.education"
        label-field="label"
        value-field="value"
      />
    </n-form-item>
    
    <!-- 省市区三级联动 -->
    <n-form-item label="所在地区">
      <n-cascader
        v-model:value="formData.region"
        :options="regionOptions"
        @update:value="handleRegionChange"
      />
    </n-form-item>
  </n-form>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const dictionaries = ref({});
const formData = ref({
  gender: null,
  ethnicity: null,
  education: null,
  region: []
});

const regionOptions = ref([]);

onMounted(async () => {
  // 加载所有字典
  const response = await fetch('/common/dict/all');
  const result = await response.json();
  dictionaries.value = result.data;
  
  // 构建省市区三级联动数据
  regionOptions.value = await buildRegionCascader();
});

async function buildRegionCascader() {
  // 加载省级数据
  const provinces = dictionaries.value.provinces;
  
  return provinces.map(province => ({
    label: province.name,
    value: province.code,
    // 异步加载市级数据
    isLeaf: false
  }));
}

async function handleRegionChange(value: string[], option: any) {
  // 动态加载下级行政区划
  if (value.length === 1) {
    // 加载市级
    const response = await fetch(`/common/dict/region/cities/${value[0]}`);
    const result = await response.json();
    // 更新选项...
  }
}
</script>
```

### Pinia Store 管理（推荐）
```typescript
// stores/dictionary.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useDictionaryStore = defineStore('dictionary', () => {
  const dictionaries = ref<Dictionaries | null>(null);
  const loading = ref(false);
  
  async function loadDictionaries() {
    if (dictionaries.value) {
      return dictionaries.value;
    }
    
    loading.value = true;
    try {
      const response = await fetch('/common/dict/all');
      const result = await response.json();
      dictionaries.value = result.data;
      return dictionaries.value;
    } finally {
      loading.value = false;
    }
  }
  
  function getLabelByValue(dictType: string, value: any): string {
    const dict = dictionaries.value?.[dictType];
    if (!dict) return '';
    
    const item = dict.find((item: DictItem) => item.value === value);
    return item?.label || '';
  }
  
  return {
    dictionaries,
    loading,
    loadDictionaries,
    getLabelByValue
  };
});
```

## 后端使用示例

### Java 代码中使用枚举
```java
import com.mumu.woodlin.common.enums.*;

public class UserDTO {
    private Long userId;
    private String username;
    
    // 使用枚举类型
    private Gender gender;
    private Ethnicity ethnicity;
    private EducationLevel education;
    private MaritalStatus maritalStatus;
    private PoliticalStatus politicalStatus;
    
    // Jackson 会自动序列化为 value-label 格式
}

// 枚举转换示例
public void example() {
    // 从代码获取枚举
    Gender gender = Gender.fromCode(1);  // 男
    System.out.println(gender.getLabel());  // 输出：男
    System.out.println(gender.getDesc());   // 输出：男 / Male
    
    // 从描述获取枚举
    Ethnicity ethnicity = Ethnicity.fromName("汉族");
    System.out.println(ethnicity.getValue());  // 输出：1
    
    // 证件号码验证
    IdType idType = IdType.ID_CARD;
    boolean isValid = idType.validate("110101199001011234");
    System.out.println(isValid);  // 验证身份证格式
}
```

## 缓存策略

所有字典数据都使用Spring Cache进行缓存：
- 缓存Key：`dict:{类型}`
- 缓存时间：永久（字典数据变化频率极低）
- 缓存刷新：应用重启或手动清除缓存

清除缓存示例：
```java
@Autowired
private CacheManager cacheManager;

public void clearDictCache() {
    cacheManager.getCache("dict:gender").clear();
    cacheManager.getCache("dict:ethnicity").clear();
    // ...
}
```

## 行政区划数据说明

### 数据来源
- 推荐使用：https://github.com/modood/Administrative-divisions-of-China
- 该项目维护了最新的省市区数据（含历史变更）
- 数据格式：JSON/SQL多种格式
- 更新频率：跟随民政部公告更新

### 导入行政区划数据
1. 下载最新数据
2. 导入到数据库表 `sys_administrative_division`
3. 服务会自动从数据库加载

### 表结构建议
```sql
CREATE TABLE sys_administrative_division (
    code VARCHAR(6) PRIMARY KEY COMMENT '行政区划代码',
    name VARCHAR(50) NOT NULL COMMENT '名称',
    parent_code VARCHAR(6) COMMENT '父级代码',
    level TINYINT NOT NULL COMMENT '层级：1-省，2-市，3-区县',
    short_name VARCHAR(10) COMMENT '简称',
    pinyin VARCHAR(50) COMMENT '拼音',
    longitude DECIMAL(10, 6) COMMENT '经度',
    latitude DECIMAL(10, 6) COMMENT '纬度',
    is_municipality BOOLEAN DEFAULT FALSE COMMENT '是否直辖市',
    sort_order INT DEFAULT 0 COMMENT '排序',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    INDEX idx_parent_code (parent_code),
    INDEX idx_level (level)
) COMMENT='行政区划表（GB/T 2260）';
```

## 注意事项

1. **标准遵循**：所有字典严格遵循国家标准，不要随意修改代码值
2. **向后兼容**：如果标准更新，保留旧代码，添加新代码
3. **前端缓存**：前端应在应用启动时加载字典并缓存
4. **行政区划**：省市区数据需要定期更新（建议每年检查一次）
5. **证件验证**：前端和后端都应该验证证件号码格式

## 扩展字典

如需添加新的字典类型：

1. 创建枚举类实现 `DictEnum` 接口
2. 在 `SystemDictionaryService` 中添加获取方法
3. 在 `SystemDictionaryController` 中添加API接口
4. 更新前端类型定义

示例：
```java
@Getter
@AllArgsConstructor
public enum BloodType implements DictEnum {
    A(1, "A型", "Type A"),
    B(2, "B型", "Type B"),
    AB(3, "AB型", "Type AB"),
    O(4, "O型", "Type O"),
    UNKNOWN(9, "未知", "Unknown");
    
    private final Integer code;
    private final String name;
    private final String nameEn;
    
    @Override
    public Object getValue() { return code; }
    
    @Override
    public String getLabel() { return name; }
    
    @Override
    public String getDesc() { return name + " / " + nameEn; }
}
```
