package com.mumu.woodlin.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Preconditions;

import com.mumu.woodlin.common.constant.PageConstant;

/**
 * 分页工具类
 * 
 * @author mumu
 * @description 分页工具类，提供分页参数校验和Page对象创建，使用Guava优化参数验证
 * @since 2025-01-01
 */
public class PageUtil {
    
    /**
     * 创建分页对象，带参数校验
     * 
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @param <T> 数据类型
     * @return 分页对象
     */
    public static <T> Page<T> createPage(Integer pageNum, Integer pageSize) {
        // 使用默认值
        int safePageNum = getSafePageNum(pageNum);
        int safePageSize = getSafePageSize(pageSize);
        
        return new Page<>(safePageNum, safePageSize);
    }
    
    /**
     * 校验分页参数（严格模式，使用Guava Preconditions）
     * 
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @throws IllegalArgumentException 如果参数不合法
     */
    public static void validatePageParams(Integer pageNum, Integer pageSize) {
        if (pageNum != null) {
            Preconditions.checkArgument(pageNum > 0, "页码必须大于0");
            Preconditions.checkArgument(pageNum <= PageConstant.MAX_PAGE_NUM, 
                "页码不能超过%s", PageConstant.MAX_PAGE_NUM);
        }
        
        if (pageSize != null) {
            Preconditions.checkArgument(pageSize >= PageConstant.MIN_PAGE_SIZE, 
                "每页大小不能小于%s", PageConstant.MIN_PAGE_SIZE);
            Preconditions.checkArgument(pageSize <= PageConstant.MAX_PAGE_SIZE, 
                "每页大小不能超过%s", PageConstant.MAX_PAGE_SIZE);
        }
    }
    
    /**
     * 检查分页结果是否为空
     * 
     * @param page 分页对象
     * @return 是否为空
     */
    public static boolean isEmpty(IPage<?> page) {
        return page == null || 
               page.getRecords() == null || 
               page.getRecords().isEmpty();
    }
    
    /**
     * 计算总页数（优化：避免Guava强制转换，直接使用long数学运算）
     * 
     * @param total 总记录数
     * @param pageSize 页面大小
     * @return 总页数
     */
    public static long calculatePages(long total, long pageSize) {
        if (total <= 0 || pageSize <= 0) {
            return 0L;
        }
        // 优化：使用长整型数学运算代替Guava的IntMath，避免类型转换和潜在溢出
        // 向上取整公式: (total + pageSize - 1) / pageSize
        return (total + pageSize - 1) / pageSize;
    }
    
    /**
     * 获取安全的页码
     * 
     * @param pageNum 页码
     * @return 安全的页码
     */
    public static int getSafePageNum(Integer pageNum) {
        if (pageNum == null || pageNum <= 0) {
            return PageConstant.DEFAULT_PAGE_NUM;
        }
        return Math.min(pageNum, PageConstant.MAX_PAGE_NUM);
    }
    
    /**
     * 获取安全的页面大小
     * 
     * @param pageSize 页面大小
     * @return 安全的页面大小
     */
    public static int getSafePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return PageConstant.DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(pageSize, PageConstant.MIN_PAGE_SIZE), PageConstant.MAX_PAGE_SIZE);
    }
}