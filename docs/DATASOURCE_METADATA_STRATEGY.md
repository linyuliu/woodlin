# 数据库元数据策略（面向 ETL / CDC）

本文定义 Woodlin 在“多数据库元数据提取”上的统一策略，目标是为后续 ETL 与 CDC 同步提供稳定、可扩展的数据基础。

## 1. 元数据标准模型

统一输出四层结构：

1. `DatabaseMetadata`：数据库产品、版本、字符集、排序规则、Schema 支持能力。
2. `SchemaMetadata`：Schema 名称、注释。
3. `TableMetadata`：表名、类型、注释、主键、创建/更新时间、引擎/排序规则（按数据库能力返回）。
4. `ColumnMetadata`：字段名、类型、长度、小数位、可空、默认值、主键、自增、顺序、注释、Java 类型映射。

该模型同时服务于前端元数据展示、SQL2API、ETL 映射配置与 CDC 主键识别。

## 2. 数据库适配分层

优先按协议族抽象，降低每种数据库单独维护 SQL 的成本：

- MySQL 兼容族：MySQL / MariaDB / TiDB / OceanBase / Doris / StarRocks，优先 `SHOW` + `information_schema`。
- PostgreSQL 兼容族：PostgreSQL / openGauss / GaussDB / Kingbase / UXDB / Vastbase，优先 `pg_catalog` + `information_schema`。
- Oracle 兼容族：Oracle / DM，优先 `ALL_*` 系统视图。
- JDBC 通用兜底：方言识别失败时走 `DatabaseMetaData`，并补齐主键、自增、默认值。

## 3. 注释与字段完整性策略

按“多级回退”处理注释缺失：

1. 优先数据库原生系统视图（如 `ALL_TAB_COMMENTS`、`pg_description`）。
2. 回退 JDBC `REMARKS`。
3. 仍缺失则前端显示为空，不阻塞 ETL 映射。

字段最小可用集：`columnName/dataType/nullable/primaryKey/ordinalPosition/defaultValue/comment`。

## 4. 国产数据库 CDC 元数据映射表（DB 类型 -> CDC 关键字段）

| DB 类型 | 方言族 | 表/列来源（优先） | CDC 主键字段 | 增量游标字段建议 | 关键校验项 |
| --- | --- | --- | --- | --- | --- |
| TiDB | MySQL 兼容 | `information_schema.tables/columns` | `column_key='PRI'` | `update_time` 或业务时间列 | 时区、`CURRENT_TIMESTAMP` 默认值解析 |
| OceanBase(MySQL) | MySQL 兼容 | `information_schema` | `column_key='PRI'` | `gmt_modified` / `update_time` | 租户 Schema 隔离、字符集 |
| DM(达梦) | Oracle 兼容 | `ALL_TABLES/ALL_TAB_COLUMNS/ALL_COL_COMMENTS` | `ALL_CONS_COLUMNS + ALL_CONSTRAINTS(P)` | `LAST_MODIFIED` 或触发器列 | 注释完整性、标识列自增识别 |
| Kingbase | PostgreSQL 兼容 | `pg_class/pg_attribute/pg_description` | `pg_index.indisprimary` | `update_time` + `xmin` 方案可选 | 序列默认值(`nextval`)识别 |
| openGauss | PostgreSQL 兼容 | `pg_catalog + information_schema` | `pg_index.indisprimary` | `update_time` / `gs_wal_lsn` | 分区表主键可见性 |
| GaussDB | PostgreSQL 兼容 | `pg_catalog + information_schema` | `pg_index.indisprimary` | `update_time` / 日志位点 | 数字精度映射与注释提取 |
| UXDB | PostgreSQL 兼容 | `information_schema + pg_catalog` | `pg_index.indisprimary` | 业务时间列 | 类型别名兼容（`bpchar` 等） |
| Vastbase | PostgreSQL 兼容 | `information_schema + pg_catalog` | `pg_index.indisprimary` | `last_update_ts` | 大对象列是否参与 CDC |
| Doris | MySQL 兼容（分析型） | `information_schema` | 主键模型表的 key 列 | `__DORIS_DELETE_SIGN__` + 导入时间 | 明确是否仅做批式同步 |
| StarRocks | MySQL 兼容（分析型） | `information_schema` | Primary/Unique Key 列 | 导入批次时间列 | Merge-on-Write 模型差异 |

说明：

- `CDC 主键字段` 是构建行级去重与幂等写入的必需字段。
- `增量游标字段` 优先业务更新时间；没有时使用日志位点（binlog/WAL）方案。
- 分析型数据库（Doris/StarRocks）通常更适合批量快照 + 分批增量，不建议直接当 OLTP CDC 源。

## 5. 可执行清单（按表落地）

对每个接入表执行以下步骤，并在 ETL 配置中留痕：

1. **主键确认**：读取主键字段；无主键标记 `cdc_key_required=true` 并阻断自动发布。
2. **增量策略确认**：优先 `update_time`，无则切日志位点；记录 `incremental_mode`。
3. **字段顺序固化**：以 `ordinalPosition` 生成字段快照，保存 `column_hash` 供变更检测。
4. **类型映射生成**：基于 `dataType + columnSize + decimalDigits` 生成 ETL 转换规则。
5. **注释质量检查**：空注释比例 > 30% 时标记 `metadata_quality=warning`。
6. **发布前校验**：对源表抽样 100 行，验证主键唯一与默认值转换。

## 6. 与当前实现对齐点

- 前端数据源工作台已支持：Schema/表层级浏览、字段注释与默认值展示、字段列配置持久化。
- 后端提取器已覆盖：主键、自增、默认值、注释回退、国产数据库 URL 识别。
- 文档中的映射表可直接转成 `etl_source_profile` 配置模板字段。
