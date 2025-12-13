package com.mumu.woodlin.common.service;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.entity.BuildInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 构建信息服务
 * 
 * @author mumu
 * @description 读取并提供应用构建信息，包括Git提交信息、构建时间等
 * @since 2025-01-01
 */
@Slf4j
@Service
public class BuildInfoService {
    
    private static final String GIT_PROPERTIES_FILE = "git.properties";
    private final BuildInfo buildInfo;
    
    /**
     * 构造函数，加载构建信息
     */
    public BuildInfoService() {
        this.buildInfo = loadBuildInfo();
    }
    
    /**
     * 获取构建信息
     * 
     * @return 构建信息对象
     */
    public BuildInfo getBuildInfo() {
        return buildInfo;
    }
    
    /**
     * 加载构建信息
     * 
     * @return 构建信息对象
     */
    private BuildInfo loadBuildInfo() {
        Properties properties = new Properties();
        
        try (InputStream inputStream = ResourceUtil.getStream(GIT_PROPERTIES_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                log.debug("成功加载构建信息: {}", GIT_PROPERTIES_FILE);
            } else {
                log.warn("未找到构建信息文件: {}", GIT_PROPERTIES_FILE);
            }
        } catch (IOException e) {
            log.error("加载构建信息失败: {}", e.getMessage(), e);
        }
        
        return BuildInfo.builder()
                .buildTime(properties.getProperty("git.build.time", "未知"))
                .buildUser(properties.getProperty("git.build.user.name", "未知"))
                .buildHost(properties.getProperty("git.build.host", "未知"))
                .buildVersion(properties.getProperty("git.build.version", "1.0.0"))
                .gitBranch(properties.getProperty("git.branch", "未知"))
                .gitCommitId(properties.getProperty("git.commit.id.full", "未知"))
                .gitCommitIdAbbrev(properties.getProperty("git.commit.id.abbrev", "未知"))
                .gitCommitTime(properties.getProperty("git.commit.time", "未知"))
                .gitCommitMessage(properties.getProperty("git.commit.message.full", "未知"))
                .gitCommitMessageShort(properties.getProperty("git.commit.message.short", "未知"))
                .gitCommitUserName(properties.getProperty("git.commit.user.name", "未知"))
                .gitCommitUserEmail(properties.getProperty("git.commit.user.email", "未知"))
                .gitTags(properties.getProperty("git.tags", ""))
                .gitTotalCommitCount(properties.getProperty("git.total.commit.count", "0"))
                .gitDirty(properties.getProperty("git.dirty", "false"))
                .gitRemoteOriginUrl(properties.getProperty("git.remote.origin.url", "未知"))
                .build();
    }
    
    /**
     * 格式化打印构建信息（Banner格式）
     * 
     * @return 格式化的构建信息字符串
     */
    public String formatBuildInfoBanner() {
        BuildInfo info = getBuildInfo();
        
        StringBuilder banner = new StringBuilder();
        banner.append("\n");
        banner.append("====================================================================\n");
        banner.append("                      Woodlin 构建信息                               \n");
        banner.append("====================================================================\n");
        banner.append(String.format("  构建时间:     %s%n", info.getBuildTime()));
        banner.append(String.format("  构建用户:     %s%n", info.getBuildUser()));
        banner.append(String.format("  构建主机:     %s%n", info.getBuildHost()));
        banner.append(String.format("  构建版本:     %s%n", info.getBuildVersion()));
        banner.append("--------------------------------------------------------------------\n");
        banner.append(String.format("  Git分支:      %s%n", info.getGitBranch()));
        banner.append(String.format("  Git提交ID:    %s%n", info.getGitCommitIdAbbrev()));
        banner.append(String.format("  Git提交时间:  %s%n", info.getGitCommitTime()));
        banner.append(String.format("  Git提交信息:  %s%n", info.getGitCommitMessageShort()));
        banner.append(String.format("  Git提交用户:  %s%n", info.getGitCommitUserName()));
        
        if (info.getGitTags() != null && !info.getGitTags().isEmpty()) {
            banner.append(String.format("  Git标签:      %s%n", info.getGitTags()));
        }
        
        banner.append(String.format("  Git提交总数:  %s%n", info.getGitTotalCommitCount()));
        banner.append(String.format("  工作区状态:   %s%n", "true".equals(info.getGitDirty()) ? "有未提交更改" : "干净"));
        banner.append("====================================================================\n");
        
        return banner.toString();
    }
    
    /**
     * 格式化打印构建信息（简单格式）
     * 
     * @return 格式化的构建信息字符串
     */
    public String formatBuildInfoSimple() {
        BuildInfo info = getBuildInfo();
        
        return String.format(
                "[构建信息] 时间: %s | 分支: %s | 提交: %s | 版本: %s",
                info.getBuildTime(),
                info.getGitBranch(),
                info.getGitCommitIdAbbrev(),
                info.getBuildVersion()
        );
    }
}
