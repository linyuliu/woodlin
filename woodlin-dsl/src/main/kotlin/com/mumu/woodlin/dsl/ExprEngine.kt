package com.mumu.woodlin.dsl

import kotlin.math.*

/**
 * 表达式引擎 - Kotlin 实现
 * 递归下降解析器 + AST 评估器
 * 支持：数字、字符串、引用/索引、函数调用、lambda、三元表达式、countIf 等
 *
 * @author mumu
 * @since 2025-11-01
 */

/**
 * 表达式 AST 节点
 */
sealed class Expr {
    data class NumberExpr(val value: Double) : Expr()
    data class StringExpr(val value: String) : Expr()
    data class RefExpr(val path: String) : Expr()
    data class UnaryExpr(val op: String, val expr: Expr) : Expr()
    data class BinaryExpr(val op: String, val left: Expr, val right: Expr) : Expr()
    data class TernaryExpr(val cond: Expr, val thenExpr: Expr, val elseExpr: Expr) : Expr()
    data class CallExpr(val name: String, val args: List<Expr>) : Expr()
    data class LambdaExpr(val param: String, val body: Expr) : Expr()
}

/**
 * 解析错误
 */
class ParseException(message: String, val pos: Int) : Exception("$message (pos=$pos)")

/**
 * Token 类型
 */
private enum class TokenType {
    NUM, STR, IDENT, OP, PUNC, EOF
}

/**
 * Token
 */
private data class Token(
    val type: TokenType,
    val value: String,
    val pos: Int
)

/**
 * 词法分析器
 */
private class Tokenizer(private val input: String) {
    private var pos = 0
    private val tokens = mutableListOf<Token>()
    
    fun tokenize(): List<Token> {
        while (pos < input.length) {
            val ch = input[pos]
            
            // 跳过空白字符
            if (ch.isWhitespace()) {
                pos++
                continue
            }
            
            // 数字（支持小数）
            if (ch.isDigit() || (ch == '.' && pos + 1 < input.length && input[pos + 1].isDigit())) {
                tokenizeNumber()
                continue
            }
            
            // 字符串字面量
            if (ch == '"' || ch == '\'') {
                tokenizeString(ch)
                continue
            }
            
            // 标识符
            if (ch.isLetter() || ch == '_') {
                tokenizeIdentifier()
                continue
            }
            
            // 操作符和标点
            if (tokenizeOperatorOrPunc()) {
                continue
            }
            
            throw ParseException("无法识别字符 '$ch'", pos)
        }
        
        tokens.add(Token(TokenType.EOF, "", pos))
        return tokens
    }
    
    private fun tokenizeNumber() {
        val start = pos
        var hasDot = false
        
        while (pos < input.length) {
            val ch = input[pos]
            if (ch.isDigit()) {
                pos++
            } else if (ch == '.' && !hasDot) {
                hasDot = true
                pos++
            } else {
                break
            }
        }
        
        tokens.add(Token(TokenType.NUM, input.substring(start, pos), start))
    }
    
    private fun tokenizeString(quote: Char) {
        val start = pos
        pos++ // skip opening quote
        val sb = StringBuilder()
        
        while (pos < input.length) {
            val ch = input[pos]
            if (ch == '\\' && pos + 1 < input.length) {
                sb.append(input[pos + 1])
                pos += 2
            } else if (ch == quote) {
                pos++
                break
            } else {
                sb.append(ch)
                pos++
            }
        }
        
        tokens.add(Token(TokenType.STR, sb.toString(), start))
    }
    
    private fun tokenizeIdentifier() {
        val start = pos
        while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) {
            pos++
        }
        tokens.add(Token(TokenType.IDENT, input.substring(start, pos), start))
    }
    
    private fun tokenizeOperatorOrPunc(): Boolean {
        val ch = input[pos]
        val start = pos
        
        // 三字符操作符
        if (pos + 2 < input.length) {
            val three = input.substring(pos, pos + 3)
            if (three == "===" || three == "!==") {
                tokens.add(Token(TokenType.OP, three, start))
                pos += 3
                return true
            }
        }
        
        // 两字符操作符
        if (pos + 1 < input.length) {
            val two = input.substring(pos, pos + 2)
            if (two in listOf("&&", "||", "==", "!=", ">=", "<=", "=>")) {
                tokens.add(Token(TokenType.OP, two, start))
                pos += 2
                return true
            }
        }
        
        // 单字符操作符
        if (ch in "+-*/%<>?:=!") {
            tokens.add(Token(TokenType.OP, ch.toString(), start))
            pos++
            return true
        }
        
        // 标点
        if (ch in "(),[].") {
            tokens.add(Token(TokenType.PUNC, ch.toString(), start))
            pos++
            return true
        }
        
        return false
    }
}

/**
 * 语法解析器
 */
private class Parser(private val tokens: List<Token>) {
    private var pos = 0
    
    // 运算符优先级
    private val precedence = mapOf(
        "||" to 1,
        "&&" to 2,
        "==" to 3, "!=" to 3, "===" to 3, "!==" to 3,
        "<" to 4, ">" to 4, "<=" to 4, ">=" to 4,
        "+" to 5, "-" to 5,
        "*" to 6, "/" to 6, "%" to 6
    )
    
    fun parse(): Expr {
        val expr = parseExpression()
        if (tokens[pos].type != TokenType.EOF) {
            throw ParseException("意外的 token: ${tokens[pos].value}", tokens[pos].pos)
        }
        return expr
    }
    
    private fun parseExpression(): Expr {
        val cond = parseBinary(0)
        
        // 三元表达式
        if (pos < tokens.size && tokens[pos].type == TokenType.OP && tokens[pos].value == "?") {
            pos++ // skip '?'
            val thenExpr = parseExpression()
            if (tokens[pos].type != TokenType.OP || tokens[pos].value != ":") {
                throw ParseException("三元表达式期望 ':'", tokens[pos].pos)
            }
            pos++ // skip ':'
            val elseExpr = parseExpression()
            return Expr.TernaryExpr(cond, thenExpr, elseExpr)
        }
        
        return cond
    }
    
    private fun parseBinary(minPrec: Int): Expr {
        var left = parsePrimary()
        
        while (pos < tokens.size && tokens[pos].type == TokenType.OP) {
            val op = tokens[pos].value
            val prec = precedence[op] ?: break
            if (prec < minPrec) break
            
            pos++ // skip operator
            val right = parseBinary(prec + 1)
            left = Expr.BinaryExpr(op, left, right)
        }
        
        return left
    }
    
    private fun parsePrimary(): Expr {
        val token = tokens[pos]
        
        // 数字
        if (token.type == TokenType.NUM) {
            pos++
            return Expr.NumberExpr(token.value.toDouble())
        }
        
        // 字符串
        if (token.type == TokenType.STR) {
            pos++
            return Expr.StringExpr(token.value)
        }
        
        // 标识符（lambda、引用、调用）
        if (token.type == TokenType.IDENT) {
            // 布尔字面量
            if (token.value == "true") {
                pos++
                return Expr.NumberExpr(1.0) // 用 1.0 表示 true
            }
            if (token.value == "false") {
                pos++
                return Expr.NumberExpr(0.0) // 用 0.0 表示 false
            }
            
            // 检查 lambda
            if (pos + 1 < tokens.size && tokens[pos + 1].type == TokenType.OP && tokens[pos + 1].value == "=>") {
                val param = token.value
                pos += 2 // skip ident and '=>'
                val body = parseExpression()
                return Expr.LambdaExpr(param, body)
            }
            
            // 引用、属性访问、函数调用
            var ref = token.value
            pos++
            
            while (pos < tokens.size) {
                val tk = tokens[pos]
                
                // 函数调用
                if (tk.type == TokenType.PUNC && tk.value == "(") {
                    pos++ // skip '('
                    val args = mutableListOf<Expr>()
                    
                    if (tokens[pos].type != TokenType.PUNC || tokens[pos].value != ")") {
                        while (true) {
                            args.add(parseExpression())
                            if (tokens[pos].type == TokenType.PUNC && tokens[pos].value == ",") {
                                pos++
                                continue
                            }
                            break
                        }
                    }
                    
                    if (tokens[pos].type != TokenType.PUNC || tokens[pos].value != ")") {
                        throw ParseException("期望 ')'", tokens[pos].pos)
                    }
                    pos++
                    return Expr.CallExpr(ref, args)
                }
                
                // 属性访问
                if (tk.type == TokenType.PUNC && tk.value == ".") {
                    pos++
                    if (tokens[pos].type != TokenType.IDENT) {
                        throw ParseException("期望标识符", tokens[pos].pos)
                    }
                    ref += ".${tokens[pos].value}"
                    pos++
                    continue
                }
                
                // 数组索引
                if (tk.type == TokenType.PUNC && tk.value == "[") {
                    pos++
                    val indexExpr = parseExpression()
                    if (tokens[pos].type != TokenType.PUNC || tokens[pos].value != "]") {
                        throw ParseException("期望 ']'", tokens[pos].pos)
                    }
                    pos++
                    ref += "[${stringifyExpr(indexExpr)}]"
                    continue
                }
                
                break
            }
            
            return Expr.RefExpr(ref)
        }
        
        // 括号表达式
        if (token.type == TokenType.PUNC && token.value == "(") {
            pos++ // skip '('
            val expr = parseExpression()
            if (tokens[pos].type != TokenType.PUNC || tokens[pos].value != ")") {
                throw ParseException("期望 ')'", tokens[pos].pos)
            }
            pos++
            return expr
        }
        
        // 一元运算符
        if (token.type == TokenType.OP && (token.value == "-" || token.value == "!")) {
            pos++
            val right = parsePrimary()
            return Expr.UnaryExpr(token.value, right)
        }
        
        throw ParseException("无法解析主表达式: ${token.type}:${token.value}", token.pos)
    }
    
    private fun stringifyExpr(expr: Expr): String = when (expr) {
        is Expr.NumberExpr -> expr.value.toString()
        is Expr.StringExpr -> "\"${expr.value}\""
        is Expr.RefExpr -> expr.path
        is Expr.UnaryExpr -> "${expr.op}${stringifyExpr(expr.expr)}"
        is Expr.BinaryExpr -> "(${stringifyExpr(expr.left)} ${expr.op} ${stringifyExpr(expr.right)})"
        is Expr.TernaryExpr -> "(${stringifyExpr(expr.cond)} ? ${stringifyExpr(expr.thenExpr)} : ${stringifyExpr(expr.elseExpr)})"
        is Expr.CallExpr -> "${expr.name}(${expr.args.joinToString(", ") { stringifyExpr(it) }})"
        is Expr.LambdaExpr -> "${expr.param} => ${stringifyExpr(expr.body)}"
    }
}

/**
 * 表达式评估器
 */
class ExprEvaluator {
    // 内置函数
    private val builtinFunctions = mapOf<String, (List<Any?>) -> Any?>(
        // 数学函数
        "abs" to { args -> abs((args[0] as Number).toDouble()) },
        "ceil" to { args -> ceil((args[0] as Number).toDouble()) },
        "floor" to { args -> floor((args[0] as Number).toDouble()) },
        "round" to { args -> round((args[0] as Number).toDouble()) },
        "max" to { args -> args.map { (it as Number).toDouble() }.maxOrNull() },
        "min" to { args -> args.map { (it as Number).toDouble() }.minOrNull() },
        "sqrt" to { args -> sqrt((args[0] as Number).toDouble()) },
        "pow" to { args -> (args[0] as Number).toDouble().pow((args[1] as Number).toDouble()) },
        
        // 字符串函数
        "len" to { args -> args[0].toString().length },
        "upper" to { args -> args[0].toString().uppercase() },
        "lower" to { args -> args[0].toString().lowercase() },
        "trim" to { args -> args[0].toString().trim() },
        "substr" to { args ->
            val str = args[0].toString()
            val start = (args[1] as Number).toInt()
            val len = if (args.size > 2) (args[2] as Number).toInt() else str.length - start
            str.substring(start, min(start + len, str.length))
        },
        
        // 数组函数
        "count" to { args -> toList(args[0]).size },
        "sum" to { args -> toList(args[0]).sumOf { (it as? Number)?.toDouble() ?: 0.0 } },
        "avg" to { args ->
            toList(args[0]).let { list ->
                if (list.isEmpty()) 0.0 else list.sumOf { (it as? Number)?.toDouble() ?: 0.0 } / list.size
            }
        },
        
        // 高阶函数
        "countIf" to { args ->
            val predicate = args[1] as (Any?) -> Boolean
            toList(args[0]).count(predicate)
        },
        "filter" to { args ->
            val predicate = args[1] as (Any?) -> Boolean
            toList(args[0]).filter(predicate)
        },
        "map" to { args ->
            val fn = args[1] as (Any?) -> Any?
            toList(args[0]).map(fn)
        },
        "some" to { args ->
            val predicate = args[1] as (Any?) -> Boolean
            toList(args[0]).any(predicate)
        },
        "every" to { args ->
            val predicate = args[1] as (Any?) -> Boolean
            toList(args[0]).all(predicate)
        }
    )
    
    /**
     * 评估表达式
     */
    fun evaluate(expr: Expr, context: Map<String, Any?> = emptyMap()): Any? {
        return when (expr) {
            is Expr.NumberExpr -> expr.value
            is Expr.StringExpr -> expr.value
            is Expr.RefExpr -> resolveRef(expr.path, context)
            is Expr.UnaryExpr -> evaluateUnary(expr, context)
            is Expr.BinaryExpr -> evaluateBinary(expr, context)
            is Expr.TernaryExpr -> {
                val cond = evaluate(expr.cond, context)
                if (toBoolean(cond)) {
                    evaluate(expr.thenExpr, context)
                } else {
                    evaluate(expr.elseExpr, context)
                }
            }
            is Expr.CallExpr -> evaluateCall(expr, context)
            is Expr.LambdaExpr -> {
                // 返回一个函数
                { param: Any? ->
                    val newContext = context + (expr.param to param)
                    evaluate(expr.body, newContext)
                }
            }
        }
    }
    
    private fun evaluateUnary(expr: Expr.UnaryExpr, context: Map<String, Any?>): Any? {
        val operand = evaluate(expr.expr, context)
        return when (expr.op) {
            "-" -> -(operand as Number).toDouble()
            "!" -> !toBoolean(operand)
            else -> throw IllegalArgumentException("未知的一元运算符: ${expr.op}")
        }
    }
    
    private fun evaluateBinary(expr: Expr.BinaryExpr, context: Map<String, Any?>): Any? {
        val left = evaluate(expr.left, context)
        val right = evaluate(expr.right, context)
        
        return when (expr.op) {
            "+" -> applyNumericOp(left, right, Double::plus)
            "-" -> applyNumericOp(left, right, Double::minus)
            "*" -> applyNumericOp(left, right, Double::times)
            "/" -> applyNumericOp(left, right, Double::div)
            "%" -> applyNumericOp(left, right, Double::rem)
            "<" -> compare(left, right) < 0
            ">" -> compare(left, right) > 0
            "<=" -> compare(left, right) <= 0
            ">=" -> compare(left, right) >= 0
            "==" -> equalsWithNumericCoercion(left, right)
            "!=" -> !equalsWithNumericCoercion(left, right)
            "===" -> left === right
            "!==" -> left !== right
            "&&" -> toBoolean(left) && toBoolean(right)
            "||" -> toBoolean(left) || toBoolean(right)
            else -> throw IllegalArgumentException("未知的二元运算符: ${expr.op}")
        }
    }
    
    private fun applyNumericOp(left: Any?, right: Any?, op: (Double, Double) -> Double): Double =
        op((left as Number).toDouble(), (right as Number).toDouble())
    
    private fun equalsWithNumericCoercion(left: Any?, right: Any?): Boolean =
        when {
            left is Number && right is Number -> left.toDouble() == right.toDouble()
            else -> left == right
        }
    
    private fun evaluateCall(expr: Expr.CallExpr, context: Map<String, Any?>): Any? {
        val fn = builtinFunctions[expr.name] ?: context[expr.name]
        
        if (fn == null) {
            throw IllegalArgumentException("未定义的函数: ${expr.name}")
        }
        
        // 评估参数（特殊处理 lambda）
        val args = expr.args.map { arg ->
            if (arg is Expr.LambdaExpr) {
                // 返回一个函数
                { param: Any? ->
                    val newContext = context + (arg.param to param)
                    evaluate(arg.body, newContext)
                }
            } else {
                evaluate(arg, context)
            }
        }
        
        return when (fn) {
            is Function<*> -> (fn as (List<Any?>) -> Any?)(args)
            else -> throw IllegalArgumentException("${expr.name} 不是一个函数")
        }
    }
    
    private fun resolveRef(path: String, context: Map<String, Any?>): Any? {
        // 解析路径，支持 obj.prop 和 obj[index]
        val firstDot = path.indexOfAny(charArrayOf('.', '['))
        val rootName = if (firstDot > 0) path.substring(0, firstDot) else path
        val root = context[rootName]
        
        return if (firstDot == -1) {
            root
        } else {
            resolvePath(path.substring(firstDot), root)
        }
    }
    
    private tailrec fun resolvePath(path: String, current: Any?, index: Int = 0): Any? {
        if (index >= path.length || current == null) return current
        
        return when (path[index]) {
            '.' -> {
                val nextDelim = path.indexOfAny(charArrayOf('.', '['), index + 1)
                val prop = if (nextDelim > 0) path.substring(index + 1, nextDelim) else path.substring(index + 1)
                val next = (current as? Map<*, *>)?.get(prop)
                resolvePath(path, next, if (nextDelim > 0) nextDelim else path.length)
            }
            '[' -> {
                val closeBracket = path.indexOf(']', index + 1)
                if (closeBracket == -1) return null
                
                val indexValue = path.substring(index + 1, closeBracket)
                    .toDoubleOrNull()?.toInt()
                
                val next = when (current) {
                    is List<*> -> indexValue?.let { current.getOrNull(it) }
                    is Array<*> -> indexValue?.let { if (it in current.indices) current[it] else null }
                    else -> null
                }
                resolvePath(path, next, closeBracket + 1)
            }
            else -> resolvePath(path, current, index + 1)
        }
    }
    
    private fun toBoolean(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        is Number -> value.toDouble() != 0.0
        is String -> value.isNotEmpty()
        is Collection<*> -> value.isNotEmpty()
        is Array<*> -> value.isNotEmpty()
        else -> true
    }
    
    private fun compare(left: Any?, right: Any?): Int = when {
        left is Number && right is Number -> left.toDouble().compareTo(right.toDouble())
        left is String && right is String -> left.compareTo(right)
        else -> 0
    }
    
    private fun toList(value: Any?): List<Any?> = when (value) {
        is Collection<*> -> value.toList()
        is Array<*> -> value.toList()
        else -> emptyList()
    }
}

/**
 * 表达式引擎 - 主入口
 */
object ExprEngine {
    /**
     * 解析表达式
     */
    fun parse(input: String): Expr {
        val tokenizer = Tokenizer(input)
        val tokens = tokenizer.tokenize()
        val parser = Parser(tokens)
        return parser.parse()
    }
    
    /**
     * 评估表达式
     */
    fun evaluate(expr: Expr, context: Map<String, Any?> = emptyMap()): Any? {
        val evaluator = ExprEvaluator()
        return evaluator.evaluate(expr, context)
    }
    
    /**
     * 执行表达式（解析 + 评估）
     */
    fun exec(input: String, context: Map<String, Any?> = emptyMap()): Any? {
        val expr = parse(input)
        return evaluate(expr, context)
    }
}
