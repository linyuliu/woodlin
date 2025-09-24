package com.mumu.woodlin.common.constant;

/**
 * 系统常量
 * 
 * @author mumu
 * @description 系统级别的常量定义
 * @since 2025-01-01
 */
public interface SystemConstant {
    
    /**
     * 系统名称
     */
    String SYSTEM_NAME = "Woodlin";
    
    /**
     * 系统版本
     */
    String SYSTEM_VERSION = "1.0.0";
    
    /**
     * 构建时间（在打包时会被替换）
     */
    String BUILD_TIME = "@maven.build.timestamp@";
    
    /**
     * Git提交ID（在打包时会被替换）
     */
    String GIT_COMMIT_ID = "@git.commit.id.abbrev@";
    
    /**
     * 构建环境
     */
    String BUILD_PROFILE = "@build.profile.name@";
    
    /**
     * 默认租户ID
     */
    String DEFAULT_TENANT_ID = "default";
    
    /**
     * 系统用户
     */
    String SYSTEM_USER = "system";
    
    /**
     * 超级管理员角色编码
     */
    String SUPER_ADMIN_ROLE_CODE = "admin";
    
    /**
     * 普通用户角色编码
     */
    String NORMAL_USER_ROLE_CODE = "user";
    
    /**
     * 默认密码
     */
    String DEFAULT_PASSWORD = "123456";
}