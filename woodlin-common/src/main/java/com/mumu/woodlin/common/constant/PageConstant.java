package com.mumu.woodlin.common.constant;

/**
 * 分页常量
 * 
 * @author mumu
 * @description 分页相关的常量定义
 * @since 2025-01-01
 */
public interface PageConstant {
    
    /**
     * 默认页码
     */
    int DEFAULT_PAGE_NUM = 1;
    
    /**
     * 默认页面大小
     */
    int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * 最小页面大小
     */
    int MIN_PAGE_SIZE = 1;
    
    /**
     * 最大页面大小
     */
    int MAX_PAGE_SIZE = 500;
    
    /**
     * 最大页码
     */
    int MAX_PAGE_NUM = 1000000;
}