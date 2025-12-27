package com.mumu.woodlin.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.entity.SysDictData;
import com.mumu.woodlin.common.entity.SysDictType;
import com.mumu.woodlin.common.entity.SysRegion;
import com.mumu.woodlin.common.mapper.SysDictDataMapper;
import com.mumu.woodlin.common.mapper.SysDictTypeMapper;
import com.mumu.woodlin.common.mapper.SysRegionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统字典服务（重构版）
 *
 * @author mumu
 * @description 提供统一的动态字典数据访问，支持数据库驱动的字典管理
 *              采用"先查类型，再查数据"的设计模式
 *              所有字典遵循国家标准：
 *              - 性别：GB/T 2261.1-2003
 *              - 民族：GB/T 3304-1991
 *              - 婚姻状况：GB/T 2261.2-2003
 *              - 政治面貌：GB/T 4762-1984
 *              - 学历：GB/T 4658-2006
 *              - 行政区划：GB/T 2260
 * @since 2025-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemDictionaryService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final SysRegionMapper regionMapper;

    /**
     * 查询所有字典类型
     *
     * @return 字典类型列表
     */
    @Cacheable(value = "dict:types", unless = "#result == null || #result.isEmpty()")
    public List<SysDictType> getAllDictTypes() {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getStatus, CommonConstant.STATUS_ENABLE)
               .eq(SysDictType::getDeleted, CommonConstant.DELETED_NO)
               .orderByAsc(SysDictType::getDictId);
        return dictTypeMapper.selectList(wrapper);
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Cacheable(value = "dict:data", key = "#dictType", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getDictDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
               .eq(SysDictData::getStatus, CommonConstant.STATUS_ENABLE)
               .eq(SysDictData::getDeleted, CommonConstant.DELETED_NO)
               .orderByAsc(SysDictData::getDictSort);
        
        List<SysDictData> dataList = dictDataMapper.selectList(wrapper);
        
        return dataList.stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", data.getDictValue());
                    map.put("label", data.getDictLabel());
                    map.put("desc", data.getDictDesc());
                    map.put("sort", data.getDictSort());
                    map.put("cssClass", data.getCssClass());
                    map.put("listClass", data.getListClass());
                    map.put("isDefault", data.getIsDefault());
                    if (data.getExtraData() != null) {
                        map.put("extra", data.getExtraData());
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询行政区划树（完整树形结构）
     *
     * @return 行政区划树
     */
    @Cacheable(value = "dict:region:tree", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getRegionTree() {
        // 查询所有省级行政区划
        List<SysRegion> provinces = regionMapper.selectProvinces();
        
        return provinces.stream()
                .map(this::buildRegionNode)
                .collect(Collectors.toList());
    }

    /**
     * 根据父代码查询子区划列表
     *
     * @param parentCode 父区划代码（为空则查询省级）
     * @return 子区划列表
     */
    @Cacheable(value = "dict:region:children", key = "#parentCode == null ? 'root' : #parentCode", 
               unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getRegionChildren(String parentCode) {
        List<SysRegion> regions;
        
        if (parentCode == null || parentCode.isEmpty()) {
            // 查询省级行政区划
            regions = regionMapper.selectProvinces();
        } else {
            // 查询子区划
            LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysRegion::getParentCode, parentCode)
                   .eq(SysRegion::getStatus, CommonConstant.STATUS_ENABLE)
                   .eq(SysRegion::getDeleted, CommonConstant.DELETED_NO)
                   .orderByAsc(SysRegion::getSortOrder);
            regions = regionMapper.selectList(wrapper);
        }
        
        return regions.stream()
                .map(this::regionToMap)
                .collect(Collectors.toList());
    }

    /**
     * 查询省级行政区划
     *
     * @return 省级列表
     */
    @Cacheable(value = "dict:provinces", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getProvinces() {
        return getRegionChildren(null);
    }

    /**
     * 根据省级代码获取市级行政区划
     *
     * @param provinceCode 省级代码
     * @return 市级列表
     */
    @Cacheable(value = "dict:cities", key = "#provinceCode", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getCitiesByProvince(String provinceCode) {
        return getRegionChildren(provinceCode);
    }

    /**
     * 根据市级代码获取区县级行政区划
     *
     * @param cityCode 市级代码
     * @return 区县列表
     */
    @Cacheable(value = "dict:districts", key = "#cityCode", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getDistrictsByCity(String cityCode) {
        return getRegionChildren(cityCode);
    }

    /**
     * 获取所有字典类型（简化版，用于前端初始化）
     *
     * @return 字典类型列表
     */
    @Cacheable(value = "dict:type:list", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getDictTypeList() {
        List<SysDictType> types = getAllDictTypes();
        
        return types.stream()
                .map(type -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dictType", type.getDictType());
                    map.put("dictName", type.getDictName());
                    map.put("dictCategory", type.getDictCategory());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建区划树节点
     */
    private Map<String, Object> buildRegionNode(SysRegion region) {
        Map<String, Object> node = regionToMap(region);
        
        // 查询子节点
        LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRegion::getParentCode, region.getRegionCode())
               .eq(SysRegion::getStatus, CommonConstant.STATUS_ENABLE)
               .eq(SysRegion::getDeleted, CommonConstant.DELETED_NO)
               .orderByAsc(SysRegion::getSortOrder);
        List<SysRegion> children = regionMapper.selectList(wrapper);
        
        if (!children.isEmpty()) {
            node.put("children", children.stream()
                    .map(this::buildRegionNode)
                    .collect(Collectors.toList()));
        }
        
        return node;
    }

    /**
     * 将区划实体转换为Map
     */
    private Map<String, Object> regionToMap(SysRegion region) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", region.getRegionCode());
        map.put("name", region.getRegionName());
        map.put("parentCode", region.getParentCode());
        map.put("level", region.getRegionLevel());
        map.put("type", region.getRegionType());
        map.put("shortName", region.getShortName());
        map.put("pinyin", region.getPinyin());
        map.put("pinyinAbbr", region.getPinyinAbbr());
        if (region.getLongitude() != null) {
            map.put("longitude", region.getLongitude());
        }
        if (region.getLatitude() != null) {
            map.put("latitude", region.getLatitude());
        }
        map.put("isMunicipality", "1".equals(region.getIsMunicipality()));
        return map;
    }

    // ==================== 兼容性方法（保留旧接口，内部调用新方法） ====================

    /**
     * 获取性别字典（兼容旧接口）
     */
    public List<Map<String, Object>> getGenderDict() {
        return getDictDataByType("gender");
    }

    /**
     * 获取民族字典（兼容旧接口）
     */
    public List<Map<String, Object>> getEthnicityDict() {
        return getDictDataByType("ethnicity");
    }

    /**
     * 获取学历字典（兼容旧接口）
     */
    public List<Map<String, Object>> getEducationLevelDict() {
        return getDictDataByType("education");
    }

    /**
     * 获取婚姻状况字典（兼容旧接口）
     */
    public List<Map<String, Object>> getMaritalStatusDict() {
        return getDictDataByType("marital");
    }

    /**
     * 获取政治面貌字典（兼容旧接口）
     */
    public List<Map<String, Object>> getPoliticalStatusDict() {
        return getDictDataByType("political");
    }

    /**
     * 获取证件类型字典（兼容旧接口）
     */
    public List<Map<String, Object>> getIdTypeDict() {
        return getDictDataByType("idtype");
    }

    /**
     * 获取所有字典数据（兼容旧接口，已废弃，建议使用分步查询）
     *
     * @return 所有字典数据
     * @deprecated 建议使用 getDictTypeList() 和 getDictDataByType() 分步查询
     */
    @Deprecated
    public Map<String, Object> getAllDictionaries() {
        Map<String, Object> result = new HashMap<>();
        result.put("gender", getGenderDict());
        result.put("ethnicity", getEthnicityDict());
        result.put("education", getEducationLevelDict());
        result.put("marital", getMaritalStatusDict());
        result.put("political", getPoliticalStatusDict());
        result.put("idType", getIdTypeDict());
        result.put("provinces", getProvinces());
        return result;
    }
}
