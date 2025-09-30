package com.mumu.woodlin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * API 加密配置属性
 * 
 * @author mumu
 * @description API接口请求和响应数据加密配置，支持多种加密算法和灵活的接口匹配规则
 * @since 2025-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.api.encryption")
public class ApiEncryptionProperties {
    
    /**
     * 是否启用加密功能
     */
    private Boolean enabled = false;
    
    /**
     * 加密算法类型
     * 支持: AES, RSA, SM4
     */
    private EncryptionAlgorithm algorithm = EncryptionAlgorithm.AES;
    
    /**
     * AES 加密密钥（Base64编码）
     * 建议使用 128、192 或 256 位密钥
     */
    private String aesKey;
    
    /**
     * AES 初始化向量 IV（Base64编码）
     */
    private String aesIv;
    
    /**
     * AES 加密模式
     * 支持: CBC, ECB, CFB, OFB, CTR
     */
    private String aesMode = "CBC";
    
    /**
     * AES 填充方式
     * 支持: PKCS5Padding, PKCS7Padding, NoPadding
     */
    private String aesPadding = "PKCS5Padding";
    
    /**
     * RSA 公钥（Base64编码）
     */
    private String rsaPublicKey;
    
    /**
     * RSA 私钥（Base64编码）
     */
    private String rsaPrivateKey;
    
    /**
     * RSA 密钥长度
     * 支持: 1024, 2048, 4096
     */
    private Integer rsaKeySize = 2048;
    
    /**
     * SM4 加密密钥（Base64编码）
     */
    private String sm4Key;
    
    /**
     * SM4 初始化向量 IV（Base64编码）
     */
    private String sm4Iv;
    
    /**
     * SM4 加密模式
     * 支持: CBC, ECB
     */
    private String sm4Mode = "CBC";
    
    /**
     * 需要加密的接口路径模式列表
     * 支持 Ant 风格的路径匹配，例如:
     * /api/user/** : 匹配 /api/user 下的所有接口
     * /api/star/save : 匹配所有模块的 save 接口
     * /api/sensitive/** : 匹配所有敏感数据接口
     */
    private List<String> includePatterns = new ArrayList<>();
    
    /**
     * 排除加密的接口路径模式列表
     * 优先级高于 includePatterns
     */
    private List<String> excludePatterns = new ArrayList<>();
    
    /**
     * 是否加密请求体
     */
    private Boolean encryptRequest = true;
    
    /**
     * 是否加密响应体
     */
    private Boolean encryptResponse = true;
    
    /**
     * 加密数据的字段名
     * 前端发送加密数据时使用此字段名
     */
    private String encryptedFieldName = "encryptedData";
    
    /**
     * 是否在响应头中添加加密标识
     */
    private Boolean addEncryptionHeader = true;
    
    /**
     * 加密标识响应头名称
     */
    private String encryptionHeaderName = "X-Encrypted";
    
    /**
     * 加密算法枚举
     */
    public enum EncryptionAlgorithm {
        /**
         * AES 对称加密算法
         * 优点：加密速度快，适合大数据量
         * 推荐场景：接口数据加密、文件加密
         */
        AES,
        
        /**
         * RSA 非对称加密算法
         * 优点：安全性高，适合密钥交换
         * 推荐场景：密码传输、敏感信息加密
         */
        RSA,
        
        /**
         * SM4 国密对称加密算法
         * 优点：符合国密标准，适合政府及金融行业
         * 推荐场景：需要国密合规的场景
         */
        SM4
    }
}
