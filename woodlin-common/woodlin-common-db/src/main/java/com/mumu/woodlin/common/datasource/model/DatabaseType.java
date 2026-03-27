package com.mumu.woodlin.common.datasource.model;

/**
 * 数据库类型枚举
 * <p>
 * 定义系统支持的所有数据库类型，包括主流关系型数据库、国产数据库、
 * MySQL协议兼容数据库、分析型数据库、时序数据库和嵌入式数据库。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public enum DatabaseType {
    
    // ===== 主流关系型数据库 =====
    
    /**
     * MySQL数据库 (5.6+)
     */
    MYSQL,
    
    /**
     * MariaDB数据库 (MySQL分支)
     */
    MARIADB,
    
    /**
     * PostgreSQL数据库 (10+)
     */
    POSTGRESQL,
    
    /**
     * Oracle数据库 (11g+)
     */
    ORACLE,
    
    /**
     * Microsoft SQL Server数据库 (2012+)
     */
    SQLSERVER,
    
    // ===== MySQL 协议兼容数据库 =====
    
    /**
     * TiDB数据库 (NewSQL分布式数据库，MySQL协议兼容)
     */
    TIDB,
    
    /**
     * OceanBase数据库 (蚂蚁金服，MySQL协议兼容)
     */
    OCEANBASE,
    
    /**
     * PolarDB数据库 (阿里云，MySQL协议兼容)
     */
    POLARDB,
    
    /**
     * Vitess数据库 (YouTube开源，MySQL分片中间件)
     */
    VITESS,
    
    // ===== 国产数据库 =====
    
    /**
     * 达梦数据库 (DM8)
     */
    DM,
    
    /**
     * 人大金仓数据库 (KingbaseES)
     */
    KINGBASE,
    
    /**
     * 南大通用数据库 (GBase 8s/8a/8t)
     */
    GBASE,
    
    /**
     * 华为GaussDB数据库 (企业版)
     */
    GAUSSDB,
    
    /**
     * 华为openGauss数据库 (开源版)
     */
    OPENGAUSS,
    
    /**
     * 神舟通用数据库 (Oscar)
     */
    OSCAR,
    
    /**
     * 优炫数据库 (UXDB，基于PostgreSQL)
     */
    UXDB,
    
    /**
     * 海量数据Vastbase数据库 (基于PostgreSQL)
     */
    VASTBASE,
    
    // ===== 分析型 / 数据仓库 =====
    
    /**
     * ClickHouse数据库 (OLAP列式存储)
     */
    CLICKHOUSE,
    
    /**
     * StarRocks数据库 (OLAP，兼容MySQL协议)
     */
    STARROCKS,
    
    /**
     * Apache Doris数据库 (OLAP，兼容MySQL协议)
     */
    DORIS,
    
    /**
     * Greenplum数据库 (基于PostgreSQL的MPP数据仓库)
     */
    GREENPLUM,
    
    /**
     * Apache Hive数据仓库 (基于Hadoop)
     */
    HIVE,
    
    /**
     * Apache Impala数据库 (基于Hadoop的MPP SQL引擎)
     */
    IMPALA,
    
    // ===== 时序 / KV数据库 =====
    
    /**
     * TDengine时序数据库 (涛思数据)
     */
    TDENGINE,
    
    /**
     * InfluxDB时序数据库
     */
    INFLUXDB,
    
    // ===== 嵌入式数据库 =====
    
    /**
     * H2数据库 (嵌入式关系型数据库)
     */
    H2,
    
    /**
     * SQLite数据库 (嵌入式关系型数据库)
     */
    SQLITE
}
