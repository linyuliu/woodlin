package com.mumu.woodlin.common.example;

import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.mumu.woodlin.common.util.DictUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

/**
 * 字典功能演示示例
 * 
 * @author mumu
 * @description 演示字典枚举的各种用法和JSON序列化效果
 * @since 2025-01-01
 */
public class DictEnumExample {
    
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        System.out.println("=== Woodlin Dictionary Enum Demo ===\n");
        
        // 1. 单个枚举序列化
        System.out.println("1. Single Enum Serialization:");
        System.out.println("UserStatus.ENABLE:");
        System.out.println(mapper.writeValueAsString(UserStatus.ENABLE));
        System.out.println("\nGender.MALE:");
        System.out.println(mapper.writeValueAsString(Gender.MALE));
        
        // 2. 字典列表
        System.out.println("\n2. Dictionary Lists:");
        System.out.println("All User Status Options:");
        System.out.println(mapper.writeValueAsString(DictUtil.toDictList(UserStatus.class)));
        System.out.println("\nAll Gender Options:");
        System.out.println(mapper.writeValueAsString(DictUtil.toDictList(Gender.class)));
        
        // 3. 在对象中使用
        System.out.println("\n3. Enum in Object:");
        DemoUser user = new DemoUser();
        user.id = 1L;
        user.name = "张三";
        user.gender = Gender.MALE;
        user.status = UserStatus.ENABLE;
        
        System.out.println("User Object with Enums:");
        System.out.println(mapper.writeValueAsString(user));
        
        // 4. 工具方法演示
        System.out.println("\n4. Utility Methods:");
        System.out.println("DictUtil.getLabelByValue(UserStatus.class, \"1\"): " + 
            DictUtil.getLabelByValue(UserStatus.class, "1"));
        System.out.println("DictUtil.getValueByLabel(Gender.class, \"男\"): " + 
            DictUtil.getValueByLabel(Gender.class, "男"));
        System.out.println("DictUtil.containsValue(UserStatus.class, \"1\"): " + 
            DictUtil.containsValue(UserStatus.class, "1"));
        
        // 5. 字典映射
        System.out.println("\n5. Dictionary Mapping:");
        Map<Object, String> userStatusMap = DictUtil.toDictMap(UserStatus.class);
        System.out.println("UserStatus Map: " + userStatusMap);
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    static class DemoUser {
        public Long id;
        public String name;
        public Gender gender;
        public UserStatus status;
    }
}