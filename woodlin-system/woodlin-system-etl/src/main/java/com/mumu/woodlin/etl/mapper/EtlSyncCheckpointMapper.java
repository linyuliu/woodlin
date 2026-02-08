package com.mumu.woodlin.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.etl.entity.EtlSyncCheckpoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * ETL 同步检查点 Mapper。
 *
 * @author mumu
 * @since 1.0.0
 */
@Mapper
public interface EtlSyncCheckpointMapper extends BaseMapper<EtlSyncCheckpoint> {
}
