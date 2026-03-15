# Woodlin System Z3 Module

Z3 约束求解模块，基于微软 [Z3 SMT Solver](https://github.com/Z3Prover/z3) 提供声明式约束求解能力。

## 功能概述

- **约束求解**：通过 `Z3SolverService` 声明式地构建整数/实数约束并求解
- **范围验证**：内置 `checkIntRange` 快速验证变量取值区间
- **线性求和**：内置 `checkLinearSum` 验证多变量求和约束（如分数分配）
- **可满足性检查**：`checkSatisfiable` 快速判断约束是否有解
- **Spring Boot 自动配置**：零配置即可注入使用

## 适用场景

| 场景 | 说明 |
|------|------|
| 考试评分规则验证 | 验证评分规则的合理性（如各题分数之和是否等于总分） |
| 量表计算约束 | 心理量表、问卷量表的维度分数与总分约束校验 |
| 排考/排课 | 时间段、教室、考生之间的冲突约束求解 |
| 数据校验 | 表单字段之间的复杂依赖关系验证 |

## 快速开始

### 1. 引入依赖

模块已在 `woodlin-admin` 中自动引入，也可在其他模块中单独使用：

```xml
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-system-z3</artifactId>
    <version>${woodlin.version}</version>
</dependency>
```

### 2. 注入使用

```java
@Autowired
private Z3SolverService z3SolverService;

// 验证分数范围
ConstraintResult result = z3SolverService.checkIntRange("score", 0, 100, null);
if (result.isSatisfiable()) {
    System.out.println("约束可满足: " + result.getModel());
}
```

### 3. 自定义约束

```java
ConstraintResult result = z3SolverService.solve(ctx -> {
    IntExpr math = ctx.mkIntConst("math");
    IntExpr english = ctx.mkIntConst("english");
    IntExpr total = ctx.mkIntConst("total");
    
    return List.of(
        ctx.mkGe(math, ctx.mkInt(0)),      // math >= 0
        ctx.mkLe(math, ctx.mkInt(100)),     // math <= 100
        ctx.mkGe(english, ctx.mkInt(0)),    // english >= 0
        ctx.mkLe(english, ctx.mkInt(100)),  // english <= 100
        ctx.mkEq(total, ctx.mkAdd(math, english)), // total = math + english
        ctx.mkGe(total, ctx.mkInt(120))     // 总分 >= 120 (及格线)
    );
});
```

### 4. 量表分数验证

```java
// 验证 5 个维度分数之和是否等于总分 100
List<String> dimensions = List.of("dim1", "dim2", "dim3", "dim4", "dim5");
int[] mins = {0, 0, 0, 0, 0};
int[] maxs = {30, 30, 30, 30, 30};

ConstraintResult result = z3SolverService.checkLinearSum(dimensions, 100, mins, maxs);
```

## API 参考

| 方法 | 说明 |
|------|------|
| `solve(ConstraintBuilder)` | 通用约束求解，通过回调构建约束 |
| `checkSatisfiable(ConstraintBuilder)` | 检查约束是否可满足 |
| `checkIntRange(name, min, max, extra)` | 验证整数变量范围 |
| `checkLinearSum(vars, sum, mins, maxs)` | 验证线性求和约束 |
| `solveReal(ConstraintBuilder)` | 实数域约束求解 |

## 技术实现

- 依赖 `tools.aqua:z3-turnkey`，自包含全平台（Linux/macOS/Windows）原生库
- 每次求解创建独立 `Context`，保证线程安全
- Spring Boot AutoConfiguration 自动注册服务 Bean
