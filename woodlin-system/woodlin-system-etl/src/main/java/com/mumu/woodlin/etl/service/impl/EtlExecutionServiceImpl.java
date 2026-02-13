package com.mumu.woodlin.etl.service.impl;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import javax.sql.DataSource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.datasource.service.InfraDatasourceService;
import com.mumu.woodlin.etl.dialect.DatabaseDialect;
import com.mumu.woodlin.etl.dialect.DatabaseDialectResolver;
import com.mumu.woodlin.etl.entity.EtlColumnMappingRule;
import com.mumu.woodlin.etl.entity.EtlDataBucketChecksum;
import com.mumu.woodlin.etl.entity.EtlDataValidationLog;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.entity.EtlSyncCheckpoint;
import com.mumu.woodlin.etl.entity.EtlTableStructureSnapshot;
import com.mumu.woodlin.etl.enums.SyncMode;
import com.mumu.woodlin.etl.model.TableColumnMetadata;
import com.mumu.woodlin.etl.model.TableSchemaMetadata;
import com.mumu.woodlin.etl.service.EtlTableMetadataInspector;
import com.mumu.woodlin.etl.service.IEtlColumnMappingRuleService;
import com.mumu.woodlin.etl.service.IEtlDataBucketChecksumService;
import com.mumu.woodlin.etl.service.IEtlDataValidationLogService;
import com.mumu.woodlin.etl.service.IEtlExecutionLogService;
import com.mumu.woodlin.etl.service.IEtlExecutionService;
import com.mumu.woodlin.etl.service.IEtlSyncCheckpointService;
import com.mumu.woodlin.etl.service.IEtlTableStructureSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ETL执行服务实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlExecutionServiceImpl implements IEtlExecutionService {

    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_BUCKET_SIZE = 64;
    private static final int MAX_IN_CLAUSE_SIZE = 900;

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final InfraDatasourceService infraDatasourceService;
    private final IEtlExecutionLogService executionLogService;
    private final IEtlColumnMappingRuleService columnMappingRuleService;
    private final IEtlSyncCheckpointService syncCheckpointService;
    private final IEtlDataBucketChecksumService bucketChecksumService;
    private final IEtlDataValidationLogService validationLogService;
    private final IEtlTableStructureSnapshotService structureSnapshotService;
    private final DatabaseDialectResolver dialectResolver;
    private final EtlTableMetadataInspector metadataInspector;
    private final ObjectMapper objectMapper;

    @Async
    @Override
    public void execute(EtlJob job) {
        Long executionLogId = executionLogService.recordExecutionStart(job);
        Summary summary = new Summary();
        try {
            validateJob(job);
            DataSource sourceDataSource = getDataSource(job.getSourceDatasource(), "源");
            DataSource targetDataSource = getDataSource(job.getTargetDatasource(), "目标");
            try (Connection sourceConnection = sourceDataSource.getConnection();
                 Connection targetConnection = targetDataSource.getConnection()) {
                DatabaseDialect sourceDialect = dialectResolver.resolve(sourceConnection);
                DatabaseDialect targetDialect = dialectResolver.resolve(targetConnection);
                TableSchemaMetadata sourceMetadata = metadataInspector.inspect(
                        job.getSourceDatasource(), job.getSourceSchema(), job.getSourceTable()
                );
                TableSchemaMetadata targetMetadata = metadataInspector.inspect(
                        job.getTargetDatasource(), job.getTargetSchema(), job.getTargetTable()
                );
                recordSnapshot(job, sourceMetadata, job.getSourceDatasource(), "SOURCE");
                recordSnapshot(job, targetMetadata, job.getTargetDatasource(), "TARGET");
                List<MappingSpec> mappings = resolveMappings(job, sourceMetadata, targetMetadata);
                boolean keyless = sourceMetadata.getPrimaryKeyColumns().isEmpty()
                        && targetMetadata.getPrimaryKeyColumns().isEmpty();
                String sourcePrimaryKey = resolveSourcePrimaryKey(sourceMetadata, mappings);
                String targetPrimaryKey = resolveTargetPrimaryKey(sourcePrimaryKey, mappings, targetMetadata);
                adjustTargetSchema(job, sourceMetadata, targetMetadata, mappings, targetConnection, targetDialect);
                SyncMode syncMode = SyncMode.fromCode(job.getSyncMode());
                EtlSyncCheckpoint checkpoint = syncCheckpointService.getOrCreate(job);
                if (syncMode == SyncMode.FULL) {
                    clearTargetTable(job, targetConnection, targetDialect);
                }
                int bucketSize = resolveBucketSize(job);
                BucketRetryPolicy retryPolicy = resolveBucketRetryPolicy(job);
                summary = runSync(
                        job,
                        syncMode,
                        sourceConnection,
                        targetConnection,
                        sourceDialect,
                        targetDialect,
                        mappings,
                        sourcePrimaryKey,
                        targetPrimaryKey,
                        checkpoint,
                        executionLogId,
                        bucketSize,
                        retryPolicy,
                        keyless
                );
                String validationStatus = summary.mismatchBucketCount > 0 ? "FAILED" : "SUCCESS";
                syncCheckpointService.updateAfterExecution(
                        checkpoint,
                        summary.lastIncrementalValue,
                        summary.extractedRows,
                        summary.targetComparedRows,
                        summary.appliedBucketCount,
                        summary.skippedBucketCount,
                        validationStatus,
                        executionLogId
                );
                recordValidation(job, executionLogId, summary, validationStatus);
                executionLogService.recordExecutionSuccess(
                        executionLogId,
                        summary.extractedRows,
                        summary.transformedRows,
                        summary.loadedRows,
                        buildExecutionDetail(summary)
                );
            }
        } catch (Exception exception) {
            long failedRows = Math.max(summary.extractedRows - summary.loadedRows, 0);
            executionLogService.recordExecutionFailure(
                    executionLogId,
                    summary.extractedRows,
                    summary.transformedRows,
                    summary.loadedRows,
                    failedRows,
                    exception.getMessage()
            );
            log.error("ETL任务执行失败: jobId={}, jobName={}", job.getJobId(), job.getJobName(), exception);
        }
    }

    private Summary runSync(
            EtlJob job,
            SyncMode syncMode,
            Connection sourceConnection,
            Connection targetConnection,
            DatabaseDialect sourceDialect,
            DatabaseDialect targetDialect,
            List<MappingSpec> mappings,
            String sourcePrimaryKey,
            String targetPrimaryKey,
            EtlSyncCheckpoint checkpoint,
            Long executionLogId,
            int bucketSize,
            BucketRetryPolicy retryPolicy,
            boolean keyless
    ) throws SQLException {
        List<String> sourceColumns = resolveSourceColumns(mappings, sourcePrimaryKey, job.getIncrementalColumn());
        List<String> targetColumns = mappings.stream().map(MappingSpec::targetColumn).distinct().collect(Collectors.toList());
        List<Map<String, Object>> sourceRows = fetchSourceRows(
                job, syncMode, sourceConnection, sourceDialect, sourceColumns, sourcePrimaryKey, checkpoint
        );
        Summary summary = new Summary();
        if (sourceRows.isEmpty()) {
            return summary;
        }
        String targetTable = targetDialect.qualifyTable(job.getTargetSchema(), job.getTargetTable());

        if (keyless) {
            return runKeylessSync(job, targetConnection, targetDialect, mappings, targetColumns,
                    sourceRows, targetTable, executionLogId);
        }

        String upsertSql = targetDialect.buildUpsertSql(targetTable, targetColumns, List.of(targetPrimaryKey));
        Map<Integer, List<Map<String, Object>>> buckets = new HashMap<>();
        for (Map<String, Object> sourceRow : sourceRows) {
            Map<String, Object> transformed = transformRow(sourceRow, mappings);
            Object primaryKeyValue = transformed.get(targetPrimaryKey);
            if (primaryKeyValue == null) {
                log.warn("逻辑主键字段值为空，跳过该行: {}", targetPrimaryKey);
                continue;
            }
            int bucket = Math.floorMod(String.valueOf(primaryKeyValue).hashCode(), bucketSize);
            buckets.computeIfAbsent(bucket, value -> new ArrayList<>()).add(transformed);
            summary.lastIncrementalValue = StringUtils.hasText(job.getIncrementalColumn())
                    ? valueToText(sourceRow.get(job.getIncrementalColumn()))
                    : summary.lastIncrementalValue;
        }
        List<EtlDataBucketChecksum> bucketRecords = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<String, Object>>> bucketEntry : buckets.entrySet()) {
            List<Map<String, Object>> sourceBucketRows = bucketEntry.getValue();
            Set<Object> primaryKeyValues = sourceBucketRows.stream()
                    .map(item -> item.get(targetPrimaryKey))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            List<Map<String, Object>> targetBefore = queryTargetRowsByPrimaryKeys(
                    targetConnection, targetDialect, targetTable, targetColumns, targetPrimaryKey, primaryKeyValues
            );
            String sourceChecksum = checksum(sourceBucketRows, targetColumns, targetPrimaryKey);
            String targetChecksum = checksum(targetBefore, targetColumns, targetPrimaryKey);
            boolean needsSync = sourceBucketRows.size() != targetBefore.size() || !sourceChecksum.equals(targetChecksum);
            boolean bucketApplied = false;
            if (needsSync) {
                upsertRows(targetConnection, upsertSql, targetColumns, sourceBucketRows);
                bucketApplied = true;
            }
            List<Map<String, Object>> targetAfter = queryTargetRowsByPrimaryKeys(
                    targetConnection, targetDialect, targetTable, targetColumns, targetPrimaryKey, primaryKeyValues
            );
            String targetAfterChecksum = checksum(targetAfter, targetColumns, targetPrimaryKey);
            boolean mismatch = sourceBucketRows.size() != targetAfter.size() || !sourceChecksum.equals(targetAfterChecksum);
            int retryCount = 0;
            boolean retrySuccess = false;
            while (mismatch && retryCount < retryPolicy.maxRetryTimes()) {
                retryCount++;
                sleepBeforeRetry(retryPolicy.retryIntervalMillis(), retryCount);
                upsertRows(targetConnection, upsertSql, targetColumns, sourceBucketRows);
                bucketApplied = true;
                targetAfter = queryTargetRowsByPrimaryKeys(
                        targetConnection, targetDialect, targetTable, targetColumns, targetPrimaryKey, primaryKeyValues
                );
                targetAfterChecksum = checksum(targetAfter, targetColumns, targetPrimaryKey);
                mismatch = sourceBucketRows.size() != targetAfter.size() || !sourceChecksum.equals(targetAfterChecksum);
                retrySuccess = !mismatch;
            }
            if (bucketApplied) {
                summary.appliedBucketCount++;
                summary.loadedRows += sourceBucketRows.size();
            } else {
                summary.skippedBucketCount++;
            }
            if (retryCount > 0) {
                summary.retriedBucketCount++;
            }
            if (retrySuccess) {
                summary.recoveredBucketCount++;
            }
            if (mismatch) {
                summary.mismatchBucketCount++;
            }
            String bucketBoundaryStart = resolveBucketBoundary(primaryKeyValues, true);
            String bucketBoundaryEnd = resolveBucketBoundary(primaryKeyValues, false);
            String skipReason = resolveBucketSkipReason(needsSync, bucketApplied, retryCount, retrySuccess, mismatch);
            summary.extractedRows += sourceBucketRows.size();
            summary.transformedRows += sourceBucketRows.size();
            summary.targetComparedRows += targetAfter.size();
            summary.sourceDigest = roll(summary.sourceDigest, sourceChecksum);
            summary.targetDigest = roll(summary.targetDigest, targetAfterChecksum);
            bucketRecords.add(new EtlDataBucketChecksum()
                    .setJobId(job.getJobId())
                    .setExecutionLogId(executionLogId)
                    .setBucketNumber(bucketEntry.getKey())
                    .setSourceRowCount((long) sourceBucketRows.size())
                    .setTargetRowCount((long) targetAfter.size())
                    .setSourceChecksum(sourceChecksum)
                    .setTargetChecksum(targetAfterChecksum)
                    .setBucketBoundaryStart(bucketBoundaryStart)
                    .setBucketBoundaryEnd(bucketBoundaryEnd)
                    .setNeedsSync(needsSync ? "1" : "0")
                    .setRetryCount(retryCount)
                    .setRetrySuccess(retrySuccess ? "1" : "0")
                    .setLastRetryTime(retryCount > 0 ? LocalDateTime.now() : null)
                    .setSkipReason(skipReason)
                    .setComparedAt(LocalDateTime.now())
                    .setTenantId(job.getTenantId()));
        }
        bucketChecksumService.saveBatchRecords(bucketRecords);
        return summary;
    }

    /**
     * 无主键表同步：直接 INSERT 全部数据，跳过桶位校验和重试。
     */
    private Summary runKeylessSync(
            EtlJob job,
            Connection targetConnection,
            DatabaseDialect targetDialect,
            List<MappingSpec> mappings,
            List<String> targetColumns,
            List<Map<String, Object>> sourceRows,
            String targetTable,
            Long executionLogId
    ) throws SQLException {
        log.info("无主键表模式，直接批量插入: table={}, rows={}", targetTable, sourceRows.size());
        String insertSql = targetDialect.buildInsertSql(targetTable, targetColumns);
        Summary summary = new Summary();
        List<Map<String, Object>> transformed = new ArrayList<>(sourceRows.size());
        for (Map<String, Object> sourceRow : sourceRows) {
            transformed.add(transformRow(sourceRow, mappings));
        }
        upsertRows(targetConnection, insertSql, targetColumns, transformed);
        summary.extractedRows = sourceRows.size();
        summary.transformedRows = sourceRows.size();
        summary.loadedRows = sourceRows.size();
        summary.appliedBucketCount = 1;
        return summary;
    }

    private List<Map<String, Object>> fetchSourceRows(
            EtlJob job,
            SyncMode syncMode,
            Connection sourceConnection,
            DatabaseDialect sourceDialect,
            List<String> sourceColumns,
            String sourcePrimaryKey,
            EtlSyncCheckpoint checkpoint
    ) throws SQLException {
        String sourceTable = sourceDialect.qualifyTable(job.getSourceSchema(), job.getSourceTable());
        String selectColumns = sourceColumns.stream().map(sourceDialect::quoteIdentifier).collect(Collectors.joining(", "));
        StringBuilder sql = new StringBuilder("SELECT ").append(selectColumns).append(" FROM ").append(sourceTable);
        List<String> conditions = new ArrayList<>();
        if (StringUtils.hasText(job.getFilterCondition())) {
            conditions.add(job.getFilterCondition());
        }
        boolean bindIncremental = false;
        if (syncMode == SyncMode.INCREMENTAL
                && StringUtils.hasText(job.getIncrementalColumn())
                && StringUtils.hasText(checkpoint.getLastIncrementalValue())) {
            conditions.add(sourceDialect.quoteIdentifier(job.getIncrementalColumn()) + " > ?");
            bindIncremental = true;
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        String orderColumn = StringUtils.hasText(job.getIncrementalColumn()) ? job.getIncrementalColumn() : sourcePrimaryKey;
        sql.append(" ORDER BY ").append(sourceDialect.quoteIdentifier(orderColumn));
        if (!orderColumn.equalsIgnoreCase(sourcePrimaryKey)) {
            sql.append(", ").append(sourceDialect.quoteIdentifier(sourcePrimaryKey));
        }
        int batchSize = resolveBatchSize(job);
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement statement = sourceConnection.prepareStatement(sql.toString())) {
            statement.setFetchSize(batchSize);
            if (bindIncremental) {
                statement.setObject(1, checkpoint.getLastIncrementalValue());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int index = 0; index < sourceColumns.size(); index++) {
                        row.put(sourceColumns.get(index), resultSet.getObject(index + 1));
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }

    private List<Map<String, Object>> queryTargetRowsByPrimaryKeys(
            Connection targetConnection,
            DatabaseDialect targetDialect,
            String targetTable,
            List<String> targetColumns,
            String targetPrimaryKey,
            Collection<Object> primaryKeyValues
    ) throws SQLException {
        if (primaryKeyValues == null || primaryKeyValues.isEmpty()) {
            return new ArrayList<>();
        }
        List<Object> keyList = new ArrayList<>(primaryKeyValues);
        List<Map<String, Object>> result = new ArrayList<>();
        int offset = 0;
        while (offset < keyList.size()) {
            int end = Math.min(offset + MAX_IN_CLAUSE_SIZE, keyList.size());
            List<Object> subKeys = keyList.subList(offset, end);
            String sql = targetDialect.buildSelectByPrimaryKeyInSql(targetTable, targetColumns, targetPrimaryKey, subKeys.size());
            try (PreparedStatement statement = targetConnection.prepareStatement(sql)) {
                for (int index = 0; index < subKeys.size(); index++) {
                    statement.setObject(index + 1, subKeys.get(index));
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int index = 0; index < targetColumns.size(); index++) {
                            row.put(targetColumns.get(index), resultSet.getObject(index + 1));
                        }
                        result.add(row);
                    }
                }
            }
            offset = end;
        }
        return result;
    }

    private static final int UPSERT_BATCH_SIZE = 500;

    private void upsertRows(
            Connection targetConnection,
            String upsertSql,
            List<String> targetColumns,
            List<Map<String, Object>> sourceRows
    ) throws SQLException {
        try (PreparedStatement statement = targetConnection.prepareStatement(upsertSql)) {
            int count = 0;
            for (Map<String, Object> sourceRow : sourceRows) {
                for (int index = 0; index < targetColumns.size(); index++) {
                    statement.setObject(index + 1, sourceRow.get(targetColumns.get(index)));
                }
                statement.addBatch();
                count++;
                if (count % UPSERT_BATCH_SIZE == 0) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
            }
            if (count % UPSERT_BATCH_SIZE != 0) {
                statement.executeBatch();
            }
        }
    }

    private void adjustTargetSchema(
            EtlJob job,
            TableSchemaMetadata sourceMetadata,
            TableSchemaMetadata targetMetadata,
            List<MappingSpec> mappings,
            Connection targetConnection,
            DatabaseDialect targetDialect
    ) throws SQLException {
        String targetTable = targetDialect.qualifyTable(job.getTargetSchema(), job.getTargetTable());
        for (MappingSpec mapping : mappings) {
            boolean exists = targetMetadata.findColumn(mapping.targetColumn()).isPresent();
            if (exists) {
                continue;
            }
            TableColumnMetadata sourceColumn = sourceMetadata.findColumn(mapping.sourceColumn())
                    .orElseThrow(() -> new BusinessException("源字段不存在: " + mapping.sourceColumn()));
            String sql = targetDialect.buildAddColumnSql(targetTable, mapping.targetColumn(), sourceColumn.getTypeName());
            try (PreparedStatement statement = targetConnection.prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

    private List<MappingSpec> resolveMappings(
            EtlJob job,
            TableSchemaMetadata sourceMetadata,
            TableSchemaMetadata targetMetadata
    ) {
        List<EtlColumnMappingRule> rules = columnMappingRuleService.listEnabledRulesByJobId(job.getJobId());
        if (!rules.isEmpty()) {
            return rules.stream().map(item -> new MappingSpec(item.getSourceColumnName(), item.getTargetColumnName()))
                    .collect(Collectors.toList());
        }
        if (StringUtils.hasText(job.getColumnMapping())) {
            try {
                Map<String, String> mapping = objectMapper.readValue(
                        job.getColumnMapping(), new TypeReference<Map<String, String>>() {
                        }
                );
                return mapping.entrySet().stream().map(item -> new MappingSpec(item.getKey(), item.getValue()))
                        .collect(Collectors.toList());
            } catch (Exception exception) {
                throw new BusinessException("字段映射JSON解析失败");
            }
        }
        List<MappingSpec> identityMappings = new ArrayList<>();
        for (TableColumnMetadata sourceColumn : sourceMetadata.getColumns()) {
            if (targetMetadata.findColumn(sourceColumn.getColumnName()).isPresent()) {
                identityMappings.add(new MappingSpec(sourceColumn.getColumnName(), sourceColumn.getColumnName()));
            }
        }
        return identityMappings;
    }

    /**
     * 解析源表主键字段。对于无主键表，回退到第一个映射字段作为逻辑主键。
     */
    private String resolveSourcePrimaryKey(TableSchemaMetadata sourceMetadata, List<MappingSpec> mappings) {
        if (!sourceMetadata.getPrimaryKeyColumns().isEmpty()) {
            return sourceMetadata.getPrimaryKeyColumns().get(0);
        }
        log.warn("源表 {} 无主键定义，回退使用第一个映射字段作为逻辑主键", sourceMetadata.getTableName());
        if (!mappings.isEmpty()) {
            return mappings.get(0).sourceColumn();
        }
        throw new BusinessException("无法解析源主键字段，源表无主键且无字段映射");
    }

    /**
     * 解析目标表主键字段。对于无主键表，回退到第一个映射字段作为逻辑主键。
     */
    private String resolveTargetPrimaryKey(
            String sourcePrimaryKey,
            List<MappingSpec> mappings,
            TableSchemaMetadata targetMetadata
    ) {
        if (!targetMetadata.getPrimaryKeyColumns().isEmpty()) {
            return targetMetadata.getPrimaryKeyColumns().get(0);
        }
        String targetPrimaryKey = mappings.stream()
                .filter(item -> item.sourceColumn().equalsIgnoreCase(sourcePrimaryKey))
                .map(MappingSpec::targetColumn)
                .findFirst()
                .orElse(sourcePrimaryKey);
        if (targetMetadata.findColumn(targetPrimaryKey).isEmpty()) {
            throw new BusinessException("目标主键字段不存在: " + targetPrimaryKey);
        }
        log.warn("目标表无主键定义，使用逻辑主键: {}", targetPrimaryKey);
        return targetPrimaryKey;
    }

    private List<String> resolveSourceColumns(List<MappingSpec> mappings, String sourcePrimaryKey, String incrementalColumn) {
        Set<String> sourceColumns = new LinkedHashSet<>();
        for (MappingSpec mapping : mappings) {
            sourceColumns.add(mapping.sourceColumn());
        }
        sourceColumns.add(sourcePrimaryKey);
        if (StringUtils.hasText(incrementalColumn)) {
            sourceColumns.add(incrementalColumn);
        }
        return new ArrayList<>(sourceColumns);
    }

    private Map<String, Object> transformRow(Map<String, Object> sourceRow, List<MappingSpec> mappings) {
        Map<String, Object> targetRow = new LinkedHashMap<>();
        for (MappingSpec mapping : mappings) {
            targetRow.put(mapping.targetColumn(), sourceRow.get(mapping.sourceColumn()));
        }
        return targetRow;
    }

    private String checksum(List<Map<String, Object>> rows, List<String> columns, String primaryKeyColumn) {
        CRC32 crc32 = new CRC32();
        List<Map<String, Object>> sortedRows = new ArrayList<>(rows);
        sortedRows.sort(Comparator.comparing(item -> valueToText(item.get(primaryKeyColumn))));
        for (Map<String, Object> row : sortedRows) {
            StringJoiner joiner = new StringJoiner("|");
            for (String column : columns) {
                joiner.add(valueToText(row.get(column)));
            }
            byte[] bytes = joiner.toString().getBytes(StandardCharsets.UTF_8);
            crc32.update(bytes, 0, bytes.length);
        }
        return Long.toHexString(crc32.getValue());
    }

    private String roll(String current, String next) {
        CRC32 crc32 = new CRC32();
        String merged = (current == null ? "" : current) + "|" + (next == null ? "" : next);
        byte[] bytes = merged.getBytes(StandardCharsets.UTF_8);
        crc32.update(bytes, 0, bytes.length);
        return Long.toHexString(crc32.getValue());
    }

    private void recordValidation(EtlJob job, Long executionLogId, Summary summary, String validationStatus) {
        validationLogService.record(new EtlDataValidationLog()
                .setJobId(job.getJobId())
                .setExecutionLogId(executionLogId)
                .setValidationType(job.getSyncMode())
                .setSourceRowCount(summary.extractedRows)
                .setTargetRowCount(summary.targetComparedRows)
                .setSourceChecksum(summary.sourceDigest)
                .setTargetChecksum(summary.targetDigest)
                .setBucketCount(summary.appliedBucketCount + summary.skippedBucketCount)
                .setMismatchBucketCount(summary.mismatchBucketCount)
                .setValidationStatus(validationStatus)
                .setValidationMessage("mismatchBuckets=" + summary.mismatchBucketCount)
                .setValidatedAt(LocalDateTime.now())
                .setTenantId(job.getTenantId()));
    }

    private String buildExecutionDetail(Summary summary) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("extractedRows", summary.extractedRows);
        detail.put("transformedRows", summary.transformedRows);
        detail.put("loadedRows", summary.loadedRows);
        detail.put("appliedBucketCount", summary.appliedBucketCount);
        detail.put("skippedBucketCount", summary.skippedBucketCount);
        detail.put("mismatchBucketCount", summary.mismatchBucketCount);
        detail.put("retriedBucketCount", summary.retriedBucketCount);
        detail.put("recoveredBucketCount", summary.recoveredBucketCount);
        detail.put("sourceDigest", summary.sourceDigest);
        detail.put("targetDigest", summary.targetDigest);
        detail.put("lastIncrementalValue", summary.lastIncrementalValue);
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (Exception exception) {
            return detail.toString();
        }
    }

    private void recordSnapshot(EtlJob job, TableSchemaMetadata metadata, String datasourceName, String tag) {
        structureSnapshotService.save(new EtlTableStructureSnapshot()
                .setJobId(job.getJobId())
                .setDatasourceName(datasourceName)
                .setSchemaName(metadata.getSchemaName())
                .setTableName(metadata.getTableName())
                .setColumnCount(metadata.getColumns().size())
                .setPrimaryKeyColumns(String.join(",", metadata.getPrimaryKeyColumns()))
                .setStructureDigest(metadata.getStructureDigest())
                .setSnapshotTime(LocalDateTime.now())
                .setTenantId(job.getTenantId())
                .setRemark(tag));
    }

    private void clearTargetTable(EtlJob job, Connection targetConnection, DatabaseDialect targetDialect) throws SQLException {
        String targetTable = targetDialect.qualifyTable(job.getTargetSchema(), job.getTargetTable());
        String truncateSql = targetDialect.buildTruncateSql(targetTable);
        try (PreparedStatement statement = targetConnection.prepareStatement(truncateSql)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            log.warn("TRUNCATE 失败，回退使用 DELETE: {}", exception.getMessage());
            String deleteSql = targetDialect.buildDeleteAllSql(targetTable);
            try (PreparedStatement statement = targetConnection.prepareStatement(deleteSql)) {
                statement.executeUpdate();
            }
        }
    }

    private int resolveBatchSize(EtlJob job) {
        if (job.getBatchSize() == null || job.getBatchSize() <= 0) {
            return DEFAULT_BATCH_SIZE;
        }
        return job.getBatchSize();
    }

    private int resolveBucketSize(EtlJob job) {
        Map<String, Object> transformConfig = parseTransformConfig(job.getTransformRules());
        Object value = transformConfig.get("bucketSize");
        if (value == null) {
            return DEFAULT_BUCKET_SIZE;
        }
        int bucketSize = parseNonNegativeInt(value, DEFAULT_BUCKET_SIZE);
        return bucketSize > 0 ? bucketSize : DEFAULT_BUCKET_SIZE;
    }

    private BucketRetryPolicy resolveBucketRetryPolicy(EtlJob job) {
        int maxRetryTimes = job.getRetryCount() == null ? 0 : Math.max(job.getRetryCount(), 0);
        long retryIntervalMillis = job.getRetryInterval() == null ? 0L : Math.max(job.getRetryInterval(), 0) * 1000L;
        Map<String, Object> transformConfig = parseTransformConfig(job.getTransformRules());
        maxRetryTimes = parseNonNegativeInt(transformConfig.get("mismatchBucketRetryCount"), maxRetryTimes);
        Object retryIntervalMs = transformConfig.get("mismatchBucketRetryIntervalMs");
        if (retryIntervalMs != null) {
            retryIntervalMillis = parseNonNegativeLong(retryIntervalMs, retryIntervalMillis);
        }
        Object retryIntervalSec = transformConfig.get("mismatchBucketRetryIntervalSec");
        if (retryIntervalSec != null) {
            long retrySeconds = parseNonNegativeLong(retryIntervalSec, retryIntervalMillis / 1000L);
            retryIntervalMillis = retrySeconds * 1000L;
        }
        return new BucketRetryPolicy(maxRetryTimes, retryIntervalMillis);
    }

    private Map<String, Object> parseTransformConfig(String transformRules) {
        if (!StringUtils.hasText(transformRules)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(transformRules, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception exception) {
            log.warn("解析 transformRules 失败，按默认策略继续: {}", exception.getMessage());
            return new HashMap<>();
        }
    }

    private int parseNonNegativeInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.toString());
            return Math.max(parsed, 0);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    private long parseNonNegativeLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.toString());
            return Math.max(parsed, 0L);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    private void sleepBeforeRetry(long retryIntervalMillis, int retryAttempt) {
        if (retryIntervalMillis <= 0) {
            return;
        }
        long backoffInterval = retryIntervalMillis * (1L << Math.min(retryAttempt - 1, 4));
        try {
            Thread.sleep(backoffInterval);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("桶位重试等待被中断", exception);
        }
    }

    private String resolveBucketSkipReason(
            boolean needsSync,
            boolean bucketApplied,
            int retryCount,
            boolean retrySuccess,
            boolean mismatch
    ) {
        if (!bucketApplied) {
            return "bucket_checksum_equal";
        }
        if (retryCount <= 0) {
            return needsSync ? null : "bucket_checksum_mismatch";
        }
        if (retrySuccess) {
            return "bucket_retry_recovered";
        }
        if (mismatch) {
            return "bucket_retry_exhausted";
        }
        return null;
    }

    private String valueToText(Object value) {
        return value == null ? "NULL" : String.valueOf(value);
    }

    private String resolveBucketBoundary(Collection<Object> primaryKeyValues, boolean first) {
        if (primaryKeyValues == null || primaryKeyValues.isEmpty()) {
            return null;
        }
        List<String> boundaries = primaryKeyValues.stream()
                .filter(Objects::nonNull)
                .map(this::valueToText)
                .sorted()
                .collect(Collectors.toList());
        if (boundaries.isEmpty()) {
            return null;
        }
        return first ? boundaries.get(0) : boundaries.get(boundaries.size() - 1);
    }

    private DataSource getDataSource(String datasourceName, String label) {
        DataSource dataSource = dynamicRoutingDataSource.getDataSource(datasourceName);
        if (dataSource != null) {
            return dataSource;
        }
        log.debug("{}数据源 {} 未在动态数据源中注册，尝试使用 Infra JDBC 数据源配置", label, datasourceName);
        try {
            return infraDatasourceService.getDataSourceByCode(datasourceName);
        } catch (BusinessException exception) {
            throw new BusinessException(label + "数据源不可用: " + datasourceName + "，" + exception.getMessage(), exception);
        }
    }

    private void validateJob(EtlJob job) {
        if (job == null || job.getJobId() == null) {
            throw new BusinessException("任务信息不能为空");
        }
        if (!StringUtils.hasText(job.getSourceDatasource()) || !StringUtils.hasText(job.getTargetDatasource())) {
            throw new BusinessException("源和目标数据源不能为空");
        }
        if (!StringUtils.hasText(job.getSourceTable()) || !StringUtils.hasText(job.getTargetTable())) {
            throw new BusinessException("源和目标表不能为空");
        }
    }

    private record MappingSpec(String sourceColumn, String targetColumn) {
    }

    private record BucketRetryPolicy(int maxRetryTimes, long retryIntervalMillis) {
    }

    private static class Summary {
        private long extractedRows;
        private long transformedRows;
        private long loadedRows;
        private long targetComparedRows;
        private int appliedBucketCount;
        private int skippedBucketCount;
        private int mismatchBucketCount;
        private int retriedBucketCount;
        private int recoveredBucketCount;
        private String lastIncrementalValue;
        private String sourceDigest = "";
        private String targetDigest = "";
    }
}
