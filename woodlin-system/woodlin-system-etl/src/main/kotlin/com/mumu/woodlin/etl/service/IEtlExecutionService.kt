package com.mumu.woodlin.etl.service

import com.mumu.woodlin.etl.entity.EtlJob

/**
 * ETL任务执行服务接口
 *
 * 负责ETL任务的实际执行，包括数据提取、转换和加载三个阶段。
 * 使用协程进行异步处理，支持大数据量的批量处理和事务控制。
 *
 * ## 执行流程
 * 1. **Extract（提取）**：从源数据库读取数据
 * 2. **Transform（转换）**：应用字段映射和数据转换规则
 * 3. **Load（加载）**：批量写入目标数据库
 *
 * ## 特性
 * - 异步执行不阻塞主线程
 * - 批量处理提高性能
 * - 事务控制保证数据一致性
 * - 错误处理和重试机制
 * - 执行过程全程记录
 *
 * @author mumu
 * @since 2025-01-01
 */
interface IEtlExecutionService {

    /**
     * 执行ETL任务
     *
     * 异步执行ETL任务的完整流程：
     * 1. 记录执行开始
     * 2. 从源数据库提取数据
     * 3. 应用数据转换规则
     * 4. 批量加载到目标数据库
     * 5. 记录执行结果（成功/失败）
     *
     * 该方法是挂起函数，使用Kotlin协程进行异步处理。
     * 执行过程中的任何异常都会被捕获并记录到执行日志中。
     *
     * @param job ETL任务配置
     */
    suspend fun execute(job: EtlJob)
}
