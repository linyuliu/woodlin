package com.mumu.woodlin.sql2api.dsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL构建器测试
 */
class SqlBuilderTest {
    
    private TableMetadata testTable;
    
    @BeforeEach
    void setUp() {
        List<ColumnMetadata> columns = Arrays.asList(
            ColumnMetadata.builder()
                .columnName("id")
                .primaryKey(true)
                .autoIncrement(true)
                .javaType("Long")
                .build(),
            ColumnMetadata.builder()
                .columnName("name")
                .javaType("String")
                .build()
        );
        
        testTable = TableMetadata.builder()
            .tableName("users")
            .schemaName("public")
            .primaryKey("id")
            .columns(columns)
            .build();
    }
    
    @Test
    void testBuildSelectSql() {
        SqlBuilder.SelectConfig config = SqlBuilder.SelectConfig.builder()
            .build();
        
        String sql = SqlBuilder.buildSelectSql(testTable, config);
        
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("FROM public.users"));
    }
    
    @Test
    void testBuildInsertSql() {
        SqlBuilder.InsertConfig config = SqlBuilder.InsertConfig.builder()
            .column("name")
            .build();
        
        String sql = SqlBuilder.buildInsertSql(testTable, config);
        
        assertTrue(sql.contains("INSERT INTO public.users"));
        assertTrue(sql.contains("#{name}"));
    }
}
