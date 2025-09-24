package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 用户业务服务接口
 * 
 * @author mumu
 * @description 用户业务服务接口，提供完整的用户管理业务功能
 * @since 2025-01-01
 */
public interface ISysUserBusinessService extends IService<SysUser> {
    
    /**
     * 分页查询用户列表
     * 
     * @param user 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户分页列表
     */
    IPage<SysUser> queryUserPage(SysUser user, Integer pageNum, Integer pageSize);
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUser getUserById(Long userId);
    
    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean createUser(SysUser user);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(SysUser user);
    
    /**
     * 批量删除用户
     * 
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    boolean deleteUsers(List<String> userIds);
    
    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetUserPassword(Long userId, String newPassword);
    
    /**
     * 更改用户状态
     * 
     * @param user 用户信息（包含ID和状态）
     * @return 是否成功
     */
    boolean changeUserStatus(SysUser user);
    
    /**
     * 导出用户数据
     * 
     * @param response HTTP响应
     * @param user 查询条件
     * @throws IOException IO异常
     */
    void exportUsers(HttpServletResponse response, SysUser user) throws IOException;
    
    /**
     * 导入用户数据
     * 
     * @param file Excel文件
     * @param updateSupport 是否支持更新
     * @return 导入结果消息
     * @throws IOException IO异常
     */
    String importUsers(MultipartFile file, Boolean updateSupport) throws IOException;
    
    /**
     * 下载导入模板
     * 
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;
}