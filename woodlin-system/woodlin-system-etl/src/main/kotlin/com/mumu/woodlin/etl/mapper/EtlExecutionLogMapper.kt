package com.mumu.woodlin.etl.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.mumu.woodlin.etl.entity.EtlExecutionLog
import org.apache.ibatis.annotations.Mapper

/**
 * ETL执行历史Mapper接口
 *
 * 提供ETL执行历史的数据库访问操作，基于MyBatis Plus的BaseMapper。
 * 用于查询任务执行记录、性能统计和问题排查。
 *
 * ## 可用方法（继承自BaseMapper）
 * - insert: 插入执行记录
 * - deleteById: 根据ID删除记录
 * - updateById: 更新执行记录
 * - selectById: 根据ID查询
 * - selectList: 条件查询列表
 * - selectPage: 分页查询历史记录
 *
 * @author mumu
 * @since 2025-01-01
 */
@Mapper
interface EtlExecutionLogMapper : BaseMapper<EtlExecutionLog>
