package com.mumu.woodlin.file.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.file.dto.UploadTokenRequest;
import com.mumu.woodlin.file.entity.SysFile;
import com.mumu.woodlin.file.service.ISysFileService;
import com.mumu.woodlin.file.service.ISysUploadTokenService;
import com.mumu.woodlin.file.vo.UploadTokenVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件上传控制器
 * 
 * @author mumu
 * @description 处理文件上传相关的API请求，支持令牌验证和安全上传
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/file/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {
    
    private final ISysFileService fileService;
    private final ISysUploadTokenService uploadTokenService;
    
    /**
     * 获取上传令牌
     * 前端在上传前需要先调用此接口获取上传令牌和签名
     *
     * @param request 上传令牌请求参数
     * @return 上传令牌信息
     */
    @PostMapping("/token")
    @Operation(summary = "获取上传令牌", description = "前端上传文件前需先获取上传令牌和签名")
    public R<UploadTokenVO> getUploadToken(@Valid @RequestBody UploadTokenRequest request) {
        log.info("请求上传令牌: policyCode={}, fileName={}", request.getPolicyCode(), request.getFileName());
        UploadTokenVO tokenVO = uploadTokenService.generateUploadToken(request);
        return R.ok(tokenVO);
    }
    
    /**
     * 上传文件（使用令牌）
     * 前端使用获取的令牌和签名上传文件，保证安全性
     *
     * @param file      上传的文件
     * @param token     上传令牌
     * @param signature 签名
     * @return 文件信息
     */
    @PostMapping
    @Operation(summary = "上传文件", description = "使用令牌和签名上传文件")
    public R<SysFile> uploadFile(
        @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
        @Parameter(description = "上传令牌") @RequestParam("token") String token,
        @Parameter(description = "签名") @RequestParam("signature") String signature
    ) {
        log.info("上传文件: fileName={}, size={}, token={}", 
            file.getOriginalFilename(), file.getSize(), token);
        
        SysFile sysFile = fileService.uploadFile(file, token, signature);
        return R.ok(sysFile);
    }
    
    /**
     * 快速上传（直接指定策略）
     * 简化的上传接口，内部自动生成令牌
     *
     * @param file       上传的文件
     * @param policyCode 上传策略编码
     * @return 文件信息
     */
    @PostMapping("/quick")
    @Operation(summary = "快速上传", description = "直接指定策略编码上传，内部自动生成令牌")
    public R<SysFile> quickUpload(
        @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
        @Parameter(description = "策略编码", example = "default") @RequestParam(value = "policyCode", defaultValue = "default") String policyCode
    ) {
        log.info("快速上传文件: fileName={}, size={}, policyCode={}", 
            file.getOriginalFilename(), file.getSize(), policyCode);
        
        SysFile sysFile = fileService.uploadFileWithPolicy(file, policyCode);
        return R.ok(sysFile);
    }
    
    /**
     * 秒传检查
     * 前端可先计算文件MD5，调用此接口检查文件是否已存在
     *
     * @param md5 文件MD5值
     * @return 文件信息，如果文件已存在则返回，否则返回null
     */
    @GetMapping("/check/{md5}")
    @Operation(summary = "秒传检查", description = "通过MD5检查文件是否已存在，实现秒传功能")
    public R<SysFile> checkFile(
        @Parameter(description = "文件MD5值") @PathVariable String md5
    ) {
        log.debug("检查文件秒传: md5={}", md5);
        SysFile file = fileService.getFileByMd5(md5);
        return R.ok(file);
    }
}
