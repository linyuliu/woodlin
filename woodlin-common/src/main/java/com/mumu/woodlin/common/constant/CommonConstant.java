package com.mumu.woodlin.common.constant;

/**
 * 通用常量定义
 * 
 * @author mumu
 * @description 定义系统中使用的通用常量，包括状态码、默认值等
 * @since 2025-01-01
 */
public interface CommonConstant {
    
    /**
     * 成功标识
     */
    String SUCCESS = "SUCCESS";
    
    /**
     * 失败标识
     */
    String FAIL = "FAIL";
    
    /**
     * 成功状态码
     */
    int SUCCESS_CODE = 200;
    
    /**
     * 失败状态码
     */
    int FAIL_CODE = 500;
    
    /**
     * 参数错误状态码
     */
    int BAD_REQUEST_CODE = 400;
    
    /**
     * 未授权状态码
     */
    int UNAUTHORIZED_CODE = 401;
    
    /**
     * 禁止访问状态码
     */
    int FORBIDDEN_CODE = 403;
    
    /**
     * 资源未找到状态码
     */
    int NOT_FOUND_CODE = 404;
    
    /**
     * 默认页码
     */
    int DEFAULT_PAGE_NUM = 1;
    
    /**
     * 默认页面大小
     */
    int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * 最大页面大小
     */
    int MAX_PAGE_SIZE = 1000;
    
    /**
     * 默认租户ID（系统租户）
     */
    String DEFAULT_TENANT_ID = "system";
    
    /**
     * 超级管理员角色编码
     */
    String SUPER_ADMIN_ROLE_CODE = "super_admin";
    
    /**
     * 启用状态
     */
    String STATUS_ENABLE = "1";
    
    /**
     * 禁用状态
     */
    String STATUS_DISABLE = "0";
    
    /**
     * 删除标识 - 已删除
     */
    String DELETED_YES = "1";
    
    /**
     * 删除标识 - 未删除
     */
    String DELETED_NO = "0";
    
    /**
     * 默认密码
     */
    String DEFAULT_PASSWORD = "123456";
    
    /**
     * UTF-8 编码
     */
    String UTF8 = "UTF-8";
    
    /**
     * 日期时间格式
     */
    String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期格式
     */
    String DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * 时间格式
     */
    String TIME_PATTERN = "HH:mm:ss";
    
}