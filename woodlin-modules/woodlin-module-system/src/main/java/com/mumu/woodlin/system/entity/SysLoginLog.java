package com.mumu.woodlin.system.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录日志实体
 *
 * @author yulin
 * @description 登录日志实体类，记录用户登录情况，仅追加写入，支持逻辑删除
 * @since 2026-06
 */
@Data
@Accessors(chain = true)
@TableName("sys_login_log")
@Schema(description = "登录日志")
public class SysLoginLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    @TableId(value = "login_id", type = IdType.ASSIGN_ID)
    @Schema(description = "日志主键")
    private Long loginId;

    /**
     * 用户名
     */
    @TableField("username")
    @Schema(description = "用户名")
    private String username;

    /**
     * 登录IP地址
     */
    @TableField("ipaddr")
    @Schema(description = "登录IP")
    private String ipaddr;

    /**
     * 登录地点
     */
    @TableField("login_location")
    @Schema(description = "登录地点")
    private String loginLocation;

    /**
     * 浏览器
     */
    @TableField("browser")
    @Schema(description = "浏览器")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    @Schema(description = "操作系统")
    private String os;

    /**
     * 提示消息
     */
    @TableField("msg")
    @Schema(description = "登录信息")
    private String msg;

    /**
     * 登录状态（0-成功，1-失败）
     */
    @TableField("status")
    @Schema(description = "登录状态（0-成功，1-失败）")
    private String status;

    /**
     * 登录时间
     */
    @TableField(value = "login_time", fill = FieldFill.INSERT)
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 删除标识（0-正常，1-删除）
     */
    @TableField("deleted")
    @TableLogic
    @Schema(description = "删除标识")
    private String deleted;
}
