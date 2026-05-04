package com.mumu.woodlin.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysRegion;

/**
 * 行政区划服务接口
 *
 * @author yulin
 * @description 行政区划业务逻辑接口
 * @since 2026-06
 */
public interface ISysRegionService extends IService<SysRegion> {

    /**
     * 获取行政区划树
     *
     * @return 行政区划树
     */
    List<SysRegion> getRegionTree();
}
