package com.mumu.woodlin.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis工具类
 * 
 * @author mumu
 * @description 基于Redisson的Redis工具类，提供优雅的缓存操作
 * @since 2025-01-01
 */
@Slf4j
@Component
public class RedisUtil {
    
    @Autowired
    private RedissonClient redissonClient;
    
    // =============================String操作===============================
    
    /**
     * 设置字符串值
     * 
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }
    
    /**
     * 设置字符串值并指定过期时间
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeout, unit);
    }
    
    /**
     * 设置字符串值并指定过期时间
     * 
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     */
    public void set(String key, Object value, Duration duration) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, duration);
    }
    
    /**
     * 如果key不存在则设置值
     * 
     * @param key 键
     * @param value 值
     * @return 是否设置成功
     */
    public boolean setIfAbsent(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.trySet(value);
    }
    
    /**
     * 如果key不存在则设置值并指定过期时间
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.trySet(value, timeout, unit);
    }
    
    /**
     * 获取字符串值
     * 
     * @param key 键
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }
    
    /**
     * 获取字符串值，如果不存在则返回默认值
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    public <T> T get(String key, T defaultValue) {
        T value = get(key);
        return ObjectUtil.isNotNull(value) ? value : defaultValue;
    }
    
    /**
     * 获取字符串值，如果不存在则执行supplier并设置值
     * 
     * @param key 键
     * @param supplier 值提供者
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 值
     */
    public <T> T get(String key, Supplier<T> supplier, long timeout, TimeUnit unit) {
        T value = get(key);
        if (ObjectUtil.isNull(value)) {
            value = supplier.get();
            if (ObjectUtil.isNotNull(value)) {
                set(key, value, timeout, unit);
            }
        }
        return value;
    }
    
    /**
     * 删除指定的key
     * 
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }
    
    /**
     * 批量删除key
     * 
     * @param keys 键集合
     * @return 删除的数量
     */
    public long delete(Collection<String> keys) {
        if (CollUtil.isEmpty(keys)) {
            return 0L;
        }
        return redissonClient.getKeys().delete(keys.toArray(new String[0]));
    }
    
    /**
     * 检查key是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return redissonClient.getBucket(key).isExists();
    }
    
    /**
     * 设置key的过期时间
     * 
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redissonClient.getBucket(key).expire(timeout, unit);
    }
    
    /**
     * 获取key的剩余生存时间
     * 
     * @param key 键
     * @return 剩余时间（秒），-1表示永不过期，-2表示key不存在
     */
    public long getExpire(String key) {
        return redissonClient.getBucket(key).remainTimeToLive() / 1000;
    }
    
    // =============================List操作===============================
    
    /**
     * 获取List
     * 
     * @param key 键
     * @return List对象
     */
    public <T> RList<T> getList(String key) {
        return redissonClient.getList(key);
    }
    
    /**
     * 向List右侧添加元素
     * 
     * @param key 键
     * @param value 值
     * @return 添加后List的大小
     */
    public <T> int listRightPush(String key, T value) {
        RList<T> rList = redissonClient.getList(key);
        rList.add(value);
        return rList.size();
    }
    
    /**
     * 从List右侧弹出元素
     * 
     * @param key 键
     * @return 弹出的元素
     */
    public <T> T listRightPop(String key) {
        RList<T> rList = redissonClient.getList(key);
        return rList.isEmpty() ? null : rList.remove(rList.size() - 1);
    }
    
    // =============================Set操作===============================
    
    /**
     * 获取Set
     * 
     * @param key 键
     * @return Set对象
     */
    public <T> RSet<T> getSet(String key) {
        return redissonClient.getSet(key);
    }
    
    /**
     * 向Set中添加元素
     * 
     * @param key 键
     * @param values 值
     * @return 添加的元素数量
     */
    @SafeVarargs
    public final <T> boolean setAdd(String key, T... values) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.addAll(Arrays.asList(values));
    }
    
    /**
     * 检查Set中是否存在指定元素
     * 
     * @param key 键
     * @param value 值
     * @return 是否存在
     */
    public <T> boolean setIsMember(String key, T value) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.contains(value);
    }
    
    // =============================Hash操作===============================
    
    /**
     * 获取Hash
     * 
     * @param key 键
     * @return Hash对象
     */
    public <K, V> RMap<K, V> getHash(String key) {
        return redissonClient.getMap(key);
    }
    
    /**
     * 向Hash中添加键值对
     * 
     * @param key 键
     * @param hashKey Hash键
     * @param value 值
     */
    public <K, V> void hashPut(String key, K hashKey, V value) {
        RMap<K, V> rMap = redissonClient.getMap(key);
        rMap.put(hashKey, value);
    }
    
    /**
     * 从Hash中获取值
     * 
     * @param key 键
     * @param hashKey Hash键
     * @return 值
     */
    public <K, V> V hashGet(String key, K hashKey) {
        RMap<K, V> rMap = redissonClient.getMap(key);
        return rMap.get(hashKey);
    }
    
    /**
     * 批量设置Hash值
     * 
     * @param key 键
     * @param map 键值对映射
     */
    public <K, V> void hashPutAll(String key, Map<K, V> map) {
        RMap<K, V> rMap = redissonClient.getMap(key);
        rMap.putAll(map);
    }
    
    // =============================分布式锁===============================
    
    /**
     * 获取分布式锁
     * 
     * @param key 锁的key
     * @return 分布式锁对象
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }
    
    /**
     * 尝试获取分布式锁
     * 
     * @param key 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = getLock(key);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 释放分布式锁
     * 
     * @param key 锁的key
     */
    public void unlock(String key) {
        RLock lock = getLock(key);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
    
    /**
     * 执行带锁的操作
     * 
     * @param key 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param unit 时间单位
     * @param runnable 要执行的操作
     * @return 是否执行成功
     */
    public boolean executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {
        RLock lock = getLock(key);
        try {
            if (lock.tryLock(waitTime, leaseTime, unit)) {
                try {
                    runnable.run();
                    return true;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("执行带锁操作失败", e);
        }
        return false;
    }
    
    // =============================发布订阅===============================
    
    /**
     * 发布消息
     * 
     * @param channel 频道
     * @param message 消息
     * @return 收到消息的订阅者数量
     */
    public <T> long publish(String channel, T message) {
        RTopic topic = redissonClient.getTopic(channel);
        return topic.publish(message);
    }
    
    /**
     * 订阅消息
     * 
     * @param channel 频道
     * @param listener 消息监听器
     * @return 监听器ID
     */
    public <T> int subscribe(String channel, org.redisson.api.listener.MessageListener<T> listener) {
        RTopic topic = redissonClient.getTopic(channel);
        return topic.addListener(Object.class, listener);
    }
    
    // =============================限流===============================
    
    /**
     * 获取限流器
     * 
     * @param key 限流器key
     * @return 限流器
     */
    public RRateLimiter getRateLimiter(String key) {
        return redissonClient.getRateLimiter(key);
    }
    
    /**
     * 尝试获取令牌
     * 
     * @param key 限流器key
     * @param permits 令牌数量
     * @return 是否获取成功
     */
    public boolean tryAcquire(String key, int permits) {
        RRateLimiter rateLimiter = getRateLimiter(key);
        return rateLimiter.tryAcquire(permits);
    }
    
    // =============================模糊查询===============================
    
    /**
     * 模糊查询key
     * 
     * @param pattern 匹配模式
     * @return 匹配的key集合
     */
    public Set<String> keys(String pattern) {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keysByPattern = keys.getKeysByPattern(pattern);
        Set<String> keySet = new HashSet<>();
        keysByPattern.forEach(keySet::add);
        return keySet;
    }
    
    /**
     * 批量删除匹配模式的key
     * 
     * @param pattern 匹配模式
     * @return 删除的数量
     */
    public long deleteByPattern(String pattern) {
        RKeys keys = redissonClient.getKeys();
        return keys.deleteByPattern(pattern);
    }
}