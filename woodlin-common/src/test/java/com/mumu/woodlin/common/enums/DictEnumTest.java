package com.mumu.woodlin.common.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字典枚举测试类
 * 
 * @author mumu
 * @description 测试字典枚举的序列化和功能
 * @since 2025-01-01
 */
class DictEnumTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testUserStatusDictEnum() {
        // 测试UserStatus枚举实现DictEnum接口
        UserStatus enable = UserStatus.ENABLE;
        assertEquals("1", enable.getValue());
        assertEquals("启用", enable.getLabel());
        
        UserStatus disable = UserStatus.DISABLE;
        assertEquals("0", disable.getValue());
        assertEquals("禁用", disable.getLabel());
    }
    
    @Test
    void testGenderDictEnum() {
        // 测试Gender枚举实现DictEnum接口
        Gender male = Gender.MALE;
        assertEquals(1, male.getValue());
        assertEquals("男", male.getLabel());
        
        Gender female = Gender.FEMALE;
        assertEquals(2, female.getValue());
        assertEquals("女", female.getLabel());
        
        Gender unknown = Gender.UNKNOWN;
        assertEquals(0, unknown.getValue());
        assertEquals("未知", unknown.getLabel());
    }
    
    @Test
    void testDictItemSerialization() throws Exception {
        // 测试字典项序列化
        DictEnum.DictItem dictItem = UserStatus.ENABLE.toDictItem();
        assertNotNull(dictItem);
        assertEquals("1", dictItem.getValue());
        assertEquals("启用", dictItem.getLabel());
        
        // 测试JSON序列化
        String json = objectMapper.writeValueAsString(UserStatus.ENABLE);
        assertTrue(json.contains("value"));
        assertTrue(json.contains("label"));
        assertTrue(json.contains("1"));
        assertTrue(json.contains("启用"));
        
        // 同样测试Gender
        String genderJson = objectMapper.writeValueAsString(Gender.MALE);
        assertTrue(genderJson.contains("value"));
        assertTrue(genderJson.contains("label"));
        assertTrue(genderJson.contains("1"));
        assertTrue(genderJson.contains("男"));
    }
    
    @Test
    void testStaticFromMethods() {
        // 测试现有的静态方法仍然工作
        assertEquals(UserStatus.ENABLE, UserStatus.fromCode("1"));
        assertEquals(UserStatus.DISABLE, UserStatus.fromCode("0"));
        assertEquals(UserStatus.DISABLE, UserStatus.fromCode("invalid"));
        
        assertEquals(Gender.MALE, Gender.fromCode(1));
        assertEquals(Gender.FEMALE, Gender.fromCode(2));
        assertEquals(Gender.UNKNOWN, Gender.fromCode(null));
        assertEquals(Gender.UNKNOWN, Gender.fromCode(999));
    }
}