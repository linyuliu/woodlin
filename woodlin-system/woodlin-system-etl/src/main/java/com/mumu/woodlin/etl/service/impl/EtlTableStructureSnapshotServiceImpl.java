package com.mumu.woodlin.etl.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.etl.entity.EtlTableStructureSnapshot;
import com.mumu.woodlin.etl.mapper.EtlTableStructureSnapshotMapper;
import com.mumu.woodlin.etl.service.IEtlTableStructureSnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ETL 表结构快照服务实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Service
public class EtlTableStructureSnapshotServiceImpl
        extends ServiceImpl<EtlTableStructureSnapshotMapper, EtlTableStructureSnapshot>
        implements IEtlTableStructureSnapshotService {

    @Override
    public EtlTableStructureSnapshot findLatest(Long jobId, String datasourceName, String schemaName, String tableName) {
        LambdaQueryWrapper<EtlTableStructureSnapshot> queryWrapper = new LambdaQueryWrapper<EtlTableStructureSnapshot>()
                .eq(EtlTableStructureSnapshot::getJobId, jobId)
                .eq(EtlTableStructureSnapshot::getDatasourceName, datasourceName)
                .eq(EtlTableStructureSnapshot::getTableName, tableName)
                .orderByDesc(EtlTableStructureSnapshot::getSnapshotTime);
        if (StringUtils.hasText(schemaName)) {
            queryWrapper.eq(EtlTableStructureSnapshot::getSchemaName, schemaName);
        } else {
            queryWrapper.isNull(EtlTableStructureSnapshot::getSchemaName);
        }
        List<EtlTableStructureSnapshot> snapshots = this.list(queryWrapper);
        if (snapshots == null || snapshots.isEmpty()) {
            return null;
        }
        return snapshots.get(0);
    }
}
