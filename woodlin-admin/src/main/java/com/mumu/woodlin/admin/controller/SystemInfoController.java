package com.mumu.woodlin.admin.controller;

import com.mumu.woodlin.common.constant.SystemConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统信息控制器
 *
 * @author mumu
 * @description 提供系统版本、构建信息等查询接口
 * @since 2025-01-01
 */
@Tag(name = "系统信息", description = "系统版本和构建信息查询接口")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemInfoController {

    private final Environment environment;

    /**
     * 获取系统版本信息
     */
    @Operation(summary = "获取系统版本信息", description = "获取系统的详细版本和构建信息")
    @GetMapping("/version")
    public SystemVersionInfo getSystemVersion() {
        SystemVersionInfo versionInfo = new SystemVersionInfo();
        
        versionInfo.setSystemName(SystemConstant.SYSTEM_NAME);
        versionInfo.setSystemVersion(SystemConstant.SYSTEM_VERSION);
        versionInfo.setBuildTime(getBuildTime());
        versionInfo.setGitCommitId(getGitCommitId());
        versionInfo.setBuildProfile(getBuildProfile());
        
        versionInfo.setJavaVersion(System.getProperty("java.version"));
        versionInfo.setSpringProfile(String.join(",", environment.getActiveProfiles()));
        versionInfo.setCurrentTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return versionInfo;
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
     * 系统版本信息响应类
     */
    @Data
    public static class SystemVersionInfo {
        private String systemName;
        private String systemVersion;
        private String buildTime;
        private String gitCommitId;
        private String buildProfile;
        private String javaVersion;
        private String springProfile;
        private String currentTime;
    }
}