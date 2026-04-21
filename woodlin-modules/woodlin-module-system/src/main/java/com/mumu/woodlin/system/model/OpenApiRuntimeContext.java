package com.mumu.woodlin.system.model;

import com.mumu.woodlin.system.dto.OpenApiGlobalSettingsDto;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import lombok.Builder;
import lombok.Data;

/**
 * 开放 API 运行时上下文。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Builder
public class OpenApiRuntimeContext {

    /**
     * 是否命中显式策略。
     */
    private boolean policyMatched;

    /**
     * 命中的策略。
     */
    private SysOpenApiPolicy policy;

    /**
     * 全局设置。
     */
    private OpenApiGlobalSettingsDto globalSettings;
}
