package com.mumu.woodlin.etl.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.util.PageUtil;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import com.mumu.woodlin.datasource.mapper.InfraDatasourceMapper;
import com.mumu.woodlin.datasource.service.DatabaseMetadataService;
import com.mumu.woodlin.etl.entity.EtlColumnMappingRule;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.model.EtlOfflineFieldRule;
import com.mumu.woodlin.etl.model.EtlOfflineRuntimeConfig;
import com.mumu.woodlin.etl.model.EtlOfflineTableMapping;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobCreateRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobPageRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineValidationRequest;
import com.mumu.woodlin.etl.model.response.EtlDatasourceTypeGroup;
import com.mumu.woodlin.etl.model.response.EtlDatasourceTypeOption;
import com.mumu.woodlin.etl.model.response.EtlOfflineCreateJobResponse;
import com.mumu.woodlin.etl.model.response.EtlOfflineDatasourceOption;
import com.mumu.woodlin.etl.model.response.EtlOfflineJobDetailResponse;
import com.mumu.woodlin.etl.model.response.EtlOfflineJobSummaryResponse;
import com.mumu.woodlin.etl.model.response.EtlOfflineTableValidationResult;
import com.mumu.woodlin.etl.model.response.EtlOfflineValidationResult;
import com.mumu.woodlin.etl.model.response.EtlOfflineWizardConfigResponse;
import com.mumu.woodlin.etl.service.IEtlColumnMappingRuleService;
import com.mumu.woodlin.etl.service.IEtlJobService;
import com.mumu.woodlin.etl.service.IEtlOfflineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
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
    private final IEtlColumnMappingRuleService columnMappingRuleService;
    private final ObjectMapper objectMapper;

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
        response.setDefaultSchemaSyncMode("AUTO_ADD_COLUMNS");
        response.setSupportedSyncModes(List.of(SYNC_MODE_FULL, SYNC_MODE_INCREMENTAL));
        response.setSupportedValidationModes(List.of("NONE", "ROW_COUNT", "CHECKSUM"));
        response.setSupportedSchemaSyncModes(List.of("NONE", "AUTO_ADD_COLUMNS"));
        response.setSupportedFieldActions(List.of("COPY", "CONSTANT", "DEFAULT", "TRIM", "UPPER", "LOWER",
            "DATE_FORMAT", "NUMBER_CAST", "NULL_IF_EMPTY"));
        response.setSupportedEmptyValuePolicies(List.of("KEEP", "NULL", "DEFAULT"));
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
    public PageResult<EtlOfflineJobSummaryResponse> pageJobs(EtlOfflineJobPageRequest request) {
        Page<EtlJob> page = PageUtil.createPage(request.getPageNum().intValue(), request.getPageSize().intValue());
        LambdaQueryWrapper<EtlJob> wrapper = new LambdaQueryWrapper<>();

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
        List<EtlOfflineJobSummaryResponse> summaries = page.getRecords().stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
        return PageResult.success(page.getCurrent(), page.getSize(), page.getTotal(), summaries);
    }

    @Override
    public EtlOfflineJobDetailResponse getJobDetail(Long jobId) {
        EtlJob job = requireJob(jobId);
        return buildJobDetail(job, listRules(jobId));
    }

    @Override
    public EtlOfflineValidationResult validate(EtlOfflineValidationRequest request) {
        EtlOfflineValidationResult result = new EtlOfflineValidationResult();
        result.setValid(Boolean.FALSE);
        if (request == null) {
            result.getErrors().add("请求体不能为空");
            return result;
        }
        if (request.getTableMappings() == null || request.getTableMappings().isEmpty()) {
            result.getErrors().add("表映射不能为空");
            return result;
        }

        InfraDatasourceConfig sourceDatasource = loadDatasourceConfig(request.getSourceDatasource(), "源", result.getErrors());
        InfraDatasourceConfig targetDatasource = loadDatasourceConfig(request.getTargetDatasource(), "目标", result.getErrors());
        EtlOfflineRuntimeConfig runtimeConfig = normalizeRuntimeConfig(request.getRuntimeConfig());
        String syncMode = normalizeSyncMode(runtimeConfig.getSyncMode());
        if (Boolean.TRUE.equals(runtimeConfig.getAutoStart()) && StrUtil.isBlank(runtimeConfig.getCronExpression())) {
            result.getErrors().add("自动启动任务时必须填写Cron表达式");
        }

        List<EtlOfflineTableValidationResult> tableResults = new ArrayList<>();
        for (EtlOfflineTableMapping tableMapping : request.getTableMappings()) {
            tableResults.add(validateTableMapping(sourceDatasource, targetDatasource, runtimeConfig, syncMode, tableMapping));
        }
        result.setTableResults(tableResults);
        result.setWarnings(mergeWarnings(result.getWarnings(), tableResults));
        result.setValid(result.getErrors().isEmpty()
            && tableResults.stream().allMatch(item -> Boolean.TRUE.equals(item.getValid())));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EtlOfflineCreateJobResponse createOfflineJob(EtlOfflineJobCreateRequest request) {
        EtlOfflineValidationResult validationResult = validate(buildValidationRequest(request));
        if (!Boolean.TRUE.equals(validationResult.getValid())) {
            throw new BusinessException(String.join("；", validationResult.getErrors()));
        }

        EtlOfflineRuntimeConfig runtimeConfig = normalizeRuntimeConfig(request.getRuntimeConfig());
        List<Long> createdJobIds = new ArrayList<>();
        for (int index = 0; index < request.getTableMappings().size(); index++) {
            EtlOfflineTableMapping tableMapping = request.getTableMappings().get(index);
            EtlJob etlJob = buildJobEntity(request, runtimeConfig, tableMapping, index, null);
            boolean success = etlJobService.createJob(etlJob);
            if (!success) {
                throw new BusinessException("创建离线ETL任务失败: " + etlJob.getJobName());
            }
            persistColumnRules(etlJob, tableMapping, false);
            createdJobIds.add(etlJob.getJobId());
        }
        return buildCreateResponse(createdJobIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EtlOfflineCreateJobResponse updateOfflineJob(Long jobId, EtlOfflineJobCreateRequest request) {
        EtlJob existing = requireJob(jobId);
        if (request.getTableMappings() == null || request.getTableMappings().size() != 1) {
            throw new BusinessException("更新离线任务时必须且只能提交1张表映射");
        }

        EtlOfflineValidationResult validationResult = validate(buildValidationRequest(request));
        if (!Boolean.TRUE.equals(validationResult.getValid())) {
            throw new BusinessException(String.join("；", validationResult.getErrors()));
        }

        EtlOfflineRuntimeConfig runtimeConfig = normalizeRuntimeConfig(request.getRuntimeConfig());
        EtlOfflineTableMapping tableMapping = request.getTableMappings().get(0);
        EtlJob etlJob = buildJobEntity(request, runtimeConfig, tableMapping, 0, existing);
        etlJob.setJobId(jobId);

        boolean success = etlJobService.updateJob(etlJob);
        if (!success) {
            throw new BusinessException("更新离线ETL任务失败");
        }
        persistColumnRules(etlJob, tableMapping, true);
        return buildCreateResponse(List.of(jobId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOfflineJob(Long jobId) {
        requireJob(jobId);
        deleteRulesByJobId(jobId);
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

    /**
     * 将创建请求转换为预校验请求。
     *
     * @param request 创建请求
     * @return 预校验请求
     */
    private EtlOfflineValidationRequest buildValidationRequest(EtlOfflineJobCreateRequest request) {
        EtlOfflineValidationRequest validationRequest = new EtlOfflineValidationRequest();
        validationRequest.setSourceDatasource(request.getSourceDatasource());
        validationRequest.setTargetDatasource(request.getTargetDatasource());
        validationRequest.setRuntimeConfig(request.getRuntimeConfig());
        validationRequest.setTableMappings(request.getTableMappings());
        return validationRequest;
    }

    /**
     * 将任务ID列表包装成创建结果。
     *
     * @param createdJobIds 已创建任务ID列表
     * @return 创建结果
     */
    private EtlOfflineCreateJobResponse buildCreateResponse(List<Long> createdJobIds) {
        EtlOfflineCreateJobResponse response = new EtlOfflineCreateJobResponse();
        response.setCreatedJobIds(new ArrayList<>(createdJobIds));
        response.setCreatedJobCount(createdJobIds.size());
        return response;
    }

    /**
     * 转换分页摘要。
     *
     * @param job 任务实体
     * @return 摘要对象
     */
    private EtlOfflineJobSummaryResponse toSummary(EtlJob job) {
        EtlOfflineJobSummaryResponse response = new EtlOfflineJobSummaryResponse();
        response.setJobId(job.getJobId());
        response.setJobName(job.getJobName());
        response.setSourceDatasource(job.getSourceDatasource());
        response.setSourceSchema(job.getSourceSchema());
        response.setSourceTable(job.getSourceTable());
        response.setTargetDatasource(job.getTargetDatasource());
        response.setTargetSchema(job.getTargetSchema());
        response.setTargetTable(job.getTargetTable());
        response.setSyncMode(job.getSyncMode());
        response.setStatus(job.getStatus());
        response.setCronExpression(job.getCronExpression());
        response.setLastExecuteTime(job.getLastExecuteTime());
        response.setLastExecuteStatus(job.getLastExecuteStatus());
        response.setFieldRuleCount((long) columnMappingRuleService.listEnabledRulesByJobId(job.getJobId()).size());
        response.setRemark(job.getRemark());
        return response;
    }

    /**
     * 构建任务详情，供前端向导回填。
     *
     * @param job   任务实体
     * @param rules 字段规则
     * @return 任务详情
     */
    private EtlOfflineJobDetailResponse buildJobDetail(EtlJob job, List<EtlColumnMappingRule> rules) {
        EtlOfflineJobDetailResponse response = new EtlOfflineJobDetailResponse();
        response.setJobId(job.getJobId());
        response.setJobName(job.getJobName());
        response.setJobGroup(job.getJobGroup());
        response.setJobDescription(job.getJobDescription());
        response.setSourceDatasource(job.getSourceDatasource());
        response.setTargetDatasource(job.getTargetDatasource());
        response.setStatus(job.getStatus());
        response.setConcurrent(job.getConcurrent());
        response.setCronExpression(job.getCronExpression());
        response.setLastExecuteTime(job.getLastExecuteTime());
        response.setLastExecuteStatus(job.getLastExecuteStatus());
        response.setRemark(job.getRemark());
        response.setRuntimeConfig(mergeRuntimeConfig(job, parseRuntimeConfig(job.getTransformRules())));
        response.setTableMapping(buildTableMapping(job, rules));
        response.setFieldRules(rules.stream()
            .map(this::toFieldRule)
            .collect(Collectors.toList()));
        return response;
    }

    /**
     * 构建单表映射回填对象。
     *
     * @param job   任务实体
     * @param rules 字段规则
     * @return 表映射
     */
    private EtlOfflineTableMapping buildTableMapping(EtlJob job, List<EtlColumnMappingRule> rules) {
        EtlOfflineTableMapping tableMapping = new EtlOfflineTableMapping();
        tableMapping.setJobName(job.getJobName());
        tableMapping.setSourceSchema(job.getSourceSchema());
        tableMapping.setSourceTable(job.getSourceTable());
        tableMapping.setTargetSchema(job.getTargetSchema());
        tableMapping.setTargetTable(job.getTargetTable());
        tableMapping.setIncrementalColumn(job.getIncrementalColumn());
        tableMapping.setFilterCondition(job.getFilterCondition());
        tableMapping.setFieldRules(rules.stream().map(this::toFieldRule).collect(Collectors.toList()));
        return tableMapping;
    }

    /**
     * 将字段规则实体转换为前端对象。
     *
     * @param rule 字段规则实体
     * @return 字段规则对象
     */
    private EtlOfflineFieldRule toFieldRule(EtlColumnMappingRule rule) {
        EtlOfflineFieldRule fieldRule = new EtlOfflineFieldRule();
        fieldRule.setSourceColumnName(rule.getSourceColumnName());
        fieldRule.setTargetColumnName(rule.getTargetColumnName());
        fieldRule.setSourceColumnType(rule.getSourceColumnType());
        fieldRule.setTargetColumnType(rule.getTargetColumnType());
        fieldRule.setMappingAction(rule.getMappingAction());
        fieldRule.setOrdinalPosition(rule.getOrdinalPosition());
        fieldRule.setEnabled(STATUS_ENABLED.equals(rule.getEnabled()));
        fieldRule.setConstantValue(rule.getConstantValue());
        fieldRule.setDefaultValue(rule.getDefaultValue());
        fieldRule.setEmptyValuePolicy(rule.getEmptyValuePolicy());
        fieldRule.setTransformParams(parseTransformParams(rule.getTransformParams()));
        fieldRule.setRemark(rule.getRemark());
        return fieldRule;
    }

    /**
     * 标准化运行配置。
     *
     * @param runtimeConfig 运行配置
     * @return 标准化后配置
     */
    private EtlOfflineRuntimeConfig normalizeRuntimeConfig(EtlOfflineRuntimeConfig runtimeConfig) {
        EtlOfflineRuntimeConfig config = runtimeConfig == null ? new EtlOfflineRuntimeConfig() : runtimeConfig;
        config.setSyncMode(normalizeSyncMode(config.getSyncMode()));
        config.setValidationMode(StrUtil.emptyToDefault(config.getValidationMode(), "NONE"));
        config.setSchemaSyncMode(StrUtil.emptyToDefault(config.getSchemaSyncMode(), "AUTO_ADD_COLUMNS"));
        config.setBatchSize(config.getBatchSize() == null ? DEFAULT_BATCH_SIZE : config.getBatchSize());
        config.setRetryCount(config.getRetryCount() == null ? DEFAULT_RETRY_COUNT : config.getRetryCount());
        config.setRetryInterval(config.getRetryInterval() == null ? DEFAULT_RETRY_INTERVAL : config.getRetryInterval());
        if (config.getTruncateTarget() == null) {
            config.setTruncateTarget(Boolean.FALSE);
        }
        if (config.getRecreateTarget() == null) {
            config.setRecreateTarget(Boolean.FALSE);
        }
        if (config.getUseParamTemplate() == null) {
            config.setUseParamTemplate(Boolean.FALSE);
        }
        if (config.getFullInit() == null) {
            config.setFullInit(Boolean.FALSE);
        }
        if (config.getSyncDdl() == null) {
            config.setSyncDdl(Boolean.FALSE);
        }
        if (config.getAllowConcurrent() == null) {
            config.setAllowConcurrent(Boolean.FALSE);
        }
        if (config.getAutoStart() == null) {
            config.setAutoStart(Boolean.FALSE);
        }
        return config;
    }

    /**
     * 合并任务实体与运行配置，供详情回填。
     *
     * @param job           任务实体
     * @param runtimeConfig 运行配置
     * @return 合并后的运行配置
     */
    private EtlOfflineRuntimeConfig mergeRuntimeConfig(EtlJob job, EtlOfflineRuntimeConfig runtimeConfig) {
        EtlOfflineRuntimeConfig config = runtimeConfig == null ? new EtlOfflineRuntimeConfig() : runtimeConfig;
        config.setSyncMode(StrUtil.emptyToDefault(config.getSyncMode(), job.getSyncMode()));
        config.setBatchSize(config.getBatchSize() == null ? job.getBatchSize() : config.getBatchSize());
        config.setRetryCount(config.getRetryCount() == null ? job.getRetryCount() : config.getRetryCount());
        config.setRetryInterval(config.getRetryInterval() == null ? job.getRetryInterval() : config.getRetryInterval());
        config.setAllowConcurrent(STATUS_ENABLED.equals(job.getConcurrent()));
        config.setAutoStart(STATUS_ENABLED.equals(job.getStatus()));
        config.setCronExpression(StrUtil.emptyToDefault(config.getCronExpression(), job.getCronExpression()));
        config.setValidationMode(StrUtil.emptyToDefault(config.getValidationMode(), "NONE"));
        config.setSchemaSyncMode(StrUtil.emptyToDefault(config.getSchemaSyncMode(), "AUTO_ADD_COLUMNS"));
        return config;
    }

    /**
     * 将创建请求转换为任务实体。
     *
     * @param request      创建请求
     * @param runtimeConfig 运行配置
     * @param tableMapping  表映射
     * @param index         序号
     * @param existing      已存在任务
     * @return 任务实体
     */
    private EtlJob buildJobEntity(EtlOfflineJobCreateRequest request, EtlOfflineRuntimeConfig runtimeConfig,
                                  EtlOfflineTableMapping tableMapping, int index, EtlJob existing) {
        EtlJob etlJob = existing == null ? new EtlJob() : existing;
        etlJob.setJobName(resolveJobName(request.getJobName(), tableMapping, index));
        etlJob.setJobGroup(StrUtil.emptyToDefault(blankToNull(request.getJobGroup()), DEFAULT_JOB_GROUP));
        etlJob.setJobDescription(blankToNull(request.getJobDescription()));
        etlJob.setSourceDatasource(blankToNull(request.getSourceDatasource()));
        etlJob.setSourceSchema(blankToNull(tableMapping.getSourceSchema()));
        etlJob.setSourceTable(blankToNull(tableMapping.getSourceTable()));
        etlJob.setTargetDatasource(blankToNull(request.getTargetDatasource()));
        etlJob.setTargetSchema(blankToNull(tableMapping.getTargetSchema()));
        etlJob.setTargetTable(blankToNull(tableMapping.getTargetTable()));
        etlJob.setSyncMode(normalizeSyncMode(runtimeConfig.getSyncMode()));
        etlJob.setIncrementalColumn(blankToNull(tableMapping.getIncrementalColumn()));
        etlJob.setFilterCondition(blankToNull(tableMapping.getFilterCondition()));
        etlJob.setBatchSize(runtimeConfig.getBatchSize());
        etlJob.setRetryCount(runtimeConfig.getRetryCount());
        etlJob.setRetryInterval(runtimeConfig.getRetryInterval());
        etlJob.setConcurrent(Boolean.TRUE.equals(runtimeConfig.getAllowConcurrent()) ? STATUS_ENABLED : STATUS_DISABLED);
        etlJob.setStatus(Boolean.TRUE.equals(runtimeConfig.getAutoStart()) ? STATUS_ENABLED : STATUS_DISABLED);
        etlJob.setCronExpression(resolveCronExpression(runtimeConfig.getCronExpression(), runtimeConfig.getAutoStart()));
        etlJob.setRemark(blankToNull(request.getRemark()));
        etlJob.setTransformRules(buildTransformRulesJson(runtimeConfig, tableMapping));
        return etlJob;
    }

    /**
     * 保存字段规则。
     *
     * @param etlJob      任务实体
     * @param tableMapping 表映射
     * @param updateMode  是否更新模式
     */
    private void persistColumnRules(EtlJob etlJob, EtlOfflineTableMapping tableMapping, boolean updateMode) {
        if (updateMode) {
            deleteRulesByJobId(etlJob.getJobId());
        }
        List<EtlColumnMappingRule> rules = buildColumnRules(etlJob, tableMapping);
        if (!rules.isEmpty()) {
            columnMappingRuleService.saveBatch(rules);
        }
    }

    /**
     * 构建字段规则实体列表。
     *
     * @param etlJob       任务实体
     * @param tableMapping 表映射
     * @return 字段规则实体列表
     */
    private List<EtlColumnMappingRule> buildColumnRules(EtlJob etlJob, EtlOfflineTableMapping tableMapping) {
        if (tableMapping.getFieldRules() == null || tableMapping.getFieldRules().isEmpty()) {
            return List.of();
        }
        List<EtlColumnMappingRule> rules = new ArrayList<>();
        for (int index = 0; index < tableMapping.getFieldRules().size(); index++) {
            EtlOfflineFieldRule fieldRule = tableMapping.getFieldRules().get(index);
            EtlColumnMappingRule rule = new EtlColumnMappingRule();
            rule.setJobId(etlJob.getJobId());
            rule.setSourceSchemaName(blankToNull(tableMapping.getSourceSchema()));
            rule.setSourceTableName(blankToNull(tableMapping.getSourceTable()));
            rule.setSourceColumnName(blankToNull(fieldRule.getSourceColumnName()));
            rule.setSourceColumnType(blankToNull(fieldRule.getSourceColumnType()));
            rule.setTargetSchemaName(blankToNull(tableMapping.getTargetSchema()));
            rule.setTargetTableName(blankToNull(tableMapping.getTargetTable()));
            rule.setTargetColumnName(blankToNull(fieldRule.getTargetColumnName()));
            rule.setTargetColumnType(blankToNull(fieldRule.getTargetColumnType()));
            rule.setMappingAction(normalizeAction(fieldRule.getMappingAction()));
            rule.setTransformParams(serializeTransformParams(fieldRule.getTransformParams()));
            rule.setConstantValue(blankToNull(fieldRule.getConstantValue()));
            rule.setDefaultValue(blankToNull(fieldRule.getDefaultValue()));
            rule.setEmptyValuePolicy(normalizeEmptyValuePolicy(fieldRule.getEmptyValuePolicy()));
            rule.setOrdinalPosition(fieldRule.getOrdinalPosition() == null ? index + 1 : fieldRule.getOrdinalPosition());
            rule.setEnabled(Boolean.FALSE.equals(fieldRule.getEnabled()) ? STATUS_DISABLED : STATUS_ENABLED);
            rule.setRemark(fieldRule.getRemark());
            rules.add(rule);
        }
        return rules;
    }

    /**
     * 构建 transformRules 包装对象。
     *
     * @param runtimeConfig 运行配置
     * @param tableMapping   表映射
     * @return JSON 字符串
     */
    private String buildTransformRulesJson(EtlOfflineRuntimeConfig runtimeConfig, EtlOfflineTableMapping tableMapping) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("runtimeConfig", runtimeConfig);
        payload.put("tableMapping", tableMapping);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("transformRules JSON生成失败", exception);
        }
    }

    /**
     * 解析 transformRules 中的运行配置。
     *
     * @param transformRules JSON 字符串
     * @return 运行配置
     */
    private EtlOfflineRuntimeConfig parseRuntimeConfig(String transformRules) {
        if (StrUtil.isBlank(transformRules)) {
            return new EtlOfflineRuntimeConfig();
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(transformRules, new TypeReference<Map<String, Object>>() {
            });
            Object runtimeConfig = payload == null ? null : payload.get("runtimeConfig");
            if (runtimeConfig == null) {
                return new EtlOfflineRuntimeConfig();
            }
            return objectMapper.convertValue(runtimeConfig, EtlOfflineRuntimeConfig.class);
        } catch (Exception exception) {
            throw new BusinessException("任务运行配置解析失败", exception);
        }
    }

    /**
     * 获取指定任务的字段规则。
     *
     * @param jobId 任务ID
     * @return 字段规则列表
     */
    private List<EtlColumnMappingRule> listRules(Long jobId) {
        return columnMappingRuleService.list(
            new LambdaQueryWrapper<EtlColumnMappingRule>()
                .eq(EtlColumnMappingRule::getJobId, jobId)
                .orderByAsc(EtlColumnMappingRule::getOrdinalPosition)
        );
    }

    /**
     * 删除任务对应的字段规则。
     *
     * @param jobId 任务ID
     */
    private void deleteRulesByJobId(Long jobId) {
        columnMappingRuleService.remove(
            new LambdaQueryWrapper<EtlColumnMappingRule>().eq(EtlColumnMappingRule::getJobId, jobId)
        );
    }

    /**
     * 校验任务是否存在。
     *
     * @param jobId 任务ID
     * @return 任务实体
     */
    private EtlJob requireJob(Long jobId) {
        if (jobId == null) {
            throw new BusinessException("任务ID不能为空");
        }
        EtlJob job = etlJobService.getById(jobId);
        if (job == null) {
            throw new BusinessException("任务不存在: " + jobId);
        }
        return job;
    }

    /**
     * 校验单个表映射。
     *
     * @param sourceDatasource 源数据源
     * @param targetDatasource 目标数据源
     * @param runtimeConfig    运行配置
     * @param syncMode         同步模式
     * @param tableMapping     表映射
     * @return 表级结果
     */
    private EtlOfflineTableValidationResult validateTableMapping(InfraDatasourceConfig sourceDatasource,
                                                                 InfraDatasourceConfig targetDatasource,
                                                                 EtlOfflineRuntimeConfig runtimeConfig,
                                                                 String syncMode,
                                                                 EtlOfflineTableMapping tableMapping) {
        EtlOfflineTableValidationResult tableResult = new EtlOfflineTableValidationResult();
        if (tableMapping == null) {
            tableResult.getErrors().add("表映射不能为空");
            tableResult.setValid(Boolean.FALSE);
            return tableResult;
        }
        tableResult.setSourceSchema(blankToNull(tableMapping.getSourceSchema()));
        tableResult.setSourceTable(blankToNull(tableMapping.getSourceTable()));
        tableResult.setTargetSchema(blankToNull(tableMapping.getTargetSchema()));
        tableResult.setTargetTable(blankToNull(tableMapping.getTargetTable()));

        List<ColumnMetadata> sourceColumns = loadColumns(sourceDatasource, tableMapping.getSourceSchema(),
            tableMapping.getSourceTable(), "源", tableResult);
        List<ColumnMetadata> targetColumns = loadColumns(targetDatasource, tableMapping.getTargetSchema(),
            tableMapping.getTargetTable(), "目标", tableResult);

        List<String> sourceColumnNames = sourceColumns.stream().map(ColumnMetadata::getColumnName).collect(Collectors.toList());
        List<String> targetColumnNames = targetColumns.stream().map(ColumnMetadata::getColumnName).collect(Collectors.toList());
        tableResult.setSourceColumns(sourceColumnNames);
        tableResult.setTargetColumns(targetColumnNames);
        tableResult.setSuggestedIncrementalColumns(suggestIncrementalColumns(sourceColumnNames));
        tableResult.setSuggestedFieldRules(buildSuggestedFieldRules(sourceColumns, targetColumns, tableMapping.getFieldRules()));

        validateIncrementalColumn(syncMode, tableMapping, sourceColumnNames, tableResult.getErrors());
        validateFieldRules(tableMapping, sourceColumnNames, targetColumnNames, runtimeConfig, tableResult.getErrors(),
            tableResult.getWarnings());

        if (!sourceColumnNames.isEmpty() && !targetColumnNames.isEmpty()) {
            Set<String> sourceNameSet = sourceColumnNames.stream().map(this::normalizeName).collect(Collectors.toSet());
            long sameNameCount = targetColumnNames.stream().map(this::normalizeName).filter(sourceNameSet::contains).count();
            if (sameNameCount == 0) {
                tableResult.getWarnings().add("源表和目标表无同名字段，建议配置字段规则");
            }
        }
        tableResult.setValid(tableResult.getErrors().isEmpty());
        return tableResult;
    }

    /**
     * 加载表字段。
     *
     * @param datasource  数据源
     * @param schemaName  schema
     * @param tableName   表名
     * @param sideLabel   左右侧标记
     * @param tableResult 表级结果
     * @return 字段列表
     */
    private List<ColumnMetadata> loadColumns(InfraDatasourceConfig datasource, String schemaName, String tableName,
                                             String sideLabel, EtlOfflineTableValidationResult tableResult) {
        if (datasource == null) {
            tableResult.getErrors().add(sideLabel + "数据源不可用");
            return List.of();
        }
        if (StrUtil.isBlank(tableName)) {
            tableResult.getErrors().add(sideLabel + "表不能为空");
            return List.of();
        }
        try {
            List<TableMetadata> tables = databaseMetadataService.getTables(datasource.getDatasourceCode(), blankToNull(schemaName));
            boolean exists = tables.stream().map(TableMetadata::getTableName).anyMatch(item -> StrUtil.equalsIgnoreCase(item, tableName));
            if (!exists) {
                tableResult.getErrors().add(sideLabel + "表不存在: " + tableName);
                return List.of();
            }
            return databaseMetadataService.getColumns(datasource.getDatasourceCode(), blankToNull(schemaName), tableName);
        } catch (Exception exception) {
            tableResult.getErrors().add(sideLabel + "表校验失败: " + exception.getMessage());
            return List.of();
        }
    }

    /**
     * 校验增量字段。
     *
     * @param syncMode         同步模式
     * @param tableMapping     表映射
     * @param sourceColumnNames 源字段
     * @param errors           错误列表
     */
    private void validateIncrementalColumn(String syncMode, EtlOfflineTableMapping tableMapping,
                                           List<String> sourceColumnNames, List<String> errors) {
        if (!SYNC_MODE_INCREMENTAL.equals(syncMode)) {
            return;
        }
        String incrementalColumn = StrUtil.trim(tableMapping.getIncrementalColumn());
        if (StrUtil.isBlank(incrementalColumn)) {
            errors.add("增量模式下必须指定增量字段");
            return;
        }
        if (!containsIgnoreCase(sourceColumnNames, incrementalColumn)) {
            errors.add("增量字段在源表中不存在: " + incrementalColumn);
        }
    }

    /**
     * 校验字段规则。
     *
     * @param tableMapping     表映射
     * @param sourceColumnNames 源字段
     * @param targetColumnNames 目标字段
     * @param runtimeConfig    运行配置
     * @param errors           错误列表
     * @param warnings         告警列表
     */
    private void validateFieldRules(EtlOfflineTableMapping tableMapping, List<String> sourceColumnNames,
                                    List<String> targetColumnNames, EtlOfflineRuntimeConfig runtimeConfig,
                                    List<String> errors, List<String> warnings) {
        if (tableMapping.getFieldRules() == null || tableMapping.getFieldRules().isEmpty()) {
            return;
        }
        Set<String> targetNames = new LinkedHashSet<>();
        for (EtlOfflineFieldRule fieldRule : tableMapping.getFieldRules()) {
            String action = normalizeAction(fieldRule.getMappingAction());
            String sourceColumnName = StrUtil.trim(fieldRule.getSourceColumnName());
            String targetColumnName = StrUtil.trim(fieldRule.getTargetColumnName());
            if (StrUtil.isBlank(targetColumnName)) {
                errors.add("目标字段名称不能为空");
                continue;
            }
            if (!targetNames.add(normalizeName(targetColumnName))) {
                errors.add("目标字段重复: " + targetColumnName);
            }
            if (!"CONSTANT".equals(action) && StrUtil.isBlank(sourceColumnName)) {
                errors.add("字段规则缺少源字段: " + targetColumnName);
            }
            if (StrUtil.isNotBlank(sourceColumnName) && !containsIgnoreCase(sourceColumnNames, sourceColumnName)) {
                errors.add("源字段不存在: " + sourceColumnName);
            }
            boolean targetExists = containsIgnoreCase(targetColumnNames, targetColumnName);
            if (!targetExists && !"AUTO_ADD_COLUMNS".equals(normalizeSchemaSyncMode(runtimeConfig))) {
                errors.add("目标字段不存在且未开启自动补字段: " + targetColumnName);
            }
            if ("DEFAULT".equals(action) && StrUtil.isBlank(fieldRule.getEmptyValuePolicy())) {
                warnings.add("字段 " + targetColumnName + " 使用 DEFAULT 动作时建议补充空值策略");
            }
        }
    }

    /**
     * 生成自动匹配字段建议。
     *
     * @param sourceColumns 源字段
     * @param targetColumns 目标字段
     * @param existingRules 已有规则
     * @return 建议列表
     */
    private List<EtlOfflineFieldRule> buildSuggestedFieldRules(List<ColumnMetadata> sourceColumns,
                                                               List<ColumnMetadata> targetColumns,
                                                               List<EtlOfflineFieldRule> existingRules) {
        if (sourceColumns.isEmpty() || targetColumns.isEmpty()) {
            return List.of();
        }
        Set<String> existingTargets = existingRules == null ? Set.of() : existingRules.stream()
            .map(EtlOfflineFieldRule::getTargetColumnName)
            .filter(StrUtil::isNotBlank)
            .map(this::normalizeName)
            .collect(Collectors.toSet());
        Map<String, ColumnMetadata> sourceMap = sourceColumns.stream()
            .collect(Collectors.toMap(item -> normalizeName(item.getColumnName()), item -> item, (left, right) -> left));
        List<EtlOfflineFieldRule> suggestions = new ArrayList<>();
        for (ColumnMetadata targetColumn : targetColumns) {
            String normalized = normalizeName(targetColumn.getColumnName());
            if (existingTargets.contains(normalized)) {
                continue;
            }
            ColumnMetadata sourceColumn = sourceMap.get(normalized);
            if (sourceColumn == null) {
                continue;
            }
            EtlOfflineFieldRule suggestion = new EtlOfflineFieldRule();
            suggestion.setSourceColumnName(sourceColumn.getColumnName());
            suggestion.setTargetColumnName(targetColumn.getColumnName());
            suggestion.setSourceColumnType(sourceColumn.getDataType());
            suggestion.setTargetColumnType(targetColumn.getDataType());
            suggestion.setMappingAction("COPY");
            suggestion.setOrdinalPosition(targetColumn.getOrdinalPosition());
            suggestion.setEnabled(Boolean.TRUE);
            suggestions.add(suggestion);
        }
        return suggestions;
    }

    /**
     * 合并全局告警。
     *
     * @param warnings    原始告警
     * @param tableResults 表级结果
     * @return 合并结果
     */
    private List<String> mergeWarnings(List<String> warnings, List<EtlOfflineTableValidationResult> tableResults) {
        LinkedHashSet<String> merged = new LinkedHashSet<>(warnings);
        for (EtlOfflineTableValidationResult tableResult : tableResults) {
            merged.addAll(tableResult.getWarnings());
        }
        return new ArrayList<>(merged);
    }

    /**
     * 解析转换参数。
     *
     * @param transformParams JSON 字符串
     * @return 参数映射
     */
    private Map<String, Object> parseTransformParams(String transformParams) {
        if (StrUtil.isBlank(transformParams)) {
            return Map.of();
        }
        try {
            Map<String, Object> params = objectMapper.readValue(transformParams, new TypeReference<Map<String, Object>>() {
            });
            return params == null ? Map.of() : params;
        } catch (Exception exception) {
            return Map.of("raw", transformParams);
        }
    }

    /**
     * 序列化转换参数。
     *
     * @param transformParams 转换参数
     * @return JSON 字符串
     */
    private String serializeTransformParams(Map<String, Object> transformParams) {
        if (transformParams == null || transformParams.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(transformParams);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("字段转换参数生成失败", exception);
        }
    }

    /**
     * 解析空值策略。
     *
     * @param policy 空值策略
     * @return 标准策略
     */
    private String normalizeEmptyValuePolicy(String policy) {
        String normalized = StrUtil.nullToEmpty(policy).trim().toUpperCase(Locale.ROOT);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        return switch (normalized) {
            case "KEEP", "KEEP_NULL", "NULL", "DEFAULT", "EMPTY_STRING", "ZERO", "CURRENT_TIME", "SKIP_FIELD" ->
                normalized;
            default -> null;
        };
    }

    /**
     * 解析字段动作。
     *
     * @param action 字段动作
     * @return 标准动作
     */
    private String normalizeAction(String action) {
        String normalized = StrUtil.nullToEmpty(action).trim().toUpperCase(Locale.ROOT);
        if (StrUtil.isBlank(normalized)) {
            return "COPY";
        }
        return switch (normalized) {
            case "COPY", "CONSTANT", "DEFAULT", "TRIM", "UPPER", "LOWER", "DATE_FORMAT", "NUMBER_CAST",
                "NULL_IF_EMPTY" -> normalized;
            default -> "COPY";
        };
    }

    /**
     * 解析结构同步模式。
     *
     * @param runtimeConfig 运行配置
     * @return 标准值
     */
    private String normalizeSchemaSyncMode(EtlOfflineRuntimeConfig runtimeConfig) {
        if (runtimeConfig == null || StrUtil.isBlank(runtimeConfig.getSchemaSyncMode())) {
            return "AUTO_ADD_COLUMNS";
        }
        String normalized = runtimeConfig.getSchemaSyncMode().trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "AUTO_ADD_COLUMNS", "NONE" -> normalized;
            default -> "AUTO_ADD_COLUMNS";
        };
    }

    /**
     * 解析任务名称。
     *
     * @param jobNamePrefix 任务名称前缀
     * @param tableMapping  表映射
     * @param index         序号
     * @return 任务名称
     */
    private String resolveJobName(String jobNamePrefix, EtlOfflineTableMapping tableMapping, int index) {
        if (StrUtil.isNotBlank(tableMapping.getJobName())) {
            return tableMapping.getJobName().trim();
        }
        String prefix = StrUtil.emptyToDefault(blankToNull(jobNamePrefix), "ETL_JOB");
        return prefix + "_" + tableMapping.getSourceTable() + "_" + tableMapping.getTargetTable() + "_" + (index + 1);
    }

    private InfraDatasourceConfig loadDatasourceConfig(String datasourceCode, String sideLabel, List<String> errors) {
        if (StrUtil.isBlank(datasourceCode)) {
            errors.add(sideLabel + "数据源不能为空");
            return null;
        }
        InfraDatasourceConfig datasource = infraDatasourceMapper.selectOne(
            new QueryWrapper<InfraDatasourceConfig>().eq("datasource_code", datasourceCode.trim())
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
