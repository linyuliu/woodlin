package com.mumu.woodlin.sql2api.service;

import java.util.List;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseDocFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseDocumentExportServiceTest {

    private DatabaseDocumentExportService exportService;
    private DatabaseMetadata metadata;
    private List<TableMetadata> tables;

    @BeforeEach
    void setUp() {
        exportService = new DatabaseDocumentExportService();
        metadata = DatabaseMetadata.builder().databaseName("testdb").build();

        List<ColumnMetadata> columns = List.of(
                ColumnMetadata.builder()
                        .columnName("id")
                        .dataType("bigint")
                        .nullable(false)
                        .primaryKey(true)
                        .comment("主键")
                        .build(),
                ColumnMetadata.builder()
                        .columnName("name")
                        .dataType("varchar")
                        .nullable(true)
                        .comment("名称")
                        .build()
        );

        tables = List.of(
                TableMetadata.builder()
                        .tableName("demo_table")
                        .comment("示例表")
                        .columns(columns)
                        .build()
        );
    }

    @Test
    void exportExcelShouldReturnContent() {
        byte[] bytes = exportService.export(metadata, tables, DatabaseDocFormat.EXCEL);
        assertTrue(bytes.length > 0);
    }

    @Test
    void exportWordShouldReturnContent() {
        byte[] bytes = exportService.export(metadata, tables, DatabaseDocFormat.WORD);
        assertTrue(bytes.length > 0);
    }

    @Test
    void exportPdfShouldReturnContent() {
        byte[] bytes = exportService.export(metadata, tables, DatabaseDocFormat.PDF);
        assertTrue(bytes.length > 0);
    }
}
