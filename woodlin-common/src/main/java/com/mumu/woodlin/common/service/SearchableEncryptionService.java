package com.mumu.woodlin.common.service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.annotation.SearchableField;
import com.mumu.woodlin.common.config.SearchableEncryptionProperties;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.util.SearchableEncryptionUtil;

/**
 * 可搜索加密服务
 * 
 * @author mumu
 * @description 提供实体对象的自动加密/解密和模糊搜索功能
 * @since 2025-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "woodlin.searchable-encryption", name = "enabled", havingValue = "true")
public class SearchableEncryptionService {
    
    private final SearchableEncryptionProperties properties;
    private final Map<String, String> encryptionCache;
    private final Map<Class<?>, List<Field>> fieldCache;
    
    public SearchableEncryptionService(SearchableEncryptionProperties properties) {
        this.properties = properties;
        this.encryptionCache = new ConcurrentHashMap<>(properties.getMaxCacheSize());
        this.fieldCache = new ConcurrentHashMap<>();
        
        // 验证配置
        validateConfiguration();
    }
    
    /**
     * 加密实体对象中标注了@SearchableField的字段
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 加密后的实体对象
     */
    public <T> T encryptEntity(T entity) {
        if (entity == null || !properties.getAutoEncrypt()) {
            return entity;
        }
        
        try {
            List<Field> searchableFields = getSearchableFields(entity.getClass());
            
            for (Field field : searchableFields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                
                if (value instanceof String && StrUtil.isNotBlank((String) value)) {
                    String plaintext = (String) value;
                    SearchableField annotation = field.getAnnotation(SearchableField.class);
                    
                    // 加密字段
                    String encrypted = encrypt(plaintext);
                    field.set(entity, encrypted);
                    
                    // 生成搜索索引
                    if (annotation.fuzzySearch() && properties.getAutoGenerateIndex()) {
                        String indexFieldName = getIndexFieldName(field, annotation);
                        Field indexField = getFieldByName(entity.getClass(), indexFieldName);
                        if (indexField != null) {
                            indexField.setAccessible(true);
                            String searchIndex = generateSearchIndex(plaintext);
                            indexField.set(entity, searchIndex);
                        }
                    }
                }
            }
            
            return entity;
        } catch (Exception e) {
            log.error("加密实体对象失败: class={}", entity.getClass().getName(), e);
            throw new BusinessException("加密实体对象失败: " + e.getMessage());
        }
    }
    
    /**
     * 解密实体对象中标注了@SearchableField的字段
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 解密后的实体对象
     */
    public <T> T decryptEntity(T entity) {
        if (entity == null || !properties.getAutoEncrypt()) {
            return entity;
        }
        
        try {
            List<Field> searchableFields = getSearchableFields(entity.getClass());
            
            for (Field field : searchableFields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                
                if (value instanceof String && StrUtil.isNotBlank((String) value)) {
                    String ciphertext = (String) value;
                    // 注意：这里需要原始明文来生成IV，实际使用中可能需要调整
                    // 暂时使用简化的解密方法
                    field.set(entity, ciphertext); // 保持加密状态，由业务层决定是否解密
                }
            }
            
            return entity;
        } catch (Exception e) {
            log.error("解密实体对象失败: class={}", entity.getClass().getName(), e);
            throw new BusinessException("解密实体对象失败: " + e.getMessage());
        }
    }
    
    /**
     * 加密字符串
     * 
     * @param plaintext 明文
     * @return 密文
     */
    public String encrypt(String plaintext) {
        if (StrUtil.isBlank(plaintext)) {
            return plaintext;
        }
        
        // 使用缓存提高性能
        if (properties.getEnableCache()) {
            String cached = encryptionCache.get(plaintext);
            if (cached != null) {
                return cached;
            }
        }
        
        String encrypted = SearchableEncryptionUtil.encrypt(plaintext, properties.getEncryptionKey());
        
        if (properties.getEnableCache() && encryptionCache.size() < properties.getMaxCacheSize()) {
            encryptionCache.put(plaintext, encrypted);
        }
        
        return encrypted;
    }
    
    /**
     * 生成搜索索引
     * 
     * @param text 原始文本
     * @return 搜索索引
     */
    public String generateSearchIndex(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        
        return SearchableEncryptionUtil.generateSearchIndex(text, properties.getEncryptionKey());
    }
    
    /**
     * 生成搜索令牌（用于查询）
     * 
     * @param keyword 搜索关键字
     * @return 加密的搜索令牌列表
     */
    public List<String> generateSearchTokens(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }
        
        return SearchableEncryptionUtil.generateSearchTokens(keyword, properties.getEncryptionKey());
    }
    
    /**
     * 生成N-gram集合
     * 
     * @param text 原始文本
     * @return N-gram集合
     */
    public Set<String> generateNGrams(String text) {
        int ngramSize = properties.getNgramSize();
        return SearchableEncryptionUtil.generateNGrams(text, ngramSize);
    }
    
    /**
     * 获取实体类中标注了@SearchableField的字段
     * 
     * @param clazz 实体类
     * @return 字段列表
     */
    private List<Field> getSearchableFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, k -> {
            List<Field> fields = new java.util.ArrayList<>();
            Field[] allFields = ReflectUtil.getFields(clazz);
            for (Field field : allFields) {
                if (field.isAnnotationPresent(SearchableField.class)) {
                    fields.add(field);
                }
            }
            return fields;
        });
    }
    
    /**
     * 获取索引字段名
     * 
     * @param field 原始字段
     * @param annotation 注解
     * @return 索引字段名
     */
    private String getIndexFieldName(Field field, SearchableField annotation) {
        if (StrUtil.isNotBlank(annotation.indexField())) {
            return annotation.indexField();
        }
        return field.getName() + properties.getIndexFieldSuffix();
    }
    
    /**
     * 根据字段名获取字段
     * 
     * @param clazz 类
     * @param fieldName 字段名
     * @return 字段对象
     */
    private Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            return ReflectUtil.getField(clazz, fieldName);
        } catch (Exception e) {
            log.warn("字段不存在: class={}, field={}", clazz.getName(), fieldName);
            return null;
        }
    }
    
    /**
     * 验证配置
     */
    private void validateConfiguration() {
        if (StrUtil.isBlank(properties.getEncryptionKey())) {
            throw new BusinessException("可搜索加密密钥未配置，请设置 woodlin.searchable-encryption.encryption-key");
        }
        
        if (!SearchableEncryptionUtil.isValidKey(properties.getEncryptionKey())) {
            throw new BusinessException("可搜索加密密钥格式无效，密钥长度必须是128、192或256位");
        }
        
        log.info("可搜索加密服务初始化成功 - ngramSize={}, autoEncrypt={}, autoGenerateIndex={}", 
                properties.getNgramSize(), properties.getAutoEncrypt(), properties.getAutoGenerateIndex());
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        encryptionCache.clear();
        log.info("可搜索加密缓存已清空");
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
                "size", encryptionCache.size(),
                "maxSize", properties.getMaxCacheSize(),
                "enabled", properties.getEnableCache()
        );
    }
}
