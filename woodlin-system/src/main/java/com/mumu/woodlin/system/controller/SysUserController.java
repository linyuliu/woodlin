package com.mumu.woodlin.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.Result;
import com.mumu.woodlin.system.dto.SysUserExcelDto;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * 
 * @author mumu
 * @description 用户管理控制器，提供用户的增删改查和Excel导入导出功能
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户管理相关接口")
public class SysUserController {
    
    private final ISysUserService userService;
    
    /**
     * 分页查询用户列表
     * 
     * @param user 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表")
    public Result<PageResult<SysUser>> list(
            SysUser user,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        IPage<SysUser> page = userService.selectUserPage(user, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }
    
    /**
     * 根据用户ID获取详细信息
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "根据用户ID获取详细信息")
    public Result<SysUser> getInfo(@Parameter(description = "用户ID") @PathVariable Long userId) {
        SysUser user = userService.getById(userId);
        return Result.success(user);
    }
    
    /**
     * 新增用户
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "新增用户")
    public Result<Void> add(@Valid @RequestBody SysUser user) {
        boolean result = userService.insertUser(user);
        return result ? Result.success() : Result.fail("新增用户失败");
    }
    
    /**
     * 修改用户
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "修改用户")
    public Result<Void> edit(@Valid @RequestBody SysUser user) {
        boolean result = userService.updateUser(user);
        return result ? Result.success() : Result.fail("修改用户失败");
    }
    
    /**
     * 删除用户
     * 
     * @param userIds 用户ID数组
     * @return 操作结果
     */
    @DeleteMapping("/{userIds}")
    @Operation(summary = "删除用户")
    public Result<Void> remove(@Parameter(description = "用户ID，多个用逗号分隔") @PathVariable String userIds) {
        List<String> ids = Arrays.asList(userIds.split(","));
        boolean result = userService.removeByIds(ids);
        return result ? Result.success() : Result.fail("删除用户失败");
    }
    
    /**
     * 重置密码
     * 
     * @param userId 用户ID
     * @param password 新密码
     * @return 操作结果
     */
    @PutMapping("/resetPwd")
    @Operation(summary = "重置密码")
    public Result<Void> resetPwd(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "新密码") @RequestParam String password) {
        boolean result = userService.resetPassword(userId, password);
        return result ? Result.success() : Result.fail("重置密码失败");
    }
    
    /**
     * 状态修改
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    @PutMapping("/changeStatus")
    @Operation(summary = "状态修改")
    public Result<Void> changeStatus(@RequestBody SysUser user) {
        boolean result = userService.updateById(user);
        return result ? Result.success() : Result.fail("修改状态失败");
    }
    
    /**
     * 导出用户数据
     * 
     * @param response HTTP响应
     * @param user 查询条件
     */
    @PostMapping("/export")
    @Operation(summary = "导出用户数据")
    public void export(HttpServletResponse response, @RequestBody SysUser user) throws IOException {
        // 查询数据
        List<SysUser> userList = userService.list();
        
        // 转换为导出DTO
        List<SysUserExcelDto> excelList = userList.stream()
                .map(this::convertToExcelDto)
                .collect(Collectors.toList());
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), SysUserExcelDto.class)
                .sheet("用户数据")
                .doWrite((List<?>) excelList);
    }
    
    /**
     * 导入用户数据
     * 
     * @param file Excel文件
     * @param updateSupport 是否更新支持
     * @return 操作结果
     */
    @PostMapping("/importData")
    @Operation(summary = "导入用户数据")
    public Result<String> importData(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "是否更新支持") @RequestParam(defaultValue = "false") Boolean updateSupport) throws IOException {
        
        // 读取Excel数据
        List<SysUserExcelDto> excelList = EasyExcel.read(file.getInputStream())
                .head(SysUserExcelDto.class)
                .sheet()
                .doReadSync();
        
        // 转换为实体对象
        List<SysUser> userList = excelList.stream()
                .map(this::convertFromExcelDto)
                .collect(Collectors.toList());
        
        // 导入数据
        String message = userService.importUser(userList, updateSupport);
        return Result.success(message);
    }
    
    /**
     * 下载导入模板
     * 
     * @param response HTTP响应
     */
    @PostMapping("/importTemplate")
    @Operation(summary = "下载导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 导出模板
        EasyExcel.write(response.getOutputStream(), SysUserExcelDto.class)
                .sheet("用户数据")
                .doWrite((List<?>) null);
    }
    
    /**
     * 转换为Excel导出DTO
     * 
     * @param user 用户实体
     * @return Excel DTO
     */
    private SysUserExcelDto convertToExcelDto(SysUser user) {
        SysUserExcelDto dto = BeanUtil.copyProperties(user, SysUserExcelDto.class);
        
        // 转换性别
        if (user.getGender() != null) {
            switch (user.getGender()) {
                case 1:
                    dto.setGenderText("男");
                    break;
                case 2:
                    dto.setGenderText("女");
                    break;
                default:
                    dto.setGenderText("未知");
                    break;
            }
        }
        
        // 转换状态
        if ("1".equals(user.getStatus())) {
            dto.setStatusText("启用");
        } else {
            dto.setStatusText("禁用");
        }
        
        return dto;
    }
    
    /**
     * 从Excel DTO转换为实体
     * 
     * @param dto Excel DTO
     * @return 用户实体
     */
    private SysUser convertFromExcelDto(SysUserExcelDto dto) {
        SysUser user = BeanUtil.copyProperties(dto, SysUser.class);
        
        // 转换性别
        if (dto.getGenderText() != null) {
            switch (dto.getGenderText()) {
                case "男":
                    user.setGender(1);
                    break;
                case "女":
                    user.setGender(2);
                    break;
                default:
                    user.setGender(0);
                    break;
            }
        }
        
        // 转换状态
        if ("启用".equals(dto.getStatusText())) {
            user.setStatus("1");
        } else {
            user.setStatus("0");
        }
        
        return user;
    }
}