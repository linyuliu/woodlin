package com.mumu.woodlin.admin.listener;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.mumu.woodlin.common.entity.BuildInfo;
import com.mumu.woodlin.common.service.BuildInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 系统启动信息打印
 * @author mumu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupInfoListener implements ApplicationListener<ApplicationReadyEvent> {

    private final BuildInfoService buildInfoService;
    private final Environment environment;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        try {
            // 1. 获取基础配置 (既然不使用随机端口，直接读配置即可，更简单)
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String appName = environment.getProperty("spring.application.name", "Woodlin-Admin");
            String[] profiles = environment.getActiveProfiles();

            // 2. 获取本机IP (Hutool)
            String localIp = NetUtil.getLocalhostStr();

            // 3. 构建信息 (处理空指针)
            BuildInfo info = Optional.ofNullable(buildInfoService.getBuildInfo()).orElse(new BuildInfo());

            // 4. 组装中文日志
            String logInfo = StrUtil.format("""

                    ====================================================================
                    [应用信息]
                    应用名称 : {}
                    运行环境 : {}
                    服务端口 : {}
                    上下文路径: {}

                    [访问地址]
                    本地访问 : http://localhost:{}{}
                    局域网   : http://{}:{}{}

                    [Java环境]
                    Java版本 : {}
                    Java厂商 : {}
                    操作系统 : {} ({})

                    [构建信息]
                    构建分支 : {}
                    提交版本 : {}
                    提交时间 : {}
                    提交作者 : {}
                    提交内容 : {}
                    构建时间 : {}
                    构建用户 : {}
                    构建机器 : {}
                    ===================================================================="
                    """,
                appName,
                profiles.length > 0 ? String.join(",", profiles) : "default",
                port,
                StrUtil.blankToDefault(contextPath, "/"),

                // 访问地址
                port, contextPath,
                localIp, port, contextPath,

                // Java环境
                SystemUtil.getJavaInfo().getVersion(),
                SystemUtil.getJavaInfo().getVendor(),
                SystemUtil.getOsInfo().getName(),
                SystemUtil.getOsInfo().getArch(),

                // Git信息
                StrUtil.blankToDefault(info.getGitBranch(), "未知分支"),
                StrUtil.blankToDefault(info.getGitCommitIdAbbrev(), "未知版本"),
                StrUtil.blankToDefault(info.getGitCommitTime(), DateUtil.now()),
                StrUtil.blankToDefault(info.getGitCommitUserName(), "未知作者"),
                StrUtil.blankToDefault(info.getGitCommitMessageShort(), "无提交信息"),
                StrUtil.blankToDefault(info.getBuildTime(), DateUtil.now()),
                StrUtil.blankToDefault(info.getBuildUser(), "未知用户"),
                StrUtil.blankToDefault(info.getBuildHost(), "未知主机")
            );
            log.info(logInfo);
        } catch (Exception e) {
            log.error("启动信息打印异常", e);
        }
    }
}
