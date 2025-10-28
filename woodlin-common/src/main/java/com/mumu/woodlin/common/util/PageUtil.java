package com.mumu.woodlin.common.util;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.mumu.woodlin.common.constant.PageConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;

/**
 * 分页工具类
 * 
 * @author mumu
 * @description 分页工具类，提供分页参数校验和Page对象创建
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
        // 参数校验
        validatePageParams(pageNum, pageSize);
        
        // 设置默认值
        if (ObjectUtil.isNull(pageNum) || pageNum <= 0) {
            pageNum = PageConstant.DEFAULT_PAGE_NUM;
        }
        if (ObjectUtil.isNull(pageSize) || pageSize <= 0) {
            pageSize = PageConstant.DEFAULT_PAGE_SIZE;
        }
        
        return new Page<>(pageNum, pageSize);
    }
    
    /**
     * 校验分页参数
     * 
     * @param pageNum 页码
     * @param pageSize 页面大小
     */
    public static void validatePageParams(Integer pageNum, Integer pageSize) {
        // 页码校验
        if (ObjectUtil.isNotNull(pageNum)) {
            if (pageNum <= 0) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "页码必须大于0");
            }
            if (pageNum > PageConstant.MAX_PAGE_NUM) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, 
                    String.format("页码不能超过%d", PageConstant.MAX_PAGE_NUM));
            }
        }
        
        // 页面大小校验
        if (ObjectUtil.isNotNull(pageSize)) {
            if (pageSize < PageConstant.MIN_PAGE_SIZE) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, 
                    String.format("每页大小不能小于%d", PageConstant.MIN_PAGE_SIZE));
            }
            if (pageSize > PageConstant.MAX_PAGE_SIZE) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, 
                    String.format("每页大小不能超过%d", PageConstant.MAX_PAGE_SIZE));
            }
        }
    }
    
    /**
     * 检查分页结果是否为空
     * 
     * @param page 分页对象
     * @return 是否为空
     */
    public static boolean isEmpty(IPage<?> page) {
        return ObjectUtil.isNull(page) || 
               ObjectUtil.isNull(page.getRecords()) || 
               page.getRecords().isEmpty();
    }
    
    /**
     * 计算总页数
     * 
     * @param total 总记录数
     * @param pageSize 页面大小
     * @return 总页数
     */
    public static long calculatePages(long total, long pageSize) {
        if (total <= 0 || pageSize <= 0) {
            return 0L;
        }
        return (total + pageSize - 1) / pageSize;
    }
    
    /**
     * 获取安全的页码
     * 
     * @param pageNum 页码
     * @return 安全的页码
     */
    public static int getSafePageNum(Integer pageNum) {
        if (ObjectUtil.isNull(pageNum) || pageNum <= 0) {
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
        if (ObjectUtil.isNull(pageSize) || pageSize <= 0) {
            return PageConstant.DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(pageSize, PageConstant.MIN_PAGE_SIZE), PageConstant.MAX_PAGE_SIZE);
    }
}