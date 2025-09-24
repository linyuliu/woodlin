package com.mumu.woodlin.test;

import com.mumu.woodlin.common.enums.DictEnum;
import com.mumu.woodlin.common.enums.Gender;
import com.mumu.woodlin.common.enums.UserStatus;
import com.mumu.woodlin.common.util.DictUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * 字典枚举工具类测试
 * 
 * @author mumu
 * @description 测试DictUtil工具类的各种功能，包括枚举转换、字典列表生成等
 * @since 2025-01-01
 */
@DisplayName("字典枚举工具类测试")
public class DictEnumUtilTest {
    
    /**
     * 测试枚举转换为字典列表
     */
    @Test
    @DisplayName("测试枚举转字典列表")
    public void testToDictList() {
        // 测试UserStatus枚举转字典列表
        List<DictEnum.DictItem> userStatusList = DictUtil.toDictList(UserStatus.class);
        
        assertNotNull(userStatusList, "字典列表不应为null");
        assertEquals(2, userStatusList.size(), "UserStatus应该有2个枚举值");
        
        // 验证第一个枚举值
        DictEnum.DictItem enableDict = userStatusList.stream()
            .filter(dict -> "1".equals(dict.getValue().toString()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(enableDict, "应该找到ENABLE枚举对应的字典项");
        assertEquals("1", enableDict.getValue().toString(), "ENABLE的value应该是'1'");
        assertEquals("启用", enableDict.getLabel(), "ENABLE的label应该是'启用'");
        
        // 测试Gender枚举转字典列表
        List<DictEnum.DictItem> genderList = DictUtil.toDictList(Gender.class);
        
        assertNotNull(genderList, "性别字典列表不应为null");
        assertEquals(3, genderList.size(), "Gender应该有3个枚举值");
        
        // 验证男性枚举值
        DictEnum.DictItem maleDict = genderList.stream()
            .filter(dict -> Integer.valueOf(1).equals(dict.getValue()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(maleDict, "应该找到MALE枚举对应的字典项");
        assertEquals(1, maleDict.getValue(), "MALE的value应该是1");
        assertEquals("男", maleDict.getLabel(), "MALE的label应该是'男'");
    }
    
    /**
     * 测试枚举转Map结构
     */
    @Test
    @DisplayName("测试枚举转Map结构")
    public void testToDictMap() {
        // 测试UserStatus枚举转Map
        Map<Object, String> userStatusMap = DictUtil.toDictMap(UserStatus.class);
        
        assertNotNull(userStatusMap, "字典Map不应为null");
        assertEquals(2, userStatusMap.size(), "UserStatus应该有2个枚举值");
        assertEquals("启用", userStatusMap.get("1"), "value为'1'应该对应'启用'");
        assertEquals("禁用", userStatusMap.get("0"), "value为'0'应该对应'禁用'");
        
        // 测试Gender枚举转Map
        Map<Object, String> genderMap = DictUtil.toDictMap(Gender.class);
        
        assertNotNull(genderMap, "性别字典Map不应为null");
        assertEquals(3, genderMap.size(), "Gender应该有3个枚举值");
        assertEquals("男", genderMap.get(1), "value为1应该对应'男'");
        assertEquals("女", genderMap.get(2), "value为2应该对应'女'");
        assertEquals("未知", genderMap.get(0), "value为0应该对应'未知'");
    }
    
    /**
     * 测试根据值获取标签
     */
    @Test
    @DisplayName("测试根据值获取标签")
    public void testGetLabelByValue() {
        // 测试通过值获取UserStatus标签
        String enableLabel = DictUtil.getLabelByValue(UserStatus.class, "1");
        assertEquals("启用", enableLabel, "值'1'应该对应标签'启用'");
        
        String disableLabel = DictUtil.getLabelByValue(UserStatus.class, "0");
        assertEquals("禁用", disableLabel, "值'0'应该对应标签'禁用'");
        
        // 测试通过值获取Gender标签
        String maleLabel = DictUtil.getLabelByValue(Gender.class, 1);
        assertEquals("男", maleLabel, "值1应该对应标签'男'");
        
        String femaleLabel = DictUtil.getLabelByValue(Gender.class, 2);
        assertEquals("女", femaleLabel, "值2应该对应标签'女'");
        
        // 测试无效值
        String nullLabel = DictUtil.getLabelByValue(UserStatus.class, "invalid");
        assertNull(nullLabel, "无效值应该返回null");
        
        // 测试null值
        String nullValueLabel = DictUtil.getLabelByValue(UserStatus.class, null);
        assertNull(nullValueLabel, "null值应该返回null");
    }
    
    /**
     * 测试根据标签获取值
     */
    @Test
    @DisplayName("测试根据标签获取值") 
    public void testGetValueByLabel() {
        // 测试通过标签获取UserStatus值
        Object enableValue = DictUtil.getValueByLabel(UserStatus.class, "启用");
        assertEquals("1", enableValue, "标签'启用'应该对应值'1'");
        
        Object disableValue = DictUtil.getValueByLabel(UserStatus.class, "禁用");
        assertEquals("0", disableValue, "标签'禁用'应该对应值'0'");
        
        // 测试通过标签获取Gender值
        Object maleValue = DictUtil.getValueByLabel(Gender.class, "男");
        assertEquals(1, maleValue, "标签'男'应该对应值1");
        
        Object femaleValue = DictUtil.getValueByLabel(Gender.class, "女");
        assertEquals(2, femaleValue, "标签'女'应该对应值2");
        
        // 测试无效标签
        Object nullValue = DictUtil.getValueByLabel(Gender.class, "invalid");
        assertNull(nullValue, "无效标签应该返回null");
        
        // 测试null标签
        Object nullLabelValue = DictUtil.getValueByLabel(Gender.class, null);
        assertNull(nullLabelValue, "null标签应该返回null");
    }
    
    /**
     * 测试值存在性检查
     */
    @Test
    @DisplayName("测试值存在性检查")
    public void testContainsValue() {
        // 测试UserStatus值存在性
        assertTrue(DictUtil.containsValue(UserStatus.class, "1"), "'1'应该是UserStatus的有效值");
        assertTrue(DictUtil.containsValue(UserStatus.class, "0"), "'0'应该是UserStatus的有效值");
        assertFalse(DictUtil.containsValue(UserStatus.class, "2"), "'2'不应该是UserStatus的有效值");
        assertFalse(DictUtil.containsValue(UserStatus.class, null), "null不应该是有效值");
        
        // 测试Gender值存在性
        assertTrue(DictUtil.containsValue(Gender.class, 1), "1应该是Gender的有效值");
        assertTrue(DictUtil.containsValue(Gender.class, 2), "2应该是Gender的有效值");
        assertTrue(DictUtil.containsValue(Gender.class, 0), "0应该是Gender的有效值");
        assertFalse(DictUtil.containsValue(Gender.class, 3), "3不应该是Gender的有效值");
    }
    
    /**
     * 测试标签存在性检查
     */
    @Test
    @DisplayName("测试标签存在性检查")
    public void testContainsLabel() {
        // 测试UserStatus标签存在性
        assertTrue(DictUtil.containsLabel(UserStatus.class, "启用"), "'启用'应该是UserStatus的有效标签");
        assertTrue(DictUtil.containsLabel(UserStatus.class, "禁用"), "'禁用'应该是UserStatus的有效标签");
        assertFalse(DictUtil.containsLabel(UserStatus.class, "无效"), "'无效'不应该是UserStatus的有效标签");
        assertFalse(DictUtil.containsLabel(UserStatus.class, null), "null不应该是有效标签");
        
        // 测试Gender标签存在性
        assertTrue(DictUtil.containsLabel(Gender.class, "男"), "'男'应该是Gender的有效标签");
        assertTrue(DictUtil.containsLabel(Gender.class, "女"), "'女'应该是Gender的有效标签");
        assertTrue(DictUtil.containsLabel(Gender.class, "未知"), "'未知'应该是Gender的有效标签");
        assertFalse(DictUtil.containsLabel(Gender.class, "其他"), "'其他'不应该是Gender的有效标签");
    }
    
    /**
     * 测试性能表现
     */
    @Test
    @DisplayName("测试工具类性能")
    public void testPerformance() {
        int iterations = 1000;
        
        long startTime = System.currentTimeMillis();
        
        // 测试大量转换操作的性能
        for (int i = 0; i < iterations; i++) {
            List<DictEnum.DictItem> userStatusList = DictUtil.toDictList(UserStatus.class);
            Map<Object, String> genderMap = DictUtil.toDictMap(Gender.class);
            
            String label = DictUtil.getLabelByValue(UserStatus.class, "1");
            Object value = DictUtil.getValueByLabel(Gender.class, "男");
            boolean containsValue = DictUtil.containsValue(UserStatus.class, "1");
            boolean containsLabel = DictUtil.containsLabel(Gender.class, "女");
            
            // 断言确保操作正确执行
            assertNotNull(userStatusList);
            assertNotNull(genderMap);
            assertNotNull(label);
            assertNotNull(value);
            assertTrue(containsValue);
            assertTrue(containsLabel);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.printf("DictUtil性能测试: %d次操作耗时 %d ms (平均: %.2f ms)%n", 
                         iterations, duration, (double) duration / iterations);
        
        // 性能要求：1000次操作应该在2秒内完成
        assertTrue(duration < 2000, "DictUtil性能应该在合理范围内");
    }
}