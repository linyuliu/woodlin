package com.mumu.woodlin.dsl

/**
 * 问卷执行器
 *
 * @author mumu
 * @description 执行问卷,收集和验证答案
 * @since 2025-10-28
 */
class QuestionnaireExecutor(private val questionnaire: Questionnaire) {
    
    /**
     * 问卷答案
     */
    data class Answer(
        val questionId: String,
        val value: Any,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 验证结果
     */
    data class ValidationResult(
        val valid: Boolean,
        val errors: List<ValidationError> = emptyList()
    ) {
        fun isInvalid() = !valid
    }
    
    /**
     * 验证错误
     */
    data class ValidationError(
        val questionId: String,
        val message: String
    )
    
    /**
     * 执行结果
     */
    data class ExecutionResult(
        val questionnaireId: String,
        val answers: List<Answer>,
        val validationResult: ValidationResult,
        val completedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 验证答案
     */
    fun validate(answers: Map<String, Any>): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        questionnaire.sections.forEach { section ->
            section.questions.forEach { question ->
                if (question.required && !answers.containsKey(question.id)) {
                    errors.add(
                        ValidationError(
                            question.id,
                            "${question.title} 是必填项"
                        )
                    )
                }
                
                question.validations.forEach { rule ->
                    val condition = rule.condition
                    if (condition != null && !condition(answers)) {
                        errors.add(
                            ValidationError(
                                question.id,
                                rule.message
                            )
                        )
                    }
                }
            }
        }
        
        questionnaire.rules.forEach { rule ->
            val condition = rule.condition
            if (condition != null && !condition(answers)) {
                errors.add(
                    ValidationError(
                        "global",
                        rule.message
                    )
                )
            }
        }
        
        return ValidationResult(
            valid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * 执行问卷
     */
    fun execute(answersMap: Map<String, Any>): ExecutionResult {
        val validationResult = validate(answersMap)
        
        val answers = answersMap.map { (questionId, value) ->
            Answer(questionId, value)
        }
        
        return ExecutionResult(
            questionnaireId = questionnaire.id,
            answers = answers,
            validationResult = validationResult
        )
    }
    
    /**
     * 获取问卷元数据
     */
    fun getMetadata(): QuestionnaireMetadata {
        return QuestionnaireMetadata(
            id = questionnaire.id,
            title = questionnaire.title,
            description = questionnaire.description,
            version = questionnaire.version,
            author = questionnaire.author,
            totalQuestions = questionnaire.sections.sumOf { it.questions.size },
            requiredQuestions = questionnaire.sections
                .flatMap { it.questions }
                .count { it.required }
        )
    }
    
    /**
     * 问卷元数据
     */
    data class QuestionnaireMetadata(
        val id: String,
        val title: String,
        val description: String,
        val version: String,
        val author: String,
        val totalQuestions: Int,
        val requiredQuestions: Int
    )
}
