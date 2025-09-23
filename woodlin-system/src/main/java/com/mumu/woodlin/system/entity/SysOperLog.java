package com.mumu.woodlin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 
 * @author mumu
 * @description 操作日志实体类，用于审计跟踪和操作记录
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oper_log")
@Schema(description = "操作日志")
public class SysOperLog extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 日志主键
     */
    @TableId(value = "oper_id", type = IdType.ASSIGN_ID)
    @Schema(description = "日志主键")
    private Long operId;
    
    /**
     * 模块标题
     */
    @TableField("title")
    @Schema(description = "模块标题")
    private String title;
    
    /**
     * 业务类型（0-其它，1-新增，2-修改，3-删除）
     */
    @TableField("business_type")
    @Schema(description = "业务类型")
    private Integer businessType;
    
    /**
     * 方法名称
     */
    @TableField("method")
    @Schema(description = "方法名称")
    private String method;
    
    /**
     * 请求方式
     */
    @TableField("request_method")
    @Schema(description = "请求方式")
    private String requestMethod;
    
    /**
     * 请求URL
     */
    @TableField("oper_url")
    @Schema(description = "请求URL")
    private String operUrl;
    
    /**
     * 主机地址
     */
    @TableField("oper_ip")
    @Schema(description = "主机地址")
    private String operIp;
    
    /**
     * 操作地点
     */
    @TableField("oper_location")
    @Schema(description = "操作地点")
    private String operLocation;
    
    /**
     * 请求参数
     */
    @TableField("oper_param")
    @Schema(description = "请求参数")
    private String operParam;
    
    /**
     * 返回参数
     */
    @TableField("json_result")
    @Schema(description = "返回参数")
    private String jsonResult;
    
    /**
     * 操作状态（0-正常，1-异常）
     */
    @TableField("status")
    @Schema(description = "操作状态")
    private Integer status;
    
    /**
     * 错误消息
     */
    @TableField("error_msg")
    @Schema(description = "错误消息")
    private String errorMsg;
    
    /**
     * 操作时间
     */
    @TableField("oper_time")
    @Schema(description = "操作时间")
    private LocalDateTime operTime;
    
    /**
     * 消耗时间
     */
    @TableField("cost_time")
    @Schema(description = "消耗时间")
    private Long costTime;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}