# ETL模块使用指南

## 简介

本指南详细介绍如何使用Woodlin ETL模块进行数据抽取、转换和加载（ETL）。本模块基于已有的SQL2API元数据提取功能和Task任务调度功能，提供了完整的离线数据同步解决方案。

## 设计理念

ETL模块的设计充分利用了Woodlin系统现有的组件：

1. **元数据提取**：复用`woodlin-system-sql2api`模块的数据库元数据提取能力
2. **任务调度**：集成`woodlin-system-task`模块的Cron调度功能
3. **多数据源**：基于Dynamic DataSource实现多数据源管理
4. **批量处理**：采用批处理和事务控制确保数据一致性

## 快速开始

### 1. 配置数据源

在`application.yml`中配置动态数据源：

```yaml
spring:
  datasource:
    dynamic:
      # 主数据源（默认）
      primary: master
      # 严格模式，如果数据源不存在则报错
      strict: false
      datasource:
        # 主数据源
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
          username: root
          password: password
        
        # 源数据库1 - MySQL
        source_mysql:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://192.168.1.100:3306/source_db?useUnicode=true&characterEncoding=utf8
          username: source_user
          password: source_pass
        
        # 源数据库2 - PostgreSQL
        source_pg:
          driver-class-name: org.postgresql.Driver
          url: jdbc:postgresql://192.168.1.101:5432/source_db
          username: postgres
          password: postgres
        
        # 目标数据库 - MySQL
        target_mysql:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://192.168.1.200:3306/target_db?useUnicode=true&characterEncoding=utf8
          username: target_user
          password: target_pass
```

### 2. 初始化数据库表

执行SQL脚本创建ETL相关表：

**MySQL:**
```bash
mysql -u root -p woodlin < sql/mysql/etl_schema.sql
```

**PostgreSQL:**
```bash
psql -U postgres -d woodlin -f sql/postgresql/etl_schema.sql
```

### 3. 创建ETL任务

通过API或数据库直接插入ETL任务配置。

#### 方式1：通过API创建（推荐）

```bash
curl -X POST http://localhost:8080/api/etl/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "jobName": "用户数据同步",
    "jobGroup": "DATA_SYNC",
    "jobDescription": "将源数据库的用户数据同步到目标数据库",
    "sourceDatasource": "source_mysql",
    "sourceTable": "users",
    "targetDatasource": "target_mysql",
    "targetTable": "users_backup",
    "syncMode": "FULL",
    "batchSize": 1000,
    "cronExpression": "0 0 2 * * ?",
    "status": "1",
    "concurrent": "0",
    "retryCount": 3,
    "retryInterval": 60
  }'
```

#### 方式2：直接插入数据库

```sql
INSERT INTO sys_etl_job (
    job_id, job_name, job_group, job_description,
    source_datasource, source_table, 
    target_datasource, target_table,
    sync_mode, batch_size, cron_expression, status
) VALUES (
    1, '用户数据同步', 'DATA_SYNC', '将源数据库的用户数据同步到目标数据库',
    'source_mysql', 'users',
    'target_mysql', 'users_backup',
    'FULL', 1000, '0 0 2 * * ?', '1'
);
```

### 4. 启用任务

```bash
curl -X POST http://localhost:8080/api/etl/jobs/1/enable
```

### 5. 查看执行历史

```bash
curl http://localhost:8080/api/etl/logs
```

## 常见场景示例

### 场景1：全量数据同步

将源数据库的完整表数据同步到目标数据库。

```json
{
  "jobName": "订单全量同步",
  "jobGroup": "ORDER_SYNC",
  "jobDescription": "每天凌晨2点全量同步订单数据",
  "sourceDatasource": "source_mysql",
  "sourceTable": "orders",
  "targetDatasource": "target_mysql",
  "targetTable": "orders_mirror",
  "syncMode": "FULL",
  "batchSize": 5000,
  "cronExpression": "0 0 2 * * ?",
  "status": "1"
}
```

**适用场景：**
- 数据量不大的表
- 不需要实时性的数据备份
- 目标表可以完全覆盖

### 场景2：增量数据同步

只同步自上次同步以来新增或修改的数据。

```json
{
  "jobName": "用户增量同步",
  "jobGroup": "USER_SYNC",
  "jobDescription": "每30分钟同步新增和更新的用户数据",
  "sourceDatasource": "source_mysql",
  "sourceTable": "users",
  "targetDatasource": "target_mysql",
  "targetTable": "users_incremental",
  "syncMode": "INCREMENTAL",
  "incrementalColumn": "update_time",
  "batchSize": 1000,
  "cronExpression": "0 */30 * * * ?",
  "status": "1"
}
```

**适用场景：**
- 数据量大的表
- 需要准实时同步
- 源表有时间戳字段标识数据变更

**注意事项：**
- 目标表必须包含与增量字段相同的列
- 增量字段应该有索引以提高查询性能

### 场景3：自定义SQL查询同步

使用自定义SQL查询提取数据并同步。

```json
{
  "jobName": "活跃用户统计",
  "jobGroup": "ANALYTICS",
  "jobDescription": "提取并同步活跃用户数据",
  "sourceDatasource": "source_mysql",
  "sourceQuery": "SELECT user_id, username, email, login_count, last_login_time FROM users WHERE status = '1' AND login_count > 10 AND last_login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)",
  "targetDatasource": "target_mysql",
  "targetTable": "active_users",
  "syncMode": "FULL",
  "batchSize": 2000,
  "cronExpression": "0 0 3 * * ?",
  "status": "1"
}
```

**适用场景：**
- 需要数据过滤或聚合
- 跨表查询
- 数据转换和计算

### 场景4：带过滤条件的同步

只同步满足特定条件的数据。

```json
{
  "jobName": "VIP用户同步",
  "jobGroup": "VIP_SYNC",
  "jobDescription": "同步VIP用户数据",
  "sourceDatasource": "source_mysql",
  "sourceTable": "users",
  "filterCondition": "vip_level > 0 AND expire_time > NOW() AND status = '1'",
  "targetDatasource": "target_mysql",
  "targetTable": "vip_users",
  "syncMode": "FULL",
  "batchSize": 1000,
  "cronExpression": "0 0 4 * * ?",
  "status": "1"
}
```

**适用场景：**
- 只需要部分数据
- 数据分区同步
- 按业务规则过滤

### 场景5：跨数据库类型同步

从MySQL同步到PostgreSQL。

```json
{
  "jobName": "MySQL到PostgreSQL同步",
  "jobGroup": "CROSS_DB",
  "jobDescription": "跨数据库类型的数据同步",
  "sourceDatasource": "source_mysql",
  "sourceTable": "products",
  "targetDatasource": "target_pg",
  "targetSchema": "public",
  "targetTable": "products",
  "syncMode": "FULL",
  "batchSize": 1000,
  "cronExpression": "0 0 1 * * ?",
  "status": "1"
}
```

**注意事项：**
- 确保字段类型兼容
- PostgreSQL需要指定schema
- 注意字符编码问题

## Cron表达式参考

| 表达式 | 说明 | 适用场景 |
|--------|------|----------|
| `0 0 2 * * ?` | 每天凌晨2点执行 | 每日全量备份 |
| `0 */30 * * * ?` | 每30分钟执行一次 | 准实时增量同步 |
| `0 0 */2 * * ?` | 每2小时执行一次 | 定期数据更新 |
| `0 0 0 * * MON` | 每周一凌晨执行 | 周报数据汇总 |
| `0 0 1 1 * ?` | 每月1号凌晨1点执行 | 月度数据归档 |
| `0 0 0 1 1 ?` | 每年1月1日凌晨执行 | 年度数据汇总 |
| `0 0/5 9-17 * * ?` | 工作时间每5分钟 | 工作时段实时同步 |

Cron表达式格式：`秒 分 时 日 月 周`

## 监控和维护

### 查看任务状态

```bash
# 查看所有任务
curl http://localhost:8080/api/etl/jobs

# 查看特定任务详情
curl http://localhost:8080/api/etl/jobs/{jobId}
```

### 查看执行历史

```bash
# 查看所有执行历史
curl http://localhost:8080/api/etl/logs

# 查看特定执行详情
curl http://localhost:8080/api/etl/logs/{logId}
```

### 手动执行任务

```bash
curl -X POST http://localhost:8080/api/etl/jobs/{jobId}/execute
```

### 暂停和恢复任务

```bash
# 禁用任务
curl -X POST http://localhost:8080/api/etl/jobs/{jobId}/disable

# 启用任务
curl -X POST http://localhost:8080/api/etl/jobs/{jobId}/enable
```

### 性能监控SQL

```sql
-- 查看最近7天的执行统计
SELECT 
    job_name,
    COUNT(*) as total_executions,
    SUM(loaded_rows) as total_rows,
    AVG(duration) as avg_duration_ms,
    SUM(CASE WHEN execution_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN execution_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count
FROM sys_etl_execution_log
WHERE start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY job_name;

-- 查看失败的任务
SELECT *
FROM sys_etl_execution_log
WHERE execution_status = 'FAILED'
ORDER BY start_time DESC
LIMIT 10;

-- 查看执行时间最长的任务
SELECT 
    job_name,
    start_time,
    duration,
    loaded_rows,
    duration / loaded_rows as avg_time_per_row
FROM sys_etl_execution_log
WHERE execution_status = 'SUCCESS'
ORDER BY duration DESC
LIMIT 10;
```

## 最佳实践

### 1. 批处理大小选择

- **小表（< 1万条）**：batchSize = 500-1000
- **中表（1万-100万条）**：batchSize = 1000-5000
- **大表（> 100万条）**：batchSize = 5000-10000

### 2. 调度时间选择

- 避免在业务高峰期执行大数据量同步
- 全量同步建议在凌晨2-5点执行
- 增量同步可根据业务需求设置频率
- 注意任务执行时间不要重叠

### 3. 错误处理

- 合理设置重试次数（建议3-5次）
- 重试间隔根据任务特性设置（30-300秒）
- 关注失败日志，及时处理异常

### 4. 性能优化

- 为增量字段创建索引
- 使用WHERE条件减少数据扫描
- 合理设置批处理大小
- 考虑数据库负载，错峰执行

### 5. 数据一致性

- 使用事务保证批次级别的一致性
- 增量同步时注意时间戳精度
- 定期进行全量同步校验数据

### 6. 监控告警

- 定期查看执行历史
- 设置失败告警机制
- 监控执行时长异常
- 关注数据量变化趋势

## 故障排查

### 问题1：数据源连接失败

**症状：** 任务执行失败，错误信息显示无法连接数据源

**排查步骤：**
1. 检查数据源配置是否正确
2. 测试数据库连接
3. 检查网络连通性
4. 验证用户名密码

### 问题2：任务未按时执行

**症状：** 任务状态为启用，但未在预定时间执行

**排查步骤：**
1. 检查Cron表达式是否正确
2. 查看任务调度服务是否运行
3. 检查系统时间是否准确
4. 查看日志是否有调度错误

### 问题3：数据同步不完整

**症状：** 部分数据未同步到目标库

**排查步骤：**
1. 检查源查询SQL是否正确
2. 查看执行日志的记录数统计
3. 检查目标表结构是否匹配
4. 验证过滤条件是否正确

### 问题4：性能问题

**症状：** 任务执行时间过长

**优化建议：**
1. 调整批处理大小
2. 为查询字段添加索引
3. 优化源查询SQL
4. 考虑分批次执行大表

## 安全考虑

### 1. 数据源凭证管理

- 不要在配置文件中硬编码密码
- 使用环境变量或配置中心
- 定期轮换数据库密码

### 2. 权限控制

- 源数据库只需要SELECT权限
- 目标数据库需要INSERT权限
- 遵循最小权限原则

### 3. 数据脱敏

- 对敏感数据进行脱敏处理
- 使用transformRules配置转换规则
- 注意合规要求

## 扩展开发

### 自定义数据转换

未来版本将支持更灵活的数据转换配置，可通过JSON配置实现：

```json
{
  "transformRules": {
    "phone": "MASK(phone, 3, 4)",
    "email": "MASK_EMAIL(email)",
    "amount": "MULTIPLY(amount, 100)"
  }
}
```

### 字段映射

支持源字段和目标字段的映射配置：

```json
{
  "columnMapping": {
    "source_user_id": "target_uid",
    "source_user_name": "target_name",
    "source_create_time": "target_created_at"
  }
}
```

## 附录

### A. 完整配置参数说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| jobName | String | 是 | - | 任务名称，必须唯一 |
| jobGroup | String | 否 | DEFAULT | 任务组名 |
| jobDescription | String | 否 | - | 任务描述 |
| sourceDatasource | String | 是 | - | 源数据源名称 |
| sourceTable | String | 否* | - | 源表名 |
| sourceSchema | String | 否 | - | 源Schema名（PostgreSQL等需要） |
| sourceQuery | String | 否* | - | 源查询SQL |
| targetDatasource | String | 是 | - | 目标数据源名称 |
| targetTable | String | 是 | - | 目标表名 |
| targetSchema | String | 否 | - | 目标Schema名（PostgreSQL等需要） |
| syncMode | String | 是 | FULL | 同步模式：FULL/INCREMENTAL |
| incrementalColumn | String | 否** | - | 增量字段名 |
| columnMapping | String | 否 | - | 字段映射（JSON格式） |
| transformRules | String | 否 | - | 转换规则（JSON格式） |
| filterCondition | String | 否 | - | WHERE条件 |
| batchSize | Integer | 否 | 1000 | 批处理大小 |
| cronExpression | String | 是 | - | Cron表达式 |
| status | String | 否 | 0 | 任务状态：1-启用，0-禁用 |
| concurrent | String | 否 | 0 | 是否并发：1-允许，0-禁止 |
| retryCount | Integer | 否 | 3 | 重试次数 |
| retryInterval | Integer | 否 | 60 | 重试间隔（秒） |
| tenantId | String | 否 | - | 租户ID |
| remark | String | 否 | - | 备注 |

*注：sourceTable和sourceQuery至少配置一个
**注：增量同步时必填

### B. API接口完整列表

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
| GET | /api/etl/logs | 查询所有执行历史 |
| GET | /api/etl/logs/{logId} | 获取执行历史详情 |

### C. 数据库驱动支持

| 数据库 | 驱动类 | Maven依赖 |
|--------|--------|-----------|
| MySQL | com.mysql.cj.jdbc.Driver | mysql-connector-j |
| PostgreSQL | org.postgresql.Driver | postgresql |
| Oracle | oracle.jdbc.OracleDriver | ojdbc8 |
| SQL Server | com.microsoft.sqlserver.jdbc.SQLServerDriver | mssql-jdbc |
| 达梦 | dm.jdbc.driver.DmDriver | DmJdbcDriver18 |

## 总结

Woodlin ETL模块提供了一个强大而灵活的数据同步解决方案，充分利用了系统现有的元数据提取和任务调度能力。通过合理的配置和使用，可以满足各种数据同步场景的需求。

如有问题或建议，请参考项目文档或提交Issue。
