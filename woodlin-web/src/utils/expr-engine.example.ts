/**
 * 表达式引擎使用示例
 * 
 * 这个文件展示了如何使用 expr-engine.ts 来解析和评估表达式
 */

/* eslint-disable no-console, @typescript-eslint/no-unused-vars */

import { parse, evaluate, exec, type Expr, type EvalContext } from './expr-engine';

console.log('=== 表达式引擎示例 ===\n');

// 示例 1: 基本算术运算
console.log('1. 基本算术运算:');
console.log('  1 + 2 * 3 =', exec('1 + 2 * 3'));  // 7
console.log('  (1 + 2) * 3 =', exec('(1 + 2) * 3'));  // 9
console.log('  10 / 2 - 3 =', exec('10 / 2 - 3'));  // 2
console.log();

// 示例 2: 比较和逻辑运算
console.log('2. 比较和逻辑运算:');
console.log('  5 > 3 =', exec('5 > 3'));  // true
console.log('  5 > 3 && 2 < 4 =', exec('5 > 3 && 2 < 4'));  // true
console.log('  5 > 3 || 2 > 4 =', exec('5 > 3 || 2 > 4'));  // true
console.log();

// 示例 3: 三元表达式
console.log('3. 三元表达式:');
console.log('  true ? "yes" : "no" =', exec('true ? "yes" : "no"'));  // "yes"
console.log('  5 > 3 ? 100 : 200 =', exec('5 > 3 ? 100 : 200'));  // 100
console.log();

// 示例 4: 使用上下文变量
console.log('4. 使用上下文变量:');
const context1: EvalContext = {
    x: 10,
    y: 20,
    user: {
        name: 'Alice',
        age: 30
    }
};
console.log('  上下文:', JSON.stringify(context1, null, 2));
console.log('  x + y =', evaluate(parse('x + y'), context1));  // 30
console.log('  user.name =', evaluate(parse('user.name'), context1));  // "Alice"
console.log('  user.age > 25 =', evaluate(parse('user.age > 25'), context1));  // true
console.log();

// 示例 5: 数组和索引访问
console.log('5. 数组和索引访问:');
const context2: EvalContext = {
    items: [10, 20, 30, 40, 50],
    answers: {
        q1: 'yes',
        q2: [1, 2, 3, 4, 5]
    }
};
console.log('  上下文:', JSON.stringify(context2, null, 2));
console.log('  items[0] =', evaluate(parse('items[0]'), context2));  // 10
console.log('  items[2] =', evaluate(parse('items[2]'), context2));  // 30
console.log('  answers.q1 =', evaluate(parse('answers.q1'), context2));  // "yes"
console.log('  answers.q2[0] =', evaluate(parse('answers.q2[0]'), context2));  // 1
console.log();

// 示例 6: 内置函数
console.log('6. 内置函数:');
console.log('  abs(-5) =', exec('abs(-5)'));  // 5
console.log('  max(1, 5, 3, 2) =', exec('max(1, 5, 3, 2)'));  // 5
console.log('  min(1, 5, 3, 2) =', exec('min(1, 5, 3, 2)'));  // 1
console.log('  round(3.7) =', exec('round(3.7)'));  // 4
console.log('  upper("hello") =', exec('upper("hello")'));  // "HELLO"
console.log('  len("test") =', exec('len("test")'));  // 4
console.log();

// 示例 7: 数组函数
console.log('7. 数组函数:');
const context3: EvalContext = {
    scores: [85, 92, 78, 95, 88]
};
console.log('  上下文:', JSON.stringify(context3, null, 2));
console.log('  count(scores) =', evaluate(parse('count(scores)'), context3));  // 5
console.log('  sum(scores) =', evaluate(parse('sum(scores)'), context3));  // 438
console.log('  avg(scores) =', evaluate(parse('avg(scores)'), context3));  // 87.6
console.log();

// 示例 8: Lambda 表达式和高阶函数
console.log('8. Lambda 表达式和高阶函数:');
const context4: EvalContext = {
    numbers: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
};
console.log('  上下文:', JSON.stringify(context4, null, 2));
console.log('  countIf(numbers, x => x > 5) =', 
    evaluate(parse('countIf(numbers, x => x > 5)'), context4));  // 5
console.log('  countIf(numbers, x => x % 2 == 0) =', 
    evaluate(parse('countIf(numbers, x => x % 2 == 0)'), context4));  // 5
console.log('  filter(numbers, x => x > 7) =', 
    JSON.stringify(evaluate(parse('filter(numbers, x => x > 7)'), context4)));  // [8, 9, 10]
console.log('  map(numbers, x => x * 2) =', 
    JSON.stringify(evaluate(parse('map(numbers, x => x * 2)'), context4)));  // [2, 4, 6, ..., 20]
console.log('  some(numbers, x => x > 9) =', 
    evaluate(parse('some(numbers, x => x > 9)'), context4));  // true
console.log('  every(numbers, x => x > 0) =', 
    evaluate(parse('every(numbers, x => x > 0)'), context4));  // true
console.log();

// 示例 9: 问卷场景 - 条件显示逻辑
console.log('9. 问卷场景 - 条件显示逻辑:');
const questionnaireContext: EvalContext = {
    answers: {
        age: 25,
        country: 'US',
        hasDriverLicense: true,
        previousAnswers: ['yes', 'no', 'yes']
    }
};
console.log('  上下文:', JSON.stringify(questionnaireContext, null, 2));
console.log('  年龄检查 (answers.age >= 18) =', 
    evaluate(parse('answers.age >= 18'), questionnaireContext));  // true
console.log('  完整条件 (answers.age >= 18 && answers.hasDriverLicense) =', 
    evaluate(parse('answers.age >= 18 && answers.hasDriverLicense'), questionnaireContext));  // true
console.log('  计数 "yes" 答案 (countIf(answers.previousAnswers, x => x == "yes")) =', 
    evaluate(parse('countIf(answers.previousAnswers, x => x == "yes")'), questionnaireContext));  // 2
console.log();

// 示例 10: 复杂表达式组合
console.log('10. 复杂表达式组合:');
const context5: EvalContext = {
    responses: [
        { score: 5, category: 'A' },
        { score: 4, category: 'B' },
        { score: 3, category: 'A' },
        { score: 5, category: 'A' },
        { score: 4, category: 'B' }
    ]
};
console.log('  上下文:', JSON.stringify(context5, null, 2));
console.log('  高分数量 (score >= 4): countIf(responses, r => r.score >= 4) =', 
    evaluate(parse('countIf(responses, r => r.score >= 4)'), context5));  // 4
console.log('  A 类别计数: countIf(responses, r => r.category == "A") =', 
    evaluate(parse('countIf(responses, r => r.category == "A")'), context5));  // 3
console.log('  是否所有分数 > 0: every(responses, r => r.score > 0) =', 
    evaluate(parse('every(responses, r => r.score > 0)'), context5));  // true
console.log();

// 示例 11: 自定义函数
console.log('11. 自定义函数:');
const context6: EvalContext = {
    double: (x: number) => x * 2,
    greet: (name: string) => `Hello, ${name}!`,
    value: 21
};
console.log('  上下文包含自定义函数: double, greet');
console.log('  double(21) =', evaluate(parse('double(value)'), context6));  // 42
console.log('  greet("World") =', evaluate(parse('greet("World")'), context6));  // "Hello, World!"
console.log();

console.log('=== 示例结束 ===');

// 导出示例函数供其他地方使用
export function runExamples() {
    console.log('运行所有示例...');
    // 这里可以添加更多示例逻辑
}
