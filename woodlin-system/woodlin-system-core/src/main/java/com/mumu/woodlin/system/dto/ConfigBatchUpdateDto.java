package com.mumu.woodlin.system.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 配置批量更新DTO
 * 
 * @author mumu
 * @description 用于批量更新配置的数据传输对象
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "配置批量更新DTO")
public class ConfigBatchUpdateDto implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 配置分类（如：api.encryption, password.policy）
     */
    @NotBlank(message = "配置分类不能为空")
    @Schema(description = "配置分类", example = "api.encryption")
    private String category;
    
    /**
     * 配置项集合，key为配置键名，value为配置值
     */
    @NotNull(message = "配置项不能为空")
    @Schema(description = "配置项集合")
    private Map<String, String> configs;
}
