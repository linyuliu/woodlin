package com.mumu.woodlin.common.service;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.config.BuildInfoProperties;
import com.mumu.woodlin.common.entity.BuildInfo;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 构建信息服务
 * <p>
 * 只负责：
 * 1. 加载 git.properties（启动时一次）
 * 2. 缓存构建信息
 * 3. 提供格式化输出
 * @author mumu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildInfoService {

    private static final String DEFAULT_GIT_FILE = "git.properties";

    private final BuildInfoProperties buildInfoProperties;

    /**
     * 获取构建信息（缓存）
     */
    public BuildInfo getBuildInfo() {
        return loadBuildInfo();
    }

    /**
     * 实际加载逻辑（仅启动时调用）
     */
    private BuildInfo loadBuildInfo() {
        Properties p = new Properties();
        String file = DEFAULT_GIT_FILE;

        try (InputStream in = ResourceUtil.getStream(file)) {
            if (in != null) {
                p.load(in);
                log.info("构建信息已加载: {}", file);
            } else {
                log.warn("构建信息文件不存在: {}", file);
            }
        } catch (IOException e) {
            log.error("加载构建信息失败: {}", file, e);
        }

        return BuildInfo.builder()
            .buildTime(get(p, "git.build.time", "UNKNOWN"))
            .buildUser(get(p, "git.build.user.name", "UNKNOWN"))
            .buildHost(get(p, "git.build.host", "UNKNOWN"))
            .buildVersion(get(p, "git.build.version", "UNKNOWN"))
            .gitBranch(get(p, "git.branch", "UNKNOWN"))
            .gitCommitId(get(p, "git.commit.id.full", "UNKNOWN"))
            .gitCommitIdAbbrev(get(p, "git.commit.id.abbrev", "UNKNOWN"))
            .gitCommitTime(get(p, "git.commit.time", "UNKNOWN"))
            .gitCommitMessage(get(p, "git.commit.message.full", ""))
            .gitCommitMessageShort(get(p, "git.commit.message.short", ""))
            .gitCommitUserName(get(p, "git.commit.user.name", ""))
            .gitCommitUserEmail(maskEmail(get(p, "git.commit.user.email", "")))
            .gitTags(get(p, "git.tags", ""))
            .gitTotalCommitCount(get(p, "git.total.commit.count", "0"))
            .gitDirty(get(p, "git.dirty", "false"))
            .gitRemoteOriginUrl(
                sanitizeRemoteUrl(get(p, "git.remote.origin.url", ""))
            )
            .build();
    }

    /**
     * Banner 格式输出
     */
    public String formatBuildInfoBanner() {
        BuildInfo i = getBuildInfo();
        if (i == null) {
            return "[BuildInfo] NOT AVAILABLE";
        }

        return """
        ====================================================================
                              Woodlin Build Info
        ====================================================================
          Build Time : %s
          Build User : %s
          Build Host : %s
          Version    : %s
        --------------------------------------------------------------------
          Branch     : %s
          Commit     : %s
          CommitTime : %s
          Message    : %s
          Dirty      : %s
        ====================================================================
        """.formatted(
            i.getBuildTime(),
            i.getBuildUser(),
            i.getBuildHost(),
            i.getBuildVersion(),
            i.getGitBranch(),
            i.getGitCommitIdAbbrev(),
            i.getGitCommitTime(),
            i.getGitCommitMessageShort(),
            Boolean.parseBoolean(i.getGitDirty()) ? "YES" : "NO"
        );
    }

    /**
     * 简要格式
     */
    public String formatBuildInfoSimple() {
        BuildInfo i = getBuildInfo();
        if (i == null) {
            return "[BuildInfo] NOT AVAILABLE";
        }

        return "[Build] %s | %s | %s"
            .formatted(i.getBuildVersion(), i.getGitBranch(), i.getGitCommitIdAbbrev());
    }

    /* ========================== private helper ========================== */

    private static String get(Properties p, String key, String def) {
        return p.getProperty(key, def);
    }

    /**
     * 清理 URL 中的认证信息
     */
    private static String sanitizeRemoteUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return url.replaceAll("(https?://)([^:@]+:[^@]+@)", "$1");
    }

    /**
     * 简单脱敏邮箱
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        return email.replaceAll("(.).+(@.+)", "$1***$2");
    }
}
