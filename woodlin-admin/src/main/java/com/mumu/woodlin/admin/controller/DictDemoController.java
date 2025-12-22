package com.mumu.woodlin.admin.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.mumu.woodlin.common.response.Result;

/**
 * 字典演示控制器
 * 
 * @author mumu
 * @description 演示字典枚举的label-value功能
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/dict")
@Tag(name = "字典管理", description = "字典数据管理接口")
public class DictDemoController {
    
    /**
     * 获取用户状态字典
     * 
     * @return 用户状态字典列表
     */
    @GetMapping("/user-status")
    @Operation(summary = "获取用户状态字典", description = "返回所有用户状态的label-value结构")
    public Result<List<Object>> getUserStatusDict() {
        log.info("获取用户状态字典");
        
        List<Object> dictList = Arrays.stream(UserStatus.values())
                .map(UserStatus::toDictItem)
                .collect(Collectors.toList());
        
        return Result.success("获取用户状态字典成功", dictList);
    }
    
    /**
     * 获取性别字典
     * 
     * @return 性别字典列表
     */
    @GetMapping("/gender")
    @Operation(summary = "获取性别字典", description = "返回所有性别选项的label-value结构")
    public Result<List<Object>> getGenderDict() {
        log.info("获取性别字典");
        
        List<Object> dictList = Arrays.stream(Gender.values())
                .map(Gender::toDictItem)
                .collect(Collectors.toList());
        
        return Result.success("获取性别字典成功", dictList);
    }
    
    /**
     * 演示对象中的字典枚举序列化
     * 
     * @return 包含字典枚举的用户对象
     */
    @GetMapping("/demo-user")
    @Operation(summary = "演示用户对象", description = "返回包含字典枚举的用户对象，展示序列化效果")
    public Result<DemoUser> getDemoUser() {
        log.info("获取演示用户对象");
        
        DemoUser user = new DemoUser();
        user.setId(1L);
        user.setName("张三");
        user.setGender(Gender.MALE);
        user.setStatus(UserStatus.ENABLE);
        
        return Result.success("获取演示用户成功", user);
    }
    
    /**
     * 演示用户对象
     */
    public static class DemoUser {
        private Long id;
        private String name;
        private Gender gender;
        private UserStatus status;
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Gender getGender() {
            return gender;
        }
        
        public void setGender(Gender gender) {
            this.gender = gender;
        }
        
        public UserStatus getStatus() {
            return status;
        }
        
        public void setStatus(UserStatus status) {
            this.status = status;
        }
    }
}