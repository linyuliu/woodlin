package com.mumu.woodlin.test;

import com.mumu.woodlin.common.config.DictEnumDeserializer;
import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test with manually configured ObjectMapper (simulating Spring configuration)
 * 
 * @author mumu
 * @description 测试Spring环境下的ObjectMapper配置，验证字典枚举的序列化和反序列化功能
 * @since 2025-01-01
 */
public class SpringContextDeserializationTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    public void setUp() {
        // 手动配置ObjectMapper，模拟Spring环境
        objectMapper = new ObjectMapper();
        
        // 注册自定义字典枚举反序列化器
        SimpleModule module = new SimpleModule("DictEnumModule");
        module.addDeserializer(UserStatus.class, new DictEnumDeserializer(UserStatus.class));
        module.addDeserializer(Gender.class, new DictEnumDeserializer(Gender.class));
        
        objectMapper.registerModule(module);
    }
    
    /**
     * 测试模拟Spring环境下的ObjectMapper配置
     * 验证字典枚举的序列化和反序列化功能是否正常工作
     */
    @Test
    public void testWithSpringConfiguration() throws Exception {
        System.out.println("Testing with Spring configured ObjectMapper");
        
        // 测试从label-value JSON结构反序列化
        String userStatusJson = "{\"value\":\"1\",\"label\":\"启用\"}";
        String genderJson = "{\"value\":1,\"label\":\"男\"}";
        
        // 测试UserStatus反序列化
        UserStatus status = objectMapper.readValue(userStatusJson, UserStatus.class);
        System.out.println("Successfully deserialized UserStatus: " + status);
        System.out.println("Serialized back: " + objectMapper.writeValueAsString(status));
        assertEquals(UserStatus.ENABLE, status);
        
        // 测试Gender反序列化
        Gender gender = objectMapper.readValue(genderJson, Gender.class);
        System.out.println("Successfully deserialized Gender: " + gender);
        System.out.println("Serialized back: " + objectMapper.writeValueAsString(gender));
        assertEquals(Gender.MALE, gender);
        
        // 测试简单值反序列化兼容性
        UserStatus statusFromValue = objectMapper.readValue("\"1\"", UserStatus.class);
        System.out.println("Deserialized from simple value: " + statusFromValue);
        assertEquals(UserStatus.ENABLE, statusFromValue);
        
        // 测试完整对象的序列化和反序列化
        String userJson = "{\"id\":1,\"name\":\"测试\",\"status\":{\"value\":\"1\",\"label\":\"启用\"},\"gender\":{\"value\":1,\"label\":\"男\"}}";
        
        SimpleTest.TestUser user = objectMapper.readValue(userJson, SimpleTest.TestUser.class);
        System.out.println("Deserialized User: " + objectMapper.writeValueAsString(user));
        assertEquals(UserStatus.ENABLE, user.status);
        assertEquals(Gender.MALE, user.gender);
    }
    
    /**
     * 测试边界情况和异常处理
     * 验证各种边界条件下的反序列化行为
     */
    @Test
    public void testEdgeCases() throws Exception {
        System.out.println("Testing edge cases...");
        
        // 测试null值处理
        assertNull(objectMapper.readValue("null", UserStatus.class));
        
        // 测试空对象
        assertThrows(Exception.class, () -> 
            objectMapper.readValue("{}", UserStatus.class));
        
        // 测试错误的value值
        assertThrows(Exception.class, () -> 
            objectMapper.readValue("{\"value\":\"invalid\",\"label\":\"测试\"}", UserStatus.class));
        
        System.out.println("Edge cases tested successfully");
    }
    
    /**
     * 测试性能表现
     * 验证大量序列化和反序列化操作的性能
     */
    @Test
    public void testPerformance() throws Exception {
        System.out.println("Testing performance...");
        
        int iterations = 1000;
        String userStatusJson = "{\"value\":\"1\",\"label\":\"启用\"}";
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            UserStatus status = objectMapper.readValue(userStatusJson, UserStatus.class);
            String serialized = objectMapper.writeValueAsString(status);
            assertNotNull(status);
            assertNotNull(serialized);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.printf("Completed %d iterations in %d ms (avg: %.2f ms per operation)%n", 
                         iterations, duration, (double) duration / iterations);
        
        // 性能应该在合理范围内
        assertTrue(duration < 5000, "Performance should be reasonable (less than 5 seconds for 1000 operations)");
    }
}