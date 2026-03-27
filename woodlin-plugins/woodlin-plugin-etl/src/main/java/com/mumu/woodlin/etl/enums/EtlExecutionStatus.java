package com.mumu.woodlin.etl.enums;

/**
 * ETL执行状态枚举
 * 
 * @author mumu
 * @description ETL任务执行状态枚举
 * @since 2025-01-01
 */
public enum EtlExecutionStatus {
    
    /**
     * 运行中
     */
    RUNNING("RUNNING", "运行中"),
    
    /**
     * 成功
     */
    SUCCESS("SUCCESS", "成功"),
    
    /**
     * 失败
     */
    FAILED("FAILED", "失败"),
    
    /**
     * 部分成功
     */
    PARTIAL_SUCCESS("PARTIAL_SUCCESS", "部分成功");
    
    private final String code;
    private final String name;
    
    EtlExecutionStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static EtlExecutionStatus fromCode(String code) {
        for (EtlExecutionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return FAILED;
    }
}
