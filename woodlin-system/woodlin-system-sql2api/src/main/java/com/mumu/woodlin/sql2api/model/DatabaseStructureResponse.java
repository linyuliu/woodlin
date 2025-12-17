package com.mumu.woodlin.sql2api.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库结构响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStructureResponse {
    private DatabaseMetadata database;
    private List<TableMetadata> tables;
}
