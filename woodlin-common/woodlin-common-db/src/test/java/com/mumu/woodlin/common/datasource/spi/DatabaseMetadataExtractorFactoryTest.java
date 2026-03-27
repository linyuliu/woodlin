package com.mumu.woodlin.common.datasource.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * DatabaseMetadataExtractorFactory tests.
 */
class DatabaseMetadataExtractorFactoryTest {

    private final DatabaseMetadataExtractorFactory factory = DatabaseMetadataExtractorFactory.getInstance();

    @Test
    void inferDatabaseTypeShouldSupportKingbaseAlias() {
        assertEquals("KINGBASE", factory.inferDatabaseType("jdbc:kingbase8://127.0.0.1:54321/test"));
        assertEquals("KINGBASE", factory.inferDatabaseType("jdbc:kingbase://127.0.0.1:54321/test"));
    }

    @Test
    void inferDatabaseTypeShouldSupportSqlServerAlias() {
        assertEquals("SQLSERVER", factory.inferDatabaseType("jdbc:sqlserver://127.0.0.1:1433;databaseName=test"));
        assertEquals("SQLSERVER", factory.inferDatabaseType("jdbc:microsoft:sqlserver://127.0.0.1:1433"));
    }

    @Test
    void getDefaultDriverClassShouldResolveAliasTypeName() {
        assertNotNull(factory.getDefaultDriverClass("kingbasees"));
        assertEquals("com.kingbase8.Driver", factory.getDefaultDriverClass("kingbasees"));
    }
}

