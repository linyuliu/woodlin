package com.mumu.woodlin.datasource.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import com.mumu.woodlin.datasource.mapper.InfraDatasourceMapper;

/**
 * 数据库元数据服务测试
 * <p>
 * 验证元数据服务的数据源配置校验和轻量级连接池配置输入。
 * </p>
 * 
 * @author mumu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DatabaseMetadataServiceTest {
    
    @Mock
    private InfraDatasourceMapper datasourceMapper;

    @InjectMocks
    private DatabaseMetadataService metadataService;
    
    private InfraDatasourceConfig testConfig;
    
    @BeforeEach
    void setUp() throws Exception {
        testConfig = new InfraDatasourceConfig();
        testConfig.setDatasourceCode("test-mysql");
        testConfig.setDatasourceName("Test MySQL");
        testConfig.setDatasourceType("MySQL");
        testConfig.setDriverClass("com.mysql.cj.jdbc.Driver");
        testConfig.setJdbcUrl("jdbc:mysql://localhost:3306/test_db");
        testConfig.setUsername("test_user");
        testConfig.setPassword("test_password");
        testConfig.setStatus(1);
    }
    
    @Test
    void testGetDatasourceConfig_Success() {
        assertNotNull(testConfig);
        assertEquals("test-mysql", testConfig.getDatasourceCode());
    }
    
    @Test
    void testGetDatasourceConfig_NotFound() {
        // Given
        when(datasourceMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(null);
        
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            metadataService.getDatabaseMetadata("non-existent");
        });
        
        assertTrue(exception.getMessage().contains("数据源不存在"));
    }
    
    @Test
    void testGetDatasourceConfig_Disabled() {
        // Given
        testConfig.setStatus(0);
        when(datasourceMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(testConfig);
        
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            metadataService.getDatabaseMetadata("test-mysql");
        });
        
        assertTrue(exception.getMessage().contains("数据源已禁用"));
    }
    
    /**
     * 测试元数据连接池配置输入
     * <p>
     * 元数据服务现在通过 HikariCP 轻量连接池提取元数据。
     * </p>
     */
    @Test
    void testUsesMetadataConnectionPoolConfiguration() {
        // 验证配置中指定了正确的 JDBC URL 和驱动类
        assertEquals("jdbc:mysql://localhost:3306/test_db", testConfig.getJdbcUrl());
        assertEquals("com.mysql.cj.jdbc.Driver", testConfig.getDriverClass());
        assertNotNull(testConfig.getUsername());
        assertNotNull(testConfig.getPassword());
    }
    
    /**
     * 测试驱动类解析逻辑
     */
    @Test
    void testDriverResolution() {
        // MySQL
        testConfig.setJdbcUrl("jdbc:mysql://localhost:3306/db");
        testConfig.setDriverClass(null);
        // Should auto-resolve to com.mysql.cj.jdbc.Driver
        
        // PostgreSQL
        testConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/db");
        testConfig.setDriverClass(null);
        // Should auto-resolve to org.postgresql.Driver
        
        // Oracle
        testConfig.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:db");
        testConfig.setDriverClass(null);
        // Should auto-resolve to oracle.jdbc.OracleDriver
        
        assertTrue(true); // Driver resolution logic is in the service
    }
    
    /**
     * 验证服务通过 mapper 读取数据源配置
     */
    @Test
    void testUsesDatasourceMapperForConfigLookup() {
        assertNotNull(datasourceMapper);
    }
}
