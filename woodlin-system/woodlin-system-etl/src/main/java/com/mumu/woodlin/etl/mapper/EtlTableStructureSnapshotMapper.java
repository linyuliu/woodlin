package com.mumu.woodlin.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.etl.entity.EtlTableStructureSnapshot;
import org.apache.ibatis.annotations.Mapper;

/**
 * ETL 表结构快照 Mapper。
 *
 * @author mumu
 * @since 1.0.0
 */
@Mapper
public interface EtlTableStructureSnapshotMapper extends BaseMapper<EtlTableStructureSnapshot> {
}
