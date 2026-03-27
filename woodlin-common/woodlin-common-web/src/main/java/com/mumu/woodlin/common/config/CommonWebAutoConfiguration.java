package com.mumu.woodlin.common.config;

import com.mumu.woodlin.common.exception.GlobalExceptionHandler;
import com.mumu.woodlin.common.interceptor.ResponseFieldInterceptor;
import com.mumu.woodlin.common.service.BuildInfoService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * woodlin-common-web 自动装配。
 */
@AutoConfiguration
@EnableConfigurationProperties({
    ApiEncryptionProperties.class,
    BuildInfoProperties.class,
    CorsProperties.class,
    ResponseProperties.class
})
@Import({
    CorsConfig.class,
    JacksonConfig.class,
    WebMvcConfig.class
})
public class CommonWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseFieldInterceptor responseFieldInterceptor(ResponseProperties responseProperties) {
        return new ResponseFieldInterceptor(responseProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BuildInfoService buildInfoService(BuildInfoProperties buildInfoProperties) {
        return new BuildInfoService(buildInfoProperties);
    }
}
