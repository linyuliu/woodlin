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
    fun fromJson(json: String): Questionnaire {
        val dto = objectMapper.readValue(json, QuestionnaireDto::class.java)
        return dtoToQuestionnaire(dto)
    }
    
    private fun dtoToQuestionnaire(dto: QuestionnaireDto): Questionnaire = 
        questionnaire(dto.id, dto.title) {
            description = dto.description
            version = dto.version
            author = dto.author
            dto.sections.forEach { sectionDto ->
                addSection(this, sectionDto)
            }
        }
    
    private fun addSection(q: Questionnaire, dto: SectionDto) {
        q.section(dto.title) {
            description = dto.description
            order = dto.order
            dto.questions.forEach { questionDto ->
                addQuestion(this, questionDto)
            }
        }
    }
    
    private fun addQuestion(s: Section, dto: QuestionDto) {
        s.question {
            id = dto.id
            type = QuestionType.valueOf(dto.type)
            title = dto.title
            description = dto.description
            required = dto.required
            order = dto.order
            dto.options.forEach { optionDto ->
                addOption(this, optionDto)
            }
        }
    }
    
    private fun addOption(q: Question, dto: OptionDto) {
        q.option(dto.text, dto.value) {
            order = dto.order
            exclusive = dto.exclusive
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
