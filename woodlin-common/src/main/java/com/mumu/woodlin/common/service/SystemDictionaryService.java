package com.mumu.woodlin.common.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.entity.SysDictData;
import com.mumu.woodlin.common.entity.SysDictType;
import com.mumu.woodlin.common.entity.SysRegion;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.mapper.SysDictDataMapper;
import com.mumu.woodlin.common.mapper.SysDictTypeMapper;
import com.mumu.woodlin.common.mapper.SysRegionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RedisCacheService redisCacheService;

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
     * 管理端查询字典类型列表（带过滤条件）
     */
    public List<SysDictType> listDictTypes(SysDictType query) {
        SysDictType filter = Optional.ofNullable(query).orElse(new SysDictType());
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDeleted, CommonConstant.DELETED_NO)
               .like(StrUtil.isNotBlank(filter.getDictName()), SysDictType::getDictName, filter.getDictName())
               .like(StrUtil.isNotBlank(filter.getDictType()), SysDictType::getDictType, filter.getDictType())
               .eq(StrUtil.isNotBlank(filter.getDictCategory()), SysDictType::getDictCategory, filter.getDictCategory())
               .eq(StrUtil.isNotBlank(filter.getStatus()), SysDictType::getStatus, filter.getStatus())
               .orderByAsc(SysDictType::getDictId);
        return dictTypeMapper.selectList(wrapper);
    }

    /**
     * 新增字典类型
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dict:types", "dict:type:list"}, allEntries = true)
    public SysDictType createDictType(SysDictType type) {
        validateDictTypeBasic(type);
        ensureDictTypeUnique(type.getDictType(), null);

        type.setDeleted(Optional.ofNullable(type.getDeleted()).orElse(CommonConstant.DELETED_NO));
        if (StrUtil.isBlank(type.getStatus())) {
            type.setStatus(CommonConstant.STATUS_ENABLE);
        }
        if (StrUtil.isBlank(type.getDictCategory())) {
            type.setDictCategory("custom");
        }
        type.setCreateTime(Optional.ofNullable(type.getCreateTime()).orElse(LocalDateTime.now()));

        dictTypeMapper.insert(type);
        evictDictCache(type.getDictType());
        return dictTypeMapper.selectById(type.getDictId());
    }

    /**
     * 更新字典类型
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dict:types", "dict:type:list"}, allEntries = true)
    public SysDictType updateDictType(SysDictType type) {
        if (type.getDictId() == null) {
            throw new BusinessException("字典类型ID不能为空");
        }
        SysDictType existing = dictTypeMapper.selectById(type.getDictId());
        if (existing == null) {
            throw new BusinessException("字典类型不存在");
        }

        validateDictTypeBasic(type);
        ensureDictTypeUnique(type.getDictType(), type.getDictId());

        type.setUpdateTime(LocalDateTime.now());
        dictTypeMapper.updateById(type);

        // 如果类型标识变化，需要同时清理旧、新缓存
        if (StrUtil.isNotBlank(existing.getDictType()) && !existing.getDictType().equals(type.getDictType())) {
            evictDictCache(existing.getDictType());
        }
        evictDictCache(type.getDictType());
        return dictTypeMapper.selectById(type.getDictId());
    }

    /**
     * 删除字典类型（逻辑删除，同时清理字典项）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dict:types", "dict:type:list", "dict:data"}, allEntries = true)
    public boolean removeDictType(Long dictId) {
        SysDictType existing = dictTypeMapper.selectById(dictId);
        if (existing == null) {
            return false;
        }

        LambdaUpdateWrapper<SysDictType> typeWrapper = new LambdaUpdateWrapper<>();
        typeWrapper.eq(SysDictType::getDictId, dictId)
            .set(SysDictType::getDeleted, CommonConstant.DELETED_YES)
            .set(SysDictType::getUpdateTime, LocalDateTime.now());
        dictTypeMapper.update(null, typeWrapper);

        LambdaUpdateWrapper<SysDictData> dataWrapper = new LambdaUpdateWrapper<>();
        dataWrapper.eq(SysDictData::getDictType, existing.getDictType())
            .set(SysDictData::getDeleted, CommonConstant.DELETED_YES)
            .set(SysDictData::getUpdateTime, LocalDateTime.now());
        dictDataMapper.update(null, dataWrapper);

        evictDictCache(existing.getDictType());
        return true;
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
            .toList();
    }

    /**
     * 管理端查询字典项（原始字段）
     */
    public List<SysDictData> listDictData(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(dictType), SysDictData::getDictType, dictType)
               .eq(SysDictData::getDeleted, CommonConstant.DELETED_NO)
               .orderByAsc(SysDictData::getDictSort)
               .orderByAsc(SysDictData::getDataId);
        return dictDataMapper.selectList(wrapper);
    }

    /**
     * 新增字典项
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:data", key = "#data.dictType")
    public SysDictData createDictData(SysDictData data) {
        if (StrUtil.isBlank(data.getDictType())) {
            throw new BusinessException("字典类型不能为空");
        }
        if (StrUtil.isBlank(data.getDictLabel()) || StrUtil.isBlank(data.getDictValue())) {
            throw new BusinessException("字典标签和值不能为空");
        }
        ensureDictTypeExists(data.getDictType());
        ensureDictValueUnique(data.getDictType(), data.getDictValue(), null);

        data.setDeleted(Optional.ofNullable(data.getDeleted()).orElse(CommonConstant.DELETED_NO));
        if (data.getDictSort() == null) {
            data.setDictSort(0);
        }
        if (StrUtil.isBlank(data.getStatus())) {
            data.setStatus(CommonConstant.STATUS_ENABLE);
        }
        data.setCreateTime(Optional.ofNullable(data.getCreateTime()).orElse(LocalDateTime.now()));

        dictDataMapper.insert(data);
        evictDictCache(data.getDictType());
        return dictDataMapper.selectById(data.getDataId());
    }

    /**
     * 更新字典项
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:data", key = "#data.dictType")
    public SysDictData updateDictData(SysDictData data) {
        if (data.getDataId() == null) {
            throw new BusinessException("字典项ID不能为空");
        }
        SysDictData existing = dictDataMapper.selectById(data.getDataId());
        if (existing == null) {
            throw new BusinessException("字典项不存在");
        }

        String dictType = StrUtil.isNotBlank(data.getDictType()) ? data.getDictType() : existing.getDictType();
        String dictValue = StrUtil.isNotBlank(data.getDictValue()) ? data.getDictValue() : existing.getDictValue();

        ensureDictTypeExists(dictType);
        ensureDictValueUnique(dictType, dictValue, data.getDataId());

        data.setDictType(dictType);
        data.setDictValue(dictValue);
        data.setUpdateTime(LocalDateTime.now());
        dictDataMapper.updateById(data);

        if (!existing.getDictType().equals(dictType)) {
            evictDictCache(existing.getDictType());
        }
        evictDictCache(dictType);
        return dictDataMapper.selectById(data.getDataId());
    }

    /**
     * 删除字典项（逻辑删除）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:data", allEntries = true)
    public boolean removeDictData(Long dataId) {
        SysDictData existing = dictDataMapper.selectById(dataId);
        if (existing == null) {
            return false;
        }

        LambdaUpdateWrapper<SysDictData> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysDictData::getDataId, dataId)
            .set(SysDictData::getDeleted, CommonConstant.DELETED_YES)
            .set(SysDictData::getUpdateTime, LocalDateTime.now());
        dictDataMapper.update(null, wrapper);

        evictDictCache(existing.getDictType());
        return true;
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
            .toList();
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
            .toList();
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
            .toList();
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
                .toList());
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

    private void validateDictTypeBasic(SysDictType type) {
        if (type == null) {
            throw new BusinessException("字典类型参数不能为空");
        }
        if (StrUtil.isBlank(type.getDictName())) {
            throw new BusinessException("字典名称不能为空");
        }
        if (StrUtil.isBlank(type.getDictType())) {
            throw new BusinessException("字典类型不能为空");
        }
    }

    private void ensureDictTypeUnique(String dictType, Long dictId) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictType, dictType)
               .eq(SysDictType::getDeleted, CommonConstant.DELETED_NO)
               .ne(dictId != null, SysDictType::getDictId, dictId);
        if (dictTypeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("字典类型已存在: " + dictType);
        }
    }

    private void ensureDictTypeExists(String dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictType, dictType)
               .eq(SysDictType::getDeleted, CommonConstant.DELETED_NO);
        if (dictTypeMapper.selectCount(wrapper) == 0) {
            throw new BusinessException("字典类型不存在: " + dictType);
        }
    }

    private void ensureDictValueUnique(String dictType, String dictValue, Long dataId) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
               .eq(SysDictData::getDictValue, dictValue)
               .eq(SysDictData::getDeleted, CommonConstant.DELETED_NO)
               .ne(dataId != null, SysDictData::getDataId, dataId);
        if (dictDataMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("同一字典类型下值已存在: " + dictValue);
        }
    }

    private void evictDictCache(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return;
        }
        redisCacheService.evictDictionaryCache(dictType);
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
