package com.mumu.woodlin.admin.id;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.LockService;

import java.util.Properties;

/**
 * Nacos LockService 工厂。
 */
@FunctionalInterface
public interface NacosLockServiceFactory {

    /**
     * 创建 Nacos 锁服务。
     *
     * @param properties Nacos 连接属性
     * @return 锁服务
     * @throws NacosException 创建异常
     */
    LockService create(Properties properties) throws NacosException;
}
