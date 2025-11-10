package com.mumu.woodlin.admin.service;

import com.mumu.woodlin.security.config.DevTokenProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * 开发令牌启动监听器
 *
 * @author mumu
 * @description 在应用启动时自动生成开发调试令牌
 * @since 2025-01-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevTokenStartupListener {

    private final DevTokenProperties devTokenProperties;
    private final RestTemplate restTemplate;
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    /**
     * 应用启动完成后生成开发令牌
     * 通过发起HTTP请求到 /auth/dev-token 端点来生成令牌，确保在完整的Web上下文中运行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 检查是否启用开发令牌功能
        if (!devTokenProperties.getEnabled()) {
            return;
        }

        // 检查是否自动生成
        if (!devTokenProperties.getAutoGenerateOnStartup()) {
            log.info("开发令牌功能已启用，但未配置自动生成。请访问 /auth/dev-token 端点手动生成。");
            return;
        }

        // 延迟一小段时间，确保应用完全启动
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 等待2秒，确保应用完全启动
                
                // 构建请求URL
                String url = buildDevTokenUrl();
                log.info("正在通过HTTP请求生成开发令牌: {}", url);
                
                // 发起HTTP GET请求到 /auth/dev-token 端点
                ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && Objects.nonNull(response.getBody())) {
                    Map<String, Object> body = response.getBody();
                    Object data = body.get("data");
                    
                    if (Objects.nonNull(data) && data instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tokenInfo = (Map<String, Object>) data;
                        printTokenInfo(tokenInfo);
                    } else {
                        log.info("开发令牌生成成功，响应: {}", body);
                    }
                } else {
                    log.warn("开发令牌生成请求返回非成功状态: {}", response.getStatusCode());
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("开发令牌生成线程被中断");
            } catch (Exception e) {
                log.error("通过HTTP请求生成开发令牌失败: {}", e.getMessage(), e);
                log.warn("开发令牌生成失败，但应用继续运行。请手动访问 /auth/dev-token 端点生成。");
            }
        }).start();
    }

    /**
     * 构建开发令牌请求URL
     *
     * @return 完整的URL
     */
    private String buildDevTokenUrl() {
        // 确保contextPath以/开头
        String path = contextPath;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        // 确保contextPath不以/结尾
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        
        return String.format("http://localhost:%d%s/auth/dev-token", serverPort, path);
    }

    /**
     * 打印令牌信息
     */
    private void printTokenInfo(Map<String, Object> tokenInfo) {
        if (!devTokenProperties.getPrintToConsole()) {
            return;
        }

        String format = devTokenProperties.getDisplayFormat();

        if ("banner".equalsIgnoreCase(format)) {
            printBannerFormat(tokenInfo);
        } else {
            printSimpleFormat(tokenInfo);
        }
    }

    /**
     * 以横幅格式打印令牌信息
     */
    private void printBannerFormat(Map<String, Object> tokenInfo) {
        StringBuilder banner = new StringBuilder();
        String username = String.valueOf(tokenInfo.get("username"));
        String token = String.valueOf(tokenInfo.get("token"));
        String generateTime = String.valueOf(tokenInfo.get("generateTime"));
        Object expiresInObj = tokenInfo.get("expiresIn");
        
        banner.append("\n");
        banner.append("╔═══════════════════════════════════════════════════════════════════════════════╗\n");
        banner.append("║                          开发调试令牌 (Development Token)                      ║\n");
        banner.append("╠═══════════════════════════════════════════════════════════════════════════════╣\n");
        banner.append(String.format("║ 用户名 (Username)    : %-54s ║\n", username));
        banner.append(String.format("║ 令牌 (Token)         : %-54s ║\n", token));
        banner.append(String.format("║ 生成时间 (Time)       : %-54s ║\n", generateTime));
        
        if (Objects.nonNull(expiresInObj)) {
            Long expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).longValue() : 0L;
            if (expiresIn > 0) {
                banner.append(String.format("║ 过期时间 (Expires)    : %-54s ║\n", formatExpireTime(expiresIn)));
            } else {
                banner.append(String.format("║ 过期时间 (Expires)    : %-54s ║\n", "永不过期 (Never)"));
            }
        } else {
            banner.append(String.format("║ 过期时间 (Expires)    : %-54s ║\n", "永不过期 (Never)"));
        }
        
        banner.append("╠═══════════════════════════════════════════════════════════════════════════════╣\n");
        banner.append("║ 使用方法 (Usage):                                                              ║\n");
        banner.append("║   1. 在请求头中添加: Authorization: " + token);
        // 填充空格以对齐
        int padding = 79 - 42 - token.length();
        for (int i = 0; i < padding && i < 20; i++) {
            banner.append(" ");
        }
        banner.append("║\n");
        banner.append("║   2. 或访问 /auth/dev-token 端点重新生成                                       ║\n");
        banner.append("╚═══════════════════════════════════════════════════════════════════════════════╝\n");

        System.out.println(banner.toString());
        log.info("开发令牌已生成并打印到控制台");
    }

    /**
     * 以简单格式打印令牌信息
     */
    private void printSimpleFormat(Map<String, Object> tokenInfo) {
        String username = String.valueOf(tokenInfo.get("username"));
        String token = String.valueOf(tokenInfo.get("token"));
        String generateTime = String.valueOf(tokenInfo.get("generateTime"));
        Object expiresInObj = tokenInfo.get("expiresIn");
        
        log.info("========== 开发调试令牌 ==========");
        log.info("用户名: {}", username);
        log.info("令牌: {}", token);
        log.info("生成时间: {}", generateTime);
        
        if (Objects.nonNull(expiresInObj)) {
            Long expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).longValue() : 0L;
            if (expiresIn > 0) {
                log.info("过期时间: {}", formatExpireTime(expiresIn));
            } else {
                log.info("过期时间: 永不过期");
            }
        } else {
            log.info("过期时间: 永不过期");
        }
        
        log.info("使用方法: 在请求头中添加 Authorization: {}", token);
        log.info("================================");
    }

    /**
     * 格式化过期时间
     */
    private String formatExpireTime(Long seconds) {
        if (seconds < 0) {
            return "永不过期";
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return String.format("%d天%d小时", days, hours);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }
}
