package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 系统配置更新DTO
 * 
 * @author mumu
 * @description 用于批量更新系统配置的数据传输对象
 * @since 2025-01-01
 */
@Data
@Schema(description = "系统配置更新请求")
public class ConfigUpdateDto {
    
    @Schema(description = "配置分类（如：api.encryption, password.policy）", example = "api.encryption")
    private String category;
    
    @Schema(description = "配置键值对，key为config_key，value为config_value")
    private Map<String, String> configs;
}
