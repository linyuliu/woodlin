package com.mumu.woodlin.dsl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * 问卷/测评序列化器
 *
 * @author mumu
 * @description 将问卷/测评 DSL 序列化为 JSON 格式，支持完整的评估概念字段往返。
 * @since 2025-10-28
 */
class QuestionnaireSerializer {

    private val objectMapper = jacksonObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    /** 序列化问卷为 JSON */
    fun toJson(questionnaire: Questionnaire): String =
        questionnaire.toDto()
            .let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) }

    private fun Questionnaire.toDto() = QuestionnaireDto(
        id = id,
        title = title,
        description = description,
        version = version,
        author = author,
        assessmentType = assessmentType.name,
        randomStrategy = randomStrategy.name,
        sections = sections.map { it.toDto() },
        dimensions = dimensions.map { it.toDto() },
        demographics = demographics.map { it.toDto() },
        normRefs = normRefs.map { it.toDto() },
        reportTags = reportTags.map { it.toDto() }
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
        options = options.map { it.toDto() },
        isScored = isScored,
        isAnchor = isAnchor,
        isReverse = isReverse,
        reverseMode = reverseMode.name,
        maxScore = maxScore,
        minScore = minScore,
        dimensionCode = dimensionCode,
        itemWeight = itemWeight,
        timeLimitSeconds = timeLimitSeconds,
        isDemographic = isDemographic,
        demographicField = demographicField
    )

    private fun Option.toDto() = OptionDto(
        text = text,
        value = value,
        order = order,
        exclusive = exclusive,
        scoreValue = scoreValue,
        scoreReverseValue = scoreReverseValue,
        isCorrect = isCorrect
    )

    private fun Dimension.toDto() = DimensionDto(
        code = code,
        name = name,
        description = description,
        scoreMode = scoreMode.name,
        scoreDsl = scoreDsl,
        weight = weight,
        parentCode = parentCode,
        normRef = normRef,
        sortOrder = sortOrder
    )

    private fun DemographicField.toDto() = DemographicFieldDto(
        fieldName = fieldName,
        label = label,
        questionId = questionId,
        description = description
    )

    private fun NormRef.toDto() = NormRefDto(
        normSetCode = normSetCode,
        normName = normName,
        condition = condition,
        normScoreType = normScoreType.name
    )

    private fun ReportTag.toDto() = ReportTagDto(
        tagCode = tagCode,
        dimensionCode = dimensionCode,
        label = label,
        scoreMin = scoreMin,
        scoreMax = scoreMax,
        interpretation = interpretation
    )

    /** 从 JSON 反序列化问卷 */
    fun fromJson(json: String): Questionnaire =
        objectMapper.readValue(json, QuestionnaireDto::class.java).toQuestionnaire()

    private fun QuestionnaireDto.toQuestionnaire(): Questionnaire =
        questionnaire(id, title) {
            description = this@toQuestionnaire.description
            version = this@toQuestionnaire.version
            author = this@toQuestionnaire.author
            assessmentType = runCatching { AssessmentType.valueOf(this@toQuestionnaire.assessmentType) }
                .getOrDefault(AssessmentType.SURVEY)
            randomStrategy = runCatching { RandomStrategy.valueOf(this@toQuestionnaire.randomStrategy) }
                .getOrDefault(RandomStrategy.NONE)
            this@toQuestionnaire.sections.forEach { sectionDto ->
                section(sectionDto.title) {
                    description = sectionDto.description
                    order = sectionDto.order
                    sectionDto.questions.forEach { questionDto ->
                        question {
                            id = questionDto.id
                            type = runCatching { QuestionType.valueOf(questionDto.type) }
                                .getOrDefault(QuestionType.SINGLE_CHOICE)
                            title = questionDto.title
                            description = questionDto.description
                            required = questionDto.required
                            order = questionDto.order
                            isScored = questionDto.isScored
                            isAnchor = questionDto.isAnchor
                            isReverse = questionDto.isReverse
                            reverseMode = runCatching { ReverseMode.valueOf(questionDto.reverseMode) }
                                .getOrDefault(ReverseMode.NONE)
                            maxScore = questionDto.maxScore
                            minScore = questionDto.minScore
                            dimensionCode = questionDto.dimensionCode
                            itemWeight = questionDto.itemWeight
                            timeLimitSeconds = questionDto.timeLimitSeconds
                            isDemographic = questionDto.isDemographic
                            demographicField = questionDto.demographicField
                            questionDto.options.forEach { optionDto ->
                                option(optionDto.text, optionDto.value) {
                                    order = optionDto.order
                                    exclusive = optionDto.exclusive
                                    scoreValue = optionDto.scoreValue
                                    scoreReverseValue = optionDto.scoreReverseValue
                                    isCorrect = optionDto.isCorrect
                                }
                            }
                        }
                    }
                }
            }
            this@toQuestionnaire.dimensions.forEach { dto ->
                dimension(dto.code, dto.name) {
                    description = dto.description
                    scoreMode = runCatching { ScoreMode.valueOf(dto.scoreMode) }.getOrDefault(ScoreMode.SUM)
                    scoreDsl = dto.scoreDsl
                    weight = dto.weight
                    parentCode = dto.parentCode
                    normRef = dto.normRef
                    sortOrder = dto.sortOrder
                }
            }
            this@toQuestionnaire.demographics.forEach { dto ->
                demographic(dto.fieldName, dto.label) {
                    questionId = dto.questionId
                    description = dto.description
                }
            }
            this@toQuestionnaire.normRefs.forEach { dto ->
                normRef(dto.normSetCode, dto.normName) {
                    condition = dto.condition
                    normScoreType = runCatching { NormScoreType.valueOf(dto.normScoreType) }
                        .getOrDefault(NormScoreType.T_SCORE)
                }
            }
            this@toQuestionnaire.reportTags.forEach { dto ->
                reportTag(dto.tagCode) {
                    dimensionCode = dto.dimensionCode
                    label = dto.label
                    scoreMin = dto.scoreMin
                    scoreMax = dto.scoreMax
                    interpretation = dto.interpretation
                }
            }
        }

    // ---------------------------------------------------------------------------
    // DTO classes
    // ---------------------------------------------------------------------------

    data class QuestionnaireDto(
        val id: String,
        val title: String,
        val description: String = "",
        val version: String = "1.0",
        val author: String = "",
        val assessmentType: String = "SURVEY",
        val randomStrategy: String = "NONE",
        val sections: List<SectionDto> = emptyList(),
        val dimensions: List<DimensionDto> = emptyList(),
        val demographics: List<DemographicFieldDto> = emptyList(),
        val normRefs: List<NormRefDto> = emptyList(),
        val reportTags: List<ReportTagDto> = emptyList()
    )

    data class SectionDto(
        val title: String,
        val description: String = "",
        val order: Int = 0,
        val questions: List<QuestionDto> = emptyList()
    )

    data class QuestionDto(
        val id: String,
        val type: String,
        val title: String,
        val description: String = "",
        val required: Boolean = false,
        val order: Int = 0,
        val options: List<OptionDto> = emptyList(),
        val isScored: Boolean = true,
        val isAnchor: Boolean = false,
        val isReverse: Boolean = false,
        val reverseMode: String = "NONE",
        val maxScore: Double? = null,
        val minScore: Double? = null,
        val dimensionCode: String? = null,
        val itemWeight: Double = 1.0,
        val timeLimitSeconds: Int = 0,
        val isDemographic: Boolean = false,
        val demographicField: String? = null
    )

    data class OptionDto(
        val text: String,
        val value: String,
        val order: Int = 0,
        val exclusive: Boolean = false,
        val scoreValue: Double? = null,
        val scoreReverseValue: Double? = null,
        val isCorrect: Boolean = false
    )

    data class DimensionDto(
        val code: String,
        val name: String,
        val description: String = "",
        val scoreMode: String = "SUM",
        val scoreDsl: String? = null,
        val weight: Double = 1.0,
        val parentCode: String? = null,
        val normRef: String? = null,
        val sortOrder: Int = 0
    )

    data class DemographicFieldDto(
        val fieldName: String,
        val label: String,
        val questionId: String? = null,
        val description: String = ""
    )

    data class NormRefDto(
        val normSetCode: String,
        val normName: String,
        val condition: String? = null,
        val normScoreType: String = "T_SCORE"
    )

    data class ReportTagDto(
        val tagCode: String,
        val dimensionCode: String? = null,
        val label: String = "",
        val scoreMin: Double? = null,
        val scoreMax: Double? = null,
        val interpretation: String = ""
    )
}
