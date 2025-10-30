package com.mumu.woodlin.system.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.response.Result;
import com.mumu.woodlin.system.entity.SensitiveData;
import com.mumu.woodlin.system.service.ISensitiveDataService;

/**
 * 敏感数据控制器
 * 
 * @author mumu
 * @description 敏感数据管理接口，演示可搜索加密功能的API使用
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/system/sensitive-data")
@RequiredArgsConstructor
@Tag(name = "敏感数据管理", description = "敏感数据加密和搜索示例")
public class SensitiveDataController {
    
    private final ISensitiveDataService sensitiveDataService;
    
    /**
     * 根据姓名模糊查询
     */
    @GetMapping("/search/name")
    @Operation(summary = "根据姓名模糊查询", description = "使用可搜索加密技术对加密数据进行模糊搜索")
    public Result<IPage<SensitiveData>> searchByName(
            @Parameter(description = "搜索关键字") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("搜索敏感数据 - 姓名模糊查询: keyword={}", keyword);
        IPage<SensitiveData> page = sensitiveDataService.searchByNameFuzzy(keyword, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 根据手机号模糊查询
     */
    @GetMapping("/search/mobile")
    @Operation(summary = "根据手机号模糊查询", description = "使用可搜索加密技术对加密手机号进行模糊搜索")
    public Result<IPage<SensitiveData>> searchByMobile(
            @Parameter(description = "搜索关键字") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("搜索敏感数据 - 手机号模糊查询: keyword={}", keyword);
        IPage<SensitiveData> page = sensitiveDataService.searchByMobileFuzzy(keyword, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 根据身份证号精确查询
     */
    @GetMapping("/search/idcard")
    @Operation(summary = "根据身份证号精确查询", description = "使用确定性加密对身份证号进行精确匹配")
    public Result<SensitiveData> searchByIdCard(
            @Parameter(description = "身份证号") @RequestParam String idCard) {
        
        log.info("搜索敏感数据 - 身份证号精确查询");
        SensitiveData data = sensitiveDataService.findByIdCardExact(idCard);
        return Result.success(data);
    }
    
    /**
     * 新增敏感数据
     */
    @PostMapping("/add")
    @Operation(summary = "新增敏感数据", description = "新增敏感数据，系统会自动加密并生成搜索索引")
    public Result<Void> add(@RequestBody SensitiveData data) {
        log.info("新增敏感数据: realName={}", data.getRealName());
        boolean success = sensitiveDataService.save(data);
        return success ? Result.success() : Result.fail("新增失败");
    }
    
    /**
     * 批量新增敏感数据
     */
    @PostMapping("/batch-add")
    @Operation(summary = "批量新增敏感数据", description = "批量新增敏感数据，系统会自动加密并生成搜索索引")
    public Result<Integer> batchAdd(@RequestBody List<SensitiveData> dataList) {
        log.info("批量新增敏感数据: count={}", dataList.size());
        int count = sensitiveDataService.batchInsertWithEncryption(dataList);
        return Result.success(count);
    }
    
    /**
     * 分页查询所有数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询所有敏感数据")
    public Result<IPage<SensitiveData>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("分页查询敏感数据: pageNum={}, pageSize={}", pageNum, pageSize);
        IPage<SensitiveData> page = sensitiveDataService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize));
        return Result.success(page);
    }
}
