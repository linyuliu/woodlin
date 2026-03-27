package com.mumu.woodlin.test;

import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test deserialization behavior
 */
public class DeserializationTest {
    
    @Test
    public void testDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Test deserializing from label-value JSON
        String userStatusJson = "{\"value\":\"1\",\"label\":\"启用\"}";
        String genderJson = "{\"value\":1,\"label\":\"男\"}";
        
        try {
            UserStatus status = mapper.readValue(userStatusJson, UserStatus.class);
            System.out.println("Deserialized UserStatus: " + status);
            assertEquals(UserStatus.ENABLE, status);
        } catch (Exception e) {
            System.out.println("Failed to deserialize UserStatus: " + e.getMessage());
        }
        
        try {
            Gender gender = mapper.readValue(genderJson, Gender.class);
            System.out.println("Deserialized Gender: " + gender);
            assertEquals(Gender.MALE, gender);
        } catch (Exception e) {
            System.out.println("Failed to deserialize Gender: " + e.getMessage());
        }
        
        // Test deserializing a full object
        String userJson = "{\"id\":1,\"name\":\"测试\",\"status\":{\"value\":\"1\",\"label\":\"启用\"},\"gender\":{\"value\":1,\"label\":\"男\"}}";
        
        try {
            SimpleTest.TestUser user = mapper.readValue(userJson, SimpleTest.TestUser.class);
            System.out.println("Deserialized User: " + mapper.writeValueAsString(user));
        } catch (Exception e) {
            System.out.println("Failed to deserialize User: " + e.getMessage());
        }
    }
}