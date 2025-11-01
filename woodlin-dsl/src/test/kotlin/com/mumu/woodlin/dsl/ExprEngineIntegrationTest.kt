package com.mumu.woodlin.dsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * 表达式引擎与问卷 DSL 集成测试
 */
class ExprEngineIntegrationTest {
    
    @Test
    fun `测试表达式验证 - 年龄范围`() {
        val survey = questionnaire("test", "年龄验证测试") {
            section("基本信息") {
                question {
                    id = "age"
                    title = "您的年龄"
                    type = QuestionType.NUMBER
                    required = true
                    
                    validate {
                        name = "年龄范围验证"
                        message = "年龄必须在18-100之间"
                        expr("age >= 18 && age <= 100")
                    }
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        // 测试有效年龄
        val validAnswers = mapOf("age" to 25)
        val validResult = executor.validate(validAnswers)
        assertTrue(validResult.valid)
        
        // 测试无效年龄（太小）
        val invalidAnswers1 = mapOf("age" to 15)
        val invalidResult1 = executor.validate(invalidAnswers1)
        assertFalse(invalidResult1.valid)
        
        // 测试无效年龄（太大）
        val invalidAnswers2 = mapOf("age" to 150)
        val invalidResult2 = executor.validate(invalidAnswers2)
        assertFalse(invalidResult2.valid)
    }
    
    @Test
    fun `测试表达式验证 - 条件显示逻辑`() {
        val survey = questionnaire("conditional", "条件显示测试") {
            section("驾驶信息") {
                question {
                    id = "age"
                    title = "您的年龄"
                    type = QuestionType.NUMBER
                    required = true
                }
                
                question {
                    id = "hasLicense"
                    title = "您有驾照吗？"
                    type = QuestionType.SINGLE_CHOICE
                    required = true
                    
                    option("是", "true")
                    option("否", "false")
                    
                    // 只有18岁以上才需要回答
                    validate {
                        name = "年龄限制"
                        message = "只有18岁以上才能持有驾照"
                        expr("age >= 18")
                    }
                }
                
                question {
                    id = "drivingYears"
                    title = "驾龄（年）"
                    type = QuestionType.NUMBER
                    
                    // 只有有驾照的才需要回答
                    validate {
                        name = "驾照检查"
                        message = "只有持有驾照的用户才需要填写驾龄"
                        expr("hasLicense == \"true\"")
                    }
                    
                    // 驾龄不能超过年龄-18
                    validate {
                        name = "驾龄合理性检查"
                        message = "驾龄不能超过（年龄-18）年"
                        expr("drivingYears <= age - 18")
                    }
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        // 测试有效答案
        val validAnswers = mapOf(
            "age" to 30,
            "hasLicense" to "true",
            "drivingYears" to 10
        )
        val validResult = executor.validate(validAnswers)
        assertTrue(validResult.valid)
        
        // 测试无效答案（驾龄过长）
        val invalidAnswers = mapOf(
            "age" to 30,
            "hasLicense" to "true",
            "drivingYears" to 15
        )
        val invalidResult = executor.validate(invalidAnswers)
        assertFalse(invalidResult.valid)
    }
    
    @Test
    fun `测试表达式验证 - 多选题验证`() {
        val survey = questionnaire("hobbies", "爱好调查") {
            section("兴趣爱好") {
                question {
                    id = "hobbies"
                    title = "您的爱好（最多选3个）"
                    type = QuestionType.MULTIPLE_CHOICE
                    required = true
                    
                    option("阅读")
                    option("运动")
                    option("音乐")
                    option("旅行")
                    option("摄影")
                    
                    validate {
                        name = "选项数量限制"
                        message = "最多只能选择3个爱好"
                        expr("count(hobbies) <= 3")
                    }
                    
                    validate {
                        name = "至少选择一个"
                        message = "至少要选择一个爱好"
                        expr("count(hobbies) >= 1")
                    }
                }
                
                question {
                    id = "hobbyDetail"
                    title = "详细描述您最喜欢的爱好"
                    type = QuestionType.TEXTAREA
                    
                    // 只有选择了2个以上爱好才需要详细描述
                    validate {
                        name = "多个爱好需要详细描述"
                        message = "选择了多个爱好需要详细描述"
                        expr("count(hobbies) >= 2")
                    }
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        // 测试有效答案
        val validAnswers = mapOf(
            "hobbies" to listOf("阅读", "运动"),
            "hobbyDetail" to "我喜欢阅读科幻小说和跑步"
        )
        val validResult = executor.validate(validAnswers)
        assertTrue(validResult.valid)
        
        // 测试无效答案（选择太多）
        val invalidAnswers = mapOf(
            "hobbies" to listOf("阅读", "运动", "音乐", "旅行"),
            "hobbyDetail" to "详细描述"
        )
        val invalidResult = executor.validate(invalidAnswers)
        assertFalse(invalidResult.valid)
    }
    
    @Test
    fun `测试表达式验证 - 复杂业务规则`() {
        val survey = questionnaire("employee", "员工调查") {
            section("员工信息") {
                question {
                    id = "department"
                    title = "所属部门"
                    type = QuestionType.SINGLE_CHOICE
                    required = true
                    
                    option("技术部", "tech")
                    option("销售部", "sales")
                    option("市场部", "marketing")
                }
                
                question {
                    id = "yearsOfService"
                    title = "工作年限"
                    type = QuestionType.NUMBER
                    required = true
                }
                
                question {
                    id = "skills"
                    title = "技能列表"
                    type = QuestionType.MULTIPLE_CHOICE
                    
                    option("Java")
                    option("Python")
                    option("JavaScript")
                    option("Kotlin")
                    
                    // 技术部门必须至少有2个技能
                    validate {
                        name = "技术部门技能要求"
                        message = "技术部门员工至少需要掌握2项技能"
                        expr("department != \"tech\" || count(skills) >= 2")
                    }
                }
                
                question {
                    id = "salary"
                    title = "期望薪资"
                    type = QuestionType.NUMBER
                    
                    // 工作年限越长，薪资期望可以越高
                    validate {
                        name = "薪资合理性检查"
                        message = "期望薪资应该与工作年限相匹配"
                        expr("salary <= yearsOfService * 10000 + 50000")
                    }
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        // 测试技术部门有效答案
        val validAnswers = mapOf(
            "department" to "tech",
            "yearsOfService" to 5,
            "skills" to listOf("Java", "Kotlin"),
            "salary" to 80000
        )
        val validResult = executor.validate(validAnswers)
        assertTrue(validResult.valid)
        
        // 测试技术部门无效答案（技能不足）
        val invalidAnswers1 = mapOf(
            "department" to "tech",
            "yearsOfService" to 5,
            "skills" to listOf("Java"),
            "salary" to 80000
        )
        val invalidResult1 = executor.validate(invalidAnswers1)
        assertFalse(invalidResult1.valid)
        
        // 测试薪资期望过高
        val invalidAnswers2 = mapOf(
            "department" to "tech",
            "yearsOfService" to 3,
            "skills" to listOf("Java", "Kotlin"),
            "salary" to 150000
        )
        val invalidResult2 = executor.validate(invalidAnswers2)
        assertFalse(invalidResult2.valid)
    }
    
    @Test
    fun `测试表达式验证 - 使用 lambda 函数`() {
        val survey = questionnaire("rating", "评分调查") {
            section("满意度评分") {
                question {
                    id = "ratings"
                    title = "各项服务评分（1-5分）"
                    type = QuestionType.MULTIPLE_CHOICE
                }
                
                question {
                    id = "overallSatisfaction"
                    title = "总体满意度"
                    type = QuestionType.RATING
                    
                    // 如果所有评分都是5分，总体满意度不能低于4分
                    validate {
                        name = "满意度一致性检查"
                        message = "所有项目都是满分，总体满意度不应该太低"
                        expr("!every(ratings, r => r == 5) || overallSatisfaction >= 4")
                    }
                    
                    // 如果有任何评分低于3分，总体满意度不能高于3分
                    validate {
                        name = "低分项检查"
                        message = "存在低分项时，总体满意度不应该太高"
                        expr("!some(ratings, r => r < 3) || overallSatisfaction <= 3")
                    }
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        // 测试有效答案
        val validAnswers = mapOf(
            "ratings" to listOf(5, 5, 5, 5),
            "overallSatisfaction" to 5
        )
        val validResult = executor.validate(validAnswers)
        assertTrue(validResult.valid)
        
        // 测试无效答案（满分但总体满意度低）
        val invalidAnswers1 = mapOf(
            "ratings" to listOf(5, 5, 5, 5),
            "overallSatisfaction" to 3
        )
        val invalidResult1 = executor.validate(invalidAnswers1)
        assertFalse(invalidResult1.valid)
        
        // 测试无效答案（有低分但总体满意度高）
        val invalidAnswers2 = mapOf(
            "ratings" to listOf(2, 3, 4, 5),
            "overallSatisfaction" to 5
        )
        val invalidResult2 = executor.validate(invalidAnswers2)
        assertFalse(invalidResult2.valid)
    }
    
    @Test
    fun `测试表达式验证 - 嵌套对象访问`() {
        val survey = questionnaire("nested", "嵌套数据测试") {
            section("用户信息") {
                question {
                    id = "user"
                    title = "用户对象"
                    type = QuestionType.TEXT
                    
                    validate {
                        name = "年龄检查"
                        message = "用户年龄必须>=18"
                        expr("user.age >= 18")
                    }
                    
                    validate {
                        name = "用户名长度检查"
                        message = "用户名长度必须在3-20之间"
                        expr("len(user.name) >= 3 && len(user.name) <= 20")
                    }
                }
            }
        }
        
        val executor = QuestionnaireExecutor(survey)
        
        // 测试有效答案
        val validAnswers = mapOf(
            "user" to mapOf(
                "name" to "Alice",
                "age" to 25
            )
        )
        val validResult = executor.validate(validAnswers)
        assertTrue(validResult.valid)
        
        // 测试无效答案（年龄不足）
        val invalidAnswers1 = mapOf(
            "user" to mapOf(
                "name" to "Bob",
                "age" to 16
            )
        )
        val invalidResult1 = executor.validate(invalidAnswers1)
        assertFalse(invalidResult1.valid)
        
        // 测试无效答案（用户名太短）
        val invalidAnswers2 = mapOf(
            "user" to mapOf(
                "name" to "Jo",
                "age" to 25
            )
        )
        val invalidResult2 = executor.validate(invalidAnswers2)
        assertFalse(invalidResult2.valid)
    }
}
