package com.mumu.woodlin.z3.model;

import java.util.Collections;
import java.util.Map;

/**
 * Z3 约束求解结果.
 *
 * <p>封装求解器返回的求解状态与变量赋值结果。</p>
 *
 * @author mumu
 * @since 2025-10-28
 */
public class ConstraintResult {

    /** 求解状态枚举. */
    public enum Status {
        /** 可满足 – 存在满足所有约束的赋值. */
        SATISFIABLE,
        /** 不可满足 – 约束之间存在矛盾. */
        UNSATISFIABLE,
        /** 未知 – 求解器无法确定. */
        UNKNOWN
    }

    private final Status status;
    private final Map<String, Object> model;

    /**
     * 构造约束求解结果.
     *
     * @param status 求解状态
     * @param model  变量赋值映射（仅当 SATISFIABLE 时有效）
     */
    public ConstraintResult(Status status, Map<String, Object> model) {
        this.status = status;
        this.model = model != null ? Collections.unmodifiableMap(model) : Collections.emptyMap();
    }

    /**
     * 获取求解状态.
     *
     * @return 求解状态枚举值
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 获取变量赋值模型.
     *
     * @return 不可变的变量名→值映射
     */
    public Map<String, Object> getModel() {
        return model;
    }

    /**
     * 判断约束是否可满足.
     *
     * @return 如果可满足返回 {@code true}
     */
    public boolean isSatisfiable() {
        return status == Status.SATISFIABLE;
    }

    @Override
    public String toString() {
        return "ConstraintResult{status=" + status + ", model=" + model + "}";
    }
}
