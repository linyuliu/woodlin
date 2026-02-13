package com.mumu.woodlin.etl.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.util.PageUtil;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import com.mumu.woodlin.datasource.mapper.InfraDatasourceMapper;
import com.mumu.woodlin.datasource.service.DatabaseMetadataService;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobCreateRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobPageRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineValidationRequest;
import com.mumu.woodlin.etl.model.response.*;
import com.mumu.woodlin.etl.service.IEtlJobService;
import com.mumu.woodlin.etl.service.IEtlOfflineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ETL离线任务向导服务实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class EtlOfflineServiceImpl implements IEtlOfflineService {

    private static final String STATUS_ENABLED = "1";
    private static final String STATUS_DISABLED = "0";
    private static final String SYNC_MODE_FULL = "FULL";
    private static final String SYNC_MODE_INCREMENTAL = "INCREMENTAL";
    private static final String DEFAULT_CRON_EXPRESSION = "0 0 2 * * ?";
    private static final String DEFAULT_JOB_GROUP = "OFFLINE_SYNC";
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int DEFAULT_RETRY_INTERVAL = 60;
    private static final int DEFAULT_FILTER_LIMIT = 500;
    private static final int MAX_FILTER_LIMIT = 2000;

    private static final List<TypeGroupTemplate> TYPE_GROUP_TEMPLATES = List.of(
        new TypeGroupTemplate(
            "RELATIONAL",
            "关系型数据库",
            List.of(
                new TypeOptionTemplate("MYSQL", "MySQL"),
                new TypeOptionTemplate("MARIADB", "MariaDB"),
                new TypeOptionTemplate("TIDB", "TiDB"),
                new TypeOptionTemplate("POSTGRESQL", "PostgreSQL"),
                new TypeOptionTemplate("POLARDB", "PolarDB-X"),
                new TypeOptionTemplate("OCEANBASE", "OceanBase"),
                new TypeOptionTemplate("OPENGAUSS", "OpenGauss"),
                new TypeOptionTemplate("DM", "Dameng"),
                new TypeOptionTemplate("DB2", "DB2"),
                new TypeOptionTemplate("ORACLE", "Oracle"),
                new TypeOptionTemplate("SQLSERVER", "SQLServer"),
                new TypeOptionTemplate("KINGBASE", "KingbaseES"),
                new TypeOptionTemplate("GAUSSDB", "GaussDB"),
                new TypeOptionTemplate("VASTBASE", "Vastbase"),
                new TypeOptionTemplate("UXDB", "UXDB")
            )
        ),
        new TypeGroupTemplate(
            "MESSAGE",
            "消息与中间件",
            List.of(
                new TypeOptionTemplate("KAFKA", "Kafka"),
                new TypeOptionTemplate("ROCKETMQ", "RocketMQ"),
                new TypeOptionTemplate("RABBITMQ", "RabbitMQ"),
                new TypeOptionTemplate("AUTOMQ", "AutoMQ"),
                new TypeOptionTemplate("MONGODB", "MongoDB"),
                new TypeOptionTemplate("REDIS", "Redis"),
                new TypeOptionTemplate("ELASTICSEARCH", "ElasticSearch"),
                new TypeOptionTemplate("PULSAR", "Pulsar")
            )
        ),
        new TypeGroupTemplate(
            "ANALYTICS",
            "分析型与湖仓",
            List.of(
                new TypeOptionTemplate("GREENPLUM", "Greenplum"),
                new TypeOptionTemplate("CLICKHOUSE", "ClickHouse"),
                new TypeOptionTemplate("STARROCKS", "StarRocks"),
                new TypeOptionTemplate("DORIS", "Doris"),
                new TypeOptionTemplate("SELECTDB", "SelectDB"),
                new TypeOptionTemplate("HIVE", "Hive"),
                new TypeOptionTemplate("KUDU", "Kudu"),
                new TypeOptionTemplate("ICEBERG", "Iceberg"),
                new TypeOptionTemplate("PAIMON", "Paimon"),
                new TypeOptionTemplate("DELTALAKE", "DeltaLake")
            )
        )
    );

    private final InfraDatasourceMapper infraDatasourceMapper;
    private final DatabaseMetadataService databaseMetadataService;
    private final IEtlJobService etlJobService;

    @Override
    public EtlOfflineWizardConfigResponse getWizardConfig() {
        List<InfraDatasourceConfig> datasourceConfigs = infraDatasourceMapper.selectList(new QueryWrapper<>());
        Map<String, Long> totalCountByType = datasourceConfigs.stream()
            .collect(Collectors.groupingBy(item -> normalizeType(item.getDatasourceType()), Collectors.counting()));
        Map<String, Long> enabledCountByType = datasourceConfigs.stream()
            .filter(item -> item.getStatus() == null || item.getStatus() == 1)
            .collect(Collectors.groupingBy(item -> normalizeType(item.getDatasourceType()), Collectors.counting()));

        List<EtlDatasourceTypeGroup> groups = new ArrayList<>();
        for (TypeGroupTemplate groupTemplate : TYPE_GROUP_TEMPLATES) {
            EtlDatasourceTypeGroup group = new EtlDatasourceTypeGroup();
            group.setGroupCode(groupTemplate.groupCode());
            group.setGroupName(groupTemplate.groupName());

            List<EtlDatasourceTypeOption> options = new ArrayList<>();
            for (TypeOptionTemplate optionTemplate : groupTemplate.options()) {
                long totalCount = totalCountByType.getOrDefault(optionTemplate.datasourceType(), 0L);
                long enabledCount = enabledCountByType.getOrDefault(optionTemplate.datasourceType(), 0L);
                EtlDatasourceTypeOption option = new EtlDatasourceTypeOption();
                option.setDatasourceType(optionTemplate.datasourceType());
                option.setDisplayName(optionTemplate.displayName());
                option.setTotalCount(totalCount);
                option.setEnabledCount(enabledCount);
                option.setAvailable(enabledCount > 0);
                options.add(option);
            }
            group.setOptions(options);
            groups.add(group);
        }

        EtlOfflineWizardConfigResponse response = new EtlOfflineWizardConfigResponse();
        response.setDatasourceTypeGroups(groups);
        response.setDefaultSyncMode(SYNC_MODE_FULL);
        response.setDefaultBatchSize(DEFAULT_BATCH_SIZE);
        response.setDefaultRetryCount(DEFAULT_RETRY_COUNT);
        response.setDefaultRetryInterval(DEFAULT_RETRY_INTERVAL);
        response.setDefaultJobGroup(DEFAULT_JOB_GROUP);
        return response;
    }

    @Override
    public List<EtlOfflineDatasourceOption> listDatasourceOptions(String datasourceType, String keyword,
                                                                  boolean enabledOnly) {
        QueryWrapper<InfraDatasourceConfig> wrapper = new QueryWrapper<>();
        if (enabledOnly) {
            wrapper.eq("status", 1);
        }
        if (StrUtil.isNotBlank(datasourceType)) {
            wrapper.eq("datasource_type", normalizeType(datasourceType));
        }

        List<InfraDatasourceConfig> list = infraDatasourceMapper.selectList(wrapper);
        return list.stream()
            .filter(item -> matchesKeyword(item, keyword))
            .sorted(
                Comparator.comparing(
                    (InfraDatasourceConfig item) -> StrUtil.nullToEmpty(item.getDatasourceType())
                ).thenComparing(item -> StrUtil.nullToEmpty(item.getDatasourceCode()))
            )
            .map(this::toDatasourceOption)
            .collect(Collectors.toList());
    }

    @Override
    public PageResult<EtlJob> pageJobs(EtlOfflineJobPageRequest request) {
        Page<EtlJob> page = PageUtil.createPage(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<EtlJob> wrapper = Wrappers.lambdaQuery(EtlJob.class);

        if (StrUtil.isNotBlank(request.getStatus())) {
            wrapper.eq(EtlJob::getStatus, request.getStatus().trim());
        }
        if (StrUtil.isNotBlank(request.getSyncMode())) {
            wrapper.eq(EtlJob::getSyncMode, normalizeSyncMode(request.getSyncMode()));
        }
        if (StrUtil.isNotBlank(request.getSourceDatasource())) {
            wrapper.eq(EtlJob::getSourceDatasource, request.getSourceDatasource().trim());
        }
        if (StrUtil.isNotBlank(request.getTargetDatasource())) {
            wrapper.eq(EtlJob::getTargetDatasource, request.getTargetDatasource().trim());
        }
        if (StrUtil.isNotBlank(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(condition -> condition
                .like(EtlJob::getJobName, keyword)
                .or()
                .like(EtlJob::getSourceDatasource, keyword)
                .or()
                .like(EtlJob::getSourceTable, keyword)
                .or()
                .like(EtlJob::getTargetDatasource, keyword)
                .or()
                .like(EtlJob::getTargetTable, keyword)
            );
        }
        wrapper.orderByDesc(EtlJob::getUpdateTime).orderByDesc(EtlJob::getCreateTime);

        etlJobService.page(page, wrapper);
        return PageResult.of(page);
    }

    @Override
    public EtlOfflineValidationResult validate(EtlOfflineValidationRequest request) {
        EtlOfflineValidationResult result = new EtlOfflineValidationResult();
        result.setValid(Boolean.FALSE);
        result.setSourceTableExists(Boolean.FALSE);
        result.setTargetTableExists(Boolean.FALSE);

        List<String> errors = result.getErrors();
        List<String> warnings = result.getWarnings();

        InfraDatasourceConfig sourceDatasource = loadDatasourceConfig(request.getSourceDatasource(), "源", errors);
        InfraDatasourceConfig targetDatasource = loadDatasourceConfig(request.getTargetDatasource(), "目标", errors);

        List<ColumnMetadata> sourceColumns = List.of();
        List<ColumnMetadata> targetColumns = List.of();

        if (sourceDatasource != null) {
            sourceColumns = validateTableAndGetColumns(
                request.getSourceDatasource(),
                request.getSourceSchema(),
                request.getSourceTable(),
                "源",
                result,
                errors
            );
        }

        if (targetDatasource != null) {
            targetColumns = validateTableAndGetColumns(
                request.getTargetDatasource(),
                request.getTargetSchema(),
                request.getTargetTable(),
                "目标",
                result,
                errors
            );
        }

        List<String> sourceColumnNames = sourceColumns.stream()
            .map(ColumnMetadata::getColumnName)
            .collect(Collectors.toList());
        List<String> targetColumnNames = targetColumns.stream()
            .map(ColumnMetadata::getColumnName)
            .collect(Collectors.toList());
        result.setSourceColumns(sourceColumnNames);
        result.setTargetColumns(targetColumnNames);
        result.setSuggestedIncrementalColumns(suggestIncrementalColumns(sourceColumnNames));

        if (!sourceColumnNames.isEmpty() && !targetColumnNames.isEmpty()) {
            Set<String> sourceNameSet = sourceColumnNames.stream()
                .map(this::normalizeName)
                .collect(Collectors.toSet());
            long sameNameCount = targetColumnNames.stream()
                .map(this::normalizeName)
                .filter(sourceNameSet::contains)
                .count();
            if (sameNameCount == 0) {
                warnings.add("源表和目标表无同名字段，建议配置 columnMapping");
            }
        }

        if (SYNC_MODE_INCREMENTAL.equals(normalizeSyncMode(request.getSyncMode()))) {
            String incrementalColumn = StrUtil.trim(request.getIncrementalColumn());
            if (StrUtil.isBlank(incrementalColumn)) {
                errors.add("增量模式下必须指定增量字段");
            } else if (!containsIgnoreCase(sourceColumnNames, incrementalColumn)) {
                errors.add("增量字段在源表中不存在: " + incrementalColumn);
            }
        }

        result.setValid(errors.isEmpty());
        return result;
    }

    @Override
    public EtlOfflineCreateJobResponse createOfflineJob(EtlOfflineJobCreateRequest request) {
        validateRequestForCreate(request);
        EtlJob etlJob = mapRequestToEntity(request, new EtlJob());

        boolean success = etlJobService.createJob(etlJob);
        if (!success) {
            throw new BusinessException("创建离线ETL任务失败");
        }

        return buildCreateResponse(etlJob);
    }

    @Override
    public EtlOfflineCreateJobResponse updateOfflineJob(Long jobId, EtlOfflineJobCreateRequest request) {
        if (jobId == null) {
            throw new BusinessException("任务ID不能为空");
        }
        EtlJob existing = etlJobService.getById(jobId);
        if (existing == null) {
            throw new BusinessException("任务不存在: " + jobId);
        }

        validateRequestForCreate(request);
        EtlJob etlJob = mapRequestToEntity(request, existing);
        etlJob.setJobId(jobId);

        boolean success = etlJobService.updateJob(etlJob);
        if (!success) {
            throw new BusinessException("更新离线ETL任务失败");
        }

        return buildCreateResponse(etlJob);
    }

    @Override
    public void deleteOfflineJob(Long jobId) {
        if (jobId == null) {
            throw new BusinessException("任务ID不能为空");
        }
        EtlJob existing = etlJobService.getById(jobId);
        if (existing == null) {
            throw new BusinessException("任务不存在: " + jobId);
        }
        boolean success = etlJobService.deleteJob(jobId);
        if (!success) {
            throw new BusinessException("删除离线ETL任务失败");
        }
    }

    @Override
    public List<TableMetadata> listTables(String datasourceCode, String schemaName, String keyword, Integer limit) {
        List<TableMetadata> tables = databaseMetadataService.getTables(
            datasourceCode,
            blankToNull(schemaName)
        );
        return tables.stream()
            .filter(item -> {
                if (StrUtil.isBlank(keyword)) {
                    return true;
                }
                String keywordText = keyword.trim();
                return StrUtil.containsIgnoreCase(item.getTableName(), keywordText)
                    || StrUtil.containsIgnoreCase(item.getComment(), keywordText);
            })
            .sorted(Comparator.comparing(item -> StrUtil.nullToEmpty(item.getTableName())))
            .limit(resolveLimit(limit))
            .collect(Collectors.toList());
    }

    @Override
    public List<ColumnMetadata> listColumns(String datasourceCode, String schemaName, String tableName, String keyword,
                                            Integer limit) {
        List<ColumnMetadata> columns = databaseMetadataService.getColumns(
            datasourceCode,
            blankToNull(schemaName),
            tableName
        );
        return columns.stream()
            .filter(item -> {
                if (StrUtil.isBlank(keyword)) {
                    return true;
                }
                String keywordText = keyword.trim();
                return StrUtil.containsIgnoreCase(item.getColumnName(), keywordText)
                    || StrUtil.containsIgnoreCase(item.getComment(), keywordText)
                    || StrUtil.containsIgnoreCase(item.getDataType(), keywordText);
            })
            .sorted(Comparator.comparing(ColumnMetadata::getOrdinalPosition,
                Comparator.nullsLast(Integer::compareTo)))
            .limit(resolveLimit(limit))
            .collect(Collectors.toList());
    }

    private void validateRequestForCreate(EtlOfflineJobCreateRequest request) {
        if (!request.hasSourceInput()) {
            throw new BusinessException("源表和源查询SQL至少填写一个");
        }
        if (StrUtil.isBlank(request.getSourceTable())) {
            throw new BusinessException("当前离线任务创建必须指定源表");
        }
        if (Boolean.TRUE.equals(request.getAutoStart()) && StrUtil.isBlank(request.getCronExpression())) {
            throw new BusinessException("自动启动任务时必须填写Cron表达式");
        }

        EtlOfflineValidationRequest validationRequest = new EtlOfflineValidationRequest();
        validationRequest.setSourceDatasource(request.getSourceDatasource());
        validationRequest.setSourceSchema(request.getSourceSchema());
        validationRequest.setSourceTable(request.getSourceTable());
        validationRequest.setTargetDatasource(request.getTargetDatasource());
        validationRequest.setTargetSchema(request.getTargetSchema());
        validationRequest.setTargetTable(request.getTargetTable());
        validationRequest.setSyncMode(request.getSyncMode());
        validationRequest.setIncrementalColumn(request.getIncrementalColumn());

        EtlOfflineValidationResult validationResult = validate(validationRequest);
        if (!Boolean.TRUE.equals(validationResult.getValid())) {
            throw new BusinessException(String.join("；", validationResult.getErrors()));
        }
    }

    private EtlJob mapRequestToEntity(EtlOfflineJobCreateRequest request, EtlJob etlJob) {
        etlJob.setJobName(StrUtil.trim(request.getJobName()));
        etlJob.setJobGroup(StrUtil.emptyToDefault(StrUtil.trim(request.getJobGroup()), DEFAULT_JOB_GROUP));
        etlJob.setJobDescription(blankToNull(StrUtil.trim(request.getJobDescription())));

        etlJob.setSourceDatasource(StrUtil.trim(request.getSourceDatasource()));
        etlJob.setSourceSchema(blankToNull(StrUtil.trim(request.getSourceSchema())));
        etlJob.setSourceTable(blankToNull(StrUtil.trim(request.getSourceTable())));
        etlJob.setSourceQuery(blankToNull(StrUtil.trim(request.getSourceQuery())));

        etlJob.setTargetDatasource(StrUtil.trim(request.getTargetDatasource()));
        etlJob.setTargetSchema(blankToNull(StrUtil.trim(request.getTargetSchema())));
        etlJob.setTargetTable(StrUtil.trim(request.getTargetTable()));

        String syncMode = normalizeSyncMode(request.getSyncMode());
        etlJob.setSyncMode(syncMode);
        if (SYNC_MODE_INCREMENTAL.equals(syncMode)) {
            etlJob.setIncrementalColumn(blankToNull(StrUtil.trim(request.getIncrementalColumn())));
        } else {
            etlJob.setIncrementalColumn(null);
        }

        etlJob.setFilterCondition(blankToNull(StrUtil.trim(request.getFilterCondition())));
        etlJob.setColumnMapping(blankToNull(StrUtil.trim(request.getColumnMapping())));
        etlJob.setTransformRules(blankToNull(StrUtil.trim(request.getTransformRules())));
        etlJob.setBatchSize(request.getBatchSize() == null ? DEFAULT_BATCH_SIZE : request.getBatchSize());
        etlJob.setCronExpression(resolveCronExpression(request.getCronExpression(), request.getAutoStart()));
        etlJob.setStatus(Boolean.TRUE.equals(request.getAutoStart()) ? STATUS_ENABLED : STATUS_DISABLED);
        etlJob.setConcurrent(Boolean.TRUE.equals(request.getAllowConcurrent()) ? STATUS_ENABLED : STATUS_DISABLED);
        etlJob.setRetryCount(request.getRetryCount() == null ? DEFAULT_RETRY_COUNT : request.getRetryCount());
        etlJob.setRetryInterval(
            request.getRetryInterval() == null ? DEFAULT_RETRY_INTERVAL : request.getRetryInterval()
        );
        etlJob.setRemark(blankToNull(StrUtil.trim(request.getRemark())));
        return etlJob;
    }

    private EtlOfflineCreateJobResponse buildCreateResponse(EtlJob etlJob) {
        EtlOfflineCreateJobResponse response = new EtlOfflineCreateJobResponse();
        response.setJobId(etlJob.getJobId());
        response.setJobName(etlJob.getJobName());
        response.setStatus(etlJob.getStatus());
        response.setCronExpression(etlJob.getCronExpression());
        return response;
    }

    private InfraDatasourceConfig loadDatasourceConfig(String datasourceCode, String sideLabel, List<String> errors) {
        if (StrUtil.isBlank(datasourceCode)) {
            errors.add(sideLabel + "数据源不能为空");
            return null;
        }
        InfraDatasourceConfig datasource = infraDatasourceMapper.selectOne(
            Wrappers.<InfraDatasourceConfig>query().eq("datasource_code", datasourceCode.trim())
        );
        if (datasource == null) {
            errors.add(sideLabel + "数据源不存在: " + datasourceCode);
            return null;
        }
        if (datasource.getStatus() != null && datasource.getStatus() == 0) {
            errors.add(sideLabel + "数据源已禁用: " + datasourceCode);
            return null;
        }
        return datasource;
    }

    private List<ColumnMetadata> validateTableAndGetColumns(String datasourceCode, String schemaName, String tableName,
                                                            String sideLabel, EtlOfflineValidationResult result,
                                                            List<String> errors) {
        if (StrUtil.isBlank(tableName)) {
            errors.add(sideLabel + "表不能为空");
            return List.of();
        }

        try {
            List<TableMetadata> tables = databaseMetadataService.getTables(
                datasourceCode,
                blankToNull(schemaName)
            );
            boolean tableExists = tables.stream()
                .map(TableMetadata::getTableName)
                .anyMatch(item -> StrUtil.equalsIgnoreCase(item, tableName));
            if (!tableExists) {
                errors.add(sideLabel + "表不存在: " + tableName);
                return List.of();
            }
            if ("源".equals(sideLabel)) {
                result.setSourceTableExists(Boolean.TRUE);
            } else {
                result.setTargetTableExists(Boolean.TRUE);
            }
            return databaseMetadataService.getColumns(datasourceCode, blankToNull(schemaName), tableName);
        } catch (Exception exception) {
            errors.add(sideLabel + "表校验失败: " + exception.getMessage());
            return List.of();
        }
    }

    private List<String> suggestIncrementalColumns(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return List.of();
        }
        return columns.stream()
            .filter(StrUtil::isNotBlank)
            .sorted((left, right) -> Integer.compare(incrementalScore(right), incrementalScore(left)))
            .limit(5)
            .collect(Collectors.toList());
    }

    private int incrementalScore(String columnName) {
        String name = normalizeName(columnName);
        int score = 0;
        if (name.contains("update") || name.contains("modified")) {
            score += 5;
        }
        if (name.contains("time") || name.contains("date") || name.endsWith("ts")) {
            score += 4;
        }
        if (name.contains("create") || name.contains("ctime")) {
            score += 2;
        }
        if (name.equals("id")) {
            score += 1;
        }
        return score;
    }

    private EtlOfflineDatasourceOption toDatasourceOption(InfraDatasourceConfig config) {
        EtlOfflineDatasourceOption option = new EtlOfflineDatasourceOption();
        option.setDatasourceCode(config.getDatasourceCode());
        option.setDatasourceName(config.getDatasourceName());
        option.setDatasourceType(config.getDatasourceType());
        option.setStatus(config.getStatus());
        option.setOwner(config.getOwner());
        option.setBizTags(config.getBizTags());
        option.setRemark(config.getRemark());
        return option;
    }

    private boolean matchesKeyword(InfraDatasourceConfig item, String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return true;
        }
        String keywordText = keyword.trim();
        return StrUtil.containsIgnoreCase(item.getDatasourceCode(), keywordText)
            || StrUtil.containsIgnoreCase(item.getDatasourceName(), keywordText)
            || StrUtil.containsIgnoreCase(item.getDatasourceType(), keywordText)
            || StrUtil.containsIgnoreCase(item.getOwner(), keywordText)
            || StrUtil.containsIgnoreCase(item.getBizTags(), keywordText);
    }

    private String blankToNull(String value) {
        String trimmed = StrUtil.trim(value);
        return StrUtil.isBlank(trimmed) ? null : trimmed;
    }

    private long resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_FILTER_LIMIT;
        }
        return Math.min(limit, MAX_FILTER_LIMIT);
    }

    private String resolveCronExpression(String cronExpression, Boolean autoStart) {
        if (Boolean.TRUE.equals(autoStart) && StrUtil.isBlank(cronExpression)) {
            throw new BusinessException("自动启动任务必须指定Cron表达式");
        }
        return StrUtil.emptyToDefault(StrUtil.trim(cronExpression), DEFAULT_CRON_EXPRESSION);
    }

    private String normalizeType(String datasourceType) {
        return StrUtil.nullToEmpty(datasourceType).trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return StrUtil.nullToEmpty(name).trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeSyncMode(String syncMode) {
        String normalized = StrUtil.nullToEmpty(syncMode).trim().toUpperCase(Locale.ROOT);
        return SYNC_MODE_INCREMENTAL.equals(normalized) ? SYNC_MODE_INCREMENTAL : SYNC_MODE_FULL;
    }

    private boolean containsIgnoreCase(List<String> values, String value) {
        if (values == null || values.isEmpty() || StrUtil.isBlank(value)) {
            return false;
        }
        return values.stream().anyMatch(item -> StrUtil.equalsIgnoreCase(item, value));
    }

    private record TypeGroupTemplate(String groupCode, String groupName, List<TypeOptionTemplate> options) {
    }

    private record TypeOptionTemplate(String datasourceType, String displayName) {
    }
}
