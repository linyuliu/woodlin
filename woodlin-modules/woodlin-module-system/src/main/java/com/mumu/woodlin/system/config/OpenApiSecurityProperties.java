package com.mumu.woodlin.system.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 开放 API 安全中心基础配置。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@ConfigurationProperties(prefix = "woodlin.openapi")
@Schema(description = "开放API安全基础配置")
public class OpenApiSecurityProperties {

    /**
     * 是否启用开放 API 安全过滤器。
     */
    private boolean enabled = true;

    /**
     * 过滤路径前缀。
     */
    private String pathPrefix = "/openapi/";

    /**
     * 主密钥，优先使用环境变量注入。
     */
    private String masterKey;
}
