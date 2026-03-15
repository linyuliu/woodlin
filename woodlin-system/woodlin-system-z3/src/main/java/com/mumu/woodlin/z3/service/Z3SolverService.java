package com.mumu.woodlin.z3.service;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.mumu.woodlin.z3.model.ConstraintResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Z3 约束求解服务.
 *
 * <p>封装微软 Z3 SMT 求解器，提供声明式约束求解能力。
 * 适用于考试评分规则验证、量表计算约束检查、排考排课等场景。</p>
 *
 * <p>每次调用均创建独立的 {@link Context}，确保线程安全。</p>
 *
 * @author mumu
 * @since 2025-10-28
 */
@Service
public class Z3SolverService {

    private static final Logger log = LoggerFactory.getLogger(Z3SolverService.class);

    /**
     * 使用回调方式进行约束求解.
     *
     * <p>调用方通过 {@link ConstraintBuilder} 函数式接口构建约束，
     * 服务负责管理 Z3 Context 的生命周期。</p>
     *
     * <p>示例：</p>
     * <pre>{@code
     * ConstraintResult result = solverService.solve(ctx -> {
     *     IntExpr x = ctx.mkIntConst("x");
     *     IntExpr y = ctx.mkIntConst("y");
     *     return List.of(
     *         ctx.mkGe(x, ctx.mkInt(0)),   // x >= 0
     *         ctx.mkLe(x, ctx.mkInt(100)), // x <= 100
     *         ctx.mkEq(ctx.mkAdd(x, y), ctx.mkInt(100)) // x + y = 100
     *     );
     * });
     * }</pre>
     *
     * @param builder 约束构建器
     * @return 约束求解结果
     */
    public ConstraintResult solve(ConstraintBuilder builder) {
        try (Context ctx = new Context()) {
            Solver solver = ctx.mkSolver();
            List<BoolExpr> constraints = builder.build(ctx);
            for (BoolExpr constraint : constraints) {
                solver.add(constraint);
            }
            return extractResult(solver);
        } catch (Exception e) {
            log.error("Z3 约束求解异常", e);
            return new ConstraintResult(ConstraintResult.Status.UNKNOWN, Map.of());
        }
    }

    /**
     * 检查一组布尔约束是否可满足.
     *
     * @param builder 约束构建器
     * @return 如果约束可满足返回 {@code true}
     */
    public boolean checkSatisfiable(ConstraintBuilder builder) {
        return solve(builder).isSatisfiable();
    }

    /**
     * 验证整数变量在给定约束下的取值范围.
     *
     * <p>典型场景：检验考试分数是否在有效区间内。</p>
     *
     * @param variableName 变量名称
     * @param min          最小值（含）
     * @param max          最大值（含）
     * @param extraBuilder 附加约束构建器（可为 {@code null}）
     * @return 约束求解结果
     */
    public ConstraintResult checkIntRange(String variableName, int min, int max,
                                          ConstraintBuilder extraBuilder) {
        return solve(ctx -> {
            IntExpr var = ctx.mkIntConst(variableName);
            java.util.List<BoolExpr> constraints = new java.util.ArrayList<>();
            constraints.add(ctx.mkGe(var, ctx.mkInt(min)));
            constraints.add(ctx.mkLe(var, ctx.mkInt(max)));
            if (extraBuilder != null) {
                constraints.addAll(extraBuilder.build(ctx));
            }
            return constraints;
        });
    }

    /**
     * 验证线性约束（等式/不等式）的可满足性.
     *
     * <p>典型场景：验证量表各维度分数之和等于总分。</p>
     *
     * @param variableNames 变量名列表
     * @param expectedSum   期望总和
     * @param minValues     各变量最小值
     * @param maxValues     各变量最大值
     * @return 约束求解结果
     */
    @SuppressWarnings("unchecked")
    public ConstraintResult checkLinearSum(List<String> variableNames, int expectedSum,
                                           int[] minValues, int[] maxValues) {
        if (variableNames.size() != minValues.length || variableNames.size() != maxValues.length) {
            throw new IllegalArgumentException("变量名列表与最值数组长度不一致");
        }
        return solve(ctx -> {
            IntExpr[] vars = new IntExpr[variableNames.size()];
            java.util.List<BoolExpr> constraints = new java.util.ArrayList<>();
            for (int i = 0; i < variableNames.size(); i++) {
                vars[i] = ctx.mkIntConst(variableNames.get(i));
                constraints.add(ctx.mkGe(vars[i], ctx.mkInt(minValues[i])));
                constraints.add(ctx.mkLe(vars[i], ctx.mkInt(maxValues[i])));
            }
            ArithExpr<?>[] arithVars = vars;
            constraints.add(ctx.mkEq(ctx.mkAdd(arithVars), ctx.mkInt(expectedSum)));
            return constraints;
        });
    }

    /**
     * 使用实数域进行约束求解.
     *
     * <p>适用于量表维度得分等需要小数精度的场景。</p>
     *
     * @param builder 约束构建器（使用 {@link Context#mkRealConst} 创建实数变量）
     * @return 约束求解结果
     */
    public ConstraintResult solveReal(ConstraintBuilder builder) {
        return solve(builder);
    }

    /**
     * 从 Solver 中提取求解结果.
     */
    private ConstraintResult extractResult(Solver solver) {
        Status status = solver.check();
        switch (status) {
            case SATISFIABLE:
                Model model = solver.getModel();
                Map<String, Object> values = new HashMap<>();
                for (FuncDecl<?> decl : model.getDecls()) {
                    String name = decl.getName().toString();
                    Expr<?> value = model.getConstInterp(decl);
                    values.put(name, parseZ3Value(value));
                }
                log.debug("Z3 求解成功: {}", values);
                return new ConstraintResult(ConstraintResult.Status.SATISFIABLE, values);
            case UNSATISFIABLE:
                log.debug("Z3 约束不可满足");
                return new ConstraintResult(ConstraintResult.Status.UNSATISFIABLE, Map.of());
            default:
                log.warn("Z3 求解状态未知");
                return new ConstraintResult(ConstraintResult.Status.UNKNOWN, Map.of());
        }
    }

    /**
     * 将 Z3 表达式解析为 Java 对象.
     */
    private Object parseZ3Value(Expr<?> expr) {
        if (expr.isIntNum()) {
            return Integer.parseInt(expr.toString());
        }
        if (expr.isRatNum()) {
            String[] parts = expr.toString().split("/");
            if (parts.length == 2) {
                return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
            }
            return Double.parseDouble(expr.toString());
        }
        if (expr.isTrue()) {
            return true;
        }
        if (expr.isFalse()) {
            return false;
        }
        return expr.toString();
    }

    /**
     * 约束构建器函数式接口.
     *
     * <p>调用方实现此接口，在回调中使用 Z3 {@link Context} 构建约束表达式。</p>
     */
    @FunctionalInterface
    public interface ConstraintBuilder {

        /**
         * 使用给定的 Z3 上下文构建约束列表.
         *
         * @param ctx Z3 上下文
         * @return 布尔约束列表
         */
        List<BoolExpr> build(Context ctx);
    }
}
