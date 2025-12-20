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

import com.mumu.woodlin.common.datasource.model.DatabaseType;
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
        assertEquals(DatabaseType.MYSQL, extractor.getDatabaseType());
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
    void testParseColumnSize() {
        // String types
        assertEquals(255, extractor.parseColumnSize("varchar(255)", "varchar"));
        assertEquals(100, extractor.parseColumnSize("char(100)", "char"));
        
        // Numeric types with length
        assertEquals(11, extractor.parseColumnSize("int(11)", "int"));
        assertEquals(20, extractor.parseColumnSize("bigint(20)", "bigint"));
        
        // Decimal types - should return precision
        assertEquals(10, extractor.parseColumnSize("decimal(10,2)", "decimal"));
        assertEquals(15, extractor.parseColumnSize("decimal(15,4)", "decimal"));
        
        // Types without size
        assertNull(extractor.parseColumnSize("text", "text"));
        assertNull(extractor.parseColumnSize("datetime", "datetime"));
        assertNull(extractor.parseColumnSize(null, null));
    }
    
    @Test
    void testParseDecimalDigits() {
        // Decimal types with scale
        assertEquals(2, extractor.parseDecimalDigits("decimal(10,2)"));
        assertEquals(4, extractor.parseDecimalDigits("decimal(15,4)"));
        assertEquals(0, extractor.parseDecimalDigits("decimal(10,0)"));
        
        // Decimal types without scale
        assertNull(extractor.parseDecimalDigits("decimal(10)"));
        
        // Non-decimal types
        assertNull(extractor.parseDecimalDigits("varchar(255)"));
        assertNull(extractor.parseDecimalDigits("int(11)"));
        assertNull(extractor.parseDecimalDigits("text"));
        assertNull(extractor.parseDecimalDigits(null));
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
