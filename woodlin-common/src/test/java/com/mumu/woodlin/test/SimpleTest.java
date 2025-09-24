package com.mumu.woodlin.test;

import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Simple test to understand current behavior
 */
public class SimpleTest {
    
    @Test
    public void testCurrentBehavior() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Test single enum serialization
        System.out.println("UserStatus.ENABLE: " + mapper.writeValueAsString(UserStatus.ENABLE));
        System.out.println("Gender.MALE: " + mapper.writeValueAsString(Gender.MALE));
        
        // Test object with enums
        TestUser user = new TestUser();
        user.id = 1L;
        user.name = "测试";
        user.status = UserStatus.ENABLE;
        user.gender = Gender.MALE;
        
        System.out.println("User object: " + mapper.writeValueAsString(user));
    }
    
    public static class TestUser {
        public Long id;
        public String name;
        public UserStatus status;
        public Gender gender;
    }
}