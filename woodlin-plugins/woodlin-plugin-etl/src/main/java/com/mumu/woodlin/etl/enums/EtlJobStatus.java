package com.mumu.woodlin.etl.enums;

/**
 * ETL任务状态枚举
 * 
 * @author mumu
 * @description ETL任务状态枚举，定义任务的各种状态
 * @since 2025-01-01
 */
public enum EtlJobStatus {
    
    /**
     * 启用状态
     */
    ENABLED("1", "启用"),
    
    /**
     * 禁用状态
     */
    DISABLED("0", "禁用");
    
    private final String code;
    private final String name;
    
    EtlJobStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static EtlJobStatus fromCode(String code) {
        for (EtlJobStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return DISABLED;
    }
}
