package com.mumu.woodlin.system.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.system.entity.SysRegion;
import com.mumu.woodlin.system.mapper.SysRegionMapper;
import com.mumu.woodlin.system.service.ISysRegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 行政区划服务实现
 *
 * @author yulin
 * @description 行政区划业务逻辑实现
 * @since 2026-06
 */
@Service
@RequiredArgsConstructor
public class SysRegionServiceImpl extends ServiceImpl<SysRegionMapper, SysRegion> implements ISysRegionService {

    @Override
    public List<SysRegion> getRegionTree() {
        List<SysRegion> provinces = baseMapper.selectProvinces();
        for (SysRegion province : provinces) {
            attachChildren(province);
        }
        return provinces;
    }

    private void attachChildren(SysRegion parent) {
        List<SysRegion> children = baseMapper.selectByParentCode(parent.getRegionCode());
        if (children == null || children.isEmpty()) {
            return;
        }
        for (SysRegion child : children) {
            attachChildren(child);
        }
    }
}
