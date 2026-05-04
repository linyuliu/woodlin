package com.mumu.woodlin.system.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.entity.SysRegion;
import com.mumu.woodlin.system.service.ISysRegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行政区划控制器
 *
 * @author yulin
 * @description 行政区划查询相关接口
 * @since 2026-06
 */
@Slf4j
@RestController
@RequestMapping("/system/region")
@RequiredArgsConstructor
@Validated
@Tag(name = "行政区划", description = "行政区划查询相关接口")
public class SysRegionController {

    private final ISysRegionService regionService;

    /**
     * 查询行政区划树
     */
    @GetMapping("/tree")
    @Operation(summary = "查询行政区划树", description = "返回省-市-区县的树形结构")
    public R<List<SysRegion>> tree() {
        return R.ok(regionService.getRegionTree());
    }

    /**
     * 按层级和父级编码查询行政区划列表（用于级联下拉）
     */
    @GetMapping
    @Operation(summary = "查询行政区划列表", description = "按层级与父级代码查询")
    public R<List<SysRegion>> list(
            @RequestParam(value = "regionLevel", required = false) Integer regionLevel,
            @RequestParam(value = "parentCode", required = false) String parentCode) {
        LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<>();
        if (regionLevel != null) {
            wrapper.eq(SysRegion::getRegionLevel, regionLevel);
        }
        if (parentCode != null && !parentCode.isEmpty()) {
            wrapper.eq(SysRegion::getParentCode, parentCode);
        }
        wrapper.orderByAsc(SysRegion::getSortOrder);
        return R.ok(regionService.list(wrapper));
    }

    /**
     * 根据区划代码查询行政区划详情
     */
    @GetMapping("/{regionCode}")
    @Operation(summary = "查询行政区划详情", description = "根据区划代码查询")
    public R<SysRegion> getInfo(
            @Parameter(description = "区划代码", required = true) @PathVariable String regionCode) {
        LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<SysRegion>()
                .eq(SysRegion::getRegionCode, regionCode);
        SysRegion region = regionService.getOne(wrapper);
        if (region == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "行政区划不存在");
        }
        return R.ok(region);
    }
}
