package com.mumu.woodlin.file.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.file.entity.SysFile;
import com.mumu.woodlin.file.service.ISysFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件管理控制器
 * 
 * @author mumu
 * @description 处理文件查询、下载、删除等管理操作
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/file/manage")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件管理相关接口")
public class FileManageController {
    
    private final ISysFileService fileService;
    
    /**
     * 分页查询文件列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 文件列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询文件", description = "分页查询文件列表")
    public R<Page<SysFile>> page(
        @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Long size
    ) {
        Page<SysFile> page = fileService.page(new Page<>(current, size));
        return R.ok(page);
    }
    
    /**
     * 获取文件详情
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "获取文件详情", description = "根据文件ID获取文件详细信息")
    public R<SysFile> getFile(
        @Parameter(description = "文件ID") @PathVariable Long fileId
    ) {
        SysFile file = fileService.getById(fileId);
        if (file == null) {
            return R.fail("文件不存在");
        }
        return R.ok(file);
    }
    
    /**
     * 获取文件访问URL
     *
     * @param fileId         文件ID
     * @param expirationTime 过期时间（秒），0表示永久
     * @return 文件URL
     */
    @GetMapping("/{fileId}/url")
    @Operation(summary = "获取文件访问URL", description = "获取文件访问URL，支持临时访问链接")
    public R<String> getFileUrl(
        @Parameter(description = "文件ID") @PathVariable Long fileId,
        @Parameter(description = "过期时间（秒）") @RequestParam(defaultValue = "3600") int expirationTime
    ) {
        String url = fileService.getFileUrl(fileId, expirationTime);
        return R.ok(url);
    }
    
    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件流
     */
    @GetMapping("/{fileId}/download")
    @Operation(summary = "下载文件", description = "下载文件到本地")
    public ResponseEntity<InputStreamResource> downloadFile(
        @Parameter(description = "文件ID") @PathVariable Long fileId
    ) throws IOException {
        SysFile file = fileService.getById(fileId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        
        InputStream inputStream = fileService.downloadFile(fileId);
        InputStreamResource resource = new InputStreamResource(inputStream);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + file.getOriginalName() + "\"")
            .contentType(MediaType.parseMediaType(file.getMimeType()))
            .body(resource);
    }
    
    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 是否成功
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "删除文件（包括存储服务和数据库记录）")
    public R<Boolean> deleteFile(
        @Parameter(description = "文件ID") @PathVariable Long fileId
    ) {
        log.info("删除文件: fileId={}", fileId);
        boolean success = fileService.deleteFile(fileId);
        return success ? R.ok(true) : R.fail("删除失败");
    }
    
    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID列表
     * @return 是否成功
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    public R<Boolean> batchDeleteFiles(
        @Parameter(description = "文件ID列表") @RequestBody java.util.List<Long> fileIds
    ) {
        log.info("批量删除文件: fileIds={}", fileIds);
        
        for (Long fileId : fileIds) {
            try {
                fileService.deleteFile(fileId);
            } catch (Exception e) {
                log.error("删除文件失败: fileId={}", fileId, e);
            }
        }
        
        return R.ok(true);
    }
    
    /**
     * 搜索文件
     *
     * @param keyword 关键词
     * @param current 当前页
     * @param size    每页大小
     * @return 文件列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索文件", description = "根据关键词搜索文件")
    public R<Page<SysFile>> search(
        @Parameter(description = "关键词") @RequestParam String keyword,
        @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Long size
    ) {
        Page<SysFile> page = fileService.page(
            new Page<>(current, size),
            new LambdaQueryWrapper<SysFile>()
                .like(SysFile::getFileName, keyword)
                .or()
                .like(SysFile::getOriginalName, keyword)
        );
        return R.ok(page);
    }
}
