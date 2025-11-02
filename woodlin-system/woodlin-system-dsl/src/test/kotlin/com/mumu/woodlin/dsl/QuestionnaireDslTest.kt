package com.mumu.woodlin.dsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * 问卷DSL测试
 *
 * @author mumu
 * @since 2025-10-28
 */
class QuestionnaireDslTest {
    
    @Test
    fun `测试创建基本问卷`() {
        val survey = questionnaire("survey001", "客户满意度调查") {
            description = "用于收集客户对服务的满意度反馈"
            version = "1.0"
            author = "张三"
            
            section("基本信息") {
                description = "收集客户基本信息"
                order = 1
                
                question {
                    id = "q1"
                    type = QuestionType.TEXT
                    title = "您的姓名"
                    required = true
                }
                
                question {
                    id = "q2"
                    type = QuestionType.NUMBER
                    title = "您的年龄"
                    required = false
                }
            }
            
            section("满意度评价") {
                description = "对服务进行评价"
                order = 2
                
                question {
                    id = "q3"
                    type = QuestionType.SINGLE_CHOICE
                    title = "您对我们的服务满意吗?"
                    required = true
                    
                    option("非常满意", "5")
                    option("满意", "4")
                    option("一般", "3")
                    option("不满意", "2")
                    option("非常不满意", "1")
                }
                
                question {
                    id = "q4"
                    type = QuestionType.MULTIPLE_CHOICE
                    title = "您希望我们改进哪些方面?"
                    
                    option("服务态度")
                    option("响应速度")
                    option("专业能力")
                    option("沟通效率")
                }
            }
        }
        
        assertEquals("survey001", survey.id)
        assertEquals("客户满意度调查", survey.title)
        assertEquals(2, survey.sections.size)
        assertEquals(4, survey.sections.flatMap { it.questions }.size)
    }
    
    @Test
    fun `测试问卷验证-必填项`() {
        val survey = questionnaire("test", "测试问卷") {
            section("测试") {
                question {
                    id = "q1"
                    title = "必填问题"
                    type = QuestionType.TEXT
                    required = true
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        val emptyAnswers = mapOf<String, Any>()
        val resultEmpty = executor.validate(emptyAnswers)
        assertFalse(resultEmpty.valid)
        assertEquals(1, resultEmpty.errors.size)
        assertTrue(resultEmpty.errors[0].message.contains("必填项"))
        
        val validAnswers = mapOf("q1" to "答案")
        val resultValid = executor.validate(validAnswers)
        assertTrue(resultValid.valid)
        assertTrue(resultValid.errors.isEmpty())
    }
    
    @Test
    fun `测试问卷验证-自定义规则`() {
        val survey = questionnaire("test", "测试问卷") {
            section("测试") {
                question {
                    id = "age"
                    title = "年龄"
                    type = QuestionType.NUMBER
                    
                    validate {
                        name = "年龄范围"
                        message = "年龄必须在18-100之间"
                        whenCondition { answers ->
                            val age = answers["age"] as? Int ?: 0
                            age in 18..100
                        }
                    }
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        val invalidAnswers = mapOf("age" to 15)
        val resultInvalid = executor.validate(invalidAnswers)
        assertFalse(resultInvalid.valid)
        
        val validAnswers = mapOf("age" to 25)
        val resultValid = executor.validate(validAnswers)
        assertTrue(resultValid.valid)
    }
    
    @Test
    fun `测试问卷执行`() {
        val survey = questionnaire("test", "测试问卷") {
            section("测试") {
                question {
                    id = "q1"
                    title = "问题1"
                    type = QuestionType.TEXT
                    required = true
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        val answers = mapOf("q1" to "答案1")
        
        val result = executor.execute(answers)
        
        assertEquals("test", result.questionnaireId)
        assertEquals(1, result.answers.size)
        assertTrue(result.validationResult.valid)
    }
    
    @Test
    fun `测试问卷元数据`() {
        val survey = questionnaire("test", "测试问卷") {
            description = "这是一个测试问卷"
            version = "2.0"
            author = "测试作者"
            
            section("部分1") {
                question {
                    id = "q1"
                    title = "问题1"
                    type = QuestionType.TEXT
                    required = true
                }
                question {
                    id = "q2"
                    title = "问题2"
                    type = QuestionType.NUMBER
                    required = false
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        val metadata = executor.getMetadata()
        
        assertEquals("test", metadata.id)
        assertEquals("测试问卷", metadata.title)
        assertEquals("2.0", metadata.version)
        assertEquals("测试作者", metadata.author)
        assertEquals(2, metadata.totalQuestions)
        assertEquals(1, metadata.requiredQuestions)
    }
    
    @Test
    fun `测试问卷序列化和反序列化`() {
        val survey = questionnaire("survey001", "测试问卷") {
            description = "测试描述"
            version = "1.0"
            author = "测试"
            
            section("部分1") {
                question {
                    id = "q1"
                    type = QuestionType.SINGLE_CHOICE
                    title = "选择题"
                    required = true
                    
                    option("选项1", "1")
                    option("选项2", "2")
                }
            }
        }
        
        val serializer = QuestionnaireSerializer()
        val json = serializer.toJson(survey)
        
        assertNotNull(json)
        assertTrue(json.contains("survey001"))
        assertTrue(json.contains("测试问卷"))
        
        val deserialized = serializer.fromJson(json)
        assertEquals(survey.id, deserialized.id)
        assertEquals(survey.title, deserialized.title)
        assertEquals(survey.sections.size, deserialized.sections.size)
    }
}
