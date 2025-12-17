package com.mumu.woodlin.sql2api.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.alibaba.excel.EasyExcel;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseColumnDocRow;
import com.mumu.woodlin.sql2api.model.DatabaseDocFormat;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库文档导出服务
 */
@Slf4j
@Service
public class DatabaseDocumentExportService {

        public byte[] export(DatabaseMetadata metadata, List<TableMetadata> tables, DatabaseDocFormat format) {
            List<TableMetadata> safeTables = Objects.requireNonNullElse(tables, Collections.emptyList());
            DatabaseMetadata safeMetadata = metadata == null ? DatabaseMetadata.builder().build() : metadata;

            if (format == null) {
                throw new BusinessException("导出格式不能为空");
            }

            return switch (format) {
                case EXCEL -> exportExcel(safeMetadata, safeTables);
                case WORD -> exportWord(safeMetadata, safeTables);
                case PDF -> exportPdf(safeMetadata, safeTables);
            };
        }

    private byte[] exportExcel(DatabaseMetadata metadata, List<TableMetadata> tables) {
        List<DatabaseColumnDocRow> rows = new ArrayList<>();
        for (TableMetadata table : tables) {
            if (CollUtil.isEmpty(table.getColumns())) {
                continue;
            }
            for (ColumnMetadata column : table.getColumns()) {
                rows.add(DatabaseColumnDocRow.builder()
                        .databaseName(CharSequenceUtil.emptyToDefault(metadata.getDatabaseName(), ""))
                        .tableName(table.getTableName())
                        .tableComment(table.getComment())
                        .columnName(column.getColumnName())
                        .dataType(column.getDataType())
                        .nullable(Boolean.TRUE.equals(column.getNullable()) ? "是" : "否")
                        .defaultValue(column.getDefaultValue())
                        .primaryKey(Boolean.TRUE.equals(column.getPrimaryKey()) ? "是" : "否")
                        .comment(column.getComment())
                        .build());
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            EasyExcel.write(outputStream, DatabaseColumnDocRow.class)
                    .sheet("数据库文档")
                    .doWrite(rows);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("生成Excel文档失败: " + e.getMessage(), e);
        }
    }

    private byte[] exportWord(DatabaseMetadata metadata, List<TableMetadata> tables) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XWPFParagraph title = document.createParagraph();
            XWPFRun titleRun = title.createRun();
            titleRun.setText("数据库：" + CharSequenceUtil.emptyToDefault(metadata.getDatabaseName(), "未知"));
            titleRun.setBold(true);
            titleRun.setFontSize(14);

            for (TableMetadata table : tables) {
                XWPFParagraph tableTitle = document.createParagraph();
                XWPFRun tableRun = tableTitle.createRun();
                tableRun.setText("表：" + table.getTableName() + "    " + CharSequenceUtil.emptyToDefault(table.getComment(), ""));
                tableRun.setBold(true);

                List<ColumnMetadata> columns = Objects.requireNonNullElse(table.getColumns(), Collections.emptyList());
                if (CollUtil.isEmpty(columns)) {
                    continue;
                }

                XWPFTable wordTable = document.createTable(columns.size() + 1, 6);
                fillHeader(wordTable.getRow(0));

                int rowIndex = 1;
                for (ColumnMetadata column : columns) {
                    XWPFTableRow row = wordTable.getRow(rowIndex++);
                    fillCell(row.getCell(0), column.getColumnName());
                    fillCell(row.getCell(1), column.getDataType());
                    fillCell(row.getCell(2), Boolean.TRUE.equals(column.getNullable()) ? "是" : "否");
                    fillCell(row.getCell(3), column.getDefaultValue());
                    fillCell(row.getCell(4), Boolean.TRUE.equals(column.getPrimaryKey()) ? "是" : "");
                    fillCell(row.getCell(5), column.getComment());
                }
            }

            document.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("生成Word文档失败: " + e.getMessage(), e);
        }
    }

    private void fillHeader(XWPFTableRow headerRow) {
        fillCell(headerRow.getCell(0), "列名");
        fillCell(headerRow.getCell(1), "数据类型");
        fillCell(headerRow.getCell(2), "可为空");
        fillCell(headerRow.getCell(3), "默认值");
        fillCell(headerRow.getCell(4), "主键");
        fillCell(headerRow.getCell(5), "注释");
    }

    private void fillCell(XWPFTableCell cell, String text) {
        cell.removeParagraph(0);
        cell.setText(CharSequenceUtil.emptyToDefault(text, ""));
    }

    private byte[] exportPdf(DatabaseMetadata metadata, List<TableMetadata> tables) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfPageWriter writer = new PdfPageWriter(document);

            writer.writeLine("数据库：" + CharSequenceUtil.emptyToDefault(metadata.getDatabaseName(), "未知"), true);

            for (TableMetadata table : tables) {
                writer.writeLine("", false);
                writer.writeLine("表：" + table.getTableName() + "    " + CharSequenceUtil.emptyToDefault(table.getComment(), ""), true);

                List<ColumnMetadata> columns = Objects.requireNonNullElse(table.getColumns(), Collections.emptyList());
                for (ColumnMetadata column : columns) {
                    String line = String.format("  - %s (%s)%s%s 默认值:%s",
                            column.getColumnName(),
                            CharSequenceUtil.emptyToDefault(column.getDataType(), "-"),
                            Boolean.TRUE.equals(column.getPrimaryKey()) ? " [PK]" : "",
                            Boolean.TRUE.equals(column.getNullable()) ? "" : " [NOT NULL]",
                            CharSequenceUtil.emptyToDefault(column.getDefaultValue(), "无"));
                    writer.writeLine(line, false);
                    if (CharSequenceUtil.isNotBlank(column.getComment())) {
                        writer.writeLine("      注释: " + column.getComment(), false);
                    }
                }
            }

            writer.finish();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("生成PDF文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简单的PDF逐行写入器
     */
    private static class PdfPageWriter {
        private static final float START_Y = 750f;
        private static final float MARGIN_X = 50f;
        private static final float MIN_Y = 80f;
        private static final float LINE_HEIGHT = 16f;
        private static final List<String> REGULAR_FONT_CANDIDATES = List.of(
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/noto/NotoSansSC-Regular.otf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "C:/Windows/Fonts/msyh.ttc",
                "/System/Library/Fonts/PingFang.ttc"
        );
        private static final List<String> BOLD_FONT_CANDIDATES = List.of(
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Bold.ttc",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                "C:/Windows/Fonts/msyhbd.ttc",
                "/System/Library/Fonts/PingFang.ttc"
        );

        private final PDDocument document;
        private PDPageContentStream contentStream;
        private float currentY = START_Y;
        private final org.apache.pdfbox.pdmodel.font.PDFont normalFont;
        private final org.apache.pdfbox.pdmodel.font.PDFont boldFont;

        PdfPageWriter(PDDocument document) throws IOException {
            this.document = document;
            List<String> regularCandidates = mergeConfiguredCandidates(REGULAR_FONT_CANDIDATES,
                    "sql2api.font.regular", "SQL2API_FONT_REGULAR");
            List<String> boldCandidates = mergeConfiguredCandidates(BOLD_FONT_CANDIDATES,
                    "sql2api.font.bold", "SQL2API_FONT_BOLD");
            this.normalFont = loadFont(regularCandidates, PDType1Font.HELVETICA);
            this.boldFont = loadFont(boldCandidates, normalFont);
            openNewPage();
        }

        void writeLine(String text, boolean bold) throws IOException {
            if (currentY < MIN_Y) {
                openNewPage();
            }
            org.apache.pdfbox.pdmodel.font.PDFont fontToUse = bold ? boldFont : normalFont;
            contentStream.setFont(fontToUse, 11);
            contentStream.showText(filterUnsupported(text, fontToUse));
            contentStream.newLineAtOffset(0, -LINE_HEIGHT);
            currentY -= LINE_HEIGHT;
        }

        void finish() throws IOException {
            if (contentStream != null) {
                contentStream.endText();
                contentStream.close();
            }
        }

        private void openNewPage() throws IOException {
            if (contentStream != null) {
                contentStream.endText();
                contentStream.close();
            }
            PDPage page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN_X, START_Y);
            currentY = START_Y;
        }

        private org.apache.pdfbox.pdmodel.font.PDFont loadFont(List<String> paths, org.apache.pdfbox.pdmodel.font.PDFont fallback) {
            for (String path : paths) {
                try (java.io.InputStream stream = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(path))) {
                    return org.apache.pdfbox.pdmodel.font.PDType0Font.load(document, stream);
                } catch (Exception ignored) {
                    // try next candidate
                }
            }
            return fallback;
        }

        private String filterUnsupported(String text, org.apache.pdfbox.pdmodel.font.PDFont font) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                try {
                    font.encode(String.valueOf(ch));
                    builder.append(ch);
                } catch (Exception e) {
                    builder.append('?');
                }
            }
            return builder.toString();
        }

        private List<String> mergeConfiguredCandidates(List<String> defaults, String propertyKey, String envKey) {
            Set<String> candidates = new LinkedHashSet<>(defaults);
            String configured = System.getProperty(propertyKey, System.getenv(envKey));
            if (CharSequenceUtil.isNotBlank(configured)) {
                for (String path : configured.split(",")) {
                    if (CharSequenceUtil.isNotBlank(path)) {
                        candidates.add(path.trim());
                    }
                }
            }
            return List.copyOf(candidates);
        }
    }
}
