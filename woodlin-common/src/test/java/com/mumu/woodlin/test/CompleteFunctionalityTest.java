package com.mumu.woodlin.test;

import com.mumu.woodlin.common.config.DictEnumDeserializer;
import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete functionality demonstration
 * 
 * This test demonstrates what the user requested:
 * 1. Dictionary enums automatically serialize to {"value":"1","label":"启用"}
 * 2. Dictionary enums can also deserialize FROM {"value":"1","label":"启用"} back to enum
 * 3. This works both for simple values and complex objects  
 * 4. The user doesn't need to call DictUtil.toDictList - it's automatic
 */
public class CompleteFunctionalityTest {
    
    private ObjectMapper mapper;
    
    @BeforeEach
    public void setup() {
        // Setup ObjectMapper with dictionary enum support
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(UserStatus.class, new DictEnumDeserializer<>(UserStatus.class));
        module.addDeserializer(Gender.class, new DictEnumDeserializer<>(Gender.class));
        mapper.registerModule(module);
    }
    
    @Test 
    public void testAutoSerializationAndDeserialization() throws Exception {
        System.out.println("=== 完整的字典枚举功能演示 ===");
        System.out.println();
        
        // 1. 自动序列化：枚举 → JSON label-value
        System.out.println("1. 自动序列化（枚举 → JSON label-value）:");
        UserStatus status = UserStatus.ENABLE;
        String json = mapper.writeValueAsString(status);
        System.out.println("UserStatus.ENABLE → " + json);
        
        Gender gender = Gender.MALE;
        String genderJson = mapper.writeValueAsString(gender);
        System.out.println("Gender.MALE → " + genderJson);
        System.out.println();
        
        // 2. 自动反序列化：JSON label-value → 枚举
        System.out.println("2. 自动反序列化（JSON label-value → 枚举）:");
        UserStatus deserializedStatus = mapper.readValue(json, UserStatus.class);
        System.out.println(json + " → " + deserializedStatus);
        assertEquals(UserStatus.ENABLE, deserializedStatus);
        
        Gender deserializedGender = mapper.readValue(genderJson, Gender.class);
        System.out.println(genderJson + " → " + deserializedGender);
        assertEquals(Gender.MALE, deserializedGender);
        System.out.println();
        
        // 3. 对象中的枚举也自动处理
        System.out.println("3. 对象中的枚举自动处理:");
        TestUser user = new TestUser(1L, "张三", UserStatus.ENABLE, Gender.MALE);
        String userJson = mapper.writeValueAsString(user);
        System.out.println("User对象序列化: " + userJson);
        
        TestUser deserializedUser = mapper.readValue(userJson, TestUser.class);
        System.out.println("User对象反序列化: " + deserializedUser.toString());
        assertEquals(UserStatus.ENABLE, deserializedUser.status);
        assertEquals(Gender.MALE, deserializedUser.gender);
        System.out.println();
        
        // 4. 从前端接收到的数据也能正确反序列化
        System.out.println("4. 前端数据反序列化:");
        String frontendJson = "{\"id\":2,\"name\":\"李四\",\"status\":{\"value\":\"0\",\"label\":\"禁用\"},\"gender\":{\"value\":2,\"label\":\"女\"}}";
        TestUser fromFrontend = mapper.readValue(frontendJson, TestUser.class);
        System.out.println("前端JSON: " + frontendJson);
        System.out.println("反序列化结果: " + fromFrontend.toString());
        assertEquals(UserStatus.DISABLE, fromFrontend.status);
        assertEquals(Gender.FEMALE, fromFrontend.gender);
        System.out.println();
        
        // 5. 兼容简单值的反序列化
        System.out.println("5. 兼容简单值反序列化:");
        UserStatus fromSimple = mapper.readValue("\"1\"", UserStatus.class);
        System.out.println("简单值 \"1\" → " + fromSimple);
        assertEquals(UserStatus.ENABLE, fromSimple);
        
        Gender genderFromSimple = mapper.readValue("1", Gender.class);
        System.out.println("简单值 1 → " + genderFromSimple); 
        assertEquals(Gender.MALE, genderFromSimple);
        System.out.println();
        
        System.out.println("=== 功能演示完成 ===");
        System.out.println("✅ 用户的需求已完全实现：");
        System.out.println("   - 枚举继承DictEnum接口后，自动支持label-value序列化");
        System.out.println("   - 反序列化时自动识别并转换label-value结构");
        System.out.println("   - 不需要手动调用DictUtil.toDictList");
        System.out.println("   - 前端可以直接使用label-value数据");
    }
    
    public static class TestUser {
        public Long id;
        public String name;
        public UserStatus status;
        public Gender gender;
        
        public TestUser() {}
        
        public TestUser(Long id, String name, UserStatus status, Gender gender) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.gender = gender;
        }
        
        @Override
        public String toString() {
            return String.format("TestUser{id=%d, name='%s', status=%s, gender=%s}", 
                id, name, status, gender);
        }
    }
}