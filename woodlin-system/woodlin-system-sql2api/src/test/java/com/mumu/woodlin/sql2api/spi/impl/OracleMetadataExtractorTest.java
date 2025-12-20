package com.mumu.woodlin.sql2api.spi.impl;

import com.mumu.woodlin.common.datasource.spi.impl.OracleMetadataExtractor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Oracle元数据提取器测试
 *
 * @author mumu
 * @since 2025-01-04
 */
class OracleMetadataExtractorTest {

    @Test
    void testGetDatabaseType() {
        OracleMetadataExtractor extractor = new OracleMetadataExtractor();
        assertEquals("Oracle", extractor.getDatabaseType());
    }

    @Test
    void testSupportsOracle() throws SQLException {
        OracleMetadataExtractor extractor = new OracleMetadataExtractor();

        Connection connection = mock(Connection.class);
        java.sql.DatabaseMetaData metaData = mock(java.sql.DatabaseMetaData.class);

        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("Oracle Database");

        assertTrue(extractor.supports(connection));
    }

    @Test
    void testDoesNotSupportMySQL() throws SQLException {
        OracleMetadataExtractor extractor = new OracleMetadataExtractor();

        Connection connection = mock(Connection.class);
        java.sql.DatabaseMetaData metaData = mock(java.sql.DatabaseMetaData.class);

        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");

        assertFalse(extractor.supports(connection));
    }

    @Test
    void testGetPriority() {
        OracleMetadataExtractor extractor = new OracleMetadataExtractor();
        assertEquals(10, extractor.getPriority());
    }
}
