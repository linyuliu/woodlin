package com.mumu.woodlin.admin.config;

import java.time.LocalDateTime;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.mumu.woodlin.security.util.SecurityUtil;

/**
 * MyBatis Plus 自动填充处理器
 * 
 * @author mumu
 * @description 自动填充创建人、创建时间、更新人、更新时间等公共字段
 * @since 2025-01-01
 */
@Slf4j
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {
    
    /**
     * 插入时自动填充
     * 
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        // 获取当前登录用户
        String currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        
        // 自动填充创建人
        this.strictInsertFill(metaObject, "createBy", String.class, currentUser);
        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        // 自动填充更新人
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUser);
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }
    
    /**
     * 更新时自动填充
     * 
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        // 获取当前登录用户
        String currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        
        // 自动填充更新人
        this.strictUpdateFill(metaObject, "updateBy", String.class, currentUser);
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
    }
    
    /**
     * 获取当前登录用户
     * 
     * @return 当前用户名，如果未登录则返回system
     */
    private String getCurrentUser() {
        try {
            // 检查用户是否登录
            if (!SecurityUtil.isLogin()) {
                return "system";
            }
            
            String username = SecurityUtil.getUsername();
            return ObjectUtil.isNotEmpty(username) ? username : "system";
        } catch (Exception e) {
            // 在获取用户失败时（例如登录过程中），使用默认用户
            log.trace("获取当前用户失败（可能是在登录流程中），使用默认用户: system", e);
            return "system";
        }
    }
}