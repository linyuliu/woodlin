# Woodlin DSL 模块

## 概述

Woodlin DSL 是一个使用 Kotlin 编写的**问卷/测评**功能 DSL（领域特定语言）模块，提供简洁优雅的声明式测评定义语法。支持心理量表（Likert量表/PHQ-9等）、试卷/考试、问卷调查三种核心测评类型，内置维度计分、反向计分、常模引用及报告标签等评估核心概念。

## 特性

- ✅ **声明式语法**: 使用 Kotlin DSL 提供直观的测评定义方式
- ✅ **类型安全**: 利用 Kotlin 的类型系统确保编译时安全
- ✅ **灵活验证**: 支持内置和自定义验证规则（含 ExprEngine 表达式）
- ✅ **序列化支持**: JSON 格式完整往返，包含所有评估字段
- ✅ **执行引擎**: 内置执行器处理答案收集和验证
- ✅ **计分引擎**: ScoringEngine 支持多种维度聚合、正向/反向计分
- ✅ **评估元数据**: 维度声明、常模引用、报告标签、人口学字段

## 快速开始

### 依赖配置

```xml
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-plugin-dsl</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 测评类型

使用对应的入口函数声明测评类型：

```kotlin
// 心理量表（含维度、反向题、常模等）
val phq9 = scale("PHQ-9", "患者健康问卷-9") { ... }

// 试卷/考试（含难度、区分度、计时等）
val quiz = exam("QUIZ-001", "基础知识测验") { ... }

// 问卷调查（以收集数据为主，计分可选）
val survey = survey("SURVEY-001", "用户调研") { ... }

// 通用入口（手动设置 assessmentType）
val form = questionnaire("FORM-001", "表单") { ... }
val form2 = assessment("FORM-002", "测评") { ... }  // 默认 SCALE
```

---

## 心理量表示例（PHQ-9）

```kotlin
val phq9 = scale("PHQ-9", "患者健康问卷-9") {
    description = "抑郁症状筛查量表"
    version = "1.0"
    author = "mumu"

    // 声明维度
    dimension("depression", "抑郁总分") {
        scoreMode = ScoreMode.SUM
        weight = 1.0
    }

    // 常模引用
    normRef("NORM-ADULT-CN", "中国成年人常模") {
        normScoreType = NormScoreType.T_SCORE
        condition = "age >= 18"
    }

    // 报告标签（分数区间 → 文字解读）
    reportTag("none") {
        dimensionCode = "depression"
        label = "没有抑郁"
        scoreMin = 0.0; scoreMax = 4.0
        interpretation = "无抑郁症状"
    }
    reportTag("mild") {
        dimensionCode = "depression"
        label = "轻度抑郁"
        scoreMin = 5.0; scoreMax = 9.0
        interpretation = "有轻度抑郁症状"
    }

    section("抑郁症状") {
        question {
            id = "phq1"
            type = QuestionType.SINGLE_CHOICE
            title = "做事时提不起劲或没有兴趣"
            required = true
            isScored = true
            dimensionCode = "depression"

            option("完全没有", "0") { scoreValue = 0.0 }
            option("有几天", "1") { scoreValue = 1.0 }
            option("超过一半天数", "2") { scoreValue = 2.0 }
            option("几乎每天", "3") { scoreValue = 3.0 }
        }
        // ... 其余9道题
    }
}
```

---

## 反向计分

```kotlin
scale("STAI", "状态-特质焦虑量表") {
    dimension("trait_anxiety", "特质焦虑") { scoreMode = ScoreMode.SUM }

    section("条目") {
        // 正向题
        question {
            id = "q1"; dimensionCode = "trait_anxiety"
            type = QuestionType.SINGLE_CHOICE; isScored = true
            option("1", "1") { scoreValue = 1.0 }
            option("4", "4") { scoreValue = 4.0 }
        }

        // 反向题（公式模式）：effectiveScore = maxScore + minScore - rawScore
        question {
            id = "q2"; dimensionCode = "trait_anxiety"
            type = QuestionType.SINGLE_CHOICE; isScored = true
            isReverse = true
            reverseMode = ReverseMode.FORMULA
            maxScore = 4.0; minScore = 1.0

            option("1", "1") { scoreValue = 1.0 }
            option("4", "4") { scoreValue = 4.0 }
        }

        // 反向题（映射表模式）：从 scoreReverseValue 取值
        question {
            id = "q3"; dimensionCode = "trait_anxiety"
            type = QuestionType.SINGLE_CHOICE; isScored = true
            isReverse = true
            reverseMode = ReverseMode.TABLE

            option("1", "1") { scoreValue = 1.0; scoreReverseValue = 5.0 }
            option("2", "2") { scoreValue = 2.0; scoreReverseValue = 4.0 }
            option("3", "3") { scoreValue = 3.0; scoreReverseValue = 3.0 }
        }
    }
}
```

---

## 使用 ScoringEngine 计分

```kotlin
val engine = ScoringEngine(phq9)

val answers = mapOf(
    "phq1" to "2",   // 超过一半天数 → 2分
    "phq2" to "1",   // 有几天 → 1分
    // ...
)

val result = engine.score(answers)

println("总原始分: ${result.totalRawScore}")
println("总加权分: ${result.totalWeightedScore}")
result.dimensionScores.forEach { (code, ds) ->
    println("维度 $code (${ds.dimensionName}): ${ds.rawScore}/${ds.scoreMode}")
}
result.itemScores.forEach { item ->
    println("题 ${item.questionId}: 原始=${item.rawScore} 有效=${item.effectiveScore}")
}
```

---

## 试卷示例（含正确答案）

```kotlin
val quiz = exam("QUIZ-JAVA", "Java 基础测验") {
    section("选择题") {
        question {
            id = "q1"
            type = QuestionType.SINGLE_CHOICE
            title = "以下哪个是 Java 中的值类型?"
            isScored = true

            option("int", "A") { scoreValue = 1.0; isCorrect = true }
            option("String", "B") { scoreValue = 0.0 }
            option("Object", "C") { scoreValue = 0.0 }
        }
    }
}
```

---

## 人口学字段声明

```kotlin
survey("BIG5", "大五人格问卷") {
    // 声明人口学字段（会被收集但不计入总分）
    demographic("gender", "性别") { questionId = "q_gender" }
    demographic("age_group", "年龄段") { questionId = "q_age" }

    section("基本信息") {
        question {
            id = "q_gender"
            type = QuestionType.DEMOGRAPHIC
            title = "您的性别"
            isDemographic = true; demographicField = "gender"
            isScored = false   // 不计入总分

            option("男", "M"); option("女", "F")
        }
    }
}
```

---

## 维度计分模式

| 模式 | 说明 |
|------|------|
| `SUM` | 维度内所有条目得分之和 |
| `MEAN` | 维度内所有条目得分均值 |
| `MAX` | 维度内最高单题得分 |
| `MIN` | 维度内最低单题得分 |
| `WEIGHTED_SUM` | 按题目 `itemWeight` 加权求和 |
| `CUSTOM_DSL` | 使用 `scoreDsl` 表达式（可用变量: `sum`, `count`, `mean`, `scores`） |

---

## 核心组件

| 组件 | 说明 |
|------|------|
| `QuestionnaireDsl.kt` | DSL 模型定义（枚举、数据类、DSL Builder）|
| `QuestionnaireExecutor` | 验证答案、获取元数据 |
| `ScoringEngine` | 题目计分、反向处理、维度聚合 |
| `QuestionnaireSerializer` | JSON 序列化/反序列化（完整往返） |
| `ExprEngine` | 表达式评估（用于验证规则和自定义计分） |

---

## 问题类型

| 类型 | 说明 |
|------|------|
| `SINGLE_CHOICE` | 单选题 |
| `MULTIPLE_CHOICE` | 多选题 |
| `TEXT` / `TEXTAREA` | 短/长文本 |
| `NUMBER` | 数字 |
| `DATE` | 日期 |
| `RATING` | 评分/李克特量表 |
| `MATRIX_SINGLE/MULTIPLE` | 矩阵单选/多选 |
| `FILL_BLANK` | 填空题 |
| `SORT` | 排序题 |
| `SLIDER` | 滑块题 |
| `DEMOGRAPHIC` | 人口学信息题（不计分）|
| `STATEMENT` | 说明性文字（不作答）|

---

## 技术细节

- **语言**: Kotlin 1.9.22
- **JVM目标**: Java 17
- **构建工具**: Maven
- **序列化**: Jackson (Kotlin模块, NON_NULL策略)
- **测试框架**: JUnit 5

## 许可证

MIT License

## 作者

mumu - yulin.1996@foxmail.com

## 更新日志

### 1.1.0
- 扩展为完整评估/计分 DSL 内核
- 新增 `AssessmentType`, `ScoreMode`, `ReverseMode`, `RandomStrategy`, `NormScoreType` 枚举
- 新增 `Dimension`, `DemographicField`, `NormRef`, `ReportTag` DSL 构建器
- `Question` 增加计分元数据字段（`isScored`, `isReverse`, `reverseMode`, `maxScore`, `minScore`, `dimensionCode`, `itemWeight` 等）
- `Option` 增加 `scoreValue`, `scoreReverseValue`, `isCorrect`
- 新增 `ScoringEngine`（支持 SUM/MEAN/MAX/MIN/WEIGHTED_SUM/CUSTOM_DSL 及 FORMULA/TABLE 反向计分）
- 新增 `scale()`, `exam()`, `survey()`, `assessment()` 顶级入口函数
- `QuestionnaireExecutor` 新增 `getAssessmentMetadata()`
- `QuestionnaireSerializer` 完整往返支持所有新字段
- 保持旧版 `questionnaire()` DSL 向后兼容

### 1.0.0 (2025-10-28)
- 初始版本发布
- 支持基本问卷DSL语法
- 实现问卷执行器
- 添加JSON序列化支持
- 完整的单元测试覆盖

