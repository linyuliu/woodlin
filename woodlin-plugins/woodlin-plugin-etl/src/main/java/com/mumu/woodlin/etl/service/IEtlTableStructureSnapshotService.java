package com.mumu.woodlin.etl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.etl.entity.EtlTableStructureSnapshot;

/**
 * ETL 表结构快照服务接口。
 *
 * @author mumu
 * @since 1.0.0
 */
public interface IEtlTableStructureSnapshotService extends IService<EtlTableStructureSnapshot> {

    /**
     * 查询最近一次快照。
     *
     * @param jobId 任务ID
     * @param datasourceName 数据源名称
     * @param schemaName schema 名称
     * @param tableName 表名称
     * @return 最近一次快照
     */
    EtlTableStructureSnapshot findLatest(Long jobId, String datasourceName, String schemaName, String tableName);
}
