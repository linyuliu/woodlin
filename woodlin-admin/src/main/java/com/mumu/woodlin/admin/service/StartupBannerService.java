package com.mumu.woodlin.admin.service;

import cn.hutool.core.io.resource.ResourceUtil;
import com.mumu.woodlin.common.entity.BuildInfo;
import com.mumu.woodlin.common.service.BuildInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * 启动横幅服务
 *
 * @author mumu
 * @description 系统启动时优雅地显示系统信息和 Git 构建信息
 * @since 2025-01-01
 */
@Slf4j
@Service
public class StartupBannerService implements ApplicationRunner {

    @Resource
    private  BuildInfoService buildInfoService;
    private final Environment environment;

    public StartupBannerService(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        printStartupBanner();
    }

    /**
     * 打印启动横幅
     */
    private void printStartupBanner() {
        try {
            BuildInfo infoBanner = buildInfoService.getBuildInfo();
            String serverPort = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String profiles = String.join(",", environment.getActiveProfiles());
            String host = InetAddress.getLocalHost().getHostAddress();
            String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            System.out.printf("""

                    ██╗    ██╗ ██████╗  ██████╗ ██████╗ ██╗     ██╗███╗   ██╗
                    ██║    ██║██╔═══██╗██╔═══██╗██╔══██╗██║     ██║████╗  ██║
                    ██║ █╗ ██║██║   ██║██║   ██║██║  ██║██║     ██║██╔██╗ ██║
                    ██║███╗██║██║   ██║██║   ██║██║  ██║██║     ██║██║╚██╗██║
                    ╚███╔███╔╝╚██████╔╝╚██████╔╝██████╔╝███████╗██║██║ ╚████║
                     ╚══╝╚══╝  ╚═════╝  ╚═════╝ ╚═════╝ ╚══════╝╚═╝╚═╝  ╚═══╝

                    ✨ 系统启动成功 (%s)

                    🌍 访问地址:
                       http://localhost:%s%s
                       http://%s:%s%s

                    📦 Git 构建信息:
                       分支: %s
                       提交: %s
                       时间: %s
                       作者: %s
                       信息: %s


                    🧩 运行环境:
                       Java: %s
                       Profile: %s

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    %n""",
                now,
                serverPort,
                contextPath,
                host,
                serverPort,
                contextPath,
                infoBanner.getGitBranch(),
                infoBanner.getGitCommitIdAbbrev(),
                infoBanner.getGitCommitTime(),
                infoBanner.getGitCommitUserName(),
                infoBanner.getGitCommitMessage(),
                System.getProperty("java.version"),
                profiles.isEmpty() ? "default" : profiles
            );

        } catch (Exception e) {
            log.error("启动横幅打印失败", e);
        }
    }

}
