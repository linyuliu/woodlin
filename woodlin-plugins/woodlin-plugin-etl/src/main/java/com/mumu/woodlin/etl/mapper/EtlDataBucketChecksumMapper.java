package com.mumu.woodlin.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.etl.entity.EtlDataBucketChecksum;
import org.apache.ibatis.annotations.Mapper;

/**
 * ETL 桶位校验快照 Mapper。
 *
 * @author mumu
 * @since 1.0.0
 */
@Mapper
public interface EtlDataBucketChecksumMapper extends BaseMapper<EtlDataBucketChecksum> {
}
