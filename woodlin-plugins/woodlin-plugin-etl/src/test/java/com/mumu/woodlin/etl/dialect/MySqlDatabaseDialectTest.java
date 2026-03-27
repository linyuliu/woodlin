package com.mumu.woodlin.etl.dialect;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL 数据库方言单元测试。
 *
 * @author mumu
 * @since 1.0.0
 */
class MySqlDatabaseDialectTest {

    private MySqlDatabaseDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new MySqlDatabaseDialect();
    }

    @Test
    void getDialectType() {
        assertEquals(DatabaseDialectType.MYSQL, dialect.getDialectType());
    }

    @Test
    void quoteIdentifier() {
        assertEquals("`user_name`", dialect.quoteIdentifier("user_name"));
    }

    @Test
    void qualifyTableWithSchema() {
        String result = dialect.qualifyTable("my_schema", "my_table");
        assertEquals("`my_schema`.`my_table`", result);
    }

    @Test
    void qualifyTableWithoutSchema() {
        String result = dialect.qualifyTable(null, "my_table");
        assertEquals("`my_table`", result);
    }

    @Test
    void buildDeleteAllSql() {
        String result = dialect.buildDeleteAllSql("`my_table`");
        assertEquals("DELETE FROM `my_table`", result);
    }

    @Test
    void buildTruncateSql() {
        String result = dialect.buildTruncateSql("`my_table`");
        assertEquals("TRUNCATE TABLE `my_table`", result);
    }

    @Test
    void buildInsertSql() {
        String result = dialect.buildInsertSql("`my_table`", List.of("id", "name", "age"));
        assertNotNull(result);
        assertTrue(result.startsWith("INSERT INTO `my_table`"));
        assertTrue(result.contains("`id`"));
        assertTrue(result.contains("`name`"));
        assertTrue(result.contains("`age`"));
        assertTrue(result.contains("?, ?, ?"));
    }

    @Test
    void buildUpsertSql() {
        String result = dialect.buildUpsertSql(
                "`my_table`",
                List.of("id", "name", "age"),
                List.of("id")
        );
        assertNotNull(result);
        assertTrue(result.contains("INSERT INTO `my_table`"));
        assertTrue(result.contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(result.contains("`name` = VALUES(`name`)"));
        assertTrue(result.contains("`age` = VALUES(`age`)"));
    }

    @Test
    void buildUpsertSqlWithPrimaryKeyOnly() {
        String result = dialect.buildUpsertSql(
                "`my_table`",
                List.of("id"),
                List.of("id")
        );
        assertNotNull(result);
        assertTrue(result.contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(result.contains("`id` = `id`"));
    }

    @Test
    void buildSelectByPrimaryKeyInSql() {
        String result = dialect.buildSelectByPrimaryKeyInSql(
                "`my_table`",
                List.of("id", "name"),
                "id",
                3
        );
        assertNotNull(result);
        assertTrue(result.contains("SELECT `id`, `name` FROM `my_table`"));
        assertTrue(result.contains("WHERE `id` IN (?, ?, ?)"));
    }

    @Test
    void buildAddColumnSql() {
        String result = dialect.buildAddColumnSql("`my_table`", "email", "VARCHAR(255)");
        assertEquals("ALTER TABLE `my_table` ADD `email` VARCHAR(255)", result);
    }
}
