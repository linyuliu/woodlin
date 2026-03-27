package com.mumu.woodlin.common.mp.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * woodlin-common-mp 自动装配。
 */
@AutoConfiguration
@Import(MyBatisPlusInterceptorConfig.class)
public class CommonMyBatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MyBatisPlusMetaObjectHandler myBatisPlusMetaObjectHandler() {
        return new MyBatisPlusMetaObjectHandler();
    }
}
