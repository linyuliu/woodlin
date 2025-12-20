package com.mumu.woodlin.sql2api.spi.impl;

import com.mumu.woodlin.common.datasource.spi.impl.PostgreSQLMetadataExtractor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PostgreSQL元数据提取器测试
 *
 * @author mumu
 * @since 2025-01-04
 */
class PostgreSQLMetadataExtractorTest {

    @Test
    void testGetDatabaseType() {
        PostgreSQLMetadataExtractor extractor = new PostgreSQLMetadataExtractor();
        assertEquals("PostgreSQL", extractor.getDatabaseType());
    }

    @Test
    void testSupportsPostgreSQL() throws SQLException {
        PostgreSQLMetadataExtractor extractor = new PostgreSQLMetadataExtractor();

        Connection connection = mock(Connection.class);
        java.sql.DatabaseMetaData metaData = mock(java.sql.DatabaseMetaData.class);

        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");

        assertTrue(extractor.supports(connection));
    }

    @Test
    void testDoesNotSupportMySQL() throws SQLException {
        PostgreSQLMetadataExtractor extractor = new PostgreSQLMetadataExtractor();

        Connection connection = mock(Connection.class);
        java.sql.DatabaseMetaData metaData = mock(java.sql.DatabaseMetaData.class);

        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");

        assertFalse(extractor.supports(connection));
    }

    @Test
    void testGetPriority() {
        PostgreSQLMetadataExtractor extractor = new PostgreSQLMetadataExtractor();
        assertEquals(10, extractor.getPriority());
    }
}
