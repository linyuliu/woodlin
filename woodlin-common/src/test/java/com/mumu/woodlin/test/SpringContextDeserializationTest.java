package com.mumu.woodlin.test;

import com.mumu.woodlin.common.config.JacksonConfig;
import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test with Spring Boot context and JacksonConfig
 */
@SpringJUnitConfig(JacksonConfig.class)
public class SpringContextDeserializationTest {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testWithSpringConfiguration() throws Exception {
        System.out.println("Testing with Spring configured ObjectMapper");
        
        // Test deserializing from label-value JSON
        String userStatusJson = "{\"value\":\"1\",\"label\":\"启用\"}";
        String genderJson = "{\"value\":1,\"label\":\"男\"}";
        
        try {
            UserStatus status = objectMapper.readValue(userStatusJson, UserStatus.class);
            System.out.println("Successfully deserialized UserStatus: " + status);
            System.out.println("Serialized back: " + objectMapper.writeValueAsString(status));
            assertEquals(UserStatus.ENABLE, status);
        } catch (Exception e) {
            System.out.println("Failed to deserialize UserStatus: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            Gender gender = objectMapper.readValue(genderJson, Gender.class);
            System.out.println("Successfully deserialized Gender: " + gender);
            System.out.println("Serialized back: " + objectMapper.writeValueAsString(gender));
            assertEquals(Gender.MALE, gender);
        } catch (Exception e) {
            System.out.println("Failed to deserialize Gender: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test with simple value
        try {
            UserStatus statusFromValue = objectMapper.readValue("\"1\"", UserStatus.class);
            System.out.println("Deserialized from simple value: " + statusFromValue);
            assertEquals(UserStatus.ENABLE, statusFromValue);
        } catch (Exception e) {
            System.out.println("Failed to deserialize from simple value: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test full object
        String userJson = "{\"id\":1,\"name\":\"测试\",\"status\":{\"value\":\"1\",\"label\":\"启用\"},\"gender\":{\"value\":1,\"label\":\"男\"}}";
        
        try {
            SimpleTest.TestUser user = objectMapper.readValue(userJson, SimpleTest.TestUser.class);
            System.out.println("Deserialized User: " + objectMapper.writeValueAsString(user));
            assertEquals(UserStatus.ENABLE, user.status);
            assertEquals(Gender.MALE, user.gender);
        } catch (Exception e) {
            System.out.println("Failed to deserialize User: " + e.getMessage());
            e.printStackTrace();
        }
    }
}