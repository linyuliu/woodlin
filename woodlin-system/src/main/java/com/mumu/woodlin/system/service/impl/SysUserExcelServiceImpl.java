package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.mumu.woodlin.system.dto.SysUserExcelDto;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysUserExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Excel处理服务实现类
 * 
 * @author mumu
 * @description 用户Excel导入导出处理服务实现
 * @since 2025-01-01
 */
@Slf4j
@Service
public class SysUserExcelServiceImpl implements ISysUserExcelService {
    
    /**
     * 将用户实体转换为Excel DTO
     * 
     * @param user 用户实体
     * @return Excel DTO
     */
    @Override
    public SysUserExcelDto convertToExcelDto(SysUser user) {
        SysUserExcelDto dto = BeanUtil.copyProperties(user, SysUserExcelDto.class);
        
        // 转换性别
        Gender gender = Gender.fromCode(user.getGender());
        dto.setGenderText(gender.getDescription());
        
        // 转换状态
        UserStatus status = UserStatus.fromCode(user.getStatus());
        dto.setStatusText(status.getDescription());
        
        return dto;
    }
    
    /**
     * 将Excel DTO转换为用户实体
     * 
     * @param dto Excel DTO
     * @return 用户实体
     */
    @Override
    public SysUser convertFromExcelDto(SysUserExcelDto dto) {
        SysUser user = BeanUtil.copyProperties(dto, SysUser.class);
        
        // 转换性别
        Gender gender = Gender.fromDescription(dto.getGenderText());
        user.setGender(gender.getCode());
        
        // 转换状态
        UserStatus status = UserStatus.fromDescription(dto.getStatusText());
        user.setStatus(status.getCode());
        
        return user;
    }
    
    /**
     * 批量转换用户实体为Excel DTO
     * 
     * @param userList 用户实体列表
     * @return Excel DTO列表
     */
    @Override
    public List<SysUserExcelDto> convertToExcelDtoList(List<SysUser> userList) {
        return userList.stream()
                .map(this::convertToExcelDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 批量转换Excel DTO为用户实体
     * 
     * @param dtoList Excel DTO列表
     * @return 用户实体列表
     */
    @Override
    public List<SysUser> convertFromExcelDtoList(List<SysUserExcelDto> dtoList) {
        return dtoList.stream()
                .map(this::convertFromExcelDto)
                .collect(Collectors.toList());
    }
}