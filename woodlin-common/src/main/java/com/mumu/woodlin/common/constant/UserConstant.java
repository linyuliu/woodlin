package com.mumu.woodlin.common.constant;

/**
 * 用户相关常量
 * 
 * @author mumu
 * @description 用户模块相关的常量定义
 * @since 2025-01-01
 */
public interface UserConstant {
    
    /**
     * 用户状态 - 启用
     */
    String STATUS_ENABLE = "1";
    
    /**
     * 用户状态 - 禁用
     */
    String STATUS_DISABLE = "0";
    
    /**
     * 性别 - 未知
     */
    Integer GENDER_UNKNOWN = 0;
    
    /**
     * 性别 - 男
     */
    Integer GENDER_MALE = 1;
    
    /**
     * 性别 - 女
     */
    Integer GENDER_FEMALE = 2;
    
    /**
     * 性别文本 - 未知
     */
    String GENDER_TEXT_UNKNOWN = "未知";
    
    /**
     * 性别文本 - 男
     */
    String GENDER_TEXT_MALE = "男";
    
    /**
     * 性别文本 - 女
     */
    String GENDER_TEXT_FEMALE = "女";
    
    /**
     * 状态文本 - 启用
     */
    String STATUS_TEXT_ENABLE = "启用";
    
    /**
     * 状态文本 - 禁用
     */
    String STATUS_TEXT_DISABLE = "禁用";
}