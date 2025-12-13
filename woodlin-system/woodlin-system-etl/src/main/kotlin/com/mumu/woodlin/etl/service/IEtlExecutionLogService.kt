package com.mumu.woodlin.etl.service

import com.baomidou.mybatisplus.extension.service.IService
import com.mumu.woodlin.etl.entity.EtlExecutionLog
import com.mumu.woodlin.etl.entity.EtlJob

/**
 * ETL执行历史服务接口
 *
 * 提供ETL任务执行历史的记录和查询功能。
 * 每次任务执行都会生成一条执行记录，记录执行状态、数据统计、错误信息等。
 *
 * ## 主要功能
 * - 记录执行开始
 * - 记录执行成功/失败
 * - 查询执行历史
 * - 性能统计分析
 *
 * @author mumu
 * @since 2025-01-01
 */
interface IEtlExecutionLogService : IService<EtlExecutionLog> {

    /**
     * 记录任务执行开始
     *
     * 在任务开始执行时调用，创建执行日志记录。
     * 初始状态为RUNNING，数据统计初始化为0。
     *
     * @param job ETL任务配置
     * @return 执行日志ID，用于后续更新执行结果
     */
    fun recordExecutionStart(job: EtlJob): Long

    /**
     * 记录任务执行成功
     *
     * 在任务成功完成时调用，更新执行状态和数据统计。
     * 计算执行耗时，记录详细的执行信息。
     *
     * @param logId 执行日志ID
     * @param extractedRows 提取的记录数
     * @param transformedRows 转换的记录数
     * @param loadedRows 加载的记录数
     * @param executionDetail 执行详情（JSON格式），可包含批次信息、性能指标等
     */
    fun recordExecutionSuccess(
        logId: Long,
        extractedRows: Long,
        transformedRows: Long,
        loadedRows: Long,
        executionDetail: String
    )

    /**
     * 记录任务执行失败
     *
     * 在任务执行失败时调用，更新执行状态和错误信息。
     * 记录失败记录数和详细的错误堆栈。
     *
     * @param logId 执行日志ID
     * @param extractedRows 已提取的记录数
     * @param transformedRows 已转换的记录数
     * @param loadedRows 已加载的记录数
     * @param failedRows 失败的记录数
     * @param errorMessage 错误信息和异常堆栈
     */
    fun recordExecutionFailure(
        logId: Long,
        extractedRows: Long,
        transformedRows: Long,
        loadedRows: Long,
        failedRows: Long,
        errorMessage: String
    )
}
