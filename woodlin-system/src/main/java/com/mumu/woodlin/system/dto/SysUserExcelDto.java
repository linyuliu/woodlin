package com.mumu.woodlin.system.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户导出Excel DTO
 * 
 * @author mumu
 * @description 用户导出Excel数据传输对象
 * @since 2025-01-01
 */
@Data
@Schema(description = "用户导出Excel DTO")
public class SysUserExcelDto {
    
    /**
     * 用户ID
     */
    @ExcelProperty("用户ID")
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 用户名
     */
    @ExcelProperty("用户名")
    @Schema(description = "用户名")
    private String username;
    
    /**
     * 用户昵称
     */
    @ExcelProperty("用户昵称")
    @Schema(description = "用户昵称")
    private String nickname;
    
    /**
     * 真实姓名
     */
    @ExcelProperty("真实姓名")
    @Schema(description = "真实姓名")
    private String realName;
    
    /**
     * 邮箱
     */
    @ExcelProperty("邮箱")
    @Schema(description = "邮箱")
    private String email;
    
    /**
     * 手机号
     */
    @ExcelProperty("手机号")
    @Schema(description = "手机号")
    private String mobile;
    
    /**
     * 性别（1-男，2-女，0-未知）
     */
    @ExcelProperty("性别")
    @Schema(description = "性别")
    private String genderText;
    
    /**
     * 用户状态（1-启用，0-禁用）
     */
    @ExcelProperty("状态")
    @Schema(description = "状态")
    private String statusText;
    
    /**
     * 部门名称
     */
    @ExcelProperty("部门")
    @Schema(description = "部门名称")
    private String deptName;
    
    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}