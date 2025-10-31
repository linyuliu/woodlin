// 文件：expr-engine.ts
// 递归下降解析器 + AST 评估器（TypeScript）
// 支持：数字、字符串、引用/索引、函数调用、lambda、三元表达式、countIf 等
export type Expr =
    | { type: 'number'; value: number }
    | { type: 'string'; value: string }
    | { type: 'ref'; value: string } // e.g. answers.q[0]
    | { type: 'unary'; op: string; expr: Expr }
    | { type: 'binary'; op: string; left: Expr; right: Expr }
    | { type: 'ternary'; cond: Expr; thenExpr: Expr; elseExpr: Expr }
    | { type: 'call'; name: string; args: Expr[] }
    | { type: 'lambda'; param: string; body: Expr };

export class ParseError extends Error {
    constructor(public message: string, public pos: number) {
        super(`${message} (pos=${pos})`);
        this.name = 'ParseError';
    }
}

type TokenType = 'num' | 'str' | 'ident' | 'op' | 'punc' | 'eof';
type Token = { type: TokenType; value: string; pos: number };

const isDigit = (ch: string) => /[0-9]/.test(ch);
const isIdStart = (ch: string) => /[a-zA-Z_]/.test(ch);
const isId = (ch: string) => /[a-zA-Z0-9_]/.test(ch);

function tokenize(input: string): Token[] {
    const out: Token[] = [];
    let i = 0;
    const len = input.length;

    const push = (type: TokenType, value: string, pos = i) =>
        out.push({type, value, pos});

    while (i < len) {
        const ch = input[i];
        if (ch === ' ' || ch === '\t' || ch === '\n' || ch === '\r') {
            i++;
            continue;
        }

        // 数字（支持小数）
        if (isDigit(ch) || (ch === '.' && isDigit(input[i + 1] || ''))) {
            let j = i;
            let hasDot = false;
            while (
                j < len &&
                (isDigit(input[j]) || (!hasDot && input[j] === '.'))
                ) {
                if (input[j] === '.') {hasDot = true;}
                j++;
            }
            push('num', input.slice(i, j), i);
            i = j;
            continue;
        }

        // 字符串字面量
        if (ch === '"' || ch === "'") {
            const quote = ch;
            let j = i + 1;
            let s = '';
            while (j < len) {
                if (input[j] === '\\') {
                    s += input[j + 1] ?? '';
                    j += 2;
                    continue;
                }
                if (input[j] === quote) {
                    j++;
                    break;
                }
                s += input[j];
                j++;
            }
            push('str', s, i);
            i = j;
            continue;
        }

        // 标识符或关键字
        if (isIdStart(ch)) {
            let j = i;
            while (j < len && isId(input[j])) {j++;}
            const id = input.slice(i, j);
            push('ident', id, i);
            i = j;
            continue;
        }

        // 操作符或标点（优先长符号）
        const two = input.slice(i, i + 2);
        const three = input.slice(i, i + 3);
        if (three === '===' || three === '!==') {
            push('op', three, i);
            i += 3;
            continue;
        }
        if (two === '&&' || two === '||' || two === '==' || two === '!=' ||
            two === '>=' || two === '<=' || two === '=>') {
            push('op', two, i);
            i += 2;
            continue;
        }
        if ('+-*/%<>?:=!'.includes(ch)) {
            push('op', ch, i);
            i++;
            continue;
        }
        if ('(),[].'.includes(ch)) {
            push('punc', ch, i);
            i++;
            continue;
        }

        throw new ParseError(`无法识别字符 '${ch}'`, i);
    }

    push('eof', '', i);
    return out;
}

// 辅助函数：将 AST 转换为字符串（用于调试和索引表达式）
function stringifyExpr(expr: Expr): string {
    switch (expr.type) {
        case 'number':
            return String(expr.value);
        case 'string':
            return `"${expr.value}"`;
        case 'ref':
            return expr.value;
        case 'unary':
            return `${expr.op}${stringifyExpr(expr.expr)}`;
        case 'binary':
            return `(${stringifyExpr(expr.left)} ${expr.op} ${stringifyExpr(expr.right)})`;
        case 'ternary':
            return `(${stringifyExpr(expr.cond)} ? ${stringifyExpr(expr.thenExpr)} : ${stringifyExpr(expr.elseExpr)})`;
        case 'call':
            return `${expr.name}(${expr.args.map(stringifyExpr).join(', ')})`;
        case 'lambda':
            return `${expr.param} => ${stringifyExpr(expr.body)}`;
        default:
            return 'unknown';
    }
}

// Parser 构造器：返回 parseExpr 起点函数
const makeParser = (tokens: Token[]) => {
    // 运算符优先级表（值越大优先级越高）
    const PRECEDENCE: Record<string, number> = {
        '||': 1,
        '&&': 2,
        '==': 3, '!=': 3, '===': 3, '!==': 3,
        '<': 4, '>': 4, '<=': 4, '>=': 4,
        '+': 5, '-': 5,
        '*': 6, '/': 6, '%': 6,
    };

    // parsePrimary：解析基础表达式：字面量、引用、调用、索引、括号、unary
    const parsePrimary = (i: number): [Expr, number] => {
        const t = tokens[i];
        if (!t) {throw new ParseError('意外结束', i);}
        if (t.type === 'num') {return [{type: 'number', value: Number(t.value)}, i + 1];}
        if (t.type === 'str') {return [{type: 'string', value: t.value}, i + 1];}

        if (t.type === 'ident') {
            // 识别 lambda（param => expr）
            const next = tokens[i + 1];
            if (next && next.type === 'op' && next.value === '=>') {
                const param = t.value;
                const [body, idx] = parseExpr(i + 2);
                return [{type: 'lambda', param, body}, idx];
            }

            // 否则处理引用/调用/属性/索引链
            let node: Expr = {type: 'ref', value: t.value};
            let cur = i + 1;
            while (true) {
                const tk = tokens[cur];
                if (!tk) {break;}
                // 调用
                if (tk.type === 'punc' && tk.value === '(') {
                    const args: Expr[] = [];
                    cur++; // skip '('
                    if (!(tokens[cur].type === 'punc' && tokens[cur].value === ')')) {
                        while (true) {
                            const [arg, nextIdx] = parseExpr(cur);
                            args.push(arg);
                            cur = nextIdx;
                            if (tokens[cur].type === 'punc' && tokens[cur].value === ',') {
                                cur++;
                                continue;
                            }
                            break;
                        }
                    }
                    if (!(tokens[cur].type === 'punc' && tokens[cur].value === ')'))
                        {throw new ParseError('期望 )', tokens[cur].pos);}
                    cur++;
                    node = {type: 'call', name: (node as any).value ?? 'unknown', args};
                    continue;
                }
                // 属性访问
                if (tk.type === 'punc' && tk.value === '.') {
                    if (!(tokens[cur + 1] && tokens[cur + 1].type === 'ident'))
                        {throw new ParseError('期望标识符作为属性名', tokens[cur].pos);}
                    const prop = tokens[cur + 1].value;
                    (node as any).value = (node as any).value + '.' + prop;
                    cur += 2;
                    continue;
                }
                // 索引 [...]
                if (tk.type === 'punc' && tk.value === '[') {
                    cur++;
                    const [idxExpr, nextIdx] = parseExpr(cur);
                    cur = nextIdx;
                    if (!(tokens[cur].type === 'punc' && tokens[cur].value === ']'))
                        {throw new ParseError('期望 ]', tokens[cur].pos);}
                    cur++;
                    // 将索引表达式序列化内嵌到 ref 字符串（evaluate 时会解析）
                    (node as any).value = (node as any).value + '[' + stringifyExpr(idxExpr) + ']';
                    continue;
                }
                break;
            }
            return [node, cur];
        }

        if (t.type === 'punc' && t.value === '(') {
            const [expr, nextIdx] = parseExpr(i + 1);
            if (!(tokens[nextIdx].type === 'punc' && tokens[nextIdx].value === ')'))
                {throw new ParseError('期望 )', tokens[nextIdx].pos);}
            return [expr, nextIdx + 1];
        }

        if (t.type === 'op' && (t.value === '-' || t.value === '!')) {
            const [right, nextIdx] = parsePrimary(i + 1);
            return [{type: 'unary', op: t.value, expr: right}, nextIdx];
        }

        throw new ParseError(`无法解析主表达式: ${t.type}:${t.value}`, t.pos);
    };

    // parseBinary：处理二元运算符（左结合，优先级爬升）
    const parseBinary = (i: number, minPrec: number): [Expr, number] => {
        let [left, cur] = parsePrimary(i);

        while (true) {
            const tk = tokens[cur];
            if (!tk || tk.type !== 'op') {break;}
            const prec = PRECEDENCE[tk.value];
            if (prec === undefined || prec < minPrec) {break;}

            cur++; // skip operator
            const [right, nextIdx] = parseBinary(cur, prec + 1);
            left = {type: 'binary', op: tk.value, left, right};
            cur = nextIdx;
        }

        return [left, cur];
    };

    // parseExpr：入口解析函数，处理三元表达式
    const parseExpr = (i: number): [Expr, number] => {
        const [cond, cur] = parseBinary(i, 0);
        
        // 三元表达式
        if (tokens[cur] && tokens[cur].type === 'op' && tokens[cur].value === '?') {
            const [thenExpr, nextIdx] = parseExpr(cur + 1);
            if (!(tokens[nextIdx].type === 'op' && tokens[nextIdx].value === ':'))
                {throw new ParseError('三元表达式期望 :', tokens[nextIdx].pos);}
            const [elseExpr, finalIdx] = parseExpr(nextIdx + 1);
            return [{type: 'ternary', cond, thenExpr, elseExpr}, finalIdx];
        }

        return [cond, cur];
    };

    return parseExpr;
};

// 解析入口函数
export function parse(input: string): Expr {
    const tokens = tokenize(input);
    const parseExpr = makeParser(tokens);
    const [expr, idx] = parseExpr(0);
    
    // 确保所有 token 都被解析
    if (tokens[idx].type !== 'eof') {
        throw new ParseError(`意外的 token: ${tokens[idx].value}`, tokens[idx].pos);
    }
    
    return expr;
}

// 评估上下文类型
export type EvalContext = Record<string, any>;

// 内置函数库
const BUILTIN_FUNCTIONS: Record<string, (...args: any[]) => any> = {
    // 数学函数
    abs: Math.abs,
    ceil: Math.ceil,
    floor: Math.floor,
    round: Math.round,
    max: Math.max,
    min: Math.min,
    sqrt: Math.sqrt,
    pow: Math.pow,
    
    // 字符串函数
    len: (s: any) => String(s).length,
    upper: (s: any) => String(s).toUpperCase(),
    lower: (s: any) => String(s).toLowerCase(),
    trim: (s: any) => String(s).trim(),
    substr: (s: any, start: number, len?: number) => String(s).substr(start, len),
    
    // 数组函数
    count: (arr: any[]) => Array.isArray(arr) ? arr.length : 0,
    sum: (arr: any[]) => Array.isArray(arr) ? arr.reduce((a, b) => a + Number(b), 0) : 0,
    avg: (arr: any[]) => {
        if (!Array.isArray(arr) || arr.length === 0) {return 0;}
        return arr.reduce((a, b) => a + Number(b), 0) / arr.length;
    },
    
    // 高阶函数
    countIf: (arr: any[], predicate: (item: any) => boolean) => {
        if (!Array.isArray(arr)) {return 0;}
        return arr.filter(predicate).length;
    },
    filter: (arr: any[], predicate: (item: any) => boolean) => {
        if (!Array.isArray(arr)) {return [];}
        return arr.filter(predicate);
    },
    map: (arr: any[], fn: (item: any) => any) => {
        if (!Array.isArray(arr)) {return [];}
        return arr.map(fn);
    },
    some: (arr: any[], predicate: (item: any) => boolean) => {
        if (!Array.isArray(arr)) {return false;}
        return arr.some(predicate);
    },
    every: (arr: any[], predicate: (item: any) => boolean) => {
        if (!Array.isArray(arr)) {return false;}
        return arr.every(predicate);
    },
};

// 解析引用路径（支持嵌套属性和索引）
function resolveRef(path: string, context: EvalContext): any {
    // 处理简单的点分隔路径和数组索引
    const parts = path.split(/\.|\[/).map(p => p.replace(/\]$/, ''));
    let value: any = context;
    
    for (const part of parts) {
        if (value === undefined || value === null) {return undefined;}
        
        // 如果是数字，当作数组索引
        if (/^\d+$/.test(part)) {
            value = value[Number(part)];
        } else {
            value = value[part];
        }
    }
    
    return value;
}

// 评估表达式
export function evaluate(expr: Expr, context: EvalContext = {}): any {
    switch (expr.type) {
        case 'number':
            return expr.value;
            
        case 'string':
            return expr.value;
            
        case 'ref':
            return resolveRef(expr.value, context);
            
        case 'unary':
            const operand = evaluate(expr.expr, context);
            if (expr.op === '-') {return -Number(operand);}
            if (expr.op === '!') {return !operand;}
            throw new Error(`未知的一元运算符: ${expr.op}`);
            
        case 'binary': {
            const left = evaluate(expr.left, context);
            const right = evaluate(expr.right, context);
            
            switch (expr.op) {
                case '+': return Number(left) + Number(right);
                case '-': return Number(left) - Number(right);
                case '*': return Number(left) * Number(right);
                case '/': return Number(left) / Number(right);
                case '%': return Number(left) % Number(right);
                case '<': return left < right;
                case '>': return left > right;
                case '<=': return left <= right;
                case '>=': return left >= right;
                case '==': return left == right;
                case '!=': return left != right;
                case '===': return left === right;
                case '!==': return left !== right;
                case '&&': return left && right;
                case '||': return left || right;
                default:
                    throw new Error(`未知的二元运算符: ${expr.op}`);
            }
        }
            
        case 'ternary': {
            const cond = evaluate(expr.cond, context);
            return cond ? evaluate(expr.thenExpr, context) : evaluate(expr.elseExpr, context);
        }
            
        case 'call': {
            const fn = BUILTIN_FUNCTIONS[expr.name] || context[expr.name];
            if (typeof fn !== 'function') {
                throw new Error(`未定义的函数: ${expr.name}`);
            }
            
            // 评估参数（特殊处理 lambda）
            const args = expr.args.map(arg => {
                if (arg.type === 'lambda') {
                    // 返回一个函数，在调用时评估 lambda body
                    return (param: any) => {
                        const newContext = {...context, [arg.param]: param};
                        return evaluate(arg.body, newContext);
                    };
                }
                return evaluate(arg, context);
            });
            
            return fn(...args);
        }
            
        case 'lambda':
            // Lambda 本身不直接评估，需要在函数调用时处理
            return (param: any) => {
                const newContext = {...context, [expr.param]: param};
                return evaluate(expr.body, newContext);
            };
            
        default:
            throw new Error(`未知的表达式类型: ${(expr as any).type}`);
    }
}

// 便捷函数：解析并评估表达式
export function exec(input: string, context: EvalContext = {}): any {
    const expr = parse(input);
    return evaluate(expr, context);
}

// 导出类型和函数
export default {
    parse,
    evaluate,
    exec,
    ParseError,
};
