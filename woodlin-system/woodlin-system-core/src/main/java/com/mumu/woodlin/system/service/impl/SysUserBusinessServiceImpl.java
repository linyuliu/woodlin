package com.mumu.woodlin.system.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.util.PageUtil;
import com.mumu.woodlin.system.dto.SysUserExcelDto;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.mapper.SysUserMapper;
import com.mumu.woodlin.system.service.ISysUserBusinessService;
import com.mumu.woodlin.system.service.ISysUserExcelService;
import com.mumu.woodlin.system.service.ISysUserService;
import com.mumu.woodlin.system.service.ISysUserRoleService;

/**
 * 用户业务服务实现类
 * 
 * @author mumu
 * @description 用户业务服务实现类，提供完整的用户管理业务功能
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserBusinessServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserBusinessService {
    
    private final ISysUserService userService;
    private final ISysUserExcelService excelService;
    private final ISysUserRoleService userRoleService;
    
    /**
     * 权限缓存服务（可选依赖）
     */
    @Autowired(required = false)
    private com.mumu.woodlin.security.service.PermissionCacheService permissionCacheService;
    
    /**
     * 分页查询用户列表
     */
    @Override
    public IPage<SysUser> queryUserPage(SysUser user, Integer pageNum, Integer pageSize) {
        // 分页参数校验和安全处理
        pageNum = PageUtil.getSafePageNum(pageNum);
        pageSize = PageUtil.getSafePageSize(pageSize);
        
        return userService.selectUserPage(user, pageNum, pageSize);
    }
    
    /**
     * 根据用户ID获取用户信息
     */
    @Override
    public SysUser getUserById(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "用户ID不能为空");
        }
        
        SysUser user = userService.getById(userId);
        if (ObjectUtil.isNull(user)) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 查询用户的角色ID列表
        List<Long> roleIds = userRoleService.selectRoleIdsByUserId(userId);
        user.setRoleIds(roleIds);
        
        return user;
    }
    
    /**
     * 创建用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user) {
        if (ObjectUtil.isNull(user)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "用户信息不能为空");
        }
        
        // 创建用户
        boolean result = userService.insertUser(user);
        
        // 保存用户角色关联
        if (result && ObjectUtil.isNotEmpty(user.getRoleIds())) {
            userRoleService.saveUserRoles(user.getUserId(), user.getRoleIds());
            log.info("用户 {} 创建成功，已分配 {} 个角色", user.getUsername(), user.getRoleIds().size());
        } else {
            log.info("用户 {} 创建成功，未分配角色", user.getUsername());
        }
        
        return result;
    }
    
    /**
     * 更新用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        if (ObjectUtil.isNull(user) || ObjectUtil.isNull(user.getUserId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "用户信息或用户ID不能为空");
        }
        
        // 验证用户存在
        SysUser existUser = getUserById(user.getUserId());
        
        // 更新用户基本信息
        boolean result = userService.updateUser(user);
        
        // 更新用户角色关联
        if (result && user.getRoleIds() != null) {
            userRoleService.saveUserRoles(user.getUserId(), user.getRoleIds());
            log.info("用户 {} 更新成功，已分配 {} 个角色", user.getUsername(), user.getRoleIds().size());
        }
        
        // 清除用户缓存
        if (result && permissionCacheService != null) {
            permissionCacheService.evictUserCache(user.getUserId());
            log.info("已清除用户 {} 的权限缓存", user.getUserId());
        }
        
        return result;
    }
    
    /**
     * 批量删除用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<String> userIds) {
        if (ObjectUtil.isEmpty(userIds)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "用户ID列表不能为空");
        }
        
        // 删除用户角色关联
        for (String userIdStr : userIds) {
            Long userId = Long.parseLong(userIdStr);
            userRoleService.deleteUserRoles(userId);
            
            // 清除用户缓存
            if (permissionCacheService != null) {
                permissionCacheService.evictUserCache(userId);
                log.info("已清除用户 {} 的权限缓存", userId);
            }
        }
        
        // 删除用户
        return userService.removeByIds(userIds);
    }
    
    /**
     * 重置用户密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserPassword(Long userId, String newPassword) {
        if (ObjectUtil.isNull(userId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "用户ID不能为空");
        }
        if (ObjectUtil.isEmpty(newPassword)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "新密码不能为空");
        }
        
        // 验证用户存在
        SysUser user = getUserById(userId);
        
        // 重置密码（会自动加密）
        boolean result = userService.resetPassword(userId, newPassword);
        
        // 清除用户缓存（强制重新登录）
        if (result && permissionCacheService != null) {
            permissionCacheService.evictUserCache(userId);
            log.info("用户 {} 密码重置成功，已清除缓存", user.getUsername());
        }
        
        return result;
    }
    
    /**
     * 更改用户状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeUserStatus(SysUser user) {
        if (ObjectUtil.isNull(user) || ObjectUtil.isNull(user.getUserId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "用户信息或用户ID不能为空");
        }
        
        // 验证用户存在
        getUserById(user.getUserId());
        
        // 更新用户状态
        boolean result = userService.updateById(user);
        
        // 清除用户缓存
        if (result && permissionCacheService != null) {
            permissionCacheService.evictUserCache(user.getUserId());
            log.info("用户 {} 状态修改成功，已清除缓存", user.getUserId());
        }
        
        return result;
    }
    
    /**
     * 导出用户数据
     */
    @Override
    public void exportUsers(HttpServletResponse response, SysUser user) throws IOException {
        try {
            log.info("开始导出用户数据，查询条件：{}", user);
            
            // 查询数据
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            // 根据查询条件构建查询
            if (ObjectUtil.isNotNull(user)) {
                queryWrapper.like(ObjectUtil.isNotEmpty(user.getUsername()), SysUser::getUsername, user.getUsername())
                           .like(ObjectUtil.isNotEmpty(user.getNickname()), SysUser::getNickname, user.getNickname())
                           .eq(ObjectUtil.isNotEmpty(user.getStatus()), SysUser::getStatus, user.getStatus());
            }
            
            List<SysUser> userList = this.list(queryWrapper);
            log.info("查询到{}条用户数据", userList.size());
            
            // 转换为导出DTO
            List<SysUserExcelDto> excelList = excelService.convertToExcelDtoList(userList);
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 导出Excel
            EasyExcel.write(response.getOutputStream(), SysUserExcelDto.class)
                    .sheet("用户数据")
                    .doWrite(excelList);
                    
            log.info("用户数据导出完成");
        } catch (Exception e) {
            log.error("导出用户数据失败", e);
            throw new BusinessException("导出用户数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 导入用户数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importUsers(MultipartFile file, Boolean updateSupport) throws IOException {
        if (ObjectUtil.isNull(file) || file.isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "导入文件不能为空");
        }
        
        try {
            log.info("开始导入用户数据，文件名：{}，是否支持更新：{}", file.getOriginalFilename(), updateSupport);
            
            // 读取Excel数据
            List<SysUserExcelDto> excelList = EasyExcel.read(file.getInputStream())
                    .head(SysUserExcelDto.class)
                    .sheet()
                    .doReadSync();
            
            if (ObjectUtil.isEmpty(excelList)) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "导入文件无有效数据");
            }
            
            log.info("读取到{}条用户数据", excelList.size());
            
            // 转换为实体对象
            List<SysUser> userList = excelService.convertFromExcelDtoList(excelList);
            
            // 导入数据
            String result = userService.importUser(userList, updateSupport);
            
            log.info("用户数据导入完成：{}", result);
            return result;
        } catch (Exception e) {
            log.error("导入用户数据失败", e);
            throw new BusinessException("导入用户数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载导入模板
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        try {
            log.info("开始下载用户导入模板");
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 导出模板
            EasyExcel.write(response.getOutputStream(), SysUserExcelDto.class)
                    .sheet("用户数据")
                    .doWrite(Collections.emptyList());
                    
            log.info("用户导入模板下载完成");
        } catch (Exception e) {
            log.error("下载用户导入模板失败", e);
            throw new BusinessException("下载模板失败：" + e.getMessage());
        }
    }
}