package com.mumu.woodlin.common.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.entity.SysRegion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 行政区划Mapper接口
 *
 * @author mumu
 * @description 行政区划数据访问层接口，基于MyBatis Plus实现，支持树形结构查询
 * @since 2025-12-27
 */
@Mapper
public interface SysRegionMapper extends BaseMapper<SysRegion> {

    /**
     * 根据父区划代码查询子区划列表
     *
     * @param parentCode 父区划代码
     * @return 子区划列表
     */
    List<SysRegion> selectByParentCode(@Param("parentCode") String parentCode);

    /**
     * 根据区划代码查询区划信息
     *
     * @param regionCode 区划代码
     * @return 区划信息
     */
    SysRegion selectByRegionCode(@Param("regionCode") String regionCode);

    /**
     * 查询指定层级的所有区划
     *
     * @param regionLevel 区划层级
     * @return 区划列表
     */
    List<SysRegion> selectByLevel(@Param("regionLevel") Integer regionLevel);

    /**
     * 查询所有省级行政区划
     *
     * @return 省级行政区划列表
     */
    default List<SysRegion> selectProvinces() {
        LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRegion::getRegionLevel, 1)
               .eq(SysRegion::getStatus, CommonConstant.STATUS_ENABLE)
               .eq(SysRegion::getDeleted, CommonConstant.DELETED_NO)
               .orderByAsc(SysRegion::getSortOrder);
        return selectList(wrapper);
    }
}
