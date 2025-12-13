package com.mumu.woodlin.etl.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.mumu.woodlin.etl.entity.EtlJob
import org.apache.ibatis.annotations.Mapper

/**
 * ETL任务Mapper接口
 *
 * 提供ETL任务的数据库访问操作，基于MyBatis Plus的BaseMapper。
 * 自动提供基础的CRUD操作，无需编写XML配置。
 *
 * ## 可用方法（继承自BaseMapper）
 * - insert: 插入记录
 * - deleteById: 根据ID删除
 * - updateById: 根据ID更新
 * - selectById: 根据ID查询
 * - selectList: 条件查询列表
 * - selectPage: 分页查询
 *
 * @author mumu
 * @since 2025-01-01
 */
@Mapper
interface EtlJobMapper : BaseMapper<EtlJob>
