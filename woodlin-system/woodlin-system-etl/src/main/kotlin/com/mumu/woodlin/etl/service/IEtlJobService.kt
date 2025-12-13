package com.mumu.woodlin.etl.service

import com.baomidou.mybatisplus.extension.service.IService
import com.mumu.woodlin.etl.entity.EtlJob

/**
 * ETL任务服务接口
 *
 * 提供ETL任务的生命周期管理功能，包括创建、更新、删除、执行等操作。
 * 与任务调度系统集成，支持Cron表达式调度和手动触发。
 *
 * ## 主要功能
 * - 任务CRUD操作
 * - 任务启用/禁用控制
 * - 手动立即执行
 * - 与调度系统集成
 * - 租户隔离支持
 *
 * @author mumu
 * @since 2025-01-01
 */
interface IEtlJobService : IService<EtlJob> {

    /**
     * 创建ETL任务
     *
     * 创建新的ETL任务并保存到数据库。如果任务状态为启用，
     * 会自动在调度系统中注册定时任务。
     *
     * @param etlJob ETL任务配置信息
     * @return 是否创建成功
     */
    fun createJob(etlJob: EtlJob): Boolean

    /**
     * 更新ETL任务
     *
     * 更新已存在的ETL任务配置。如果状态或cron表达式发生变化，
     * 会自动更新调度系统中的任务配置。
     *
     * @param etlJob ETL任务配置信息（必须包含jobId）
     * @return 是否更新成功
     */
    fun updateJob(etlJob: EtlJob): Boolean

    /**
     * 删除ETL任务
     *
     * 从数据库中删除ETL任务，同时从调度系统中移除对应的定时任务。
     *
     * @param jobId 任务ID
     * @return 是否删除成功
     */
    fun deleteJob(jobId: Long): Boolean

    /**
     * 启用ETL任务
     *
     * 将任务状态设置为启用，并在调度系统中注册定时任务。
     * 启用后任务将按照cron表达式自动执行。
     *
     * @param jobId 任务ID
     * @return 是否启用成功
     */
    fun enableJob(jobId: Long): Boolean

    /**
     * 禁用ETL任务
     *
     * 将任务状态设置为禁用，并从调度系统中移除定时任务。
     * 禁用后任务不会自动执行，但可以手动触发。
     *
     * @param jobId 任务ID
     * @return 是否禁用成功
     */
    fun disableJob(jobId: Long): Boolean

    /**
     * 立即执行ETL任务
     *
     * 手动触发任务执行，不受任务启用状态限制。
     * 适用于测试、紧急数据同步等场景。
     *
     * @param jobId 任务ID
     * @return 是否提交执行成功（注意：返回true只表示任务已提交，不代表执行成功）
     */
    fun executeJob(jobId: Long): Boolean

    /**
     * 查询所有启用的ETL任务
     *
     * 返回状态为启用的所有ETL任务列表。
     * 可用于监控、统计等场景。
     *
     * @return 启用的ETL任务列表
     */
    fun listEnabledJobs(): List<EtlJob>

    /**
     * 根据租户ID查询ETL任务
     *
     * 返回指定租户的所有ETL任务列表。
     * 用于多租户环境下的数据隔离。
     *
     * @param tenantId 租户ID
     * @return 该租户的ETL任务列表
     */
    fun listJobsByTenantId(tenantId: String): List<EtlJob>
}
