package com.mumu.woodlin.admin.service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.constant.SystemConstant;

/**
 * 启动横幅服务
 * 
 * @author mumu
 * @description 系统启动时优雅地显示系统信息和版本信息
 * @since 2025-01-01
 */
@Slf4j
@Service
public class StartupBannerService implements ApplicationRunner {
    
    private final Environment environment;
    
    public StartupBannerService(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        printStartupBanner();
    }
    
    /**
     * 打印启动横幅
     */
    private void printStartupBanner() {
        try {
            String serverPort = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String activeProfiles = String.join(",", environment.getActiveProfiles());
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            System.out.println("""
                
                ██╗    ██╗ ██████╗  ██████╗ ██████╗ ██╗     ██╗███╗   ██╗
                ██║    ██║██╔═══██╗██╔═══██╗██╔══██╗██║     ██║████╗  ██║
                ██║ █╗ ██║██║   ██║██║   ██║██║  ██║██║     ██║██╔██╗ ██║
                ██║███╗██║██║   ██║██║   ██║██║  ██║██║     ██║██║╚██╗██║
                ╚███╔███╔╝╚██████╔╝╚██████╔╝██████╔╝███████╗██║██║ ╚████║
                 ╚══╝╚══╝  ╚═════╝  ╚═════╝ ╚═════╝ ╚══════╝╚═╝╚═╝  ╚═══╝
                
                :: %s 多租户中后台管理系统 ::        (v%s)
                :: 作者: mumu ::                      :: %s ::
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                ✨ 系统启动成功！
                
                🌍 应用访问地址:
                   本地访问: http://localhost:%s%s
                   网络访问: http://%s:%s%s
                   API文档:  http://localhost:%s%s/doc.html
                   
                📊 系统环境信息:
                   Java版本: %s
                   Spring环境: %s
                   启动时间: %s
                   
                🔧 构建信息:
                   构建时间: %s
                   Git提交: %s
                   构建环境: %s
                   Maven版本: %s
                   
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                """.formatted(
                    SystemConstant.SYSTEM_NAME,
                    SystemConstant.SYSTEM_VERSION,
                    currentTime.substring(0, 10),
                    serverPort,
                    contextPath,
                    hostAddress,
                    serverPort,
                    contextPath,
                    serverPort,
                    contextPath,
                    System.getProperty("java.version"),
                    activeProfiles.isEmpty() ? "default" : activeProfiles,
                    currentTime,
                    getBuildTime(),
                    getGitCommitId(),
                    getBuildProfile(),
                    getMavenVersion()
            ));
            
            // 输出环境变量提示
            if (isUsingDefaultValues()) {
                System.out.println("💡 提示: 您可以通过设置环境变量来自定义配置，详情请查看 README.md");
                System.out.println();
            }
            
        } catch (Exception e) {
            log.error("打印启动横幅失败", e);
        }
    }
    
    /**
     * 检查是否使用了默认值
     */
    private boolean isUsingDefaultValues() {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        String redisHost = environment.getProperty("REDIS_HOST");
        return databaseUrl == null || redisHost == null;
    }
    
    /**
     * 获取构建时间
     */
    private String getBuildTime() {
        String buildTime = SystemConstant.BUILD_TIME;
        if (buildTime.startsWith("@") && buildTime.endsWith("@")) {
            return "开发环境";
        }
        return buildTime;
    }
    
    /**
     * 获取Git提交ID
     */
    private String getGitCommitId() {
        String gitCommitId = SystemConstant.GIT_COMMIT_ID;
        if (gitCommitId.startsWith("@") && gitCommitId.endsWith("@")) {
            return "dev";
        }
        return gitCommitId;
    }
    
    /**
     * 获取构建环境
     */
    private String getBuildProfile() {
        String buildProfile = SystemConstant.BUILD_PROFILE;
        if (buildProfile.startsWith("@") && buildProfile.endsWith("@")) {
            return "development";
        }
        return buildProfile;
    }
    
    /**
     * 获取Maven版本信息
     */
    private String getMavenVersion() {
        try {
            // 尝试从Maven相关的系统属性获取版本信息
            String version = System.getProperty("maven.version", 
                environment.getProperty("maven.version", "未知"));
            if ("未知".equals(version)) {
                // 尝试从环境变量获取
                version = System.getenv().getOrDefault("MAVEN_VERSION", "3.x");
            }
            return version;
        } catch (Exception e) {
            return "未知";
        }
    }
}