package com.mumu.woodlin.sql2api.model;

/**
 * 数据库文档导出格式
 *
 * @author mumu
 * @since 2025-01-01
 */
public enum DatabaseDocFormat {
    EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    WORD("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PDF("pdf", "application/pdf");

    private final String fileExtension;
    private final String contentType;

    DatabaseDocFormat(String fileExtension, String contentType) {
        this.fileExtension = fileExtension;
        this.contentType = contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getContentType() {
        return contentType;
    }
}
