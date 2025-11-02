# 表达式引擎 (Expression Engine)

Woodlin DSL 表达式引擎是一个强大的表达式解析和评估工具，用于在问卷验证规则中实现复杂的业务逻辑。

## 功能特性

### 1. 基本数据类型
- **数字**: 整数和小数 (`42`, `3.14`)
- **字符串**: 单引号或双引号 (`"hello"`, `'world'`)
- **布尔值**: 通过表达式计算得出

### 2. 运算符

#### 算术运算符
- `+` 加法
- `-` 减法 (也支持一元负号)
- `*` 乘法
- `/` 除法
- `%` 取模

#### 比较运算符
- `<` 小于
- `>` 大于
- `<=` 小于等于
- `>=` 大于等于
- `==` 等于
- `!=` 不等于
- `===` 严格等于
- `!==` 严格不等于

#### 逻辑运算符
- `&&` 逻辑与
- `||` 逻辑或
- `!` 逻辑非 (一元)

#### 三元运算符
- `condition ? thenValue : elseValue`

### 3. 引用和访问

```kotlin
// 简单引用
x
y

// 嵌套属性访问
user.name
user.address.city

// 数组索引访问
items[0]
answers.q2[1]

// 组合访问
users[0].name
data.items[2].value
```

### 4. 内置函数

#### 数学函数
- `abs(x)` - 绝对值
- `ceil(x)` - 向上取整
- `floor(x)` - 向下取整
- `round(x)` - 四舍五入
- `max(...args)` - 最大值
- `min(...args)` - 最小值
- `sqrt(x)` - 平方根
- `pow(base, exp)` - 幂运算

#### 字符串函数
- `len(str)` - 字符串长度
- `upper(str)` - 转大写
- `lower(str)` - 转小写
- `trim(str)` - 去除首尾空格
- `substr(str, start, len?)` - 子字符串

#### 数组函数
- `count(arr)` - 数组长度
- `sum(arr)` - 数组求和
- `avg(arr)` - 数组平均值

#### 高阶函数 (支持 Lambda)
- `countIf(arr, predicate)` - 计数满足条件的元素
- `filter(arr, predicate)` - 过滤数组
- `map(arr, fn)` - 映射数组
- `some(arr, predicate)` - 是否有元素满足条件
- `every(arr, predicate)` - 是否所有元素满足条件

### 5. Lambda 表达式

支持简单的 lambda 表达式语法：

```kotlin
// 语法: param => expression
x => x * 2
x => x > 5
item => item.score >= 4
```

## API 使用

### ExprEngine.parse()

解析表达式字符串为 AST：

```kotlin
val ast = ExprEngine.parse("1 + 2 * 3")
```

### ExprEngine.evaluate()

评估 AST 表达式：

```kotlin
val ast = ExprEngine.parse("x + y")
val result = ExprEngine.evaluate(ast, mapOf("x" to 10, "y" to 20))
println(result) // 30
```

### ExprEngine.exec()

便捷函数，解析并评估表达式：

```kotlin
val result = ExprEngine.exec("1 + 2 * 3")
println(result) // 7

val result2 = ExprEngine.exec("user.age >= 18", mapOf(
    "user" to mapOf("age" to 25)
))
println(result2) // true
```

## 与问卷 DSL 集成

### 使用 expr() 方法定义验证规则

```kotlin
val survey = questionnaire("test", "年龄验证") {
    section("基本信息") {
        question {
            id = "age"
            title = "您的年龄"
            type = QuestionType.NUMBER
            
            validate {
                name = "年龄范围验证"
                message = "年龄必须在18-100之间"
                expr("age >= 18 && age <= 100")
            }
        }
    }
}
```

### 示例1: 条件显示逻辑

```kotlin
questionnaire("driving", "驾驶调查") {
    section("驾驶信息") {
        question {
            id = "age"
            title = "您的年龄"
            type = QuestionType.NUMBER
        }
        
        question {
            id = "hasLicense"
            title = "您有驾照吗？"
            type = QuestionType.SINGLE_CHOICE
            
            // 只有18岁以上才能回答
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
            
            // 驾龄不能超过（年龄-18）
            validate {
                name = "驾龄合理性"
                message = "驾龄不能超过（年龄-18）年"
                expr("drivingYears <= age - 18")
            }
        }
    }
}
```

### 示例2: 多选题数量限制

```kotlin
questionnaire("hobbies", "爱好调查") {
    section("兴趣爱好") {
        question {
            id = "hobbies"
            title = "您的爱好（最多选3个）"
            type = QuestionType.MULTIPLE_CHOICE
            
            option("阅读")
            option("运动")
            option("音乐")
            option("旅行")
            
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
    }
}
```

### 示例3: 复杂业务规则

```kotlin
questionnaire("employee", "员工调查") {
    section("员工信息") {
        question {
            id = "department"
            title = "所属部门"
            type = QuestionType.SINGLE_CHOICE
            
            option("技术部", "tech")
            option("销售部", "sales")
        }
        
        question {
            id = "skills"
            title = "技能列表"
            type = QuestionType.MULTIPLE_CHOICE
            
            option("Java")
            option("Python")
            option("Kotlin")
            
            // 技术部门必须至少有2个技能
            validate {
                name = "技术部门技能要求"
                message = "技术部门员工至少需要掌握2项技能"
                expr("department != \"tech\" || count(skills) >= 2")
            }
        }
        
        question {
            id = "yearsOfService"
            title = "工作年限"
            type = QuestionType.NUMBER
        }
        
        question {
            id = "salary"
            title = "期望薪资"
            type = QuestionType.NUMBER
            
            // 薪资与工作年限匹配
            validate {
                name = "薪资合理性"
                message = "期望薪资应该与工作年限相匹配"
                expr("salary <= yearsOfService * 10000 + 50000")
            }
        }
    }
}
```

### 示例4: 使用 Lambda 表达式

```kotlin
questionnaire("rating", "评分调查") {
    section("满意度评分") {
        question {
            id = "ratings"
            title = "各项服务评分"
            type = QuestionType.MULTIPLE_CHOICE
        }
        
        question {
            id = "overallSatisfaction"
            title = "总体满意度"
            type = QuestionType.RATING
            
            // 如果所有评分都是5分，总体满意度不能低于4分
            validate {
                name = "满意度一致性"
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
```

### 示例5: 嵌套对象访问

```kotlin
questionnaire("user", "用户信息") {
    section("个人资料") {
        question {
            id = "user"
            title = "用户信息"
            type = QuestionType.TEXT
            
            validate {
                name = "年龄检查"
                message = "用户年龄必须>=18"
                expr("user.age >= 18")
            }
            
            validate {
                name = "用户名长度"
                message = "用户名长度必须在3-20之间"
                expr("len(user.name) >= 3 && len(user.name) <= 20")
            }
        }
    }
}
```

## 运算符优先级

从低到高：

1. `||` (逻辑或)
2. `&&` (逻辑与)
3. `==`, `!=`, `===`, `!==` (相等比较)
4. `<`, `>`, `<=`, `>=` (大小比较)
5. `+`, `-` (加减)
6. `*`, `/`, `%` (乘除模)
7. 一元运算符 `-`, `!`
8. 函数调用、属性访问、数组索引

## 与传统验证方式对比

### 传统方式（使用 when_ 闭包）

```kotlin
validate {
    name = "年龄范围验证"
    message = "年龄必须在18-100之间"
    when_ { answers ->
        val age = answers["age"] as? Int ?: 0
        age in 18..100
    }
}
```

### 表达式方式（使用 expr）

```kotlin
validate {
    name = "年龄范围验证"
    message = "年龄必须在18-100之间"
    expr("age >= 18 && age <= 100")
}
```

### 优势

1. **更简洁**: 表达式语法更接近自然语言
2. **更安全**: 无需类型转换，自动处理
3. **更易维护**: 表达式可以存储在数据库中，动态加载
4. **更易测试**: 表达式可以单独测试，无需完整的问卷上下文
5. **更好的可移植性**: 表达式可以在前后端共享

## 错误处理

表达式引擎会抛出 `ParseException` 异常：

```kotlin
try {
    val result = ExprEngine.exec("1 + ")
} catch (e: ParseException) {
    println("解析错误: ${e.message}")
    println("位置: ${e.pos}")
}
```

在验证规则中，解析错误会被自动捕获并返回 `false`。

## 性能考虑

- 表达式在每次验证时都会重新解析和评估
- 对于频繁使用的表达式，建议在应用层缓存 AST
- Lambda 表达式在每次评估时创建新函数

### 优化建议

```kotlin
// 不推荐：每次验证都解析
fun validate(expr: String, context: Map<String, Any?>): Boolean {
    return ExprEngine.exec(expr, context) as? Boolean ?: false
}

// 推荐：缓存 AST
class CachedValidator(expr: String) {
    private val ast = ExprEngine.parse(expr)
    
    fun validate(context: Map<String, Any?>): Boolean {
        return ExprEngine.evaluate(ast, context) as? Boolean ?: false
    }
}
```

## 限制

- 不支持多参数 lambda (只支持单参数)
- 不支持块级 lambda (只支持表达式 lambda)
- 不支持对象字面量
- 不支持数组字面量
- 不支持赋值操作

## 扩展

可以通过上下文添加自定义函数：

```kotlin
val context = mapOf(
    "isValidEmail" to { args: List<Any?> ->
        val email = args[0].toString()
        email.contains("@") && email.contains(".")
    },
    "email" to "test@example.com"
)

val result = ExprEngine.exec("isValidEmail(email)", context)
```

## 测试

查看 `ExprEngineTest.kt` 和 `ExprEngineIntegrationTest.kt` 获取更多测试示例。

## 许可证

与 Woodlin 项目相同的 MIT License。

## 作者

mumu - yulin.1996@foxmail.com

## 更新日志

### 1.0.0 (2025-11-01)
- 初始版本发布
- 完整的表达式解析器和评估器
- 与问卷 DSL 集成
- 20+ 内置函数
- Lambda 表达式支持
- 完整的单元测试覆盖
