package com.mumu.woodlin.etl.dialect;

/**
 * ETL数据库方言类型枚举。
 *
 * @author mumu
 * @since 1.0.0
 */
public enum DatabaseDialectType {

    /**
     * MySQL 及兼容方言。
     */
    MYSQL,

    /**
     * PostgreSQL 及兼容方言。
     */
    POSTGRESQL,

    /**
     * Oracle 方言。
     */
    ORACLE,

    /**
     * SQL Server 方言。
     */
    SQL_SERVER,

    /**
     * 通用方言。
     */
    GENERIC
}
