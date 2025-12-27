package com.mumu.woodlin.common.controller;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.common.service.SystemDictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统字典API接口
 *
 * @author mumu
 * @description 提供统一的动态字典数据访问接口，所有字典遵循国家标准
 *              采用"先查类型，再查数据"的设计模式，支持前端按需加载
 * @since 2025-12-27
 */
@Tag(name = "系统字典", description = "系统字典数据接口（符合GB国标，支持动态字典）")
@RestController
@RequestMapping("/common/dict")
@RequiredArgsConstructor
public class SystemDictionaryController {

    private final SystemDictionaryService dictionaryService;

    // ==================== 新版动态字典接口 ====================

    @Operation(summary = "查询所有字典类型", description = "获取系统中所有可用的字典类型列表，用于前端按需加载")
    @GetMapping("/types")
    public R<List<Map<String, Object>>> getDictTypes() {
        return R.ok(dictionaryService.getDictTypeList());
    }

    @Operation(summary = "根据类型查询字典数据", description = "根据字典类型获取对应的字典数据列表")
    @GetMapping("/data/{dictType}")
    public R<List<Map<String, Object>>> getDictData(@PathVariable String dictType) {
        return R.ok(dictionaryService.getDictDataByType(dictType));
    }

    @Operation(summary = "查询行政区划树", description = "获取完整的行政区划树形结构")
    @GetMapping("/region/tree")
    public R<List<Map<String, Object>>> getRegionTree() {
        return R.ok(dictionaryService.getRegionTree());
    }

    @Operation(summary = "根据父代码查询子区划", description = "根据父区划代码查询子区划列表，parentCode为空则查询省级")
    @GetMapping("/region/children")
    public R<List<Map<String, Object>>> getRegionChildren(@RequestParam(required = false) String parentCode) {
        return R.ok(dictionaryService.getRegionChildren(parentCode));
    }

    // ==================== 兼容性接口（保留旧接口） ====================

    @Operation(summary = "获取所有字典数据（已废弃）", description = "一次性获取所有常用字典数据，已废弃，建议使用分步查询")
    @GetMapping("/all")
    @Deprecated
    public R<Map<String, Object>> getAllDictionaries() {
        return R.ok(dictionaryService.getAllDictionaries());
    }

    @Operation(summary = "获取性别字典", description = "GB/T 2261.1-2003 标准")
    @GetMapping("/gender")
    public R<List<Map<String, Object>>> getGenderDict() {
        return R.ok(dictionaryService.getGenderDict());
    }

    @Operation(summary = "获取民族字典", description = "GB/T 3304-1991 标准，56个民族")
    @GetMapping("/ethnicity")
    public R<List<Map<String, Object>>> getEthnicityDict() {
        return R.ok(dictionaryService.getEthnicityDict());
    }

    @Operation(summary = "获取学历字典", description = "GB/T 4658-2006 标准")
    @GetMapping("/education")
    public R<List<Map<String, Object>>> getEducationLevelDict() {
        return R.ok(dictionaryService.getEducationLevelDict());
    }

    @Operation(summary = "获取婚姻状况字典", description = "GB/T 2261.2-2003 标准")
    @GetMapping("/marital")
    public R<List<Map<String, Object>>> getMaritalStatusDict() {
        return R.ok(dictionaryService.getMaritalStatusDict());
    }

    @Operation(summary = "获取政治面貌字典", description = "GB/T 4762-1984 标准")
    @GetMapping("/political")
    public R<List<Map<String, Object>>> getPoliticalStatusDict() {
        return R.ok(dictionaryService.getPoliticalStatusDict());
    }

    @Operation(summary = "获取证件类型字典", description = "GB/T 2261.4 标准")
    @GetMapping("/idtype")
    public R<List<Map<String, Object>>> getIdTypeDict() {
        return R.ok(dictionaryService.getIdTypeDict());
    }

    @Operation(summary = "获取省级行政区划", description = "GB/T 2260 标准，34个省级行政区")
    @GetMapping("/region/provinces")
    public R<List<Map<String, Object>>> getProvinces() {
        return R.ok(dictionaryService.getProvinces());
    }

    @Operation(summary = "获取市级行政区划", description = "根据省级代码获取市级行政区划")
    @GetMapping("/region/cities/{provinceCode}")
    public R<List<Map<String, Object>>> getCitiesByProvince(@PathVariable String provinceCode) {
        return R.ok(dictionaryService.getCitiesByProvince(provinceCode));
    }

    @Operation(summary = "获取区县级行政区划", description = "根据市级代码获取区县级行政区划")
    @GetMapping("/region/districts/{cityCode}")
    public R<List<Map<String, Object>>> getDistrictsByCity(@PathVariable String cityCode) {
        return R.ok(dictionaryService.getDistrictsByCity(cityCode));
    }
}
