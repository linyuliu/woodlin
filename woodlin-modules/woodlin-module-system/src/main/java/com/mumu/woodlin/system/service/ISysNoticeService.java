package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.system.entity.SysNotice;

/**
 * 通知公告服务接口
 *
 * @author yulin
 * @description 通知公告业务逻辑接口
 * @since 2026-06
 */
public interface ISysNoticeService extends IService<SysNotice> {

    /**
     * 分页查询通知公告
     *
     * @param query    查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<SysNotice> queryPage(SysNotice query, Integer pageNum, Integer pageSize);
}
