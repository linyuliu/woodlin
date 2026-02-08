# Woodlin ETL Module

## 概述

ETL (Extract, Transform, Load) 模块是Woodlin系统的数据集成工具，提供离线数据同步功能，支持定时调度和多数据源之间的数据迁移。

## 核心功能

1. **数据提取 (Extract)**
   - 支持从多种数据库提取数据（MySQL, PostgreSQL, Oracle等）
   - 支持自定义SQL查询
   - 支持全量和增量数据提取
   - 基于已实现的元数据提取功能

2. **数据转换 (Transform)**
   - 字段映射配置
   - 数据类型转换
   - 自定义转换规则（JSON配置）
   - 数据过滤和清洗

3. **数据加载 (Load)**
   - 批量数据插入
   - 事务控制
   - 错误处理和重试机制
   - 支持多种目标数据库

4. **任务调度**
   - 基于Cron表达式的定时调度
   - 集成现有的Task模块
   - 支持手动立即执行
   - 并发控制

5. **监控和日志**
   - 执行历史记录
   - 性能统计（提取、转换、加载记录数）
   - 错误日志和详细信息
   - 执行时长统计

## 架构设计

### 模块结构

```
woodlin-system-etl/
├── entity/              # 实体类
│   ├── EtlJob.java             # ETL任务配置
│   └── EtlExecutionLog.java    # ETL执行历史
├── enums/               # 枚举类
│   ├── EtlJobStatus.java       # 任务状态
│   ├── EtlExecutionStatus.java # 执行状态
│   └── SyncMode.java           # 同步模式
├── service/             # 服务接口
│   ├── IEtlJobService.java           # 任务管理服务
│   ├── IEtlExecutionService.java     # 任务执行服务
│   ├── IEtlExecutionLogService.java  # 日志服务
│   └── impl/                         # 服务实现
├── mapper/              # MyBatis Mapper
├── controller/          # REST API控制器
└── config/              # 配置类
```

### 关键运行基座表

- `sys_etl_column_mapping_rule`: 源字段到目标字段映射规则，支持启用禁用与顺序控制。
- `sys_etl_sync_checkpoint`: 增量同步检查点，记录上次增量值与桶位执行统计。
- `sys_etl_table_structure_snapshot`: 源/目标表结构快照，用于结构变更检测。
- `sys_etl_data_bucket_checksum`: 分桶校验结果，支持“校验相等直接跳过同步”策略。
- `sys_etl_data_validation_log`: 每次执行后的数据一致性校验日志。

### 依赖关系

ETL模块依赖以下模块：

- **woodlin-system-task**: 任务调度功能
- **woodlin-system-datasource**: 元数据提取、缓存与数据库适配能力
- **Dynamic DataSource**: 多数据源支持
- **ETL内置方言适配器**: 负责跨库写入 SQL（upsert / merge / identifier quote）适配
- **XXL-Job（可选）**: 支持通过 `woodlinEtlSyncHandler` 触发 ETL 任务

### XXL-Job 触发参数

- 处理器名：`woodlinEtlSyncHandler`
- 参数支持：
  - `10001`
  - `jobId=10001`
  - `{"jobId":10001}`

### 分桶重试策略配置

在 `sys_etl_job.transform_rules` 中可配置：

- `bucketSize`: 分桶数（默认 `64`）
- `mismatchBucketRetryCount`: 差异桶重试次数（默认取 `retry_count`）
- `mismatchBucketRetryIntervalMs`: 差异桶重试间隔毫秒（默认取 `retry_interval * 1000`）

## 快速开始

### 1. 数据库初始化

执行主库脚本创建ETL相关表：

```sql
-- MySQL
source sql/mysql/woodlin_schema.sql

-- PostgreSQL
\i sql/postgresql/woodlin_schema.sql
```

### 2. 配置数据源

在 `application.yml` 中配置数据源：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        # 源数据库
        source_db:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/source_db
          username: root
          password: password
        
        # 目标数据库
        target_db:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/target_db
          username: root
          password: password
```

### 3. 创建ETL任务

通过API创建ETL任务：

```bash
POST /api/etl/jobs
Content-Type: application/json

{
  "jobName": "用户数据同步",
  "jobGroup": "DATA_SYNC",
  "jobDescription": "将源数据库的用户数据同步到目标数据库",
  "sourceDatasource": "source_db",
  "sourceTable": "users",
  "targetDatasource": "target_db",
  "targetTable": "users_copy",
  "syncMode": "FULL",
  "batchSize": 1000,
  "cronExpression": "0 0 2 * * ?",
  "status": "1",
  "concurrent": "0",
  "retryCount": 3,
  "retryInterval": 60
}
```

### 4. 启用任务

```bash
POST /api/etl/jobs/{jobId}/enable
```

### 5. 查看执行历史

```bash
GET /api/etl/logs
```

## 使用示例

### 全量同步

```json
{
  "jobName": "全量订单同步",
  "jobGroup": "ORDER_SYNC",
  "sourceDatasource": "source_db",
  "sourceTable": "orders",
  "targetDatasource": "target_db",
  "targetTable": "orders_backup",
  "syncMode": "FULL",
  "cronExpression": "0 0 1 * * ?",
  "batchSize": 5000
}
```

### 增量同步

```json
{
  "jobName": "增量用户同步",
  "jobGroup": "USER_SYNC",
  "sourceDatasource": "source_db",
  "sourceTable": "users",
  "targetDatasource": "target_db",
  "targetTable": "users_mirror",
  "syncMode": "INCREMENTAL",
  "incrementalColumn": "update_time",
  "cronExpression": "0 */30 * * * ?",
  "batchSize": 1000
}
```

### 自定义SQL查询

```json
{
  "jobName": "活跃用户统计",
  "jobGroup": "ANALYTICS",
  "sourceDatasource": "source_db",
  "sourceQuery": "SELECT user_id, username, login_count FROM users WHERE status = '1' AND login_count > 10",
  "targetDatasource": "target_db",
  "targetTable": "active_users",
  "syncMode": "FULL",
  "cronExpression": "0 0 3 * * ?",
  "batchSize": 2000
}
```

### 带过滤条件的同步

```json
{
  "jobName": "付费用户同步",
  "jobGroup": "VIP_SYNC",
  "sourceDatasource": "source_db",
  "sourceTable": "users",
  "filterCondition": "vip_level > 0 AND expire_time > NOW()",
  "targetDatasource": "target_db",
  "targetTable": "vip_users",
  "syncMode": "FULL",
  "cronExpression": "0 0 4 * * ?",
  "batchSize": 1000
}
```

## API接口

### ETL任务管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/etl/jobs | 创建ETL任务 |
| PUT | /api/etl/jobs/{jobId} | 更新ETL任务 |
| DELETE | /api/etl/jobs/{jobId} | 删除ETL任务 |
| GET | /api/etl/jobs/{jobId} | 获取任务详情 |
| GET | /api/etl/jobs | 查询所有任务 |
| POST | /api/etl/jobs/{jobId}/enable | 启用任务 |
| POST | /api/etl/jobs/{jobId}/disable | 禁用任务 |
| POST | /api/etl/jobs/{jobId}/execute | 立即执行任务 |

### 执行历史查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/etl/logs | 查询所有执行历史 |
| GET | /api/etl/logs/{logId} | 获取执行历史详情 |

## 配置说明

### ETL任务配置字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| jobName | String | 是 | 任务名称 |
| jobGroup | String | 否 | 任务组名 |
| jobDescription | String | 否 | 任务描述 |
| sourceDatasource | String | 是 | 源数据源名称 |
| sourceTable | String | 否* | 源表名 |
| sourceSchema | String | 否 | 源Schema名 |
| sourceQuery | String | 否* | 源查询SQL |
| targetDatasource | String | 是 | 目标数据源名称 |
| targetTable | String | 是 | 目标表名 |
| targetSchema | String | 否 | 目标Schema名 |
| syncMode | String | 是 | 同步模式（FULL/INCREMENTAL） |
| incrementalColumn | String | 否** | 增量字段 |
| columnMapping | String | 否 | 字段映射（JSON） |
| transformRules | String | 否 | 转换规则（JSON） |
| filterCondition | String | 否 | 过滤条件 |
| batchSize | Integer | 否 | 批处理大小（默认1000） |
| cronExpression | String | 是 | Cron表达式 |
| status | String | 否 | 状态（1-启用，0-禁用） |
| concurrent | String | 否 | 是否并发（默认0） |
| retryCount | Integer | 否 | 重试次数（默认3） |
| retryInterval | Integer | 否 | 重试间隔秒（默认60） |

*注：sourceTable和sourceQuery至少配置一个
**注：增量同步时必填

### Cron表达式示例

| 表达式 | 说明 |
|--------|------|
| 0 0 2 * * ? | 每天凌晨2点执行 |
| 0 */30 * * * ? | 每30分钟执行一次 |
| 0 0 */2 * * ? | 每2小时执行一次 |
| 0 0 0 * * MON | 每周一凌晨执行 |
| 0 0 1 1 * ? | 每月1号凌晨1点执行 |

## 集成现有功能

### 1. 元数据提取集成

ETL模块直接使用 `woodlin-system-sql2api` 模块的元数据提取功能：

```java
// 获取表结构信息
TableMetadata tableMetadata = databaseMetadataService.getTableMetadata(
    datasourceName, schemaName, tableName
);

// 获取列信息
List<ColumnMetadata> columns = tableMetadata.getColumns();
```

### 2. 任务调度集成

ETL模块集成 `woodlin-system-task` 模块的调度功能：

```java
// 创建调度任务
taskScheduleService.createJob(
    jobName, jobGroup, cronExpression, 
    "com.mumu.woodlin.etl.job.EtlJobExecutor"
);

// 暂停任务
taskScheduleService.pauseJob(jobName, jobGroup);

// 恢复任务
taskScheduleService.resumeJob(jobName, jobGroup);
```

## 性能优化

1. **批量处理**：使用批处理减少数据库交互次数
2. **连接池**：使用数据库连接池提高性能
3. **异步执行**：任务执行使用异步方式，不阻塞主线程
4. **事务控制**：批次级别的事务控制，失败时自动回滚
5. **增量同步**：支持增量同步，减少数据传输量

## 错误处理

1. **重试机制**：支持自动重试，可配置重试次数和间隔
2. **错误日志**：详细记录错误信息和堆栈
3. **执行状态**：记录每次执行的状态和统计信息
4. **事务回滚**：批处理失败时自动回滚

## 监控和维护

### 执行历史查询

```sql
SELECT * FROM sys_etl_execution_log 
WHERE job_id = ? 
ORDER BY start_time DESC 
LIMIT 10;
```

### 性能统计

```sql
SELECT 
    job_name,
    COUNT(*) as total_executions,
    SUM(loaded_rows) as total_rows,
    AVG(duration) as avg_duration,
    SUM(CASE WHEN execution_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count
FROM sys_etl_execution_log
WHERE start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY job_name;
```

## 最佳实践

1. **选择合适的批次大小**：根据数据量和网络情况调整batchSize
2. **合理设置调度时间**：避免业务高峰期执行大数据量同步
3. **使用增量同步**：对于大表尽量使用增量同步减少数据传输
4. **监控执行状态**：定期查看执行历史，及时发现问题
5. **备份重要数据**：在执行ETL前备份重要数据
6. **测试SQL查询**：在配置前测试SQL查询的正确性和性能
7. **设置合理的重试策略**：根据业务需求配置重试次数和间隔

## 注意事项

1. 确保源和目标数据源配置正确
2. 目标表需要提前创建，ETL不会自动创建表
3. 字段映射需要保证类型兼容
4. 大数据量同步建议在业务低峰期执行
5. 注意数据库连接数限制
6. 定期清理执行历史日志

## 扩展开发

### 自定义转换规则

可以通过实现自定义的转换逻辑扩展ETL功能：

```java
public interface DataTransformer {
    Map<String, Object> transform(Map<String, Object> sourceData);
}
```

### 自定义数据验证

可以添加数据验证逻辑：

```java
public interface DataValidator {
    boolean validate(Map<String, Object> data);
}
```

## 许可证

本模块遵循项目整体许可证。
