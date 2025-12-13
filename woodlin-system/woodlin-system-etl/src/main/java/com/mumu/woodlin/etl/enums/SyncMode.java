package com.mumu.woodlin.etl.enums;

/**
 * 同步模式枚举
 * 
 * @author mumu
 * @description 数据同步模式枚举
 * @since 2025-01-01
 */
public enum SyncMode {
    
    /**
     * 全量同步
     */
    FULL("FULL", "全量同步"),
    
    /**
     * 增量同步
     */
    INCREMENTAL("INCREMENTAL", "增量同步");
    
    private final String code;
    private final String name;
    
    SyncMode(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static SyncMode fromCode(String code) {
        for (SyncMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return FULL;
    }
}
