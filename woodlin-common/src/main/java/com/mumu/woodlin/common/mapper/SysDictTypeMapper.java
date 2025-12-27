package com.mumu.woodlin.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.common.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 字典类型Mapper接口
 *
 * @author mumu
 * @description 字典类型数据访问层接口，基于MyBatis Plus实现
 * @since 2025-12-27
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    /**
     * 根据字典类型查询字典类型信息
     *
     * @param dictType 字典类型
     * @return 字典类型信息
     */
    SysDictType selectByDictType(@Param("dictType") String dictType);

    /**
     * 根据字典类型和租户ID查询字典类型信息
     *
     * @param dictType 字典类型
     * @param tenantId 租户ID
     * @return 字典类型信息
     */
    SysDictType selectByDictTypeAndTenant(@Param("dictType") String dictType, @Param("tenantId") String tenantId);
}
