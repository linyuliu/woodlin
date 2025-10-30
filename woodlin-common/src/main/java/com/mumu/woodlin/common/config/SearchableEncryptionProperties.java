package com.mumu.woodlin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 可搜索加密配置属性
 * 
 * @author mumu
 * @description 数据库字段可搜索加密的配置，支持模糊查询和精确匹配
 * @since 2025-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.searchable-encryption")
public class SearchableEncryptionProperties {
    
    /**
     * 是否启用可搜索加密功能
     */
    private Boolean enabled = false;
    
    /**
     * 加密密钥（Base64编码）
     * 建议使用256位密钥以获得更高安全性
     * 可通过 SearchableEncryptionUtil.generateKey() 生成
     */
    private String encryptionKey;
    
    /**
     * N-gram的大小，用于模糊搜索
     * 默认为2，表示将文本分割为2个字符的片段
     * 值越大，索引越精确但存储空间越大
     */
    private Integer ngramSize = 2;
    
    /**
     * 是否自动加密标注字段
     * 当设置为true时，带有@SearchableField注解的字段会自动加密/解密
     */
    private Boolean autoEncrypt = true;
    
    /**
     * 是否自动生成搜索索引
     * 当设置为true时，会自动为加密字段生成N-gram搜索索引
     */
    private Boolean autoGenerateIndex = true;
    
    /**
     * 搜索索引字段的后缀
     * 例如：字段名为"name"，则搜索索引字段为"name_search_index"
     */
    private String indexFieldSuffix = "_search_index";
    
    /**
     * 是否启用缓存
     * 缓存加密/解密结果，提高性能
     */
    private Boolean enableCache = true;
    
    /**
     * 缓存过期时间（秒）
     */
    private Integer cacheExpireSeconds = 3600;
    
    /**
     * 最大缓存条目数
     */
    private Integer maxCacheSize = 10000;
}
