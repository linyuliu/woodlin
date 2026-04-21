package com.mumu.woodlin.dsl

/**
 * 问卷/测评 DSL 构建器
 *
 * @author mumu
 * @description 提供声明式的问卷/量表/试卷定义语法，支持维度计分、反向题、人口学字段、常模引用等评估核心概念。
 * @since 2025-10-28
 */
@DslMarker
annotation class QuestionnaireDsl

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

/** 测评类型 */
enum class AssessmentType {
    /** 心理量表（含维度、反向题、常模等） */
    SCALE,
    /** 试卷/考试（含难度、区分度、计时等） */
    EXAM,
    /** 问卷调查（以收集数据为主，计分可选） */
    SURVEY
}

/** 维度计分模式 */
enum class ScoreMode {
    SUM, MEAN, MAX, MIN, WEIGHTED_SUM,
    /** 使用 DSL 脚本自定义计分逻辑 */
    CUSTOM_DSL
}

/** 反向计分模式 */
enum class ReverseMode {
    NONE,
    /** 公式反向：score = maxScore + minScore - rawScore */
    FORMULA,
    /** 映射表反向：通过 option.scoreReverseValue 字段指定 */
    TABLE
}

/** 随机化策略 */
enum class RandomStrategy {
    NONE, RANDOM_ITEMS, RANDOM_OPTIONS, RANDOM_BOTH
}

/** 常模分数类型 */
enum class NormScoreType {
    T_SCORE, Z_SCORE, PERCENTILE, STANINE, STEN, GRADE_EQUIVALENT, RAW_GRADE
}

/** 问题类型 */
enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE,
    TEXT, TEXTAREA,
    NUMBER, DATE, RATING,
    MATRIX_SINGLE, MATRIX_MULTIPLE,
    FILL_BLANK, SORT, SLIDER,
    /** 人口学信息题（收集受试者背景，通常不计入总分） */
    DEMOGRAPHIC,
    /** 说明性文字（不需要作答） */
    STATEMENT
}

// ---------------------------------------------------------------------------
// Model classes
// ---------------------------------------------------------------------------

/**
 * 问卷/测评
 */
@QuestionnaireDsl
class Questionnaire(val id: String, val title: String) {
    var description: String = ""
    var version: String = "1.0"
    var author: String = ""
    /** 测评类型，默认为问卷调查以保持向后兼容 */
    var assessmentType: AssessmentType = AssessmentType.SURVEY
    /** 随机化策略 */
    var randomStrategy: RandomStrategy = RandomStrategy.NONE
    val sections = mutableListOf<Section>()
    val rules = mutableListOf<ValidationRule>()
    val dimensions = mutableListOf<Dimension>()
    val demographics = mutableListOf<DemographicField>()
    val normRefs = mutableListOf<NormRef>()
    val reportTags = mutableListOf<ReportTag>()

    fun section(title: String, block: Section.() -> Unit) {
        sections += Section(title).apply(block)
    }

    fun validation(block: ValidationRule.() -> Unit) {
        rules += ValidationRule().apply(block)
    }

    /** 声明一个维度/因子 */
    fun dimension(code: String, name: String, block: Dimension.() -> Unit = {}) {
        dimensions += Dimension(code, name).apply(block)
    }

    /** 声明一个人口学字段 */
    fun demographic(fieldName: String, label: String, block: DemographicField.() -> Unit = {}) {
        demographics += DemographicField(fieldName, label).apply(block)
    }

    /** 声明一个常模引用 */
    fun normRef(normSetCode: String, normName: String, block: NormRef.() -> Unit = {}) {
        normRefs += NormRef(normSetCode, normName).apply(block)
    }

    /** 声明一个报告标签 */
    fun reportTag(tagCode: String, block: ReportTag.() -> Unit) {
        reportTags += ReportTag(tagCode).apply(block)
    }

    operator fun Section.unaryPlus() {
        this@Questionnaire.sections += this
    }

    operator fun ValidationRule.unaryPlus() {
        this@Questionnaire.rules += this
    }
}

/**
 * 维度/因子/分量表
 */
@QuestionnaireDsl
class Dimension(val code: String, val name: String) {
    var description: String = ""
    var scoreMode: ScoreMode = ScoreMode.SUM
    /** 自定义计分 DSL 表达式（当 scoreMode = CUSTOM_DSL 时使用） */
    var scoreDsl: String? = null
    /** 维度权重（在加权求和或跨维度汇总时使用） */
    var weight: Double = 1.0
    /** 父维度编码（null 表示顶层维度，支持二阶因子结构） */
    var parentCode: String? = null
    /** 关联的常模集编码 */
    var normRef: String? = null
    var sortOrder: Int = 0
}

/**
 * 人口学字段声明
 */
@QuestionnaireDsl
class DemographicField(val fieldName: String, val label: String) {
    /** 映射到的问题 ID */
    var questionId: String? = null
    var description: String = ""
}

/**
 * 常模引用
 */
@QuestionnaireDsl
class NormRef(val normSetCode: String, val normName: String) {
    /** 匹配条件表达式（基于人口学变量，如 "gender == 'F' && age >= 18"） */
    var condition: String? = null
    var normScoreType: NormScoreType = NormScoreType.T_SCORE
}

/**
 * 报告标签（分数区间映射到文字解读）
 */
@QuestionnaireDsl
class ReportTag(val tagCode: String) {
    /** 关联的维度编码 */
    var dimensionCode: String? = null
    var label: String = ""
    /** 分数区间下限（含） */
    var scoreMin: Double? = null
    /** 分数区间上限（含） */
    var scoreMax: Double? = null
    var interpretation: String = ""
}

/**
 * 章节
 */
@QuestionnaireDsl
class Section(val title: String) {
    var description: String = ""
    var order: Int = 0
    val questions = mutableListOf<Question>()

    fun question(block: Question.() -> Unit) {
        questions += Question().apply(block).also { it.order = questions.size + 1 }
    }

    operator fun Question.unaryPlus() {
        this@Section.questions += this.also { it.order = this@Section.questions.size + 1 }
    }
}

/**
 * 问题/条目
 */
@QuestionnaireDsl
class Question {
    var id: String = ""
    var type: QuestionType = QuestionType.SINGLE_CHOICE
    var title: String = ""
    var description: String = ""
    var required: Boolean = false
    var order: Int = 0
    val options = mutableListOf<Option>()
    val validations = mutableListOf<ValidationRule>()

    // --- Scoring metadata ---

    /** 是否计分题（false 则跳过计分逻辑） */
    var isScored: Boolean = true
    /** 是否锚题（用于跨版本/跨批次等值） */
    var isAnchor: Boolean = false
    /** 是否反向题 */
    var isReverse: Boolean = false
    /** 反向计分模式 */
    var reverseMode: ReverseMode = ReverseMode.NONE
    /** 单题最高分（用于公式反向计分等） */
    var maxScore: Double? = null
    /** 单题最低分 */
    var minScore: Double? = null
    /** 所属维度编码（单维度快捷关联） */
    var dimensionCode: String? = null
    /** 题目在加权求和时的权重 */
    var itemWeight: Double = 1.0
    /** 单题作答时限（秒，0 表示不限） */
    var timeLimitSeconds: Int = 0
    /** 是否人口学信息题 */
    var isDemographic: Boolean = false
    /** 人口学字段名（如 gender/age，当 isDemographic=true 时填写） */
    var demographicField: String? = null

    fun option(text: String, value: String = text, block: (Option.() -> Unit)? = null) {
        options += Option(text, value).apply { block?.invoke(this) }
    }

    fun validate(block: ValidationRule.() -> Unit) {
        validations += ValidationRule().apply(block)
    }

    operator fun Option.unaryPlus() {
        this@Question.options += this
    }

    operator fun ValidationRule.unaryPlus() {
        this@Question.validations += this
    }
}

/**
 * 选项
 */
data class Option(
    val text: String,
    val value: String,
    var order: Int = 0,
    var exclusive: Boolean = false,
    /** 正向得分值 */
    var scoreValue: Double? = null,
    /** 反向得分值（TABLE 模式反向计分时使用） */
    var scoreReverseValue: Double? = null,
    /** 是否为正确答案（试卷客观题） */
    var isCorrect: Boolean = false
)

/**
 * 验证规则
 */
@QuestionnaireDsl
class ValidationRule {
    var name: String = ""
    var message: String = ""
    var condition: ((Map<String, Any>) -> Boolean)? = null
    var expression: String? = null

    infix fun whenCondition(block: (Map<String, Any>) -> Boolean) {
        condition = block
    }

    /**
     * 使用表达式定义验证条件
     *
     * 示例:
     * ```
     * expr("age >= 18 && age <= 100")
     * ```
     */
    infix fun expr(expression: String) {
        this.expression = expression
        this.condition = { answers ->
            runCatching {
                ExprEngine.exec(expression, answers) as? Boolean ?: false
            }.getOrDefault(false)
        }
    }
}

// ---------------------------------------------------------------------------
// Top-level DSL entry points
// ---------------------------------------------------------------------------

/** 通用问卷 DSL 入口函数 */
fun questionnaire(id: String, title: String, block: Questionnaire.() -> Unit): Questionnaire =
    Questionnaire(id, title).apply(block)

/** 测评/量表 DSL 入口函数（assessmentType 默认为 SCALE） */
fun assessment(id: String, title: String, block: Questionnaire.() -> Unit): Questionnaire =
    Questionnaire(id, title).also { it.assessmentType = AssessmentType.SCALE }.apply(block)

/** 心理量表 DSL 入口函数（assessmentType = SCALE） */
fun scale(id: String, title: String, block: Questionnaire.() -> Unit): Questionnaire =
    Questionnaire(id, title).also { it.assessmentType = AssessmentType.SCALE }.apply(block)

/** 试卷/考试 DSL 入口函数（assessmentType = EXAM） */
fun exam(id: String, title: String, block: Questionnaire.() -> Unit): Questionnaire =
    Questionnaire(id, title).also { it.assessmentType = AssessmentType.EXAM }.apply(block)

/** 问卷调查 DSL 入口函数（assessmentType = SURVEY） */
fun survey(id: String, title: String, block: Questionnaire.() -> Unit): Questionnaire =
    Questionnaire(id, title).also { it.assessmentType = AssessmentType.SURVEY }.apply(block)
