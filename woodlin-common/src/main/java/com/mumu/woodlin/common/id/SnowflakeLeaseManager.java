package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import com.mumu.woodlin.common.util.IdGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Snowflake 节点租约管理器。
 */
@Slf4j
public class SnowflakeLeaseManager implements SmartLifecycle {

    private final List<SnowflakeLeaseProvider> providers;
    private final SnowflakeIdGenerator generator;
    private final SnowflakeIdProperties properties;
    private final AtomicReference<SnowflakeLease> currentLease = new AtomicReference<>();
    private final AtomicReference<ScheduledExecutorService> heartbeatExecutor = new AtomicReference<>();

    private volatile boolean running;

    public SnowflakeLeaseManager(
        List<SnowflakeLeaseProvider> providers,
        SnowflakeIdGenerator generator,
        SnowflakeIdProperties properties
    ) {
        this.providers = providers.stream()
            .sorted(Comparator.comparingInt(SnowflakeLeaseProvider::order))
            .toList();
        this.generator = generator;
        this.properties = properties;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        IdGeneratorUtil.setSnowflakeGenerator(generator);
        initializeLease();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                Thread thread = new Thread(runnable, "woodlin-snowflake-heartbeat");
                thread.setDaemon(true);
                return thread;
            }
        );
        heartbeatExecutor.set(executor);
        long delayMillis = Math.max(properties.resolveHeartbeatInterval().toMillis(), 100L);
        executor.scheduleWithFixedDelay(this::heartbeat, delayMillis, delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        ScheduledExecutorService executor = heartbeatExecutor.getAndSet(null);
        if (executor != null) {
            executor.shutdownNow();
        }
        releaseCurrentLease();
        generator.markUnavailable("Snowflake 生成器已停止");
        IdGeneratorUtil.setSnowflakeGenerator(null);
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private void initializeLease() {
        Optional<SnowflakeLease> lease = acquireAnyLease();
        if (lease.isPresent()) {
            activateLease(lease.get());
        } else {
            generator.markUnavailable("无法分配 Snowflake 节点");
        }
    }

    private void heartbeat() {
        if (!running) {
            return;
        }
        SnowflakeLease lease = currentLease.get();
        if (lease == null || !lease.isDynamic()) {
            return;
        }
        if (lease.renew(properties.getLeaseTtl())) {
            return;
        }
        log.warn("Snowflake 动态租约续约失败，开始重新分配");
        reacquireLease();
    }

    private synchronized void reacquireLease() {
        SnowflakeLease previousLease = currentLease.getAndSet(null);
        if (previousLease != null) {
            safeRelease(previousLease);
        }
        Optional<SnowflakeLease> nextLease = acquireAnyLease();
        if (nextLease.isPresent()) {
            activateLease(nextLease.get());
            return;
        }
        generator.markUnavailable("Snowflake 动态租约续约失败且无法重新分配节点");
    }

    private Optional<SnowflakeLease> acquireAnyLease() {
        for (SnowflakeLeaseProvider provider : providers) {
            try {
                Optional<SnowflakeLease> lease = provider.acquire();
                if (lease.isPresent()) {
                    return lease;
                }
            } catch (Exception exception) {
                log.warn("Snowflake 租约提供者申请失败: source={}", provider.source(), exception);
            }
        }
        return Optional.empty();
    }

    private void activateLease(SnowflakeLease lease) {
        currentLease.set(lease);
        generator.updateAssignment(lease.assignment());
    }

    private void releaseCurrentLease() {
        SnowflakeLease lease = currentLease.getAndSet(null);
        if (lease != null) {
            safeRelease(lease);
        }
    }

    private void safeRelease(SnowflakeLease lease) {
        try {
            lease.release();
        } catch (Exception exception) {
            log.warn("Snowflake 租约释放失败: assignment={}", lease.assignment(), exception);
        }
    }
}
