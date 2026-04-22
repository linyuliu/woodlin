package com.mumu.woodlin.dsl

/**
 * 评估计分引擎
 *
 * @author mumu
 * @description 根据问卷/量表 DSL 定义和作答数据计算题目有效分、维度贡献和总分。
 * @since 2025-10-28
 */
class ScoringEngine(private val questionnaire: Questionnaire) {

    /** 单题到单维度的贡献明细 */
    data class DimensionContribution(
        val questionId: String,
        val dimensionCode: String,
        val rawScore: Double,
        val effectiveScore: Double,
        val itemWeight: Double = 1.0,
        val reverseMode: ReverseMode = ReverseMode.NONE,
        val scoreModeOverride: ScoreMode? = null
    )

    /** 单题计分结果 */
    data class ItemScore(
        val questionId: String,
        val rawScore: Double,
        val effectiveScore: Double,
        val contributions: List<DimensionContribution> = emptyList()
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
        val totalRawScore: Double,
        val totalWeightedScore: Double,
        val dimensionScores: Map<String, DimensionScore>,
        val itemScores: List<ItemScore>,
        val dimensionContributions: List<DimensionContribution>
    )

    /**
     * 对给定作答计分。
     *
     * @param answers 作答 map，key 为 question.id，value 为答案
     */
    fun score(answers: Map<String, Any>): ScoringResult {
        val allQuestions = questionnaire.sections.flatMap { it.questions }
        val itemScores = allQuestions.mapNotNull { scoreItem(it, answers) }
        val dimensionContributions = itemScores.flatMap { it.contributions }
        val dimensionScores = computeDimensionScores(dimensionContributions)
        val totalRaw = itemScores.sumOf { it.effectiveScore }
        val totalWeighted = if (dimensionScores.isEmpty()) {
            totalRaw
        } else {
            dimensionScores.values.sumOf { it.weightedScore }
        }
        return ScoringResult(totalRaw, totalWeighted, dimensionScores, itemScores, dimensionContributions)
    }

    private fun scoreItem(question: Question, answers: Map<String, Any>): ItemScore? {
        if (!question.isScored || question.isDemographic) return null
        val answer = answers[question.id] ?: return null

        val rawScore = when (question.type) {
            QuestionType.SINGLE_CHOICE, QuestionType.RATING, QuestionType.MATRIX_SINGLE ->
                scoreSingleChoice(question, answer)

            QuestionType.MULTIPLE_CHOICE, QuestionType.MATRIX_MULTIPLE ->
                scoreMultipleChoice(question, answer)
            QuestionType.NUMBER ->
                (answer as? Number)?.toDouble() ?: answer.toString().toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        val questionReverseMode = if (question.isReverse) question.reverseMode else ReverseMode.NONE
        val bindings = resolveBindings(question, questionReverseMode)
        val contributions = bindings.map { binding ->
            val reverseMode = binding.reverseModeOverride ?: questionReverseMode
            val effectiveScore = if (reverseMode == ReverseMode.NONE) {
                rawScore
            } else {
                applyReverse(question, rawScore, answer, reverseMode)
            }
            DimensionContribution(
                questionId = question.id,
                dimensionCode = binding.dimensionCode,
                rawScore = rawScore,
                effectiveScore = effectiveScore,
                itemWeight = binding.weight,
                reverseMode = reverseMode,
                scoreModeOverride = binding.scoreModeOverride
            )
        }

        val effectiveScore = contributions.firstOrNull()?.effectiveScore
            ?: if (questionReverseMode == ReverseMode.NONE) rawScore else applyReverse(
                question,
                rawScore,
                answer,
                questionReverseMode
            )

        return ItemScore(
            questionId = question.id,
            rawScore = rawScore,
            effectiveScore = effectiveScore,
            contributions = contributions
        )
    }

    private fun resolveBindings(question: Question, questionReverseMode: ReverseMode): List<DimensionBinding> {
        if (question.dimensionBindings.isNotEmpty()) {
            return question.dimensionBindings
        }
        val dimensionCode = question.dimensionCode ?: return emptyList()
        return listOf(DimensionBinding(dimensionCode).apply {
            weight = question.itemWeight
            reverseModeOverride = questionReverseMode.takeIf { it != ReverseMode.NONE }
        })
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
        return selectedValues.sumOf { value ->
            question.options.firstOrNull { it.value == value }?.scoreValue ?: 0.0
        }
    }

    /**
     * 应用反向计分逻辑。
     */
    private fun applyReverse(question: Question, rawScore: Double, answer: Any, reverseMode: ReverseMode): Double {
        return when (reverseMode) {
            ReverseMode.FORMULA -> {
                val max = question.maxScore ?: return rawScore
                val min = question.minScore ?: 0.0
                max + min - rawScore
            }
            ReverseMode.TABLE -> {
                val selected = answer.toString()
                question.options.firstOrNull { it.value == selected }?.scoreReverseValue ?: rawScore
            }
            ReverseMode.NONE -> rawScore
        }
    }

    private fun computeDimensionScores(contributions: List<DimensionContribution>): Map<String, DimensionScore> {
        if (contributions.isEmpty()) return emptyMap()

        val dimMap = questionnaire.dimensions.associateBy { it.code }
        return contributions
            .groupBy { it.dimensionCode }
            .mapValues { (code, scores) ->
                val dim = dimMap[code]
                val scoreMode = scores.firstNotNullOfOrNull { it.scoreModeOverride } ?: dim?.scoreMode ?: ScoreMode.SUM
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

    private fun aggregate(mode: ScoreMode, scores: List<DimensionContribution>, scoreDsl: String?): Double {
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
                    val values = scores.map { it.effectiveScore }
                    val ctx = mapOf(
                        "scores" to values,
                        "weights" to scores.map { it.itemWeight },
                        "sum" to values.sum(),
                        "count" to scores.size.toDouble(),
                        "mean" to values.sum() / scores.size
                    )
                    runCatching {
                        (ExprEngine.exec(scoreDsl, ctx) as? Number)?.toDouble()
                    }.getOrNull() ?: values.sum()
                } else {
                    scores.sumOf { it.effectiveScore }
                }
            }
        }
    }
}
