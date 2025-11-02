# Woodlin DSL 模块

## 概述

Woodlin DSL 是一个使用 Kotlin 编写的问卷功能 DSL(领域特定语言)模块,提供简洁优雅的声明式问卷定义语法。

## 特性

- ✅ **声明式语法**: 使用 Kotlin DSL 提供直观的问卷定义方式
- ✅ **类型安全**: 利用 Kotlin 的类型系统确保编译时安全
- ✅ **灵活验证**: 支持内置和自定义验证规则
- ✅ **序列化支持**: 可序列化为 JSON 格式存储和传输
- ✅ **执行引擎**: 内置执行器处理答案收集和验证

## 快速开始

### 依赖配置

```xml
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-system-dsl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```kotlin
val survey = questionnaire("survey001", "客户满意度调查") {
    description = "用于收集客户对服务的满意度反馈"
    version = "1.0"
    author = "张三"
    
    section("基本信息") {
        question {
            id = "q1"
            type = QuestionType.TEXT
            title = "您的姓名"
            required = true
        }
        
        question {
            id = "q2"
            type = QuestionType.NUMBER
            title = "您的年龄"
        }
    }
    
    section("满意度评价") {
        question {
            id = "q3"
            type = QuestionType.SINGLE_CHOICE
            title = "您对我们的服务满意吗?"
            required = true
            
            option("非常满意", "5")
            option("满意", "4")
            option("一般", "3")
            option("不满意", "2")
            option("非常不满意", "1")
        }
    }
}
```

### 执行问卷

```kotlin
val executor = QuestionnaireExecutor(survey)

val answers = mapOf(
    "q1" to "张三",
    "q2" to 25,
    "q3" to "5"
)

val result = executor.execute(answers)

if (result.validationResult.valid) {
    println("问卷提交成功")
} else {
    result.validationResult.errors.forEach { error ->
        println("${error.questionId}: ${error.message}")
    }
}
```

### 自定义验证

```kotlin
questionnaire("test", "年龄验证") {
    section("基本信息") {
        question {
            id = "age"
            title = "年龄"
            type = QuestionType.NUMBER
            
            validate {
                name = "年龄范围验证"
                message = "年龄必须在18-100之间"
                when_ { answers ->
                    val age = answers["age"] as? Int ?: 0
                    age in 18..100
                }
            }
        }
    }
}
```

### 序列化和反序列化

```kotlin
val serializer = QuestionnaireSerializer()

// 序列化为 JSON
val json = serializer.toJson(survey)
println(json)

// 从 JSON 反序列化
val restored = serializer.fromJson(json)
```

## 问题类型

支持以下问题类型:

- `SINGLE_CHOICE` - 单选题
- `MULTIPLE_CHOICE` - 多选题
- `TEXT` - 短文本
- `TEXTAREA` - 长文本
- `NUMBER` - 数字
- `DATE` - 日期
- `RATING` - 评分
- `MATRIX_SINGLE` - 矩阵单选
- `MATRIX_MULTIPLE` - 矩阵多选

## 核心组件

### 1. Questionnaire (问卷)

主要的问卷对象,包含:
- `id` - 问卷ID
- `title` - 问卷标题
- `description` - 问卷描述
- `version` - 版本号
- `author` - 作者
- `sections` - 章节列表
- `rules` - 全局验证规则

### 2. Section (章节)

问卷的组织单元:
- `title` - 章节标题
- `description` - 章节描述
- `order` - 顺序
- `questions` - 问题列表

### 3. Question (问题)

具体的问题:
- `id` - 问题ID
- `type` - 问题类型
- `title` - 问题标题
- `description` - 问题描述
- `required` - 是否必填
- `options` - 选项列表
- `validations` - 验证规则

### 4. QuestionnaireExecutor (执行器)

负责执行问卷:
- `validate(answers)` - 验证答案
- `execute(answers)` - 执行问卷
- `getMetadata()` - 获取元数据

### 5. QuestionnaireSerializer (序列化器)

负责序列化:
- `toJson(questionnaire)` - 序列化为JSON
- `fromJson(json)` - 从JSON反序列化

## 示例场景

### 场景1: 员工满意度调查

```kotlin
val employeeSurvey = questionnaire("emp_survey_2025", "2025年员工满意度调查") {
    description = "年度员工满意度和敬业度调查"
    author = "人力资源部"
    
    section("工作环境") {
        question {
            id = "env_rating"
            type = QuestionType.RATING
            title = "您对工作环境的满意度?"
            required = true
        }
    }
    
    section("职业发展") {
        question {
            id = "career_options"
            type = QuestionType.MULTIPLE_CHOICE
            title = "您希望获得哪些发展机会?"
            
            option("技能培训")
            option("晋升机会")
            option("跨部门轮岗")
            option("海外交流")
        }
    }
}
```

### 场景2: 用户注册信息收集

```kotlin
val registration = questionnaire("user_reg", "用户注册") {
    section("基本信息") {
        question {
            id = "username"
            type = QuestionType.TEXT
            title = "用户名"
            required = true
            
            validate {
                message = "用户名长度必须在3-20个字符之间"
                when_ { answers ->
                    val username = answers["username"] as? String ?: ""
                    username.length in 3..20
                }
            }
        }
        
        question {
            id = "email"
            type = QuestionType.TEXT
            title = "电子邮箱"
            required = true
            
            validate {
                message = "请输入有效的电子邮箱地址"
                when_ { answers ->
                    val email = answers["email"] as? String ?: ""
                    email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
                }
            }
        }
    }
}
```

## 扩展和定制

### 自定义问题类型

可以通过扩展 `QuestionType` 枚举添加新的问题类型:

```kotlin
enum class CustomQuestionType {
    SIGNATURE,      // 签名
    FILE_UPLOAD,    // 文件上传
    LOCATION,       // 位置选择
    COLOR_PICKER    // 颜色选择器
}
```

### 自定义验证器

可以创建可复用的验证器:

```kotlin
object Validators {
    fun email() = ValidationRule().apply {
        name = "邮箱验证"
        message = "请输入有效的电子邮箱地址"
        when_ { answers ->
            val value = answers[questionId] as? String ?: ""
            value.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
        }
    }
    
    fun phoneNumber() = ValidationRule().apply {
        name = "手机号验证"
        message = "请输入有效的手机号码"
        when_ { answers ->
            val value = answers[questionId] as? String ?: ""
            value.matches(Regex("^1[3-9]\\d{9}$"))
        }
    }
}
```

## 最佳实践

1. **使用有意义的ID**: 为问题使用描述性的ID,便于后续维护
2. **合理组织章节**: 将相关问题组织在同一章节中
3. **提供清晰的描述**: 为问卷和问题提供详细的描述
4. **验证规则分层**: 在问题级别和全局级别合理设置验证规则
5. **版本管理**: 使用版本号管理问卷的变更

## 技术细节

- **语言**: Kotlin 1.9.22
- **JVM目标**: Java 17
- **构建工具**: Maven
- **序列化**: Jackson (Kotlin模块)
- **测试框架**: JUnit 5

## 许可证

MIT License

## 作者

mumu - yulin.1996@foxmail.com

## 更新日志

### 1.0.0 (2025-10-28)
- 初始版本发布
- 支持基本问卷DSL语法
- 实现问卷执行器
- 添加JSON序列化支持
- 完整的单元测试覆盖
