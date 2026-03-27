package com.mumu.woodlin.dsl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * 问卷序列化器
 *
 * @author mumu
 * @description 将问卷DSL序列化为JSON格式
 * @since 2025-10-28
 */
class QuestionnaireSerializer {
    
    private val objectMapper = jacksonObjectMapper()
    
    /**
     * 序列化问卷为JSON
     */
    fun toJson(questionnaire: Questionnaire): String =
        questionnaire.toDto()
            .let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) }
    
    private fun Questionnaire.toDto() = QuestionnaireDto(
        id = id,
        title = title,
        description = description,
        version = version,
        author = author,
        sections = sections.map { it.toDto() }
    )
    
    private fun Section.toDto() = SectionDto(
        title = title,
        description = description,
        order = order,
        questions = questions.map { it.toDto() }
    )
    
    private fun Question.toDto() = QuestionDto(
        id = id,
        type = type.name,
        title = title,
        description = description,
        required = required,
        order = order,
        options = options.map { it.toDto() }
    )
    
    private fun Option.toDto() = OptionDto(
        text = text,
        value = value,
        order = order,
        exclusive = exclusive
    )
    
    /**
     * 从JSON反序列化问卷
     */
    fun fromJson(json: String): Questionnaire =
        objectMapper.readValue(json, QuestionnaireDto::class.java).toQuestionnaire()
    
    private fun QuestionnaireDto.toQuestionnaire(): Questionnaire = 
        questionnaire(id, title) {
            description = this@toQuestionnaire.description
            version = this@toQuestionnaire.version
            author = this@toQuestionnaire.author
            this@toQuestionnaire.sections.forEach { sectionDto ->
                section(sectionDto.title) {
                    description = sectionDto.description
                    order = sectionDto.order
                    sectionDto.questions.forEach { questionDto ->
                        question {
                            id = questionDto.id
                            type = QuestionType.valueOf(questionDto.type)
                            title = questionDto.title
                            description = questionDto.description
                            required = questionDto.required
                            order = questionDto.order
                            questionDto.options.forEach { optionDto ->
                                option(optionDto.text, optionDto.value) {
                                    order = optionDto.order
                                    exclusive = optionDto.exclusive
                                }
                            }
                        }
                    }
                }
            }
        }
    
    /**
     * DTO类定义
     */
    data class QuestionnaireDto(
        val id: String,
        val title: String,
        val description: String,
        val version: String,
        val author: String,
        val sections: List<SectionDto>
    )
    
    data class SectionDto(
        val title: String,
        val description: String,
        val order: Int,
        val questions: List<QuestionDto>
    )
    
    data class QuestionDto(
        val id: String,
        val type: String,
        val title: String,
        val description: String,
        val required: Boolean,
        val order: Int,
        val options: List<OptionDto>
    )
    
    data class OptionDto(
        val text: String,
        val value: String,
        val order: Int,
        val exclusive: Boolean
    )
}
