package com.mumu.woodlin.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * woodlin-common-cloud 自动装配。
 */
@AutoConfiguration
@Import({
    SnowflakeIdConfiguration.class,
    NacosSnowflakeConfiguration.class
})
public class CommonCloudAutoConfiguration {
}
