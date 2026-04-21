package com.mumu.woodlin.dsl

/**
 * 评估计分引擎
 *
 * @author mumu
 * @description 根据问卷/量表 DSL 定义和作答数据计算各题原始分、有效分及维度汇总分，
 *              支持正向/反向计分、多种维度聚合模式以及加权求和。
 * @since 2025-10-28
 */
class ScoringEngine(private val questionnaire: Questionnaire) {

    // ---------------------------------------------------------------------------
    // Result types
    // ---------------------------------------------------------------------------

    /** 单题计分结果 */
    data class ItemScore(
        val questionId: String,
        /** 题目原始得分（选项正向 scoreValue 或直接答案值） */
        val rawScore: Double,
        /** 经反向处理后的有效得分 */
        val effectiveScore: Double,
        /** 所属维度编码 */
        val dimensionCode: String?,
        /** 该题在维度内的权重 */
        val itemWeight: Double = 1.0
    )

    /** 维度汇总得分 */
    data class DimensionScore(
        val dimensionCode: String,
        val dimensionName: String,
        val scoreMode: ScoreMode,
        val rawScore: Double,
        val weightedScore: Double,
        val itemCount: Int
    )

    /** 整体计分结果 */
    data class ScoringResult(
        /** 所有计分题有效分之和（未做维度权重）*/
        val totalRawScore: Double,
        /** 所有维度加权后总分（sum of dimensionScore.weightedScore）*/
        val totalWeightedScore: Double,
        val dimensionScores: Map<String, DimensionScore>,
        val itemScores: List<ItemScore>
    )

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    /**
     * 对给定作答计分。
     *
     * @param answers 作答 map，key 为 question.id，value 为答案（单选: String，多选: List<String>，评分: Number 等）
     */
    fun score(answers: Map<String, Any>): ScoringResult {
        val allQuestions = questionnaire.sections.flatMap { it.questions }
        val itemScores = allQuestions.mapNotNull { scoreItem(it, answers) }
        val dimensionScores = computeDimensionScores(itemScores)
        val totalRaw = itemScores.sumOf { it.effectiveScore }
        val totalWeighted = dimensionScores.values.sumOf { it.weightedScore }
        return ScoringResult(totalRaw, totalWeighted, dimensionScores, itemScores)
    }

    // ---------------------------------------------------------------------------
    // Item-level scoring
    // ---------------------------------------------------------------------------

    private fun scoreItem(question: Question, answers: Map<String, Any>): ItemScore? {
        if (!question.isScored || question.isDemographic) return null
        val answer = answers[question.id] ?: return null

        val rawScore = when (question.type) {
            QuestionType.SINGLE_CHOICE, QuestionType.RATING ->
                scoreSingleChoice(question, answer)
            QuestionType.MULTIPLE_CHOICE ->
                scoreMultipleChoice(question, answer)
            QuestionType.NUMBER ->
                (answer as? Number)?.toDouble() ?: answer.toString().toDoubleOrNull() ?: 0.0
            QuestionType.MATRIX_SINGLE ->
                scoreSingleChoice(question, answer)
            else -> 0.0
        }

        val effectiveScore = when {
            question.isReverse -> applyReverse(question, rawScore, answer)
            else -> rawScore
        }

        return ItemScore(
            questionId = question.id,
            rawScore = rawScore,
            effectiveScore = effectiveScore,
            dimensionCode = question.dimensionCode,
            itemWeight = question.itemWeight
        )
    }

    private fun scoreSingleChoice(question: Question, answer: Any): Double {
        val selected = answer.toString()
        return question.options.firstOrNull { it.value == selected }?.scoreValue ?: 0.0
    }

    private fun scoreMultipleChoice(question: Question, answer: Any): Double {
        val selectedValues: List<String> = when (answer) {
            is List<*> -> answer.map { it.toString() }
            is Array<*> -> answer.map { it.toString() }
            is String -> listOf(answer)
            else -> emptyList()
        }
        return selectedValues.sumOf { v ->
            question.options.firstOrNull { it.value == v }?.scoreValue ?: 0.0
        }
    }

    /**
     * 应用反向计分逻辑。
     *
     * - FORMULA: effectiveScore = maxScore + minScore - rawScore
     * - TABLE:   从选中选项的 scoreReverseValue 取值（若无则回退到 rawScore）
     * - NONE:    不做反向，直接返回 rawScore
     */
    private fun applyReverse(question: Question, rawScore: Double, answer: Any): Double {
        return when (question.reverseMode) {
            ReverseMode.FORMULA -> {
                val max = question.maxScore ?: return rawScore
                val min = question.minScore ?: 0.0
                max + min - rawScore
            }
            ReverseMode.TABLE -> {
                val selected = answer.toString()
                question.options.firstOrNull { it.value == selected }
                    ?.scoreReverseValue ?: rawScore
            }
            ReverseMode.NONE -> rawScore
        }
    }

    // ---------------------------------------------------------------------------
    // Dimension-level aggregation
    // ---------------------------------------------------------------------------

    private fun computeDimensionScores(itemScores: List<ItemScore>): Map<String, DimensionScore> {
        val dimMap = questionnaire.dimensions.associateBy { it.code }
        return itemScores
            .filter { it.dimensionCode != null }
            .groupBy { it.dimensionCode!! }
            .mapValues { (code, scores) ->
                val dim = dimMap[code]
                val scoreMode = dim?.scoreMode ?: ScoreMode.SUM
                val dimWeight = dim?.weight ?: 1.0

                val rawScore = aggregate(scoreMode, scores, dim?.scoreDsl)
                val weightedScore = rawScore * dimWeight

                DimensionScore(
                    dimensionCode = code,
                    dimensionName = dim?.name ?: code,
                    scoreMode = scoreMode,
                    rawScore = rawScore,
                    weightedScore = weightedScore,
                    itemCount = scores.size
                )
            }
    }

    private fun aggregate(mode: ScoreMode, scores: List<ItemScore>, scoreDsl: String?): Double {
        if (scores.isEmpty()) return 0.0
        return when (mode) {
            ScoreMode.SUM ->
                scores.sumOf { it.effectiveScore }
            ScoreMode.MEAN ->
                scores.sumOf { it.effectiveScore } / scores.size
            ScoreMode.MAX ->
                scores.maxOf { it.effectiveScore }
            ScoreMode.MIN ->
                scores.minOf { it.effectiveScore }
            ScoreMode.WEIGHTED_SUM ->
                scores.sumOf { it.effectiveScore * it.itemWeight }
            ScoreMode.CUSTOM_DSL -> {
                if (scoreDsl != null) {
                    val ctx = mapOf(
                        "scores" to scores.map { it.effectiveScore },
                        "sum" to scores.sumOf { it.effectiveScore },
                        "count" to scores.size.toDouble(),
                        "mean" to scores.sumOf { it.effectiveScore } / scores.size
                    )
                    runCatching {
                        (ExprEngine.exec(scoreDsl, ctx) as? Number)?.toDouble()
                    }.getOrNull() ?: scores.sumOf { it.effectiveScore }
                } else {
                    scores.sumOf { it.effectiveScore }
                }
            }
        }
    }
}
