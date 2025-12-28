package com.mumu.woodlin.common.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

/**
 * Redis 配置类
 * 
 * @author mumu
 * @description Redis配置，使用Redisson作为主要客户端，提供优雅的API和高性能
 *              RedisTemplate仅用于与Spring生态的兼容性（如Spring Session等）
 *              建议新代码直接使用RedissonClient或RedisUtil工具类
 * @since 2025-01-01
 */
@Configuration
@ConditionalOnClass(RedissonClient.class)
public class RedisConfig {
    
    /**
     * 自定义RedisTemplate配置，使用FastJSON2序列化
     * 
     * 注意：推荐使用RedissonClient或RedisUtil工具类进行Redis操作，
     * RedisTemplate主要用于Spring生态兼容（如Spring Session等）
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedissonConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用FastJSON2序列化器
        FastJson2RedisSerializer<Object> serializer = new FastJson2RedisSerializer<>(Object.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // key采用String的序列化方式
        template.setKeySerializer(stringSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringSerializer);
        // value序列化方式采用FastJSON2
        template.setValueSerializer(serializer);
        // hash的value序列化方式采用FastJSON2
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * StringRedisTemplate配置
     * 
     * 注意：推荐使用RedissonClient或RedisUtil工具类进行Redis操作
     * 
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedissonConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
    
    /**
     * FastJSON2 Redis序列化器
     * 
     * @param <T> 对象类型
     */
    public static class FastJson2RedisSerializer<T> implements RedisSerializer<T> {
        
        private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
        private final Class<T> clazz;
        
        public FastJson2RedisSerializer(Class<T> clazz) {
            super();
            this.clazz = clazz;
        }
        
        @Override
        public byte[] serialize(T t) throws SerializationException {
            if (t == null) {
                return new byte[0];
            }
            try {
                return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(DEFAULT_CHARSET);
            } catch (Exception ex) {
                throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                String str = new String(bytes, DEFAULT_CHARSET);
                if (clazz == Object.class) {
                    return (T) JSON.parseObject(str, Object.class, 
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased);
                } else {
                    return JSON.parseObject(str, clazz);
                }
            } catch (Exception ex) {
                throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
            }
        }
        
        @Override
        public Class<?> getTargetType() {
            return clazz;
        }
    }
}