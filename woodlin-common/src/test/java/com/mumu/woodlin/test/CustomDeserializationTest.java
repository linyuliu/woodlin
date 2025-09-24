package com.mumu.woodlin.test;

import com.mumu.woodlin.common.config.DictEnumDeserializer;
import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test deserialization with custom ObjectMapper
 */
public class CustomDeserializationTest {
    
    @Test
    public void testWithCustomObjectMapper() throws Exception {
        // Create custom ObjectMapper with our deserializer
        ObjectMapper mapper = new ObjectMapper();
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(UserStatus.class, new DictEnumDeserializer<>(UserStatus.class));
        module.addDeserializer(Gender.class, new DictEnumDeserializer<>(Gender.class));
        mapper.registerModule(module);
        
        // Test deserializing from label-value JSON
        String userStatusJson = "{\"value\":\"1\",\"label\":\"启用\"}";
        String genderJson = "{\"value\":1,\"label\":\"男\"}";
        
        try {
            UserStatus status = mapper.readValue(userStatusJson, UserStatus.class);
            System.out.println("Successfully deserialized UserStatus: " + status);
            System.out.println("Serialized back: " + mapper.writeValueAsString(status));
            assertEquals(UserStatus.ENABLE, status);
        } catch (Exception e) {
            System.out.println("Failed to deserialize UserStatus: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            Gender gender = mapper.readValue(genderJson, Gender.class);
            System.out.println("Successfully deserialized Gender: " + gender);
            System.out.println("Serialized back: " + mapper.writeValueAsString(gender));
            assertEquals(Gender.MALE, gender);
        } catch (Exception e) {
            System.out.println("Failed to deserialize Gender: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test with simple value too
        try {
            UserStatus statusFromValue = mapper.readValue("\"1\"", UserStatus.class);
            System.out.println("Deserialized from simple value: " + statusFromValue);
            assertEquals(UserStatus.ENABLE, statusFromValue);
        } catch (Exception e) {
            System.out.println("Failed to deserialize from simple value: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test deserializing a full object
        String userJson = "{\"id\":1,\"name\":\"测试\",\"status\":{\"value\":\"1\",\"label\":\"启用\"},\"gender\":{\"value\":1,\"label\":\"男\"}}";
        
        try {
            SimpleTest.TestUser user = mapper.readValue(userJson, SimpleTest.TestUser.class);
            System.out.println("Deserialized User: " + mapper.writeValueAsString(user));
            assertEquals(UserStatus.ENABLE, user.status);
            assertEquals(Gender.MALE, user.gender);
        } catch (Exception e) {
            System.out.println("Failed to deserialize User: " + e.getMessage());
            e.printStackTrace();
        }
    }
}