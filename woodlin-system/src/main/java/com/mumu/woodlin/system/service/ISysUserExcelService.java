package com.mumu.woodlin.system.service;

import com.mumu.woodlin.system.dto.SysUserExcelDto;
import com.mumu.woodlin.system.entity.SysUser;

import java.util.List;

/**
 * 用户Excel处理服务接口
 * 
 * @author mumu
 * @description 用户Excel导入导出处理服务
 * @since 2025-01-01
 */
public interface ISysUserExcelService {
    
    /**
     * 将用户实体转换为Excel DTO
     * 
     * @param user 用户实体
     * @return Excel DTO
     */
    SysUserExcelDto convertToExcelDto(SysUser user);
    
    /**
     * 将Excel DTO转换为用户实体
     * 
     * @param dto Excel DTO
     * @return 用户实体
     */
    SysUser convertFromExcelDto(SysUserExcelDto dto);
    
    /**
     * 批量转换用户实体为Excel DTO
     * 
     * @param userList 用户实体列表
     * @return Excel DTO列表
     */
    List<SysUserExcelDto> convertToExcelDtoList(List<SysUser> userList);
    
    /**
     * 批量转换Excel DTO为用户实体
     * 
     * @param dtoList Excel DTO列表
     * @return 用户实体列表
     */
    List<SysUser> convertFromExcelDtoList(List<SysUserExcelDto> dtoList);
}