package com.mumu.woodlin.dsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * 测评/量表 DSL 及计分引擎测试
 *
 * @author mumu
 * @since 2025-10-28
 */
class AssessmentDslTest {

    // -------------------------------------------------------------------------
    // DSL construction tests
    // -------------------------------------------------------------------------

    @Test
    fun `scale DSL 构建维度与计分元数据`() {
        val phq9 = scale("PHQ-9", "患者健康问卷-9") {
            description = "抑郁症状筛查量表"
            version = "1.0"
            author = "mumu"

            dimension("depression", "抑郁总分") {
                scoreMode = ScoreMode.SUM
                weight = 1.0
            }

            section("抑郁症状") {
                repeat(3) { i ->
                    question {
                        id = "q${i + 1}"
                        type = QuestionType.SINGLE_CHOICE
                        title = "症状 ${i + 1}"
                        required = true
                        isScored = true
                        dimensionCode = "depression"

                        option("完全没有", "0") { scoreValue = 0.0 }
                        option("有几天", "1") { scoreValue = 1.0 }
                        option("超过一半天数", "2") { scoreValue = 2.0 }
                        option("几乎每天", "3") { scoreValue = 3.0 }
                    }
                }
            }
        }

        assertEquals(AssessmentType.SCALE, phq9.assessmentType)
        assertEquals(1, phq9.dimensions.size)
        assertEquals("depression", phq9.dimensions[0].code)
        assertEquals(ScoreMode.SUM, phq9.dimensions[0].scoreMode)
        assertEquals(3, phq9.sections.flatMap { it.questions }.size)
    }

    @Test
    fun `exam DSL 构建试卷`() {
        val quiz = exam("QUIZ-001", "基础知识测验") {
            section("选择题") {
                question {
                    id = "q1"
                    type = QuestionType.SINGLE_CHOICE
                    title = "Kotlin 的 data class 默认实现哪些方法?"
                    isScored = true

                    option("equals/hashCode/toString", "A") {
                        scoreValue = 1.0
                        isCorrect = true
                    }
                    option("只有 toString", "B") { scoreValue = 0.0 }
                    option("只有 equals", "C") { scoreValue = 0.0 }
                }
            }
        }

        assertEquals(AssessmentType.EXAM, quiz.assessmentType)
        val q1 = quiz.sections[0].questions[0]
        val correctOption = q1.options.first { it.isCorrect }
        assertEquals("A", correctOption.value)
        assertEquals(1.0, correctOption.scoreValue)
    }

    @Test
    fun `survey DSL 支持人口学字段声明`() {
        val s = survey("DEMO-SURVEY", "人口统计调查") {
            demographic("gender", "性别") {
                questionId = "q_gender"
            }
            demographic("age_group", "年龄段") {
                questionId = "q_age"
            }

            section("基本信息") {
                question {
                    id = "q_gender"
                    type = QuestionType.DEMOGRAPHIC
                    title = "您的性别"
                    isDemographic = true
                    demographicField = "gender"
                    isScored = false

                    option("男", "M")
                    option("女", "F")
                }
            }
        }

        assertEquals(AssessmentType.SURVEY, s.assessmentType)
        assertEquals(2, s.demographics.size)
        assertEquals("gender", s.demographics[0].fieldName)
        assertEquals("q_gender", s.demographics[0].questionId)
    }

    @Test
    fun `assessment DSL 支持反向题与常模引用`() {
        val form = assessment("SCALE-001", "反向题测试量表") {
            dimension("anxiety", "焦虑") { scoreMode = ScoreMode.SUM }

            normRef("NORM-ADULT-CN", "中国成年人常模") {
                normScoreType = NormScoreType.T_SCORE
            }

            section("条目") {
                question {
                    id = "r1"
                    type = QuestionType.SINGLE_CHOICE
                    title = "我感到平静"
                    isReverse = true
                    reverseMode = ReverseMode.FORMULA
                    maxScore = 4.0
                    minScore = 1.0
                    dimensionCode = "anxiety"

                    option("几乎没有", "1") { scoreValue = 1.0 }
                    option("有时", "2") { scoreValue = 2.0 }
                    option("经常", "3") { scoreValue = 3.0 }
                    option("几乎总是", "4") { scoreValue = 4.0 }
                }
                question {
                    id = "r2"
                    type = QuestionType.SINGLE_CHOICE
                    title = "我感到焦虑"
                    isReverse = false
                    reverseMode = ReverseMode.NONE
                    dimensionCode = "anxiety"

                    option("几乎没有", "1") { scoreValue = 1.0 }
                    option("有时", "2") { scoreValue = 2.0 }
                    option("经常", "3") { scoreValue = 3.0 }
                    option("几乎总是", "4") { scoreValue = 4.0 }
                }
            }
        }

        assertEquals(1, form.normRefs.size)
        assertEquals("NORM-ADULT-CN", form.normRefs[0].normSetCode)
        assertEquals(NormScoreType.T_SCORE, form.normRefs[0].normScoreType)

        val q = form.sections[0].questions[0]
        assertTrue(q.isReverse)
        assertEquals(ReverseMode.FORMULA, q.reverseMode)
        assertEquals(4.0, q.maxScore)
        assertEquals(1.0, q.minScore)
    }

    @Test
    fun `assessment DSL 支持报告标签`() {
        val form = scale("BDI-II", "贝克抑郁量表") {
            dimension("total", "总分") { scoreMode = ScoreMode.SUM }

            reportTag("minimal") {
                dimensionCode = "total"
                label = "最小抑郁"
                scoreMin = 0.0
                scoreMax = 13.0
                interpretation = "没有或极少抑郁症状"
            }
            reportTag("mild") {
                dimensionCode = "total"
                label = "轻度抑郁"
                scoreMin = 14.0
                scoreMax = 19.0
                interpretation = "轻度抑郁症状"
            }
        }

        assertEquals(2, form.reportTags.size)
        assertEquals("minimal", form.reportTags[0].tagCode)
        assertEquals(0.0, form.reportTags[0].scoreMin)
        assertEquals(13.0, form.reportTags[0].scoreMax)
    }

    @Test
    fun `randomStrategy 可在测评中声明`() {
        val form = scale("RAND-SCALE", "随机量表") {
            randomStrategy = RandomStrategy.RANDOM_BOTH
        }
        assertEquals(RandomStrategy.RANDOM_BOTH, form.randomStrategy)
    }

    // -------------------------------------------------------------------------
    // ScoringEngine tests
    // -------------------------------------------------------------------------

    @Test
    fun `ScoringEngine - 单选题正向计分 SUM 模式`() {
        val form = scale("S1", "量表1") {
            dimension("dep", "抑郁") { scoreMode = ScoreMode.SUM }

            section("条目") {
                question {
                    id = "q1"; dimensionCode = "dep"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("0分", "0") { scoreValue = 0.0 }
                    option("1分", "1") { scoreValue = 1.0 }
                    option("2分", "2") { scoreValue = 2.0 }
                    option("3分", "3") { scoreValue = 3.0 }
                }
                question {
                    id = "q2"; dimensionCode = "dep"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("0分", "0") { scoreValue = 0.0 }
                    option("1分", "1") { scoreValue = 1.0 }
                    option("2分", "2") { scoreValue = 2.0 }
                    option("3分", "3") { scoreValue = 3.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        val result = engine.score(mapOf("q1" to "2", "q2" to "3"))

        assertEquals(5.0, result.totalRawScore)
        assertNotNull(result.dimensionScores["dep"])
        assertEquals(5.0, result.dimensionScores["dep"]!!.rawScore)
        assertEquals(ScoreMode.SUM, result.dimensionScores["dep"]!!.scoreMode)
        assertEquals(2, result.dimensionScores["dep"]!!.itemCount)
    }

    @Test
    fun `ScoringEngine - MEAN 维度计分模式`() {
        val form = scale("S2", "量表2") {
            dimension("anx", "焦虑") { scoreMode = ScoreMode.MEAN }

            section("条目") {
                question {
                    id = "a1"; dimensionCode = "anx"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("1", "1") { scoreValue = 1.0 }
                    option("3", "3") { scoreValue = 3.0 }
                }
                question {
                    id = "a2"; dimensionCode = "anx"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("1", "1") { scoreValue = 1.0 }
                    option("3", "3") { scoreValue = 3.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        val result = engine.score(mapOf("a1" to "1", "a2" to "3"))

        assertEquals(2.0, result.dimensionScores["anx"]!!.rawScore)
        assertEquals(ScoreMode.MEAN, result.dimensionScores["anx"]!!.scoreMode)
    }

    @Test
    fun `ScoringEngine - 公式反向计分 (FORMULA)`() {
        val form = scale("S3", "反向量表") {
            dimension("dim", "维度") { scoreMode = ScoreMode.SUM }

            section("条目") {
                question {
                    id = "fwd"; dimensionCode = "dim"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    isReverse = false
                    option("1", "1") { scoreValue = 1.0 }
                    option("4", "4") { scoreValue = 4.0 }
                }
                question {
                    id = "rev"; dimensionCode = "dim"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    isReverse = true
                    reverseMode = ReverseMode.FORMULA
                    maxScore = 4.0; minScore = 1.0
                    option("1", "1") { scoreValue = 1.0 }
                    option("4", "4") { scoreValue = 4.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        // fwd选4=4分, rev选1=1分，公式反向后为 4+1-1=4 → 总计8
        val result = engine.score(mapOf("fwd" to "4", "rev" to "1"))

        val items = result.itemScores.associateBy { it.questionId }
        assertEquals(4.0, items["fwd"]!!.effectiveScore)
        assertEquals(1.0, items["rev"]!!.rawScore)
        assertEquals(4.0, items["rev"]!!.effectiveScore, "公式反向: 4+1-1=4")
        assertEquals(8.0, result.dimensionScores["dim"]!!.rawScore)
    }

    @Test
    fun `ScoringEngine - TABLE 反向计分模式`() {
        val form = scale("S4", "TABLE反向量表") {
            dimension("d", "维度") { scoreMode = ScoreMode.SUM }

            section("条目") {
                question {
                    id = "q1"; dimensionCode = "d"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    isReverse = true
                    reverseMode = ReverseMode.TABLE

                    option("完全不同意", "1") { scoreValue = 1.0; scoreReverseValue = 5.0 }
                    option("不同意", "2") { scoreValue = 2.0; scoreReverseValue = 4.0 }
                    option("中立", "3") { scoreValue = 3.0; scoreReverseValue = 3.0 }
                    option("同意", "4") { scoreValue = 4.0; scoreReverseValue = 2.0 }
                    option("完全同意", "5") { scoreValue = 5.0; scoreReverseValue = 1.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        // 选 "1" (最低正向分), TABLE反向应得 5.0
        val result = engine.score(mapOf("q1" to "1"))

        val item = result.itemScores.first()
        assertEquals(1.0, item.rawScore)
        assertEquals(5.0, item.effectiveScore, "TABLE 反向: 选 '1' 对应 scoreReverseValue=5.0")
    }

    @Test
    fun `ScoringEngine - 多选题计分`() {
        val form = scale("S5", "多选量表") {
            dimension("dim", "维度") { scoreMode = ScoreMode.SUM }

            section("条目") {
                question {
                    id = "mc"; dimensionCode = "dim"
                    type = QuestionType.MULTIPLE_CHOICE; isScored = true
                    option("选项A", "A") { scoreValue = 1.0 }
                    option("选项B", "B") { scoreValue = 2.0 }
                    option("选项C", "C") { scoreValue = 3.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        val result = engine.score(mapOf("mc" to listOf("A", "C")))

        assertEquals(4.0, result.totalRawScore, "A(1) + C(3) = 4")
    }

    @Test
    fun `ScoringEngine - 加权求和维度模式 (WEIGHTED_SUM)`() {
        val form = scale("S6", "加权量表") {
            dimension("d1", "维度1") { scoreMode = ScoreMode.WEIGHTED_SUM; weight = 2.0 }
            dimension("d2", "维度2") { scoreMode = ScoreMode.SUM; weight = 1.0 }

            section("条目") {
                question {
                    id = "q1"; dimensionCode = "d1"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    itemWeight = 1.5
                    option("2", "2") { scoreValue = 2.0 }
                }
                question {
                    id = "q2"; dimensionCode = "d2"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("3", "3") { scoreValue = 3.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        val result = engine.score(mapOf("q1" to "2", "q2" to "3"))

        // d1: WEIGHTED_SUM = 2.0 * 1.5 = 3.0; dimensionWeight=2.0 → weightedScore = 6.0
        assertEquals(3.0, result.dimensionScores["d1"]!!.rawScore, "WEIGHTED_SUM: 2.0 * 1.5 = 3.0")
        assertEquals(6.0, result.dimensionScores["d1"]!!.weightedScore, "3.0 * dimensionWeight(2.0)")
        // d2: SUM=3.0; dimensionWeight=1.0 → weightedScore=3.0
        assertEquals(3.0, result.dimensionScores["d2"]!!.rawScore)
        assertEquals(3.0, result.dimensionScores["d2"]!!.weightedScore)
        // totalWeighted = 6.0 + 3.0
        assertEquals(9.0, result.totalWeightedScore)
    }

    @Test
    fun `ScoringEngine - 人口学题不计分`() {
        val form = scale("S7", "混合量表") {
            dimension("dep", "抑郁") { scoreMode = ScoreMode.SUM }

            section("条目") {
                question {
                    id = "gender"
                    type = QuestionType.DEMOGRAPHIC
                    isDemographic = true; isScored = false
                    option("男", "M"); option("女", "F")
                }
                question {
                    id = "q1"; dimensionCode = "dep"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("1", "1") { scoreValue = 1.0 }
                    option("3", "3") { scoreValue = 3.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        val result = engine.score(mapOf("gender" to "M", "q1" to "3"))

        assertEquals(1, result.itemScores.size, "人口学题应被排除")
        assertEquals("q1", result.itemScores[0].questionId)
        assertEquals(3.0, result.totalRawScore)
    }

    @Test
    fun `ScoringEngine - 未作答题目不参与计分`() {
        val form = scale("S8", "部分作答量表") {
            dimension("dim", "维度") { scoreMode = ScoreMode.SUM }

            section("条目") {
                question {
                    id = "q1"; dimensionCode = "dim"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("2", "2") { scoreValue = 2.0 }
                }
                question {
                    id = "q2"; dimensionCode = "dim"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("3", "3") { scoreValue = 3.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        // 只回答 q1
        val result = engine.score(mapOf("q1" to "2"))

        assertEquals(1, result.itemScores.size)
        assertEquals(2.0, result.totalRawScore)
    }

    @Test
    fun `ScoringEngine - CUSTOM_DSL 自定义计分`() {
        val form = scale("S9", "自定义计分量表") {
            dimension("dim", "维度") {
                scoreMode = ScoreMode.CUSTOM_DSL
                scoreDsl = "sum * 2"   // 将求和分乘以2
            }

            section("条目") {
                question {
                    id = "q1"; dimensionCode = "dim"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("3", "3") { scoreValue = 3.0 }
                }
                question {
                    id = "q2"; dimensionCode = "dim"
                    type = QuestionType.SINGLE_CHOICE; isScored = true
                    option("2", "2") { scoreValue = 2.0 }
                }
            }
        }

        val engine = ScoringEngine(form)
        val result = engine.score(mapOf("q1" to "3", "q2" to "2"))

        // sum=5, custom_dsl = "sum * 2" = 10
        assertEquals(10.0, result.dimensionScores["dim"]!!.rawScore, "CUSTOM_DSL: sum(5) * 2 = 10")
    }

    // -------------------------------------------------------------------------
    // Serialization round-trip tests for new fields
    // -------------------------------------------------------------------------

    @Test
    fun `序列化反序列化保留维度和计分元数据`() {
        val original = scale("ROUND-TRIP", "往返量表") {
            description = "测试序列化"
            assessmentType = AssessmentType.SCALE
            randomStrategy = RandomStrategy.RANDOM_ITEMS

            dimension("dim1", "维度1") {
                scoreMode = ScoreMode.WEIGHTED_SUM
                weight = 1.5
            }

            normRef("NORM-CN", "中国常模") {
                normScoreType = NormScoreType.T_SCORE
                condition = "age >= 18"
            }

            reportTag("high") {
                dimensionCode = "dim1"
                label = "高分"
                scoreMin = 60.0
                scoreMax = 100.0
                interpretation = "显著高于均值"
            }

            section("量表条目") {
                question {
                    id = "i1"; dimensionCode = "dim1"
                    type = QuestionType.SINGLE_CHOICE
                    isScored = true; isReverse = true
                    reverseMode = ReverseMode.FORMULA
                    maxScore = 5.0; minScore = 1.0
                    itemWeight = 2.0

                    option("1", "1") { scoreValue = 1.0; scoreReverseValue = 5.0 }
                    option("5", "5") { scoreValue = 5.0; scoreReverseValue = 1.0 }
                }
            }
        }

        val serializer = QuestionnaireSerializer()
        val json = serializer.toJson(original)
        val restored = serializer.fromJson(json)

        assertEquals(AssessmentType.SCALE, restored.assessmentType)
        assertEquals(RandomStrategy.RANDOM_ITEMS, restored.randomStrategy)
        assertEquals(1, restored.dimensions.size)
        assertEquals("dim1", restored.dimensions[0].code)
        assertEquals(ScoreMode.WEIGHTED_SUM, restored.dimensions[0].scoreMode)
        assertEquals(1.5, restored.dimensions[0].weight)
        assertEquals(1, restored.normRefs.size)
        assertEquals("NORM-CN", restored.normRefs[0].normSetCode)
        assertEquals(NormScoreType.T_SCORE, restored.normRefs[0].normScoreType)
        assertEquals("age >= 18", restored.normRefs[0].condition)
        assertEquals(1, restored.reportTags.size)
        assertEquals("high", restored.reportTags[0].tagCode)
        assertEquals(60.0, restored.reportTags[0].scoreMin)
        assertEquals(100.0, restored.reportTags[0].scoreMax)

        val q = restored.sections[0].questions[0]
        assertEquals("i1", q.id)
        assertTrue(q.isReverse)
        assertEquals(ReverseMode.FORMULA, q.reverseMode)
        assertEquals(5.0, q.maxScore)
        assertEquals(1.0, q.minScore)
        assertEquals(2.0, q.itemWeight)
        assertEquals(1.0, q.options[0].scoreValue)
        assertEquals(5.0, q.options[0].scoreReverseValue)
    }

    @Test
    fun `向后兼容 - 旧格式 questionnaire DSL 仍可正常序列化`() {
        val survey = questionnaire("legacy-001", "旧格式问卷") {
            description = "兼容性测试"
            section("基本信息") {
                question {
                    id = "q1"
                    type = QuestionType.TEXT
                    title = "姓名"
                    required = true
                }
                question {
                    id = "q2"
                    type = QuestionType.SINGLE_CHOICE
                    title = "满意度"
                    option("满意", "Y")
                    option("不满意", "N")
                }
            }
        }

        // assessmentType 应默认为 SURVEY
        assertEquals(AssessmentType.SURVEY, survey.assessmentType)
        assertEquals(RandomStrategy.NONE, survey.randomStrategy)
        assertTrue(survey.dimensions.isEmpty())

        // 序列化和反序列化仍然正常工作
        val serializer = QuestionnaireSerializer()
        val json = serializer.toJson(survey)
        val restored = serializer.fromJson(json)

        assertEquals(survey.id, restored.id)
        assertEquals(survey.title, restored.title)
        assertEquals(2, restored.sections.flatMap { it.questions }.size)
    }

    // -------------------------------------------------------------------------
    // QuestionnaireExecutor assessment metadata tests
    // -------------------------------------------------------------------------

    @Test
    fun `QuestionnaireExecutor - getAssessmentMetadata 返回正确统计`() {
        val form = scale("META-TEST", "元数据测试量表") {
            dimension("d1", "维度1") {}
            dimension("d2", "维度2") {}

            section("条目") {
                question {
                    id = "q1"; type = QuestionType.SINGLE_CHOICE
                    required = true; isScored = true; isReverse = true
                    dimensionCode = "d1"
                    option("1", "1") { scoreValue = 1.0 }
                }
                question {
                    id = "q2"; type = QuestionType.SINGLE_CHOICE
                    required = false; isScored = true; isAnchor = true
                    dimensionCode = "d2"
                    option("1", "1") { scoreValue = 1.0 }
                }
                question {
                    id = "q3"; type = QuestionType.DEMOGRAPHIC
                    isDemographic = true; isScored = false
                }
            }
        }

        val executor = QuestionnaireExecutor(form)
        val meta = executor.getAssessmentMetadata()

        assertEquals("META-TEST", meta.id)
        assertEquals(AssessmentType.SCALE, meta.assessmentType)
        assertEquals(3, meta.totalQuestions)
        assertEquals(1, meta.requiredQuestions)
        assertEquals(2, meta.scoredQuestions)
        assertEquals(1, meta.reversedQuestions)
        assertEquals(1, meta.anchorQuestions)
        assertEquals(1, meta.demographicQuestions)
        assertEquals(2, meta.dimensionCount)
        assertIterableEquals(listOf("d1", "d2"), meta.dimensionCodes)
    }
}
