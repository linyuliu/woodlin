package com.mumu.woodlin.admin.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Druid 广告移除配置
 *
 * @author mumu
 * @description 移除Druid监控页面底部的广告信息
 * @since 2025-01-01
 */
@Slf4j
@Configuration
@SuppressWarnings("all")
public class DruidAdRemovalConfiguration {

    /**
     * 注册过滤器来移除Druid广告
     */
    @Bean
    public FilterRegistrationBean<DruidAdRemovalFilter> druidAdRemovalFilter() {
        FilterRegistrationBean<DruidAdRemovalFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DruidAdRemovalFilter());
        registration.addUrlPatterns("/druid/*");
        registration.setName("druidAdRemovalFilter");
        registration.setOrder(1);
        return registration;
    }

    /**
     * 自定义过滤器，移除广告
     */
    public static class DruidAdRemovalFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            if (response instanceof HttpServletResponse httpResponse) {
                DruidResponseWrapper wrapper = new DruidResponseWrapper(httpResponse);
                chain.doFilter(request, wrapper);

                String content = wrapper.getContent();
                if (content != null && content.contains("支持")) {
                    // 移除广告相关内容
                    content = content.replaceAll("<a.*?www\\.aliyun\\.com.*?</a>", "");
                    content = content.replaceAll("支持.*?阿里云.*?</div>", "</div>");
                    content = content.replace("powered by Alibaba", "");
                }
                if (content != null) {
                    httpResponse.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * 响应包装器
     */
    private static class DruidResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final ServletOutputStream stream = new ServletOutputStreamWrapper(buffer);

        public DruidResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return stream;
        }

        public String getContent() {
            try {
                return buffer.toString(StandardCharsets.UTF_8);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * ServletOutputStream 包装器
     */
    private static class ServletOutputStreamWrapper extends ServletOutputStream {
        private final ByteArrayOutputStream buffer;

        public ServletOutputStreamWrapper(ByteArrayOutputStream buffer) {
            this.buffer = buffer;
        }

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            // Not implemented
        }
    }
}
