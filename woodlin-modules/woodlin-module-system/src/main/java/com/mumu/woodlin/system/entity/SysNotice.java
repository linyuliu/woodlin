package com.mumu.woodlin.system.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 通知公告实体
 *
 * @author yulin
 * @description 系统通知公告信息实体类
 * @since 2026-06
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
@Schema(description = "通知公告")
public class SysNotice extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @TableId(value = "notice_id", type = IdType.ASSIGN_ID)
    @Schema(description = "公告ID")
    private Long noticeId;

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @TableField("notice_title")
    @Schema(description = "公告标题")
    private String noticeTitle;

    /**
     * 公告类型（1-通知，2-公告）
     */
    @TableField("notice_type")
    @Schema(description = "公告类型（1-通知，2-公告）")
    private String noticeType;

    /**
     * 公告内容
     */
    @TableField("notice_content")
    @Schema(description = "公告内容")
    private String noticeContent;

    /**
     * 公告状态（0-正常，1-关闭）
     */
    @TableField("status")
    @Schema(description = "公告状态（0-正常，1-关闭）")
    private String status;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
