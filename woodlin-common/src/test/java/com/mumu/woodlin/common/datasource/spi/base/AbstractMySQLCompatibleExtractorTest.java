package com.mumu.woodlin.common.datasource.spi.base;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mumu.woodlin.common.datasource.spi.impl.MySQLMetadataExtractor;

/**
 * MySQL元数据提取器测试
 * <p>
 * 测试原生命令（SHOW/DESC）与 information_schema 两种模式的切换
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
class AbstractMySQLCompatibleExtractorTest {
    
    private MySQLMetadataExtractor extractor;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData metaData;
    
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet resultSet;
    
    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        extractor = new MySQLMetadataExtractor();
        
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("8.0.28");
    }
    
    @Test
    void testUseNativeCommandsDefaultValue() {
        // 默认应该使用原生命令
        assertTrue(extractor.isUseNativeCommands());
    }
    
    @Test
    void testSetUseNativeCommands() {
        extractor.setUseNativeCommands(false);
        assertFalse(extractor.isUseNativeCommands());
        
        extractor.setUseNativeCommands(true);
        assertTrue(extractor.isUseNativeCommands());
    }
    
    @Test
    void testGetDatabaseType() {
        assertEquals("MySQL", extractor.getDatabaseType());
    }
    
    @Test
    void testGetMinSupportedVersion() {
        assertEquals("5.6", extractor.getMinSupportedVersion());
    }
    
    @Test
    void testSupportsMySQL() throws SQLException {
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        assertTrue(extractor.supports(connection));
    }
    
    @Test
    void testSupportsMariaDB() throws SQLException {
        when(metaData.getDatabaseProductName()).thenReturn("MariaDB");
        assertTrue(extractor.supports(connection));
    }
    
    @Test
    void testDoesNotSupportPostgreSQL() throws SQLException {
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        assertFalse(extractor.supports(connection));
    }
    
    @Test
    void testParseDataType() {
        assertEquals("varchar", extractor.parseDataType("varchar(255)"));
        assertEquals("int", extractor.parseDataType("int(11)"));
        assertEquals("decimal", extractor.parseDataType("decimal(10,2)"));
        assertEquals("bigint", extractor.parseDataType("bigint(20) unsigned"));
        assertEquals("text", extractor.parseDataType("text"));
        assertNull(extractor.parseDataType(null));
    }
    
    @Test
    void testEscapeSqlIdentifier() {
        assertEquals("test_table", extractor.escapeSqlIdentifier("test_table"));
        assertEquals("test``table", extractor.escapeSqlIdentifier("test`table"));
        assertNull(extractor.escapeSqlIdentifier(null));
    }
    
    @Test
    void testMapToJavaType() {
        // 基础类型映射
        assertEquals("Integer", extractor.mapToJavaType("int", "8.0.28"));
        assertEquals("Long", extractor.mapToJavaType("bigint", "8.0.28"));
        assertEquals("String", extractor.mapToJavaType("varchar", "8.0.28"));
        assertEquals("LocalDateTime", extractor.mapToJavaType("datetime", "8.0.28"));
        assertEquals("BigDecimal", extractor.mapToJavaType("decimal", "8.0.28"));
        
        // JSON类型（5.7+支持）
        assertEquals("String", extractor.mapToJavaType("json", "8.0.28"));
        assertEquals("String", extractor.mapToJavaType("json", "5.7.0"));
        
        // 未知类型
        assertEquals("Object", extractor.mapToJavaType("unknown_type", "8.0.28"));
        assertEquals("Object", extractor.mapToJavaType(null, "8.0.28"));
    }
    
    @Test
    void testGetPriority() {
        assertEquals(10, extractor.getPriority());
    }
    
    @Test
    void testVersionParsing() {
        assertEquals(8, extractor.getMajorVersion("8.0.28"));
        assertEquals(0, extractor.getMinorVersion("8.0.28"));
        assertEquals(5, extractor.getMajorVersion("5.7.36"));
        assertEquals(7, extractor.getMinorVersion("5.7.36"));
        assertEquals(0, extractor.getMajorVersion(null));
        assertEquals(0, extractor.getMinorVersion(null));
    }
}
