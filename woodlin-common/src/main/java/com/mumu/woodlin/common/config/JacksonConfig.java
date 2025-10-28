package com.mumu.woodlin.common.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.mumu.woodlin.common.enums.DictEnum;

/**
 * 全局 Jackson 配置
 * <p>
 * 自动处理：
 *  - 日期时间格式化（yyyy-MM-dd HH:mm:ss）
 *  - Long/BigInteger 转字符串（防止 JS 精度丢失）
 *  - DictEnum 子类自动序列化为 {value, label, desc}
 * @author mumu
 * @date 2025-10-25 21:00
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // ============ 基础配置 ============
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // ============ 注册模块 ============
        SimpleModule module = new SimpleModule("WoodlinModule");

        // Long / BigInteger / BigDecimal → String，避免JS精度丢失
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        module.addSerializer(BigInteger.class, ToStringSerializer.instance);
        module.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        // 时间序列化配置
        module.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        module.addSerializer(LocalDate.class,
            new LocalDateSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        module.addSerializer(LocalTime.class,
            new LocalTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));

        // 时间反序列化配置
        module.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        module.addDeserializer(LocalDate.class,
            new LocalDateDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        module.addDeserializer(LocalTime.class,
            new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));

        // 自动注册所有 DictEnum 子类
        registerDictEnumSerializers(module);

        // 注册模块
        objectMapper.registerModule(module);
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    /**
     * 自动扫描并注册所有实现 DictEnum 的枚举类
     * 无需手动写包名，基于 DictEnum 所在包自动扫描。
     */
    private void registerDictEnumSerializers(SimpleModule module) {
        try {
            // 自动确定 DictEnum 的基础包路径
            String basePackage = DictEnum.class.getPackage().getName();

            // Spring 原生类扫描器
            ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(DictEnum.class));

            // 扫描并注册序列化器与反序列化器
            scanner.findCandidateComponents(basePackage).forEach(beanDef -> {
                try {
                    Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                    if (clazz.isEnum() && DictEnum.class.isAssignableFrom(clazz)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends DictEnum> enumClass = (Class<? extends DictEnum>) clazz;

                        module.addSerializer(enumClass, new DictEnumJsonSerializer());
                        module.addDeserializer(enumClass, new DictEnumDeserializer(enumClass));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("注册 DictEnum 枚举序列化器失败: " + beanDef.getBeanClassName(), e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("扫描 DictEnum 枚举类失败", e);
        }
    }

    /**
     * DictEnum 通用序列化器
     * 输出形如：
     * {
     * "value": "M",
     * "label": "男",
     * "desc": "男性"
     * }
     */
    public static class DictEnumJsonSerializer extends JsonSerializer<DictEnum> {
        @Override
        public void serialize(DictEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("value", value.getValue().toString());
            gen.writeStringField("label", value.getLabel());
            gen.writeStringField("desc", value.getDesc());
            gen.writeEndObject();
        }
    }
}
