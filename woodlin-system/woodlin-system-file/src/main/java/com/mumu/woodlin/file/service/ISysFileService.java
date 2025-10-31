package com.mumu.woodlin.file.service;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.file.entity.SysFile;

/**
 * 文件服务接口
 * 
 * @author mumu
 * @description 文件服务，处理文件上传、下载、删除等操作
 * @since 2025-01-30
 */
public interface ISysFileService extends IService<SysFile> {
    
    /**
     * 上传文件（使用令牌）
     *
     * @param file      文件
     * @param token     上传令牌
     * @param signature 签名
     * @return 文件信息
     */
    SysFile uploadFile(MultipartFile file, String token, String signature);
    
    /**
     * 上传文件（直接指定策略）
     *
     * @param file       文件
     * @param policyCode 策略编码
     * @return 文件信息
     */
    SysFile uploadFileWithPolicy(MultipartFile file, String policyCode);
    
    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream downloadFile(Long fileId);
    
    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean deleteFile(Long fileId);
    
    /**
     * 获取文件访问URL
     *
     * @param fileId         文件ID
     * @param expirationTime 过期时间（秒），如果为0则返回永久URL
     * @return 文件URL
     */
    String getFileUrl(Long fileId, int expirationTime);
    
    /**
     * 通过MD5查询文件（用于秒传）
     *
     * @param md5 文件MD5
     * @return 文件信息
     */
    SysFile getFileByMd5(String md5);
}
