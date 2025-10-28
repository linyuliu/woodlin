package com.mumu.woodlin.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mumu.woodlin.common.enums.DictEnum;

/**
 * 字典工具类
 *
 * @author mumu
 * @description 提供字典枚举相关的工具方法
 * @since 2025-01-01
 */
@Component
public class DictUtil {


    /**
     * 将字典枚举数组转换为label-value列表
     *
     * @param enumClass 枚举类
     * @param <T> 枚举类型
     * @return label-value列表
     */
    public static <T extends Enum<T> & DictEnum> List<DictEnum.DictItem> toDictList(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(DictEnum::toDictItem)
                .collect(Collectors.toList());
    }

    /**
     * 将字典枚举数组转换为Map结构（value -> label）
     *
     * @param enumClass 枚举类
     * @param <T> 枚举类型
     * @return value到label的映射
     */
    public static <T extends Enum<T> & DictEnum> Map<Object, String> toDictMap(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(DictEnum::getValue, DictEnum::getLabel));
    }

    /**
     * 根据value获取对应的label
     *
     * @param enumClass 枚举类
     * @param value 值
     * @param <T> 枚举类型
     * @return 对应的标签，如果找不到返回null
     */
    public static <T extends Enum<T> & DictEnum> String getLabelByValue(Class<T> enumClass, Object value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(item -> value.equals(item.getValue()))
                .map(DictEnum::getLabel)
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据label获取对应的value
     *
     * @param enumClass 枚举类
     * @param label 标签
     * @param <T> 枚举类型
     * @return 对应的值，如果找不到返回null
     */
    public static <T extends Enum<T> & DictEnum> Object getValueByLabel(Class<T> enumClass, String label) {
        if (label == null) {
            return null;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(item -> label.equals(item.getLabel()))
                .map(DictEnum::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查指定的值是否存在于字典枚举中
     *
     * @param enumClass 枚举类
     * @param value 值
     * @param <T> 枚举类型
     * @return 是否存在
     */
    public static <T extends Enum<T> & DictEnum> boolean containsValue(Class<T> enumClass, Object value) {
        if (value == null) {
            return false;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(item -> value.equals(item.getValue()));
    }

    /**
     * 检查指定的标签是否存在于字典枚举中
     *
     * @param enumClass 枚举类
     * @param label 标签
     * @param <T> 枚举类型
     * @return 是否存在
     */
    public static <T extends Enum<T> & DictEnum> boolean containsLabel(Class<T> enumClass, String label) {
        if (label == null) {
            return false;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(item -> label.equals(item.getLabel()));
    }
}
