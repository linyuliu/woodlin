package com.mumu.woodlin.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 构建信息实体
 * 
 * @author mumu
 * @description 记录应用构建时的Git信息，用于版本追踪和问题诊断
 * @since 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "构建信息")
public class BuildInfo implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 构建时间
     */
    @Schema(description = "构建时间")
    private String buildTime;
    
    /**
     * 构建用户
     */
    @Schema(description = "构建用户")
    private String buildUser;
    
    /**
     * 构建主机
     */
    @Schema(description = "构建主机")
    private String buildHost;
    
    /**
     * 构建版本
     */
    @Schema(description = "构建版本")
    private String buildVersion;
    
    /**
     * Git分支
     */
    @Schema(description = "Git分支")
    private String gitBranch;
    
    /**
     * Git提交ID（完整）
     */
    @Schema(description = "Git提交ID（完整）")
    private String gitCommitId;
    
    /**
     * Git提交ID（缩写）
     */
    @Schema(description = "Git提交ID（缩写）")
    private String gitCommitIdAbbrev;
    
    /**
     * Git提交时间
     */
    @Schema(description = "Git提交时间")
    private String gitCommitTime;
    
    /**
     * Git提交信息（完整）
     */
    @Schema(description = "Git提交信息（完整）")
    private String gitCommitMessage;
    
    /**
     * Git提交信息（简短）
     */
    @Schema(description = "Git提交信息（简短）")
    private String gitCommitMessageShort;
    
    /**
     * Git提交用户名
     */
    @Schema(description = "Git提交用户名")
    private String gitCommitUserName;
    
    /**
     * Git提交用户邮箱
     */
    @Schema(description = "Git提交用户邮箱")
    private String gitCommitUserEmail;
    
    /**
     * Git标签
     */
    @Schema(description = "Git标签")
    private String gitTags;
    
    /**
     * Git提交总数
     */
    @Schema(description = "Git提交总数")
    private String gitTotalCommitCount;
    
    /**
     * Git仓库是否有未提交的更改
     */
    @Schema(description = "Git仓库是否有未提交的更改")
    private String gitDirty;
    
    /**
     * Git远程仓库URL
     */
    @Schema(description = "Git远程仓库URL")
    private String gitRemoteOriginUrl;
}
