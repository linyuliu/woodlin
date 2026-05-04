package com.mumu.woodlin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.system.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告Mapper接口
 *
 * @author yulin
 * @description 通知公告数据访问层接口
 * @since 2026-06
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
}
