package com.mumu.woodlin.common.mp.support;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mumu.woodlin.common.response.PageResult;

/**
 * MyBatis Plus 分页结果适配器。
 */
public final class MyBatisPlusPageResults {

    private MyBatisPlusPageResults() {
    }

    /**
     * 将 MP 分页对象转换为框架无关的分页结果。
     *
     * @param page MP 分页对象
     * @param <T>  记录类型
     * @return 通用分页结果
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return PageResult.success(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
