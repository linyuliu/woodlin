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
    fun toJson(questionnaire: Questionnaire): String {
        val dto = QuestionnaireDto(
            id = questionnaire.id,
            title = questionnaire.title,
            description = questionnaire.description,
            version = questionnaire.version,
            author = questionnaire.author,
            sections = questionnaire.sections.map { section ->
                SectionDto(
                    title = section.title,
                    description = section.description,
                    order = section.order,
                    questions = section.questions.map { question ->
                        QuestionDto(
                            id = question.id,
                            type = question.type.name,
                            title = question.title,
                            description = question.description,
                            required = question.required,
                            order = question.order,
                            options = question.options.map { option ->
                                OptionDto(
                                    text = option.text,
                                    value = option.value,
                                    order = option.order,
                                    exclusive = option.exclusive
                                )
                            }
                        )
                    }
                )
            }
        )
        
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto)
    }
    
    /**
     * 从JSON反序列化问卷
     */
    fun fromJson(json: String): Questionnaire {
        val dto = objectMapper.readValue(json, QuestionnaireDto::class.java)
        
        return questionnaire(dto.id, dto.title) {
            description = dto.description
            version = dto.version
            author = dto.author
            
            dto.sections.forEach { sectionDto ->
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
