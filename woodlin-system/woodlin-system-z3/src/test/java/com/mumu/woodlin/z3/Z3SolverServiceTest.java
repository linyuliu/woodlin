package com.mumu.woodlin.z3;

import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.mumu.woodlin.z3.model.ConstraintResult;
import com.mumu.woodlin.z3.service.Z3SolverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Z3 求解服务单元测试.
 *
 * @author mumu
 * @since 2025-10-28
 */
class Z3SolverServiceTest {

    private Z3SolverService solverService;

    @BeforeEach
    void setUp() {
        solverService = new Z3SolverService();
    }

    @Test
    @DisplayName("基本约束求解 - 整数变量范围")
    void testBasicIntegerConstraint() {
        ConstraintResult result = solverService.solve(ctx -> {
            IntExpr x = ctx.mkIntConst("x");
            return List.of(
                ctx.mkGe(x, ctx.mkInt(0)),
                ctx.mkLe(x, ctx.mkInt(100))
            );
        });

        assertTrue(result.isSatisfiable());
        assertNotNull(result.getModel());
        assertEquals(ConstraintResult.Status.SATISFIABLE, result.getStatus());
    }

    @Test
    @DisplayName("不可满足约束 - 矛盾条件")
    void testUnsatisfiableConstraint() {
        ConstraintResult result = solverService.solve(ctx -> {
            IntExpr x = ctx.mkIntConst("x");
            return List.of(
                ctx.mkGt(x, ctx.mkInt(100)),
                ctx.mkLt(x, ctx.mkInt(0))
            );
        });

        assertFalse(result.isSatisfiable());
        assertEquals(ConstraintResult.Status.UNSATISFIABLE, result.getStatus());
    }

    @Test
    @DisplayName("线性求和约束 - 验证分数分配")
    void testLinearSumConstraint() {
        List<String> vars = List.of("math", "english", "science");
        int[] mins = {0, 0, 0};
        int[] maxs = {100, 100, 100};

        ConstraintResult result = solverService.checkLinearSum(vars, 250, mins, maxs);

        assertTrue(result.isSatisfiable());
        assertNotNull(result.getModel().get("math"));
        assertNotNull(result.getModel().get("english"));
        assertNotNull(result.getModel().get("science"));
    }

    @Test
    @DisplayName("线性求和约束 - 不可满足的分数总和")
    void testLinearSumUnsatisfiable() {
        List<String> vars = List.of("a", "b");
        int[] mins = {0, 0};
        int[] maxs = {10, 10};

        // 总和为100，但每个变量最大为10，不可满足
        ConstraintResult result = solverService.checkLinearSum(vars, 100, mins, maxs);

        assertFalse(result.isSatisfiable());
    }

    @Test
    @DisplayName("线性求和约束 - 参数长度不一致应抛异常")
    void testLinearSumInvalidArguments() {
        List<String> vars = List.of("a", "b");
        int[] mins = {0};
        int[] maxs = {10, 10};

        assertThrows(IllegalArgumentException.class,
            () -> solverService.checkLinearSum(vars, 10, mins, maxs));
    }

    @Test
    @DisplayName("整数范围检查 - 基本范围")
    void testCheckIntRange() {
        ConstraintResult result = solverService.checkIntRange("score", 0, 100, null);

        assertTrue(result.isSatisfiable());
        Object score = result.getModel().get("score");
        assertNotNull(score);
    }

    @Test
    @DisplayName("整数范围检查 - 带附加约束")
    void testCheckIntRangeWithExtraConstraints() {
        ConstraintResult result = solverService.checkIntRange("score", 0, 100, ctx -> {
            IntExpr score = ctx.mkIntConst("score");
            return List.of(ctx.mkEq(score, ctx.mkInt(85)));
        });

        assertTrue(result.isSatisfiable());
        assertEquals(85, result.getModel().get("score"));
    }

    @Test
    @DisplayName("布尔可满足性检查")
    void testCheckSatisfiable() {
        boolean sat = solverService.checkSatisfiable(ctx -> {
            IntExpr x = ctx.mkIntConst("x");
            return List.of(
                ctx.mkGe(x, ctx.mkInt(1)),
                ctx.mkLe(x, ctx.mkInt(10))
            );
        });

        assertTrue(sat);
    }

    @Test
    @DisplayName("多变量约束求解 - 考试评分规则")
    void testExamScoringConstraints() {
        ConstraintResult result = solverService.solve(ctx -> {
            IntExpr totalScore = ctx.mkIntConst("totalScore");
            IntExpr objective = ctx.mkIntConst("objective");
            IntExpr subjective = ctx.mkIntConst("subjective");

            return List.of(
                // 客观题 0-60 分
                ctx.mkGe(objective, ctx.mkInt(0)),
                ctx.mkLe(objective, ctx.mkInt(60)),
                // 主观题 0-40 分
                ctx.mkGe(subjective, ctx.mkInt(0)),
                ctx.mkLe(subjective, ctx.mkInt(40)),
                // 总分 = 客观题 + 主观题
                ctx.mkEq(totalScore, ctx.mkAdd(objective, subjective)),
                // 及格线: 总分 >= 60
                ctx.mkGe(totalScore, ctx.mkInt(60))
            );
        });

        assertTrue(result.isSatisfiable());
        int total = (int) result.getModel().get("totalScore");
        assertTrue(total >= 60 && total <= 100);
    }

    @Test
    @DisplayName("ConstraintResult toString 输出")
    void testConstraintResultToString() {
        ConstraintResult result = new ConstraintResult(
            ConstraintResult.Status.SATISFIABLE, java.util.Map.of("x", 42));
        String str = result.toString();
        assertTrue(str.contains("SATISFIABLE"));
        assertTrue(str.contains("x"));
    }
}
