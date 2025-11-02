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
    fun validate(answers: Map<String, Any>): ValidationResult =
        (questionnaire.sections
            .asSequence()
            .flatMap { it.questions }
            .flatMap { validateQuestion(it, answers).asSequence() } +
        questionnaire.rules
            .asSequence()
            .mapNotNull { validateRule(it, answers, "global") })
            .toList()
            .let { ValidationResult(valid = it.isEmpty(), errors = it) }
    
    private fun validateQuestion(question: Question, answers: Map<String, Any>): List<ValidationError> =
        buildList {
            if (question.required && question.id !in answers) {
                add(ValidationError(question.id, "${question.title} 是必填项"))
            }
            addAll(question.validations.mapNotNull { validateRule(it, answers, question.id) })
        }
    
    private fun validateRule(rule: ValidationRule, answers: Map<String, Any>, questionId: String): ValidationError? =
        rule.condition?.takeUnless { it(answers) }?.let { ValidationError(questionId, rule.message) }
    
    /**
     * 执行问卷
     */
    fun execute(answersMap: Map<String, Any>): ExecutionResult =
        ExecutionResult(
            questionnaireId = questionnaire.id,
            answers = answersMap.map { (questionId, value) -> Answer(questionId, value) },
            validationResult = validate(answersMap)
        )
    
    /**
     * 获取问卷元数据
     */
    fun getMetadata(): QuestionnaireMetadata =
        QuestionnaireMetadata(
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
