# 表达式引擎 (Expression Engine)

`expr-engine.ts` 是一个完整的递归下降解析器和 AST 评估器，用于解析和评估类似 JavaScript 的表达式。

## 功能特性

### 1. 基本数据类型
- **数字**: 整数和小数 (`42`, `3.14`)
- **字符串**: 单引号或双引号 (`"hello"`, `'world'`)
- **布尔值**: 通过上下文传入

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
- `==` 等于 (类型转换)
- `!=` 不等于 (类型转换)
- `===` 严格等于
- `!==` 严格不等于

#### 逻辑运算符
- `&&` 逻辑与
- `||` 逻辑或
- `!` 逻辑非 (一元)

#### 三元运算符
- `condition ? thenValue : elseValue`

### 3. 引用和访问

#### 变量引用
```typescript
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

### 4. 函数调用

#### 内置数学函数
- `abs(x)` - 绝对值
- `ceil(x)` - 向上取整
- `floor(x)` - 向下取整
- `round(x)` - 四舍五入
- `max(...args)` - 最大值
- `min(...args)` - 最小值
- `sqrt(x)` - 平方根
- `pow(base, exp)` - 幂运算

#### 内置字符串函数
- `len(str)` - 字符串长度
- `upper(str)` - 转大写
- `lower(str)` - 转小写
- `trim(str)` - 去除首尾空格
- `substr(str, start, len?)` - 子字符串

#### 内置数组函数
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

```typescript
// 语法: param => expression
x => x * 2
x => x > 5
item => item.score >= 4
```

Lambda 表达式通常与高阶函数一起使用：

```typescript
countIf(numbers, x => x > 5)
filter(items, x => x % 2 == 0)
map(scores, x => x * 2)
```

## API 使用

### parse(input: string): Expr

解析表达式字符串为 AST：

```typescript
import { parse } from './expr-engine';

const ast = parse('1 + 2 * 3');
// 返回 AST 对象
```

### evaluate(expr: Expr, context?: EvalContext): any

评估 AST 表达式：

```typescript
import { parse, evaluate } from './expr-engine';

const ast = parse('x + y');
const result = evaluate(ast, { x: 10, y: 20 });
console.log(result); // 30
```

### exec(input: string, context?: EvalContext): any

便捷函数，解析并评估表达式：

```typescript
import { exec } from './expr-engine';

const result = exec('1 + 2 * 3');
console.log(result); // 7

const result2 = exec('user.age >= 18', {
    user: { age: 25 }
});
console.log(result2); // true
```

## 使用示例

### 示例 1: 基本运算

```typescript
import { exec } from './expr-engine';

console.log(exec('1 + 2 * 3')); // 7
console.log(exec('(1 + 2) * 3')); // 9
console.log(exec('5 > 3 && 2 < 4')); // true
console.log(exec('true ? "yes" : "no"')); // "yes"
```

### 示例 2: 使用上下文

```typescript
import { exec } from './expr-engine';

const context = {
    age: 25,
    country: 'US',
    user: {
        name: 'Alice',
        scores: [85, 92, 78]
    }
};

console.log(exec('age >= 18', context)); // true
console.log(exec('user.name', context)); // "Alice"
console.log(exec('user.scores[0]', context)); // 85
console.log(exec('count(user.scores)', context)); // 3
console.log(exec('avg(user.scores)', context)); // 85
```

### 示例 3: Lambda 和高阶函数

```typescript
import { exec } from './expr-engine';

const context = {
    numbers: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    responses: [
        { score: 5, pass: true },
        { score: 3, pass: false },
        { score: 4, pass: true }
    ]
};

// 计数大于 5 的数字
console.log(exec('countIf(numbers, x => x > 5)', context)); // 5

// 过滤偶数
console.log(exec('filter(numbers, x => x % 2 == 0)', context)); 
// [2, 4, 6, 8, 10]

// 映射：所有数字乘以 2
console.log(exec('map(numbers, x => x * 2)', context)); 
// [2, 4, 6, ..., 20]

// 计数通过的响应
console.log(exec('countIf(responses, r => r.pass)', context)); // 2

// 检查是否有高分
console.log(exec('some(responses, r => r.score >= 5)', context)); // true
```

### 示例 4: 问卷场景

```typescript
import { exec } from './expr-engine';

// 条件显示逻辑
const questionnaireContext = {
    answers: {
        age: 25,
        country: 'US',
        hasLicense: true,
        previousAnswers: ['yes', 'no', 'yes', 'yes']
    }
};

// 检查是否应该显示某个问题
const shouldShowQ5 = exec(
    'answers.age >= 18 && answers.hasLicense',
    questionnaireContext
);
console.log(shouldShowQ5); // true

// 计算 "yes" 答案的数量
const yesCount = exec(
    'countIf(answers.previousAnswers, x => x == "yes")',
    questionnaireContext
);
console.log(yesCount); // 3

// 复杂条件
const showAdvancedQuestions = exec(
    'answers.age >= 21 && answers.country == "US" && yesCount >= 2',
    { ...questionnaireContext, yesCount }
);
console.log(showAdvancedQuestions); // true
```

### 示例 5: 自定义函数

```typescript
import { exec } from './expr-engine';

const context = {
    // 自定义函数
    celsius2fahrenheit: (c: number) => c * 9/5 + 32,
    greet: (name: string) => `Hello, ${name}!`,
    
    // 数据
    temp: 25,
    name: 'Alice'
};

console.log(exec('celsius2fahrenheit(temp)', context)); // 77
console.log(exec('greet(name)', context)); // "Hello, Alice!"
```

## 错误处理

表达式引擎会抛出 `ParseError` 异常：

```typescript
import { exec, ParseError } from './expr-engine';

try {
    const result = exec('1 + ');
} catch (error) {
    if (error instanceof ParseError) {
        console.error(`解析错误: ${error.message}`);
        console.error(`位置: ${error.pos}`);
    }
}
```

## AST 结构

表达式被解析为以下 AST 节点类型：

```typescript
type Expr =
    | { type: 'number'; value: number }
    | { type: 'string'; value: string }
    | { type: 'ref'; value: string }
    | { type: 'unary'; op: string; expr: Expr }
    | { type: 'binary'; op: string; left: Expr; right: Expr }
    | { type: 'ternary'; cond: Expr; thenExpr: Expr; elseExpr: Expr }
    | { type: 'call'; name: string; args: Expr[] }
    | { type: 'lambda'; param: string; body: Expr };
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

## 与 Vue 集成

可以在 Vue 组件中使用表达式引擎：

```vue
<script setup lang="ts">
import { ref, computed } from 'vue';
import { exec } from '@/utils/expr-engine';

const answers = ref({
    age: 25,
    country: 'US'
});

const canProceed = computed(() => {
    return exec('age >= 18 && country == "US"', { ...answers.value });
});
</script>

<template>
    <div v-if="canProceed">
        <!-- 显示后续问题 -->
    </div>
</template>
```

## 性能考虑

- 表达式会在每次调用时重新解析和评估
- 对于频繁使用的表达式，考虑缓存 AST
- Lambda 表达式在每次评估时创建新函数

## 限制

- 不支持多参数 lambda (只支持单参数)
- 不支持块级 lambda (只支持表达式 lambda)
- 不支持对象字面量和数组字面量
- 不支持赋值操作
- 函数调用参数按值传递

## 扩展

可以通过上下文添加自定义函数：

```typescript
const context = {
    // 自定义函数
    myCustomFunction: (x: number) => x * x,
    
    // 数据
    value: 10
};

const result = exec('myCustomFunction(value)', context);
```

## 许可证

此代码遵循项目的整体许可证。
