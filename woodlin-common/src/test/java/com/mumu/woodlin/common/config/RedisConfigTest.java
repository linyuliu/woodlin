package com.mumu.woodlin.common.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis配置测试类
 * 
 * @author mumu
 * @description 测试Redis FastJSON2序列化功能
 * @since 2025-01-01
 */
class RedisConfigTest {
    
    @Test
    void testFastJson2RedisSerializerString() {
        // 创建序列化器实例
        RedisConfig.FastJson2RedisSerializer<String> serializer = 
            new RedisConfig.FastJson2RedisSerializer<>(String.class);
        
        // 测试字符串序列化
        String original = "Hello World";
        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
        
        // 测试反序列化
        String deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertEquals(original, deserialized);
    }
    
    @Test
    void testNullSerialization() {
        RedisConfig.FastJson2RedisSerializer<Object> serializer = 
            new RedisConfig.FastJson2RedisSerializer<>(Object.class);
        
        // 测试null值序列化
        byte[] serialized = serializer.serialize(null);
        assertNotNull(serialized);
        assertEquals(0, serialized.length);
        
        // 测试null值反序列化
        Object deserialized = serializer.deserialize(null);
        assertNull(deserialized);
        
        // 测试空字节数组反序列化
        Object deserializedEmpty = serializer.deserialize(new byte[0]);
        assertNull(deserializedEmpty);
    }
    
    @Test
    void testSimpleObjectSerialization() {
        RedisConfig.FastJson2RedisSerializer<TestObject> serializer = 
            new RedisConfig.FastJson2RedisSerializer<>(TestObject.class);
        
        TestObject original = new TestObject("test", 123);
        
        // 序列化
        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);
        
        // 反序列化
        TestObject deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertEquals(original.name, deserialized.name);
        assertEquals(original.value, deserialized.value);
    }
    
    /**
     * 简单测试对象
     */
    static class TestObject {
        public String name;
        public Integer value;
        
        public TestObject() {}
        
        public TestObject(String name, Integer value) {
            this.name = name;
            this.value = value;
        }
    }
}