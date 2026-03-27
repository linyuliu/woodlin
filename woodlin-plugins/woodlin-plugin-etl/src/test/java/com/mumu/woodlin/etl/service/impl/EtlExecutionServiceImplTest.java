package com.mumu.woodlin.etl.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.datasource.service.InfraDatasourceService;
import com.mumu.woodlin.etl.entity.EtlColumnMappingRule;
import com.mumu.woodlin.etl.service.EtlTableMetadataInspector;
import com.mumu.woodlin.etl.service.IEtlColumnMappingRuleService;
import com.mumu.woodlin.etl.service.IEtlDataBucketChecksumService;
import com.mumu.woodlin.etl.service.IEtlDataValidationLogService;
import com.mumu.woodlin.etl.service.IEtlExecutionLogService;
import com.mumu.woodlin.etl.service.IEtlSyncCheckpointService;
import com.mumu.woodlin.etl.service.IEtlTableStructureSnapshotService;
import com.mumu.woodlin.etl.dialect.DatabaseDialectResolver;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ETL 执行转换逻辑测试。
 *
 * @author mumu
 * @since 1.0.0
 */
class EtlExecutionServiceImplTest {

    private EtlExecutionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EtlExecutionServiceImpl(
            mock(DynamicRoutingDataSource.class),
            mock(InfraDatasourceService.class),
            mock(IEtlExecutionLogService.class),
            mock(IEtlColumnMappingRuleService.class),
            mock(IEtlSyncCheckpointService.class),
            mock(IEtlDataBucketChecksumService.class),
            mock(IEtlDataValidationLogService.class),
            mock(IEtlTableStructureSnapshotService.class),
            mock(DatabaseDialectResolver.class),
            mock(EtlTableMetadataInspector.class),
            new ObjectMapper()
        );
    }

    @Test
    void transformValueShouldApplyStructuredRule() throws Exception {
        Method method = EtlExecutionServiceImpl.class.getDeclaredMethod(
            "transformValue",
            Object.class,
            EtlColumnMappingRule.class
        );
        method.setAccessible(true);

        EtlColumnMappingRule trimRule = new EtlColumnMappingRule();
        trimRule.setMappingAction("TRIM");
        Object trimResult = method.invoke(service, "  hello  ", trimRule);
        assertThat(trimResult).isEqualTo("hello");

        EtlColumnMappingRule upperRule = new EtlColumnMappingRule();
        upperRule.setMappingAction("UPPER");
        Object upperResult = method.invoke(service, "abc", upperRule);
        assertThat(upperResult).isEqualTo("ABC");

        EtlColumnMappingRule constantRule = new EtlColumnMappingRule();
        constantRule.setMappingAction("CONSTANT");
        constantRule.setConstantValue("固定值");
        Object constantResult = method.invoke(service, null, constantRule);
        assertThat(constantResult).isEqualTo("固定值");

        EtlColumnMappingRule defaultRule = new EtlColumnMappingRule();
        defaultRule.setMappingAction("DEFAULT");
        defaultRule.setDefaultValue("备用值");
        Object defaultResult = method.invoke(service, "", defaultRule);
        assertThat(defaultResult).isEqualTo("备用值");
    }
}
