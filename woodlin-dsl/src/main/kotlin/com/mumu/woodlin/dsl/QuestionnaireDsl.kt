package com.mumu.woodlin.dsl

/**
 * 问卷DSL构建器
 *
 * @author mumu
 * @description 提供声明式的问卷定义语法
 * @since 2025-10-28
 */
@DslMarker
annotation class QuestionnaireDsl

/**
 * 问卷
 */
@QuestionnaireDsl
class Questionnaire(val id: String, val title: String) {
    var description: String = ""
    var version: String = "1.0"
    var author: String = ""
    val sections = mutableListOf<Section>()
    val rules = mutableListOf<ValidationRule>()
    
    fun section(title: String, block: Section.() -> Unit) {
        val section = Section(title)
        section.block()
        sections.add(section)
    }
    
    fun validation(block: ValidationRule.() -> Unit) {
        val rule = ValidationRule()
        rule.block()
        rules.add(rule)
    }
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
        val question = Question()
        question.block()
        question.order = questions.size + 1
        questions.add(question)
    }
}

/**
 * 问题
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
    
    fun option(text: String, value: String = text, block: (Option.() -> Unit)? = null) {
        val option = Option(text, value)
        block?.invoke(option)
        options.add(option)
    }
    
    fun validate(block: ValidationRule.() -> Unit) {
        val rule = ValidationRule()
        rule.block()
        validations.add(rule)
    }
}

/**
 * 选项
 */
data class Option(
    val text: String,
    val value: String,
    var order: Int = 0,
    var exclusive: Boolean = false
)

/**
 * 问题类型
 */
enum class QuestionType {
    /**
     * 单选题
     */
    SINGLE_CHOICE,
    
    /**
     * 多选题
     */
    MULTIPLE_CHOICE,
    
    /**
     * 填空题
     */
    TEXT,
    
    /**
     * 长文本
     */
    TEXTAREA,
    
    /**
     * 数字
     */
    NUMBER,
    
    /**
     * 日期
     */
    DATE,
    
    /**
     * 评分
     */
    RATING,
    
    /**
     * 矩阵单选
     */
    MATRIX_SINGLE,
    
    /**
     * 矩阵多选
     */
    MATRIX_MULTIPLE
}

/**
 * 验证规则
 */
@QuestionnaireDsl
class ValidationRule {
    var name: String = ""
    var message: String = ""
    var condition: ((Map<String, Any>) -> Boolean)? = null
    var expression: String? = null
    
    fun when_(block: (Map<String, Any>) -> Boolean) {
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
    fun expr(expression: String) {
        this.expression = expression
        this.condition = { answers ->
            try {
                val result = ExprEngine.exec(expression, answers)
                when (result) {
                    is Boolean -> result
                    else -> false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}

/**
 * 问卷DSL入口函数
 */
fun questionnaire(id: String, title: String, block: Questionnaire.() -> Unit): Questionnaire {
    val questionnaire = Questionnaire(id, title)
    questionnaire.block()
    return questionnaire
}
