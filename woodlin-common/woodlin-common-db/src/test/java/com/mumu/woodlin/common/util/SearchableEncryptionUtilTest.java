package com.mumu.woodlin.common.util;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 可搜索加密工具测试类
 * 
 * @author mumu
 * @since 2025-01-01
 */
class SearchableEncryptionUtilTest {
    
    private static final String TEST_KEY = SearchableEncryptionUtil.generateKey(256);
    
    @Test
    void testGenerateKey() {
        // 生成密钥
        String key256 = SearchableEncryptionUtil.generateKey(256);
        String key192 = SearchableEncryptionUtil.generateKey(192);
        String key128 = SearchableEncryptionUtil.generateKey(128);
        
        assertNotNull(key256);
        assertNotNull(key192);
        assertNotNull(key128);
        
        // 验证密钥有效性
        assertTrue(SearchableEncryptionUtil.isValidKey(key256));
        assertTrue(SearchableEncryptionUtil.isValidKey(key192));
        assertTrue(SearchableEncryptionUtil.isValidKey(key128));
    }
    
    @Test
    void testEncryptDecrypt() {
        String plaintext = "张三";
        
        // 加密
        String encrypted = SearchableEncryptionUtil.encrypt(plaintext, TEST_KEY);
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        
        // 解密
        String decrypted = SearchableEncryptionUtil.decrypt(encrypted, TEST_KEY, plaintext);
        assertEquals(plaintext, decrypted);
    }
    
    @Test
    void testDeterministicEncryption() {
        String plaintext = "李四";
        
        // 相同明文应产生相同密文
        String encrypted1 = SearchableEncryptionUtil.encrypt(plaintext, TEST_KEY);
        String encrypted2 = SearchableEncryptionUtil.encrypt(plaintext, TEST_KEY);
        
        assertEquals(encrypted1, encrypted2, "相同明文应产生相同密文");
    }
    
    @Test
    void testGenerateNGrams() {
        String text = "张三丰";
        
        // 生成2-gram
        Set<String> ngrams2 = SearchableEncryptionUtil.generateNGrams(text, 2);
        assertFalse(ngrams2.isEmpty());
        assertTrue(ngrams2.contains("张三"));
        assertTrue(ngrams2.contains("三丰"));
        
        // 生成3-gram
        Set<String> ngrams3 = SearchableEncryptionUtil.generateNGrams(text, 3);
        assertFalse(ngrams3.isEmpty());
        assertTrue(ngrams3.contains("张三丰"));
    }
    
    @Test
    void testGenerateNGramsWithShortText() {
        String text = "张";
        
        Set<String> ngrams = SearchableEncryptionUtil.generateNGrams(text, 2);
        assertEquals(1, ngrams.size());
        assertTrue(ngrams.contains("张"));
    }
    
    @Test
    void testGenerateSearchIndex() {
        String text = "王五";
        
        String searchIndex = SearchableEncryptionUtil.generateSearchIndex(text, TEST_KEY);
        assertNotNull(searchIndex);
        assertFalse(searchIndex.isEmpty());
        assertTrue(searchIndex.contains(",") || searchIndex.length() > 0);
    }
    
    @Test
    void testGenerateSearchTokens() {
        String keyword = "张";
        
        List<String> tokens = SearchableEncryptionUtil.generateSearchTokens(keyword, TEST_KEY);
        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());
        
        // 验证令牌是加密的
        for (String token : tokens) {
            assertNotEquals(keyword, token);
        }
    }
    
    @Test
    void testEncryptNGrams() {
        Set<String> ngrams = Set.of("张三", "三丰");
        
        Set<String> encryptedNGrams = SearchableEncryptionUtil.encryptNGrams(ngrams, TEST_KEY);
        assertEquals(ngrams.size(), encryptedNGrams.size());
        
        // 验证每个N-gram都被加密了
        for (String encrypted : encryptedNGrams) {
            assertFalse(ngrams.contains(encrypted));
        }
    }
    
    @Test
    void testEmptyInput() {
        // 测试空字符串
        String emptyEncrypted = SearchableEncryptionUtil.encrypt("", TEST_KEY);
        assertEquals("", emptyEncrypted);
        
        // 测试null
        String nullEncrypted = SearchableEncryptionUtil.encrypt(null, TEST_KEY);
        assertNull(nullEncrypted);
        
        // 测试空N-grams
        Set<String> emptyNGrams = SearchableEncryptionUtil.generateNGrams("", 2);
        assertTrue(emptyNGrams.isEmpty());
    }
    
    @Test
    void testChineseCharacters() {
        String[] testCases = {
            "中文测试",
            "李明华",
            "北京市朝阳区",
            "13800138000",
            "test@example.com"
        };
        
        for (String text : testCases) {
            String encrypted = SearchableEncryptionUtil.encrypt(text, TEST_KEY);
            assertNotNull(encrypted);
            assertNotEquals(text, encrypted);
            
            String decrypted = SearchableEncryptionUtil.decrypt(encrypted, TEST_KEY, text);
            assertEquals(text, decrypted);
        }
    }
    
    @Test
    void testSearchScenario() {
        // 模拟真实搜索场景：搜索"张三"在"张三丰"中
        String fullName = "张三丰";
        String searchKeyword = "张三";
        
        // 生成完整姓名的搜索索引
        String searchIndex = SearchableEncryptionUtil.generateSearchIndex(fullName, TEST_KEY);
        assertNotNull(searchIndex);
        assertFalse(searchIndex.isEmpty());
        
        // 生成搜索令牌（搜索"张三"）
        List<String> searchTokens = SearchableEncryptionUtil.generateSearchTokens(searchKeyword, TEST_KEY);
        assertFalse(searchTokens.isEmpty());
        
        // 验证搜索令牌可以在索引中找到
        // "张三丰" N-grams (size=2): ["张三", "三丰"]
        // "张三" N-grams (size=2): ["张三"]
        // 所以"张三"的加密N-gram应该在"张三丰"的索引中
        Set<String> indexTokens = Set.of(searchIndex.split(","));
        
        boolean found = false;
        for (String token : searchTokens) {
            if (indexTokens.contains(token)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "搜索令牌应该在索引中找到");
    }
    
    @Test
    void testPerformance() {
        int iterations = 1000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            String plaintext = "测试数据" + i;
            SearchableEncryptionUtil.encrypt(plaintext, TEST_KEY);
            SearchableEncryptionUtil.generateSearchIndex(plaintext, TEST_KEY);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double avgTime = duration / (double) iterations;
        
        System.out.printf("加密 %d 次，总耗时 %d ms，平均 %.2f ms/次%n", 
                iterations, duration, avgTime);
        
        // 性能断言：平均每次加密应该在10ms以内
        assertTrue(avgTime < 10, "加密性能应该小于10ms/次");
    }
}
