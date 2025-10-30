package com.mumu.woodlin.system.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.system.entity.SensitiveData;

/**
 * 敏感数据服务接口
 * 
 * @author mumu
 * @description 敏感数据业务逻辑接口，演示可搜索加密功能的使用
 * @since 2025-01-01
 */
public interface ISensitiveDataService extends IService<SensitiveData> {
    
    /**
     * 根据姓名模糊查询
     * 使用加密的N-gram索引进行模糊匹配
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<SensitiveData> searchByNameFuzzy(String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 根据手机号模糊查询
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<SensitiveData> searchByMobileFuzzy(String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 根据身份证号精确查询
     * 使用确定性加密进行精确匹配
     * 
     * @param idCard 身份证号
     * @return 敏感数据
     */
    SensitiveData findByIdCardExact(String idCard);
    
    /**
     * 批量插入敏感数据（自动加密）
     * 
     * @param dataList 数据列表
     * @return 插入成功的数量
     */
    int batchInsertWithEncryption(List<SensitiveData> dataList);
}
