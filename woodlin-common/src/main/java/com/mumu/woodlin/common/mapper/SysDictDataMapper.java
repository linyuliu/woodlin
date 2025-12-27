package com.mumu.woodlin.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.common.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据Mapper接口
 *
 * @author mumu
 * @description 字典数据访问层接口，基于MyBatis Plus实现
 * @since 2025-12-27
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDictData> selectByDictType(@Param("dictType") String dictType);

    /**
     * 根据字典类型和租户ID查询字典数据列表
     *
     * @param dictType 字典类型
     * @param tenantId 租户ID
     * @return 字典数据列表
     */
    List<SysDictData> selectByDictTypeAndTenant(@Param("dictType") String dictType, @Param("tenantId") String tenantId);

    /**
     * 根据字典类型和字典值查询字典数据
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @return 字典数据
     */
    SysDictData selectByTypeAndValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue);
}
