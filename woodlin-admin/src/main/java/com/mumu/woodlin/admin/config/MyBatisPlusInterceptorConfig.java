package com.mumu.woodlin.admin.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 拦截器配置
 *
 * @author mumu
 * @since 1.0.0
 */
@Configuration
public class MyBatisPlusInterceptorConfig {

    /**
     * 注册 MyBatis Plus 核心拦截器
     * <p>
     * OptimisticLockerInnerInterceptor 用于处理 {@code @Version} 乐观锁字段，
     * 解决 updateById 场景下 MP_OPTLOCK_VERSION_ORIGINAL 参数注入问题。
     * </p>
     *
     * @return MyBatis Plus 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
