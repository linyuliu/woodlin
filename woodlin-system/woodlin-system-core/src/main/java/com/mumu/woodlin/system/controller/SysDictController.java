package com.mumu.woodlin.system.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.entity.SysDictData;
import com.mumu.woodlin.common.entity.SysDictType;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.common.service.SystemDictionaryService;

/**
 * 字典管理接口（管理端）
 *
 * @author mumu
 * @description 提供字典类型与字典项的增删改查，配合前端管理页使用
 * @since 2026-01-31
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
@Validated
@Tag(name = "字典管理", description = "字典类型、字典项 CRUD 接口")
public class SysDictController {

    private final SystemDictionaryService dictionaryService;

    @GetMapping("/types")
    @Operation(summary = "查询字典类型列表")
    public R<List<SysDictType>> listTypes(SysDictType query) {
        return R.ok(dictionaryService.listDictTypes(query));
    }

    @PostMapping("/types")
    @Operation(summary = "新增字典类型")
    public R<SysDictType> createType(@RequestBody SysDictType type) {
        return R.ok(dictionaryService.createDictType(type));
    }

    @PutMapping("/types/{dictId}")
    @Operation(summary = "更新字典类型")
    public R<SysDictType> updateType(@PathVariable Long dictId, @RequestBody SysDictType type) {
        type.setDictId(dictId);
        return R.ok(dictionaryService.updateDictType(type));
    }

    @DeleteMapping("/types/{dictId}")
    @Operation(summary = "删除字典类型")
    public R<Void> removeType(@PathVariable Long dictId) {
        boolean removed = dictionaryService.removeDictType(dictId);
        return removed ? R.ok("删除成功") : R.fail("字典类型不存在");
    }

    @GetMapping("/data")
    @Operation(summary = "查询字典项列表")
    public R<List<SysDictData>> listData(@RequestParam String dictType) {
        return R.ok(dictionaryService.listDictData(dictType));
    }

    @PostMapping("/data")
    @Operation(summary = "新增字典项")
    public R<SysDictData> createData(@RequestBody SysDictData data) {
        return R.ok(dictionaryService.createDictData(data));
    }

    @PutMapping("/data/{dataId}")
    @Operation(summary = "更新字典项")
    public R<SysDictData> updateData(@PathVariable Long dataId, @RequestBody SysDictData data) {
        data.setDataId(dataId);
        return R.ok(dictionaryService.updateDictData(data));
    }

    @DeleteMapping("/data/{dataId}")
    @Operation(summary = "删除字典项")
    public R<Void> removeData(@PathVariable Long dataId) {
        boolean removed = dictionaryService.removeDictData(dataId);
        return removed ? R.ok("删除成功") : R.fail("字典数据不存在");
    }
}
