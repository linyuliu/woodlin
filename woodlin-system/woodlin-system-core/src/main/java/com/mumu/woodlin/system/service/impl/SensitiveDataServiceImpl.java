package com.mumu.woodlin.system.service.impl;

import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mumu.woodlin.common.service.SearchableEncryptionService;
import com.mumu.woodlin.system.entity.SensitiveData;
import com.mumu.woodlin.system.mapper.SensitiveDataMapper;
import com.mumu.woodlin.system.service.ISensitiveDataService;

/**
 * 敏感数据服务实现类
 * 
 * @author mumu
 * @description 敏感数据业务逻辑实现，展示可搜索加密的完整使用示例
 * @since 2025-01-01
 */
@Slf4j
@Service
public class SensitiveDataServiceImpl extends ServiceImpl<SensitiveDataMapper, SensitiveData> 
        implements ISensitiveDataService {
    
    private final Optional<SearchableEncryptionService> encryptionService;
    
    public SensitiveDataServiceImpl(@Autowired(required = false) SearchableEncryptionService encryptionService) {
        this.encryptionService = Optional.ofNullable(encryptionService);
    }
    
    /**
     * 根据姓名模糊查询
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public IPage<SensitiveData> searchByNameFuzzy(String keyword, Integer pageNum, Integer pageSize) {
        Page<SensitiveData> page = new Page<>(pageNum, pageSize);
        
        if (StrUtil.isBlank(keyword)) {
            return this.page(page);
        }
        
        if (encryptionService.isEmpty()) {
            log.warn("SearchableEncryptionService not available, skipping encrypted search");
            return page;
        }
        
        // 生成搜索令牌
        List<String> searchTokens = encryptionService.get().generateSearchTokens(keyword);
        
        if (searchTokens.isEmpty()) {
            return page;
        }
        
        // 使用加密的N-gram进行模糊搜索
        LambdaQueryWrapper<SensitiveData> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建模糊查询条件：搜索索引字段包含任意一个搜索令牌
        for (String token : searchTokens) {
            queryWrapper.or().like(SensitiveData::getRealNameSearchIndex, token);
        }
        
        queryWrapper.orderByDesc(SensitiveData::getCreateTime);
        
        return this.page(page, queryWrapper);
    }
    
    /**
     * 根据手机号模糊查询
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public IPage<SensitiveData> searchByMobileFuzzy(String keyword, Integer pageNum, Integer pageSize) {
        Page<SensitiveData> page = new Page<>(pageNum, pageSize);
        
        if (StrUtil.isBlank(keyword)) {
            return this.page(page);
        }
        
        if (encryptionService.isEmpty()) {
            log.warn("SearchableEncryptionService not available, skipping encrypted search");
            return page;
        }
        
        // 生成搜索令牌
        List<String> searchTokens = encryptionService.get().generateSearchTokens(keyword);
        
        if (searchTokens.isEmpty()) {
            return page;
        }
        
        // 使用加密的N-gram进行模糊搜索
        LambdaQueryWrapper<SensitiveData> queryWrapper = new LambdaQueryWrapper<>();
        
        for (String token : searchTokens) {
            queryWrapper.or().like(SensitiveData::getMobileSearchIndex, token);
        }
        
        queryWrapper.orderByDesc(SensitiveData::getCreateTime);
        
        return this.page(page, queryWrapper);
    }
    
    /**
     * 根据身份证号精确查询
     * 
     * @param idCard 身份证号
     * @return 敏感数据
     */
    @Override
    public SensitiveData findByIdCardExact(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return null;
        }
        
        if (encryptionService.isEmpty()) {
            log.warn("SearchableEncryptionService not available, skipping encrypted search");
            return null;
        }
        
        // 加密身份证号进行精确匹配
        String encryptedIdCard = encryptionService.get().encrypt(idCard);
        
        return this.getOne(new LambdaQueryWrapper<SensitiveData>()
                .eq(SensitiveData::getIdCard, encryptedIdCard)
                .last("LIMIT 1"));
    }
    
    /**
     * 批量插入敏感数据（自动加密）
     * 
     * @param dataList 数据列表
     * @return 插入成功的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertWithEncryption(List<SensitiveData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        
        // 加密所有数据
        encryptionService.ifPresent(service -> 
            dataList.forEach(service::encryptEntity)
        );
        
        // 批量插入
        boolean success = this.saveBatch(dataList);
        
        return success ? dataList.size() : 0;
    }
    
    /**
     * 保存敏感数据（重写以支持自动加密）
     * 
     * @param entity 敏感数据实体
     * @return 是否成功
     */
    @Override
    public boolean save(SensitiveData entity) {
        // 自动加密
        encryptionService.ifPresent(service -> service.encryptEntity(entity));
        return super.save(entity);
    }
    
    /**
     * 更新敏感数据（重写以支持自动加密）
     * 
     * @param entity 敏感数据实体
     * @return 是否成功
     */
    @Override
    public boolean updateById(SensitiveData entity) {
        // 自动加密
        encryptionService.ifPresent(service -> service.encryptEntity(entity));
        return super.updateById(entity);
    }
}
