package com.mumu.woodlin.etl.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.mumu.woodlin.etl.entity.EtlExecutionLog
import com.mumu.woodlin.etl.entity.EtlJob
import com.mumu.woodlin.etl.enums.EtlExecutionStatus
import com.mumu.woodlin.etl.mapper.EtlExecutionLogMapper
import com.mumu.woodlin.etl.service.IEtlExecutionLogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
/**
 * ETL执行历史服务实现
 *
 * 提供ETL任务执行历史的记录和管理功能。
 * 负责记录每次任务执行的详细信息，包括时间、状态、数据统计等。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Service

class EtlExecutionLogServiceImpl : ServiceImpl<EtlExecutionLogMapper, EtlExecutionLog>(),
    IEtlExecutionLogService {
    private val log = LoggerFactory.getLogger(javaClass)
    /**
     * 记录任务执行开始
     *
     * 创建新的执行日志记录，初始状态为RUNNING。
     * 所有数据统计字段初始化为0。
     *
     * @param job ETL任务配置
     * @return 执行日志ID
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun recordExecutionStart(job: EtlJob): Long {
        val executionLog = EtlExecutionLog().apply {
            jobId = job.jobId
            jobName = job.jobName
            executionStatus = EtlExecutionStatus.RUNNING.code
            startTime = LocalDateTime.now()
            extractedRows = 0L
            transformedRows = 0L
            loadedRows = 0L
            failedRows = 0L
        }

        save(executionLog)
        log.info("开始执行ETL任务: jobId={}, logId={}", job.jobId, executionLog.logId)
        return executionLog.logId!!
    }

    /**
     * 记录任务执行成功
     *
     * 更新执行日志状态为SUCCESS，记录数据统计和执行时长。
     *
     * @param logId 执行日志ID
     * @param extractedRows 提取的记录数
     * @param transformedRows 转换的记录数
     * @param loadedRows 加载的记录数
     * @param executionDetail 执行详情（JSON格式）
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun recordExecutionSuccess(
        logId: Long,
        extractedRows: Long,
        transformedRows: Long,
        loadedRows: Long,
        executionDetail: String
    ) {
        val executionLog = getById(logId)
        if (executionLog == null) {
            log.error("执行日志不存在: logId={}", logId)
            return
        }

        val endTime = LocalDateTime.now()
        val duration = ChronoUnit.MILLIS.between(executionLog.startTime, endTime)

        executionLog.apply {
            this.executionStatus = EtlExecutionStatus.SUCCESS.code
            this.endTime = endTime
            this.duration = duration
            this.extractedRows = extractedRows
            this.transformedRows = transformedRows
            this.loadedRows = loadedRows
            this.failedRows = 0L
            this.executionDetail = executionDetail
        }

        updateById(executionLog)
        log.info(
            "ETL任务执行成功: logId={}, 耗时={}ms, 加载={}条",
            logId, duration, loadedRows
        )
    }

    /**
     * 记录任务执行失败
     *
     * 更新执行日志状态为FAILED，记录错误信息和数据统计。
     *
     * @param logId 执行日志ID
     * @param extractedRows 已提取的记录数
     * @param transformedRows 已转换的记录数
     * @param loadedRows 已加载的记录数
     * @param failedRows 失败的记录数
     * @param errorMessage 错误信息
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun recordExecutionFailure(
        logId: Long,
        extractedRows: Long,
        transformedRows: Long,
        loadedRows: Long,
        failedRows: Long,
        errorMessage: String
    ) {
        val executionLog = getById(logId)
        if (executionLog == null) {
            log.error("执行日志不存在: logId={}", logId)
            return
        }

        val endTime = LocalDateTime.now()
        val duration = ChronoUnit.MILLIS.between(executionLog.startTime, endTime)

        executionLog.apply {
            this.executionStatus = EtlExecutionStatus.FAILED.code
            this.endTime = endTime
            this.duration = duration
            this.extractedRows = extractedRows
            this.transformedRows = transformedRows
            this.loadedRows = loadedRows
            this.failedRows = failedRows
            this.errorMessage = errorMessage
        }

        updateById(executionLog)
        log.error(
            "ETL任务执行失败: logId={}, 耗时={}ms, 失败={}条, 错误={}",
            logId, duration, failedRows, errorMessage
        )
    }
}
