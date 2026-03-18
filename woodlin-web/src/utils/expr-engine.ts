export type Expr =
  | { type: 'number'; value: number }
  | { type: 'string'; value: string }
  | { type: 'ref'; value: string }
  | { type: 'unary'; op: string; expr: Expr }
  | { type: 'binary'; op: string; left: Expr; right: Expr }
  | { type: 'ternary'; cond: Expr; thenExpr: Expr; elseExpr: Expr }
  | { type: 'call'; name: string; args: Expr[] }
  | { type: 'lambda'; param: string; body: Expr }

export class ParseError extends Error {
  readonly pos: number

  constructor(message: string, pos: number) {
    super(`${message} (pos=${pos})`)
    this.name = 'ParseError'
    this.pos = pos
  }
}

type TokenType = 'num' | 'str' | 'ident' | 'op' | 'punc' | 'eof'
type Token = { type: TokenType; value: string; pos: number }

type ScanResult = {
  token: Token
  nextIndex: number
}

type OperatorProbeResult = {
  value: string
  nextIndex: number
}

const PRECEDENCE: Readonly<Record<string, number>> = {
  '||': 1,
  '&&': 2,
  '==': 3,
  '!=': 3,
  '===': 3,
  '!==': 3,
  '<': 4,
  '>': 4,
  '<=': 4,
  '>=': 4,
  '+': 5,
  '-': 5,
  '*': 6,
  '/': 6,
  '%': 6
}

const THREE_CHAR_OPS = new Set(['===', '!=='])
const TWO_CHAR_OPS = new Set(['&&', '||', '==', '!=', '>=', '<=', '=>'])
const SINGLE_CHAR_OPS = new Set(['+', '-', '*', '/', '%', '<', '>', '?', ':', '=', '!'])
const PUNCTUATION_CHARS = new Set(['(', ')', ',', '[', ']', '.'])

const isDigit = (ch: string) => /[0-9]/.test(ch)
const isIdentifierStart = (ch: string) => /[a-zA-Z_]/.test(ch)
const isIdentifierBody = (ch: string) => /[a-zA-Z0-9_]/.test(ch)

function assertNever(value: never): never {
  throw new Error(`未支持的表达式类型: ${JSON.stringify(value)}`)
}

function stringifyExpr(expr: Expr): string {
  switch (expr.type) {
    case 'number':
      return String(expr.value)
    case 'string':
      return `"${expr.value}"`
    case 'ref':
      return expr.value
    case 'unary':
      return `${expr.op}${stringifyExpr(expr.expr)}`
    case 'binary':
      return `(${stringifyExpr(expr.left)} ${expr.op} ${stringifyExpr(expr.right)})`
    case 'ternary':
      return `(${stringifyExpr(expr.cond)} ? ${stringifyExpr(expr.thenExpr)} : ${stringifyExpr(expr.elseExpr)})`
    case 'call':
      return `${expr.name}(${expr.args.map(stringifyExpr).join(', ')})`
    case 'lambda':
      return `${expr.param} => ${stringifyExpr(expr.body)}`
  }

  return assertNever(expr)
}

/**
 * 扫描数字
 */
function scanNumber(input: string, start: number): ScanResult {
  let index = start
  let hasDot = false

  while (index < input.length) {
    const current = input[index]
    const isDot = current === '.'
    if (!isDigit(current) && !(isDot && !hasDot)) {
      break
    }
    if (isDot) {
      hasDot = true
    }
    index++
  }

  return {
    token: { type: 'num', value: input.slice(start, index), pos: start },
    nextIndex: index
  }
}

/**
 * 扫描字符串字面量
 */
function scanString(input: string, start: number): ScanResult {
  const quote = input[start]
  let index = start + 1
  let value = ''
  let closed = false

  while (index < input.length) {
    const current = input[index]
    if (current === '\\') {
      value += input[index + 1] ?? ''
      index += 2
      continue
    }
    if (current === quote) {
      index++
      closed = true
      break
    }
    value += current
    index++
  }

  if (!closed) {
    throw new ParseError('字符串未闭合', start)
  }

  return {
    token: { type: 'str', value, pos: start },
    nextIndex: index
  }
}

/**
 * 扫描标识符
 */
function scanIdentifier(input: string, start: number): ScanResult {
  let index = start
  while (index < input.length && isIdentifierBody(input[index])) {
    index++
  }
  return {
    token: { type: 'ident', value: input.slice(start, index), pos: start },
    nextIndex: index
  }
}

/**
 * 探测操作符
 */
function probeOperator(input: string, index: number): OperatorProbeResult | null {
  const threeChars = input.slice(index, index + 3)
  if (THREE_CHAR_OPS.has(threeChars)) {
    return { value: threeChars, nextIndex: index + 3 }
  }

  const twoChars = input.slice(index, index + 2)
  if (TWO_CHAR_OPS.has(twoChars)) {
    return { value: twoChars, nextIndex: index + 2 }
  }

  const oneChar = input[index]
  if (SINGLE_CHAR_OPS.has(oneChar)) {
    return { value: oneChar, nextIndex: index + 1 }
  }

  return null
}

/**
 * 扫描操作符或标点
 */
function scanSymbol(input: string, index: number): ScanResult {
  const operator = probeOperator(input, index)
  if (operator) {
    return {
      token: { type: 'op', value: operator.value, pos: index },
      nextIndex: operator.nextIndex
    }
  }

  const current = input[index]
  if (PUNCTUATION_CHARS.has(current)) {
    return {
      token: { type: 'punc', value: current, pos: index },
      nextIndex: index + 1
    }
  }

  throw new ParseError(`无法识别字符 '${current}'`, index)
}

/**
 * 词法分析
 */
function tokenize(input: string): Token[] {
  const tokens: Token[] = []
  let index = 0

  while (index < input.length) {
    const current = input[index]
    if (/\s/.test(current)) {
      index++
      continue
    }

    const isDecimalStart = current === '.' && isDigit(input[index + 1] || '')
    if (isDigit(current) || isDecimalStart) {
      const scanned = scanNumber(input, index)
      tokens.push(scanned.token)
      index = scanned.nextIndex
      continue
    }

    if (current === '"' || current === '\'') {
      const scanned = scanString(input, index)
      tokens.push(scanned.token)
      index = scanned.nextIndex
      continue
    }

    if (isIdentifierStart(current)) {
      const scanned = scanIdentifier(input, index)
      tokens.push(scanned.token)
      index = scanned.nextIndex
      continue
    }

    const scanned = scanSymbol(input, index)
    tokens.push(scanned.token)
    index = scanned.nextIndex
  }

  tokens.push({ type: 'eof', value: '', pos: index })
  return tokens
}

/**
 * 表达式解析器
 */
class Parser {
  private cursor = 0

  constructor(private readonly tokens: Token[]) {}

  parse(): Expr {
    const expr = this.parseExpression()
    this.expect('eof')
    return expr
  }

  private current(): Token {
    return this.tokens[this.cursor] || this.tokens[this.tokens.length - 1]
  }

  private previous(): Token {
    return this.tokens[Math.max(0, this.cursor - 1)]
  }

  private advance(): Token {
    const token = this.current()
    this.cursor++
    return token
  }

  private match(type: TokenType, value?: string): boolean {
    const token = this.current()
    if (token.type !== type) {
      return false
    }
    if (value !== undefined && token.value !== value) {
      return false
    }
    this.advance()
    return true
  }

  private expect(type: TokenType, value?: string): Token {
    const token = this.current()
    if (this.match(type, value)) {
      return this.previous()
    }

    const expected = value ? `${type}:${value}` : type
    throw new ParseError(`期望 ${expected}`, token.pos)
  }

  private parseExpression(): Expr {
    return this.parseTernary()
  }

  private parseTernary(): Expr {
    const condition = this.parseBinary(0)
    if (!this.match('op', '?')) {
      return condition
    }

    const thenExpr = this.parseExpression()
    this.expect('op', ':')
    const elseExpr = this.parseExpression()
    return {
      type: 'ternary',
      cond: condition,
      thenExpr,
      elseExpr
    }
  }

  private parseBinary(minPrecedence: number): Expr {
    let left = this.parseUnary()

    while (true) {
      const token = this.current()
      const precedence = token.type === 'op' ? PRECEDENCE[token.value] : undefined
      if (precedence === undefined || precedence < minPrecedence) {
        break
      }

      const operator = token.value
      this.advance()
      const right = this.parseBinary(precedence + 1)
      left = {
        type: 'binary',
        op: operator,
        left,
        right
      }
    }

    return left
  }

  private parseUnary(): Expr {
    if (this.match('op', '-') || this.match('op', '!')) {
      const operator = this.previous().value
      return {
        type: 'unary',
        op: operator,
        expr: this.parseUnary()
      }
    }
    return this.parsePostfix()
  }

  private parsePostfix(): Expr {
    let node = this.parsePrimary()

    while (true) {
      if (this.match('punc', '(')) {
        node = this.parseCall(node)
        continue
      }
      if (this.match('punc', '.')) {
        node = this.parsePropertyAccess(node)
        continue
      }
      if (this.match('punc', '[')) {
        node = this.parseIndexAccess(node)
        continue
      }
      break
    }

    return node
  }

  private parsePrimary(): Expr {
    const token = this.current()

    if (this.match('num')) {
      return { type: 'number', value: Number(token.value) }
    }
    if (this.match('str')) {
      return { type: 'string', value: token.value }
    }
    if (this.match('ident')) {
      if (this.match('op', '=>')) {
        return {
          type: 'lambda',
          param: token.value,
          body: this.parseExpression()
        }
      }
      return { type: 'ref', value: token.value }
    }
    if (this.match('punc', '(')) {
      const expr = this.parseExpression()
      this.expect('punc', ')')
      return expr
    }

    throw new ParseError(`无法解析主表达式: ${token.type}:${token.value}`, token.pos)
  }

  private parseCall(node: Expr): Expr {
    const args = this.parseArguments()
    const name = node.type === 'ref' ? node.value : 'unknown'
    return {
      type: 'call',
      name,
      args
    }
  }

  private parseArguments(): Expr[] {
    const args: Expr[] = []
    if (this.match('punc', ')')) {
      return args
    }

    while (true) {
      args.push(this.parseExpression())
      if (this.match('punc', ',')) {
        continue
      }
      break
    }

    this.expect('punc', ')')
    return args
  }

  private parsePropertyAccess(node: Expr): Expr {
    const property = this.expect('ident')
    if (node.type !== 'ref') {
      throw new ParseError('期望标识符作为属性名', property.pos)
    }

    return {
      type: 'ref',
      value: `${node.value}.${property.value}`
    }
  }

  private parseIndexAccess(node: Expr): Expr {
    const indexExpr = this.parseExpression()
    this.expect('punc', ']')
    if (node.type !== 'ref') {
      throw new ParseError('仅引用类型支持索引访问', this.current().pos)
    }

    return {
      type: 'ref',
      value: `${node.value}[${stringifyExpr(indexExpr)}]`
    }
  }
}

/**
 * 解析入口
 */
export function parse(input: string): Expr {
  const tokens = tokenize(input)
  const parser = new Parser(tokens)
  return parser.parse()
}

export type EvalContext = Record<string, unknown>
type BuiltinFunction = (...args: unknown[]) => unknown
type PredicateFunction = (item: unknown) => boolean
type MapperFunction = (item: unknown) => unknown
type ComparableValue = string | number | boolean

const BUILTIN_FUNCTIONS: Record<string, BuiltinFunction> = {
  abs: Math.abs as BuiltinFunction,
  ceil: Math.ceil as BuiltinFunction,
  floor: Math.floor as BuiltinFunction,
  round: Math.round as BuiltinFunction,
  max: Math.max as BuiltinFunction,
  min: Math.min as BuiltinFunction,
  sqrt: Math.sqrt as BuiltinFunction,
  pow: Math.pow as BuiltinFunction,
  len: (...args: unknown[]) => String(args[0]).length,
  upper: (...args: unknown[]) => String(args[0]).toUpperCase(),
  lower: (...args: unknown[]) => String(args[0]).toLowerCase(),
  trim: (...args: unknown[]) => String(args[0]).trim(),
  substr: (...args: unknown[]) => {
    const source = String(args[0])
    const start = Number(args[1] ?? 0)
    const len = args[2] !== undefined ? Number(args[2]) : undefined
    return len === undefined ? source.slice(start) : source.slice(start, start + len)
  },
  count: (...args: unknown[]) => {
    const arr = args[0]
    return Array.isArray(arr) ? arr.length : 0
  },
  sum: (...args: unknown[]) => {
    const arr = args[0]
    return Array.isArray(arr) ? arr.reduce<number>((a, b) => a + Number(b), 0) : 0
  },
  avg: (...args: unknown[]) => {
    const arr = args[0]
    if (!Array.isArray(arr) || arr.length === 0) {
      return 0
    }
    return arr.reduce<number>((a, b) => a + Number(b), 0) / arr.length
  },
  countIf: (...args: unknown[]) => {
    const [arr, predicate] = args as [unknown, PredicateFunction?]
    if (!Array.isArray(arr) || typeof predicate !== 'function') {
      return 0
    }
    return arr.filter(predicate).length
  },
  filter: (...args: unknown[]) => {
    const [arr, predicate] = args as [unknown, PredicateFunction?]
    if (!Array.isArray(arr) || typeof predicate !== 'function') {
      return []
    }
    return arr.filter(predicate)
  },
  map: (...args: unknown[]) => {
    const [arr, fn] = args as [unknown, MapperFunction?]
    if (!Array.isArray(arr) || typeof fn !== 'function') {
      return []
    }
    return arr.map(fn)
  },
  some: (...args: unknown[]) => {
    const [arr, predicate] = args as [unknown, PredicateFunction?]
    if (!Array.isArray(arr) || typeof predicate !== 'function') {
      return false
    }
    return arr.some(predicate)
  },
  every: (...args: unknown[]) => {
    const [arr, predicate] = args as [unknown, PredicateFunction?]
    if (!Array.isArray(arr) || typeof predicate !== 'function') {
      return false
    }
    return arr.every(predicate)
  }
}

function normalizeRefSegment(segment: string): string {
  const normalized = segment.replace(/\]$/, '').trim()
  if (
    (normalized.startsWith('"') && normalized.endsWith('"')) ||
    (normalized.startsWith('\'') && normalized.endsWith('\''))
  ) {
    return normalized.slice(1, -1)
  }
  return normalized
}

function splitRefPath(path: string): string[] {
  return path
    .split(/\.|\[/)
    .map(normalizeRefSegment)
    .filter(Boolean)
}

function resolveRef(path: string, context: EvalContext): unknown {
  const parts = splitRefPath(path)
  let current: unknown = context

  for (const part of parts) {
    if (current === undefined || current === null) {
      return undefined
    }

    const isArrayIndex = /^\d+$/.test(part)
    if (isArrayIndex && Array.isArray(current)) {
      current = current[Number(part)]
      continue
    }

    if (typeof current === 'object') {
      current = (current as Record<string, unknown>)[part]
      continue
    }

    return undefined
  }

  return current
}

type BinaryOperator = (left: unknown, right: unknown) => unknown
type UnaryOperator = (value: unknown) => unknown

const looseEqual: BinaryOperator = (left, right) =>
  // eslint-disable-next-line eqeqeq
  left == right

const looseNotEqual: BinaryOperator = (left, right) =>
  // eslint-disable-next-line eqeqeq
  left != right

const BINARY_OPERATORS: Readonly<Record<string, BinaryOperator>> = {
  '+': (left, right) => Number(left) + Number(right),
  '-': (left, right) => Number(left) - Number(right),
  '*': (left, right) => Number(left) * Number(right),
  '/': (left, right) => Number(left) / Number(right),
  '%': (left, right) => Number(left) % Number(right),
  '<': (left, right) => (left as ComparableValue) < (right as ComparableValue),
  '>': (left, right) => (left as ComparableValue) > (right as ComparableValue),
  '<=': (left, right) => (left as ComparableValue) <= (right as ComparableValue),
  '>=': (left, right) => (left as ComparableValue) >= (right as ComparableValue),
  '==': looseEqual,
  '!=': looseNotEqual,
  '===': (left, right) => left === right,
  '!==': (left, right) => left !== right
}

const UNARY_OPERATORS: Readonly<Record<string, UnaryOperator>> = {
  '-': (value) => -Number(value),
  '!': (value) => !value
}

/**
 * 评估 lambda
 */
function evaluateLambda(expr: Extract<Expr, { type: 'lambda' }>, context: EvalContext) {
  return (param: unknown) => {
    const newContext = { ...context, [expr.param]: param }
    return evaluate(expr.body, newContext)
  }
}

/**
 * 评估函数调用
 */
function evaluateCall(expr: Extract<Expr, { type: 'call' }>, context: EvalContext): unknown {
  const fn = BUILTIN_FUNCTIONS[expr.name] || context[expr.name]
  if (typeof fn !== 'function') {
    throw new Error(`未定义的函数: ${expr.name}`)
  }

  const args = expr.args.map((arg) => {
    if (arg.type === 'lambda') {
      return evaluateLambda(arg, context)
    }
    return evaluate(arg, context)
  })
  return fn(...args)
}

/**
 * 评估一元表达式
 */
function evaluateUnary(expr: Extract<Expr, { type: 'unary' }>, context: EvalContext): unknown {
  const operation = UNARY_OPERATORS[expr.op]
  if (!operation) {
    throw new Error(`未知的一元运算符: ${expr.op}`)
  }
  return operation(evaluate(expr.expr, context))
}

/**
 * 评估二元表达式
 */
function evaluateBinary(expr: Extract<Expr, { type: 'binary' }>, context: EvalContext): unknown {
  if (expr.op === '&&') {
    const left = evaluate(expr.left, context)
    return left ? evaluate(expr.right, context) : left
  }

  if (expr.op === '||') {
    const left = evaluate(expr.left, context)
    return left ? left : evaluate(expr.right, context)
  }

  const operation = BINARY_OPERATORS[expr.op]
  if (!operation) {
    throw new Error(`未知的二元运算符: ${expr.op}`)
  }
  const left = evaluate(expr.left, context)
  const right = evaluate(expr.right, context)
  return operation(left, right)
}

type ExprEvaluatorMap = {
  [K in Expr['type']]: (expr: Extract<Expr, { type: K }>, context: EvalContext) => unknown
}

const EXPRESSION_EVALUATORS: ExprEvaluatorMap = {
  number: (expr) => expr.value,
  string: (expr) => expr.value,
  ref: (expr, context) => resolveRef(expr.value, context),
  unary: (expr, context) => evaluateUnary(expr, context),
  binary: (expr, context) => evaluateBinary(expr, context),
  ternary: (expr, context) =>
    evaluate(expr.cond, context) ? evaluate(expr.thenExpr, context) : evaluate(expr.elseExpr, context),
  call: (expr, context) => evaluateCall(expr, context),
  lambda: (expr, context) => evaluateLambda(expr, context)
}

export function evaluate(expr: Expr, context: EvalContext = {}): unknown {
  const evaluator = EXPRESSION_EVALUATORS[expr.type] as (value: Expr, scope: EvalContext) => unknown
  return evaluator(expr, context)
}

/**
 * 便捷函数：解析并评估表达式
 */
export function exec(input: string, context: EvalContext = {}): unknown {
  return evaluate(parse(input), context)
}

export default {
  parse,
  evaluate,
  exec,
  ParseError
}
