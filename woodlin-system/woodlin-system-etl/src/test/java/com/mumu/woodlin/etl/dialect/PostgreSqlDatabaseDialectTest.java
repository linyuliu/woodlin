package com.mumu.woodlin.etl.dialect;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL 数据库方言单元测试。
 *
 * @author mumu
 * @since 1.0.0
 */
class PostgreSqlDatabaseDialectTest {

    private PostgreSqlDatabaseDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new PostgreSqlDatabaseDialect();
    }

    @Test
    void getDialectType() {
        assertEquals(DatabaseDialectType.POSTGRESQL, dialect.getDialectType());
    }

    @Test
    void quoteIdentifier() {
        assertEquals("\"user_name\"", dialect.quoteIdentifier("user_name"));
    }

    @Test
    void buildTruncateSql() {
        String result = dialect.buildTruncateSql("\"my_table\"");
        assertEquals("TRUNCATE TABLE \"my_table\"", result);
    }

    @Test
    void buildInsertSql() {
        String result = dialect.buildInsertSql("\"my_table\"", List.of("id", "name"));
        assertNotNull(result);
        assertTrue(result.startsWith("INSERT INTO \"my_table\""));
        assertTrue(result.contains("\"id\""));
        assertTrue(result.contains("\"name\""));
    }

    @Test
    void buildUpsertSql() {
        String result = dialect.buildUpsertSql(
                "\"my_table\"",
                List.of("id", "name", "age"),
                List.of("id")
        );
        assertNotNull(result);
        assertTrue(result.contains("ON CONFLICT"));
        assertTrue(result.contains("DO UPDATE SET"));
    }
}
