package com.mumu.woodlin.dsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

/**
 * 表达式引擎测试
 */
class ExprEngineTest {
    
    @Test
    fun `测试数字字面量`() {
        val result = ExprEngine.exec("42")
        assertEquals(42.0, result)
    }
    
    @Test
    fun `测试小数`() {
        val result = ExprEngine.exec("3.14")
        assertEquals(3.14, result)
    }
    
    @Test
    fun `测试字符串字面量`() {
        val result = ExprEngine.exec("\"hello\"")
        assertEquals("hello", result)
    }
    
    @Test
    fun `测试算术运算`() {
        assertEquals(3.0, ExprEngine.exec("1 + 2"))
        assertEquals(7.0, ExprEngine.exec("10 - 3"))
        assertEquals(20.0, ExprEngine.exec("4 * 5"))
        assertEquals(5.0, ExprEngine.exec("20 / 4"))
        assertEquals(1.0, ExprEngine.exec("10 % 3"))
    }
    
    @Test
    fun `测试运算符优先级`() {
        assertEquals(14.0, ExprEngine.exec("2 + 3 * 4"))
        assertEquals(20.0, ExprEngine.exec("(2 + 3) * 4"))
        assertEquals(4.0, ExprEngine.exec("10 - 2 * 3"))
    }
    
    @Test
    fun `测试比较运算`() {
        assertEquals(true, ExprEngine.exec("5 > 3"))
        assertEquals(false, ExprEngine.exec("5 < 3"))
        assertEquals(true, ExprEngine.exec("5 >= 5"))
        assertEquals(true, ExprEngine.exec("3 <= 5"))
        assertEquals(true, ExprEngine.exec("5 == 5"))
        assertEquals(true, ExprEngine.exec("5 != 3"))
    }
    
    @Test
    fun `测试逻辑运算`() {
        assertEquals(true, ExprEngine.exec("true && true"))
        assertEquals(false, ExprEngine.exec("true && false"))
        assertEquals(true, ExprEngine.exec("true || false"))
        assertEquals(false, ExprEngine.exec("false || false"))
    }
    
    @Test
    fun `测试一元运算`() {
        assertEquals(-5.0, ExprEngine.exec("-5"))
        assertEquals(false, ExprEngine.exec("!true"))
        assertEquals(true, ExprEngine.exec("!false"))
    }
    
    @Test
    fun `测试三元表达式`() {
        assertEquals(1.0, ExprEngine.exec("true ? 1 : 2"))
        assertEquals(2.0, ExprEngine.exec("false ? 1 : 2"))
        assertEquals("yes", ExprEngine.exec("5 > 3 ? \"yes\" : \"no\""))
    }
    
    @Test
    fun `测试简单引用`() {
        val context = mapOf("x" to 10, "y" to 20)
        assertEquals(10, ExprEngine.exec("x", context))
        assertEquals(20, ExprEngine.exec("y", context))
    }
    
    @Test
    fun `测试嵌套属性引用`() {
        val context = mapOf(
            "user" to mapOf(
                "name" to "Alice",
                "age" to 30
            )
        )
        assertEquals("Alice", ExprEngine.exec("user.name", context))
        assertEquals(30, ExprEngine.exec("user.age", context))
    }
    
    @Test
    fun `测试数组索引`() {
        val context = mapOf(
            "items" to listOf(10, 20, 30)
        )
        assertEquals(10, ExprEngine.exec("items[0]", context))
        assertEquals(20, ExprEngine.exec("items[1]", context))
        assertEquals(30, ExprEngine.exec("items[2]", context))
    }
    
    @Test
    fun `测试内置数学函数`() {
        assertEquals(5.0, ExprEngine.exec("abs(-5)"))
        assertEquals(5.0, ExprEngine.exec("max(1, 5, 3)"))
        assertEquals(1.0, ExprEngine.exec("min(1, 5, 3)"))
        assertEquals(4.0, ExprEngine.exec("round(3.7)"))
    }
    
    @Test
    fun `测试内置字符串函数`() {
        assertEquals("HELLO", ExprEngine.exec("upper(\"hello\")"))
        assertEquals("world", ExprEngine.exec("lower(\"WORLD\")"))
        assertEquals(4, ExprEngine.exec("len(\"test\")"))
    }
    
    @Test
    fun `测试内置数组函数`() {
        val context = mapOf("arr" to listOf(1, 2, 3, 4, 5))
        assertEquals(5, ExprEngine.exec("count(arr)", context))
        assertEquals(15.0, ExprEngine.exec("sum(arr)", context))
        assertEquals(3.0, ExprEngine.exec("avg(arr)", context))
    }
    
    @Test
    fun `测试 countIf 与 lambda`() {
        val context = mapOf("arr" to listOf(1, 2, 3, 4, 5))
        val result = ExprEngine.exec("countIf(arr, x => x > 3)", context)
        assertEquals(2, result)
    }
    
    @Test
    fun `测试 filter 与 lambda`() {
        val context = mapOf("arr" to listOf(1, 2, 3, 4, 5))
        val result = ExprEngine.exec("filter(arr, x => x % 2 == 0)", context) as List<*>
        assertEquals(listOf(2, 4), result)
    }
    
    @Test
    fun `测试 map 与 lambda`() {
        val context = mapOf("arr" to listOf(1, 2, 3))
        val result = ExprEngine.exec("map(arr, x => x * 2)", context) as List<*>
        assertEquals(listOf(2.0, 4.0, 6.0), result)
    }
    
    @Test
    fun `测试 some 与 lambda`() {
        val context = mapOf("arr" to listOf(1, 2, 3, 4, 5))
        assertEquals(true, ExprEngine.exec("some(arr, x => x > 4)", context))
        assertEquals(false, ExprEngine.exec("some(arr, x => x > 10)", context))
    }
    
    @Test
    fun `测试 every 与 lambda`() {
        val context = mapOf("arr" to listOf(1, 2, 3, 4, 5))
        assertEquals(true, ExprEngine.exec("every(arr, x => x > 0)", context))
        assertEquals(false, ExprEngine.exec("every(arr, x => x > 3)", context))
    }
    
    @Test
    fun `测试复杂表达式 - 问卷场景`() {
        val context = mapOf(
            "answers" to mapOf(
                "age" to 25,
                "country" to "US",
                "hasLicense" to true
            )
        )
        
        // 年龄检查
        assertEquals(true, ExprEngine.exec("answers.age >= 18", context))
        
        // 复杂条件
        assertEquals(true, ExprEngine.exec("answers.age >= 18 && answers.hasLicense", context))
    }
    
    @Test
    fun `测试复杂表达式 - 数组计数`() {
        val context = mapOf(
            "responses" to listOf(
                mapOf("score" to 5),
                mapOf("score" to 4),
                mapOf("score" to 3),
                mapOf("score" to 5),
                mapOf("score" to 4)
            )
        )
        
        val result = ExprEngine.exec("countIf(responses, r => r.score >= 4)", context)
        assertEquals(4, result)
    }
    
    @Test
    fun `测试嵌套三元表达式`() {
        val context = mapOf("score" to 85)
        val result = ExprEngine.exec(
            "score >= 90 ? \"A\" : score >= 80 ? \"B\" : \"C\"",
            context
        )
        assertEquals("B", result)
    }
    
    @Test
    fun `测试复杂算术表达式`() {
        val context = mapOf("a" to 10, "b" to 5, "c" to 2)
        assertEquals(20.0, ExprEngine.exec("a + b * c", context))
        assertEquals(30.0, ExprEngine.exec("(a + b) * c", context))
    }
    
    @Test
    fun `测试组合函数与运算符`() {
        val context = mapOf("arr" to listOf(1, 2, 3, 4, 5))
        assertEquals(3.0, ExprEngine.exec("sum(arr) / count(arr)", context))
    }
    
    @Test
    fun `测试嵌套函数调用`() {
        assertEquals(5.0, ExprEngine.exec("max(abs(-5), abs(-3))"))
        assertEquals("HE", ExprEngine.exec("upper(substr(\"hello\", 0, 2))"))
    }
    
    @Test
    fun `测试解析错误`() {
        assertThrows<ParseException> {
            ExprEngine.parse("1 + ")
        }
    }
    
    @Test
    fun `测试未定义引用`() {
        val context = mapOf("x" to 10)
        val result = ExprEngine.exec("y", context)
        assertNull(result)
    }
    
    @Test
    fun `测试自定义函数`() {
        val context = mapOf(
            "double" to { args: List<Any?> -> 
                ((args[0] as Number).toDouble() * 2)
            },
            "value" to 21
        )
        assertEquals(42.0, ExprEngine.exec("double(value)", context))
    }
    
    @Test
    fun `测试问卷验证场景 - 年龄范围`() {
        val context = mapOf("age" to 25)
        val result = ExprEngine.exec("age >= 18 && age <= 100", context)
        assertEquals(true, result)
    }
    
    @Test
    fun `测试问卷验证场景 - 邮箱格式`() {
        val context = mapOf(
            "email" to "test@example.com",
            "isValidEmail" to { args: List<Any?> ->
                val email = args[0].toString()
                email.contains("@") && email.contains(".")
            }
        )
        assertEquals(true, ExprEngine.exec("isValidEmail(email)", context))
    }
    
    @Test
    fun `测试条件显示逻辑`() {
        val context = mapOf(
            "answers" to mapOf(
                "q1" to "yes",
                "q2" to listOf(1, 2, 3)
            )
        )
        
        // 检查答案是否为 "yes"
        assertEquals(true, ExprEngine.exec("answers.q1 == \"yes\"", context))
        
        // 计数数组项
        assertEquals(3, ExprEngine.exec("count(answers.q2)", context))
    }
}
