package com.mumu.woodlin.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可搜索加密字段注解
 * 
 * @author mumu
 * @description 标记需要进行可搜索加密的实体字段
 *              被标记的字段将自动进行加密存储，并生成搜索索引支持模糊查询
 * @since 2025-01-01
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SearchableField {
    
    /**
     * 是否启用模糊搜索
     * 如果设置为true，将生成N-gram索引支持模糊查询
     * 如果设置为false，仅支持精确匹配
     */
    boolean fuzzySearch() default true;
    
    /**
     * N-gram的大小
     * 用于模糊搜索时的分词粒度
     * 默认使用全局配置的值
     */
    int ngramSize() default -1;
    
    /**
     * 搜索索引字段名
     * 用于存储加密的N-gram索引
     * 如果不指定，将使用字段名 + "_search_index"
     */
    String indexField() default "";
}
