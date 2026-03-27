package com.mumu.woodlin.etl.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import com.mumu.woodlin.datasource.mapper.InfraDatasourceMapper;
import com.mumu.woodlin.datasource.service.DatabaseMetadataService;
import com.mumu.woodlin.etl.entity.EtlColumnMappingRule;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.model.EtlOfflineFieldRule;
import com.mumu.woodlin.etl.model.EtlOfflineRuntimeConfig;
import com.mumu.woodlin.etl.model.EtlOfflineTableMapping;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobCreateRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineValidationRequest;
import com.mumu.woodlin.etl.model.response.EtlOfflineCreateJobResponse;
import com.mumu.woodlin.etl.model.response.EtlOfflineTableValidationResult;
import com.mumu.woodlin.etl.model.response.EtlOfflineValidationResult;
import com.mumu.woodlin.etl.service.IEtlColumnMappingRuleService;
import com.mumu.woodlin.etl.service.IEtlJobService;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ETL 离线向导服务测试。
 *
 * @author mumu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EtlOfflineServiceImplTest {

    @Mock
    private InfraDatasourceMapper infraDatasourceMapper;

    @Mock
    private DatabaseMetadataService databaseMetadataService;

    @Mock
    private IEtlJobService etlJobService;

    @Mock
    private IEtlColumnMappingRuleService columnMappingRuleService;

    private EtlOfflineServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EtlOfflineServiceImpl(
            infraDatasourceMapper,
            databaseMetadataService,
            etlJobService,
            columnMappingRuleService,
            new ObjectMapper()
        );
    }

    @Test
    void createOfflineJobShouldBatchCreateJobsAndPersistRules() {
        InfraDatasourceConfig sourceDatasource = buildDatasource("src_ds");
        InfraDatasourceConfig targetDatasource = buildDatasource("dst_ds");
        when(infraDatasourceMapper.selectOne(any(QueryWrapper.class)))
            .thenReturn(sourceDatasource, targetDatasource);
        when(databaseMetadataService.getTables(anyString(), any())).thenReturn(
            List.of(buildTable("orders_src")),
            List.of(buildTable("orders_tgt")),
            List.of(buildTable("orders_src2")),
            List.of(buildTable("orders_tgt2"))
        );
        when(databaseMetadataService.getColumns(anyString(), any(), anyString())).thenReturn(
            List.of(buildColumn("id", "BIGINT", 1), buildColumn("name", "VARCHAR", 2)),
            List.of(buildColumn("id", "BIGINT", 1), buildColumn("name", "VARCHAR", 2)),
            List.of(buildColumn("id", "BIGINT", 1), buildColumn("name", "VARCHAR", 2)),
            List.of(buildColumn("id", "BIGINT", 1), buildColumn("name", "VARCHAR", 2))
        );
        when(etlJobService.createJob(any(EtlJob.class))).thenAnswer(invocation -> {
            EtlJob job = invocation.getArgument(0);
            job.setJobId(jobSequence.incrementAndGet());
            return true;
        });
        when(columnMappingRuleService.saveBatch(anyList())).thenReturn(true);

        EtlOfflineJobCreateRequest request = buildCreateRequest();
        EtlOfflineCreateJobResponse response = service.createOfflineJob(request);

        assertThat(response.getCreatedJobCount()).isEqualTo(2);
        assertThat(response.getCreatedJobIds()).hasSize(2);

        ArgumentCaptor<EtlJob> jobCaptor = ArgumentCaptor.forClass(EtlJob.class);
        verify(etlJobService, times(2)).createJob(jobCaptor.capture());
        EtlJob firstJob = jobCaptor.getAllValues().get(0);
        assertThat(firstJob.getColumnMapping()).contains("\"id\":\"id\"");
        assertThat(firstJob.getTransformRules()).contains("runtimeConfig");

        ArgumentCaptor<List<EtlColumnMappingRule>> ruleCaptor = ArgumentCaptor.forClass(List.class);
        verify(columnMappingRuleService, times(2)).saveBatch(ruleCaptor.capture());
        assertThat(ruleCaptor.getAllValues().get(0)).hasSize(1);
        assertThat(ruleCaptor.getAllValues().get(0).get(0).getTargetColumnName()).isEqualTo("id");
    }

    @Test
    void validateShouldReturnTableSuggestionsAndWarnings() {
        InfraDatasourceConfig sourceDatasource = buildDatasource("src_ds");
        InfraDatasourceConfig targetDatasource = buildDatasource("dst_ds");
        when(infraDatasourceMapper.selectOne(any(QueryWrapper.class)))
            .thenReturn(sourceDatasource, targetDatasource);
        when(databaseMetadataService.getTables(anyString(), any())).thenReturn(
            List.of(buildTable("orders_src")),
            List.of(buildTable("orders_tgt"))
        );
        when(databaseMetadataService.getColumns(anyString(), any(), anyString())).thenReturn(
            List.of(
                buildColumn("id", "BIGINT", 1),
                buildColumn("name", "VARCHAR", 2),
                buildColumn("updated_at", "TIMESTAMP", 3)
            ),
            List.of(
                buildColumn("id", "BIGINT", 1),
                buildColumn("name", "VARCHAR", 2),
                buildColumn("extra_field", "VARCHAR", 3)
            )
        );

        EtlOfflineValidationRequest request = new EtlOfflineValidationRequest();
        request.setSourceDatasource("src_ds");
        request.setTargetDatasource("dst_ds");
        EtlOfflineRuntimeConfig runtimeConfig = new EtlOfflineRuntimeConfig();
        runtimeConfig.setSyncMode("FULL");
        runtimeConfig.setSchemaSyncMode("AUTO_ADD_COLUMNS");
        request.setRuntimeConfig(runtimeConfig);
        EtlOfflineTableMapping tableMapping = new EtlOfflineTableMapping();
        tableMapping.setSourceTable("orders_src");
        tableMapping.setTargetTable("orders_tgt");
        request.setTableMappings(List.of(tableMapping));

        EtlOfflineValidationResult result = service.validate(request);

        assertThat(result.getValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getTableResults()).hasSize(1);
        EtlOfflineTableValidationResult tableResult = result.getTableResults().get(0);
        assertThat(tableResult.getSuggestedFieldRules()).hasSize(2);
        assertThat(tableResult.getSuggestedIncrementalColumns()).contains("updated_at");
    }

    private final AtomicLong jobSequence = new AtomicLong(1000L);

    /**
     * 构造数据源配置。
     *
     * @param code 数据源编码
     * @return 数据源配置
     */
    private InfraDatasourceConfig buildDatasource(String code) {
        InfraDatasourceConfig config = new InfraDatasourceConfig();
        config.setDatasourceCode(code);
        config.setDatasourceName(code);
        config.setDatasourceType("MYSQL");
        config.setStatus(1);
        return config;
    }

    /**
     * 构造表元数据。
     *
     * @param tableName 表名
     * @return 表元数据
     */
    private TableMetadata buildTable(String tableName) {
        return TableMetadata.builder()
            .tableName(tableName)
            .schemaName("public")
            .comment(tableName)
            .build();
    }

    /**
     * 构造字段元数据。
     *
     * @param columnName  字段名
     * @param dataType    字段类型
     * @param ordinal     字段顺序
     * @return 字段元数据
     */
    private ColumnMetadata buildColumn(String columnName, String dataType, Integer ordinal) {
        return ColumnMetadata.builder()
            .columnName(columnName)
            .dataType(dataType)
            .ordinalPosition(ordinal)
            .nullable(Boolean.TRUE)
            .build();
    }

    /**
     * 构造批量创建请求。
     *
     * @return 创建请求
     */
    private EtlOfflineJobCreateRequest buildCreateRequest() {
        EtlOfflineJobCreateRequest request = new EtlOfflineJobCreateRequest();
        request.setJobName("batch_job");
        request.setJobGroup("OFFLINE_SYNC");
        request.setJobDescription("批量创建测试");
        request.setSourceDatasource("src_ds");
        request.setTargetDatasource("dst_ds");
        request.setRemark("remark");

        EtlOfflineRuntimeConfig runtimeConfig = new EtlOfflineRuntimeConfig();
        runtimeConfig.setSyncMode("FULL");
        runtimeConfig.setBatchSize(1000);
        runtimeConfig.setRetryCount(3);
        runtimeConfig.setRetryInterval(60);
        runtimeConfig.setAllowConcurrent(Boolean.FALSE);
        runtimeConfig.setAutoStart(Boolean.TRUE);
        runtimeConfig.setCronExpression("0 0 2 * * ?");
        runtimeConfig.setSchemaSyncMode("AUTO_ADD_COLUMNS");
        runtimeConfig.setTruncateTarget(Boolean.TRUE);
        request.setRuntimeConfig(runtimeConfig);

        request.setTableMappings(List.of(buildMapping("orders_src", "orders_tgt"), buildMapping("orders_src2", "orders_tgt2")));
        return request;
    }

    /**
     * 构造表映射。
     *
     * @param sourceTable 源表
     * @param targetTable 目标表
     * @return 表映射
     */
    private EtlOfflineTableMapping buildMapping(String sourceTable, String targetTable) {
        EtlOfflineTableMapping mapping = new EtlOfflineTableMapping();
        mapping.setSourceSchema("public");
        mapping.setSourceTable(sourceTable);
        mapping.setTargetSchema("public");
        mapping.setTargetTable(targetTable);
        mapping.setIncrementalColumn("updated_at");
        mapping.setFilterCondition("status = 1");

        EtlOfflineFieldRule fieldRule = new EtlOfflineFieldRule();
        fieldRule.setSourceColumnName("id");
        fieldRule.setTargetColumnName("id");
        fieldRule.setSourceColumnType("BIGINT");
        fieldRule.setTargetColumnType("BIGINT");
        fieldRule.setMappingAction("COPY");
        fieldRule.setEnabled(Boolean.TRUE);
        mapping.setFieldRules(List.of(fieldRule));
        return mapping;
    }
}
