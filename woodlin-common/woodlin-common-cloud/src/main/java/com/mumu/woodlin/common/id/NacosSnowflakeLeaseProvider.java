package com.mumu.woodlin.common.id;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.lock.LockService;
import com.alibaba.nacos.api.lock.model.LockInstance;
import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/**
 * 基于 Nacos LockService 的 Snowflake 节点提供者。
 */
@Slf4j
public class NacosSnowflakeLeaseProvider implements SnowflakeLeaseProvider, DisposableBean {

    public static final int ORDER = 200;
    private static final int LOCK_FEATURE_UNSUPPORTED_ERROR_CODE = 501;
    private static final String SOURCE = "NACOS";
    private static final String LOCK_TYPE = "NACOS_LOCK";
    private static final String OWNER_TOKEN_PARAM = "ownerToken";

    private final SnowflakeIdProperties properties;
    private final Environment environment;
    private final NacosLockServiceFactory lockServiceFactory;
    private final String allocationNamespace;

    private volatile LockService lockService;

    public NacosSnowflakeLeaseProvider(
        SnowflakeIdProperties properties,
        Environment environment,
        NacosLockServiceFactory lockServiceFactory
    ) {
        this.properties = properties;
        this.environment = environment;
        this.lockServiceFactory = lockServiceFactory;
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : null;
        this.allocationNamespace = properties.resolveNamespace(activeProfile);
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public String source() {
        return SOURCE;
    }

    @Override
    public Optional<SnowflakeLease> acquire() {
        if (!Boolean.TRUE.equals(properties.getNacos().getEnabled())) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(resolveServerAddr())) {
            return Optional.empty();
        }
        LockService service = getLockService();
        String ownerToken = UUID.randomUUID().toString();
        for (int slot = 0; slot < properties.getSlotCount(); slot++) {
            String lockKey = buildLockKey(slot);
            LockInstance lockInstance = newLockInstance(lockKey, ownerToken);
            try {
                if (Boolean.TRUE.equals(service.remoteTryLock(lockInstance))) {
                    SnowflakeNodeAssignment assignment =
                        new SnowflakeNodeAssignment(SOURCE, slot, slot & 31, (slot >>> 5) & 31);
                    return Optional.of(new NacosLease(assignment, lockInstance, ownerToken));
                }
            } catch (NacosException exception) {
                throw new IllegalStateException("Nacos Snowflake 节点申请失败", exception);
            } catch (RuntimeException exception) {
                if (isLockFeatureUnsupported(exception)) {
                    log.warn("当前 Nacos 服务端不支持 LockService，跳过 Snowflake Nacos 租约提供者");
                    return Optional.empty();
                }
                throw new IllegalStateException("Nacos Snowflake 节点申请失败", exception);
            }
        }
        return Optional.empty();
    }

    @Override
    public void destroy() throws Exception {
        LockService service = lockService;
        if (service != null) {
            service.shutdown();
        }
    }

    private LockService getLockService() {
        if (lockService != null) {
            return lockService;
        }
        synchronized (this) {
            if (lockService == null) {
                try {
                    lockService = lockServiceFactory.create(buildNacosProperties());
                } catch (NacosException exception) {
                    throw new IllegalStateException("创建 Nacos LockService 失败", exception);
                }
            }
            return lockService;
        }
    }

    private Properties buildNacosProperties() {
        Properties nacosProperties = new Properties();
        nacosProperties.setProperty(PropertyKeyConst.SERVER_ADDR, resolveServerAddr());
        putIfHasText(nacosProperties, PropertyKeyConst.NAMESPACE, resolveNacosNamespace());
        putIfHasText(nacosProperties, PropertyKeyConst.USERNAME, resolveUsername());
        putIfHasText(nacosProperties, PropertyKeyConst.PASSWORD, resolvePassword());
        return nacosProperties;
    }

    private LockInstance newLockInstance(String key, String ownerToken) {
        long expiredTime = System.currentTimeMillis() + properties.getLeaseTtl().toMillis();
        LockInstance instance = new LockInstance(key, expiredTime, LOCK_TYPE);
        instance.setParams(Map.of(OWNER_TOKEN_PARAM, ownerToken));
        return instance;
    }

    private String buildLockKey(int slot) {
        return properties.getNacos().getLockPrefix() + ":" + allocationNamespace + ":slot:" + slot;
    }

    private String resolveServerAddr() {
        return resolveValue(
            properties.getNacos().getServerAddr(),
            environment.getProperty("spring.cloud.nacos.server-addr")
        );
    }

    private String resolveNacosNamespace() {
        return resolveValue(
            properties.getNacos().getNamespace(),
            environment.getProperty("spring.cloud.nacos.namespace"),
            environment.getProperty("spring.cloud.nacos.config.namespace")
        );
    }

    private String resolveUsername() {
        return resolveValue(
            properties.getNacos().getUsername(),
            environment.getProperty("spring.cloud.nacos.username")
        );
    }

    private String resolvePassword() {
        return resolveValue(
            properties.getNacos().getPassword(),
            environment.getProperty("spring.cloud.nacos.password")
        );
    }

    private String resolveValue(String... candidates) {
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void putIfHasText(Properties properties, String key, String value) {
        if (StringUtils.hasText(value)) {
            properties.setProperty(key, value);
        }
    }

    private boolean isLockFeatureUnsupported(RuntimeException exception) {
        if (exception instanceof NacosRuntimeException nacosRuntimeException) {
            if (nacosRuntimeException.getErrCode() == LOCK_FEATURE_UNSUPPORTED_ERROR_CODE) {
                return true;
            }
            return containsUnsupportedLockMessage(nacosRuntimeException.getMessage());
        }
        return containsUnsupportedLockMessage(exception.getMessage());
    }

    private boolean containsUnsupportedLockMessage(String message) {
        return StringUtils.hasText(message) && message.contains("not support lock feature");
    }

    private final class NacosLease implements SnowflakeLease {

        private final SnowflakeNodeAssignment assignment;
        private final LockInstance lockInstance;
        private final String ownerToken;

        private NacosLease(SnowflakeNodeAssignment assignment, LockInstance lockInstance, String ownerToken) {
            this.assignment = assignment;
            this.lockInstance = lockInstance;
            this.ownerToken = ownerToken;
        }

        @Override
        public SnowflakeNodeAssignment assignment() {
            return assignment;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public boolean renew(java.time.Duration leaseTtl) {
            lockInstance.setExpiredTime(System.currentTimeMillis() + leaseTtl.toMillis());
            lockInstance.setParams(Map.of(OWNER_TOKEN_PARAM, (Serializable) ownerToken));
            try {
                boolean renewed = Boolean.TRUE.equals(getLockService().remoteTryLock(lockInstance));
                if (!renewed) {
                    log.warn("Nacos Snowflake 租约续约失败: key={}", lockInstance.getKey());
                }
                return renewed;
            } catch (NacosException exception) {
                throw new IllegalStateException("Nacos Snowflake 租约续约失败", exception);
            }
        }

        @Override
        public void release() {
            try {
                getLockService().remoteReleaseLock(lockInstance);
            } catch (NacosException exception) {
                throw new IllegalStateException("Nacos Snowflake 租约释放失败", exception);
            }
        }
    }
}
