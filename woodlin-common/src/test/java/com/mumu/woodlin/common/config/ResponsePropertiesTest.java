package com.mumu.woodlin.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.common.response.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 响应配置属性测试类
 * 
 * @author mumu
 * @description 测试响应字段过滤配置功能
 * @since 2025-01-10
 */
@SpringBootTest(classes = {ResponseProperties.class})
@TestPropertySource(properties = {
    "woodlin.response.filter-mode=CUSTOM",
    "woodlin.response.include-timestamp=false",
    "woodlin.response.include-request-id=false",
    "woodlin.response.include-message=true",
    "woodlin.response.include-code=true"
})
class ResponsePropertiesTest {
    
    @Autowired(required = false)
    private ResponseProperties responseProperties;
    
    @Test
    void testResponsePropertiesLoaded() {
        assertNotNull(responseProperties, "ResponseProperties should be loaded");
    }
    
    @Test
    void testFilterMode() {
        if (responseProperties != null) {
            assertEquals(ResponseProperties.FilterMode.CUSTOM, responseProperties.getFilterMode());
        }
    }
    
    @Test
    void testCustomModeFieldInclusion() {
        if (responseProperties != null) {
            responseProperties.setFilterMode(ResponseProperties.FilterMode.CUSTOM);
            responseProperties.setIncludeTimestamp(false);
            responseProperties.setIncludeRequestId(false);
            responseProperties.setIncludeMessage(true);
            responseProperties.setIncludeCode(true);
            
            assertFalse(responseProperties.shouldIncludeTimestamp(), 
                "Timestamp should not be included in CUSTOM mode");
            assertFalse(responseProperties.shouldIncludeRequestId(), 
                "RequestId should not be included in CUSTOM mode");
            assertTrue(responseProperties.shouldIncludeMessage(), 
                "Message should be included in CUSTOM mode");
            assertTrue(responseProperties.shouldIncludeCode(), 
                "Code should be included in CUSTOM mode");
        }
    }
    
    @Test
    void testMinimalMode() {
        ResponseProperties props = new ResponseProperties();
        props.setFilterMode(ResponseProperties.FilterMode.MINIMAL);
        
        assertFalse(props.shouldIncludeTimestamp(), 
            "Timestamp should not be included in MINIMAL mode");
        assertFalse(props.shouldIncludeRequestId(), 
            "RequestId should not be included in MINIMAL mode");
        assertFalse(props.shouldIncludeMessage(), 
            "Message should not be included in MINIMAL mode");
        assertTrue(props.shouldIncludeCode(), 
            "Code should be included in MINIMAL mode");
    }
    
    @Test
    void testNoneMode() {
        ResponseProperties props = new ResponseProperties();
        props.setFilterMode(ResponseProperties.FilterMode.NONE);
        
        assertTrue(props.shouldIncludeTimestamp(), 
            "Timestamp should be included in NONE mode");
        assertTrue(props.shouldIncludeRequestId(), 
            "RequestId should be included in NONE mode");
        assertTrue(props.shouldIncludeMessage(), 
            "Message should be included in NONE mode");
        assertTrue(props.shouldIncludeCode(), 
            "Code should be included in NONE mode");
    }
    
    @Test
    void testResultClassBasicStructure() {
        Result<String> result = Result.success("操作成功", "test data");
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("test data", result.getData());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    void testRClassBasicStructure() {
        R<String> r = R.ok("操作成功", "test data");
        
        assertNotNull(r);
        assertEquals(200, r.getCode());
        assertEquals("操作成功", r.getMessage());
        assertEquals("test data", r.getData());
        assertNotNull(r.getTimestamp());
    }
}
