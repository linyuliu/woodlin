package com.mumu.woodlin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.system.entity.SysNotice;
import com.mumu.woodlin.system.mapper.SysNoticeMapper;
import com.mumu.woodlin.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 通知公告服务实现
 *
 * @author yulin
 * @description 通知公告业务逻辑实现
 * @since 2026-06
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    @Override
    public PageResult<SysNotice> queryPage(SysNotice query, Integer pageNum, Integer pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        Page<SysNotice> page = new Page<>(current, size);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getNoticeTitle())) {
                wrapper.like(SysNotice::getNoticeTitle, query.getNoticeTitle());
            }
            if (StringUtils.hasText(query.getNoticeType())) {
                wrapper.eq(SysNotice::getNoticeType, query.getNoticeType());
            }
            if (StringUtils.hasText(query.getStatus())) {
                wrapper.eq(SysNotice::getStatus, query.getStatus());
            }
        }
        wrapper.orderByDesc(SysNotice::getCreateTime);
        page = this.page(page, wrapper);
        return PageResult.success(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
