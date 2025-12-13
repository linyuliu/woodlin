package com.mumu.woodlin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 构建信息配置属性
 * 
 * @author mumu
 * @description 构建信息相关的配置项
 * @since 2025-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.build-info")
public class BuildInfoProperties {
    
    /**
     * 是否在API响应中包含远程仓库URL
     * 生产环境建议设置为false，以避免泄露仓库地址等敏感信息
     */
    private Boolean includeRemoteUrl = false;
}
