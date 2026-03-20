package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import com.mumu.woodlin.common.util.IdGeneratorUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Snowflake ID 生成器。
 */
@Slf4j
public class SnowflakeIdGenerator implements IdGeneratorUtil.LongIdGenerator {

    private static final int WORKER_BITS = 5;
    private static final int DATACENTER_BITS = 5;
    private static final int SEQUENCE_BITS = 12;
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1;
    private static final int WORKER_SHIFT = SEQUENCE_BITS;
    private static final int DATACENTER_SHIFT = WORKER_SHIFT + WORKER_BITS;
    private static final int TIMESTAMP_SHIFT = DATACENTER_SHIFT + DATACENTER_BITS;
    private static final long MAX_WORKER_ID = (1L << WORKER_BITS) - 1;
    private static final long MAX_DATACENTER_ID = (1L << DATACENTER_BITS) - 1;
    private static final long MAX_TIMESTAMP_DELTA = (1L << 41) - 1;

    private final long epoch;
    private final long rollbackToleranceMillis;
    private final LongSupplier timeSupplier;
    private final AtomicLong logicalTimeState = new AtomicLong(0L);
    private final AtomicReference<SnowflakeNodeAssignment> currentAssignment = new AtomicReference<>();
    private final AtomicReference<String> unavailableReason = new AtomicReference<>("Snowflake 节点尚未初始化");

    public SnowflakeIdGenerator(SnowflakeIdProperties properties) {
        this(properties, System::currentTimeMillis);
    }

    SnowflakeIdGenerator(SnowflakeIdProperties properties, LongSupplier timeSupplier) {
        this.epoch = properties.getEpoch();
        this.rollbackToleranceMillis = properties.getRollbackToleranceMillis();
        this.timeSupplier = timeSupplier;
    }

    /**
     * 生成 Snowflake long ID。
     *
     * @return long ID
     */
    public long nextId() {
        return nextLong();
    }

    /**
     * 生成 Snowflake 字符串 ID。
     *
     * @return 十进制字符串 ID
     */
    public String nextIdString() {
        return next();
    }

    /**
     * 获取当前节点分配。
     *
     * @return 当前节点分配，未初始化时返回 null
     */
    public SnowflakeNodeAssignment currentAssignment() {
        return currentAssignment.get();
    }

    /**
     * 更新当前节点分配。
     *
     * @param assignment 新分配结果
     */
    public void updateAssignment(SnowflakeNodeAssignment assignment) {
        validateAssignment(assignment);
        currentAssignment.set(assignment);
        unavailableReason.set(null);
        log.info(
            "Snowflake 节点分配成功: source={}, slot={}, datacenterId={}, workerId={}",
            assignment.source(),
            assignment.slot(),
            assignment.datacenterId(),
            assignment.workerId()
        );
    }

    /**
     * 标记当前生成器不可用。
     *
     * @param reason 不可用原因
     */
    public void markUnavailable(String reason) {
        currentAssignment.set(null);
        unavailableReason.set(reason);
        log.warn("Snowflake 生成器不可用: {}", reason);
    }

    @Override
    public long nextLong() {
        SnowflakeNodeAssignment assignment = currentAssignment.get();
        if (assignment == null) {
            throw new IllegalStateException(resolveUnavailableReason());
        }
        long nextState = nextState(timeSupplier.getAsLong());
        long logicalMillis = extractMillis(nextState);
        long sequence = extractSequence(nextState);
        return composeId(logicalMillis, assignment, sequence);
    }

    private long nextState(long currentMillis) {
        while (true) {
            long previousState = logicalTimeState.get();
            long nextState = calculateNextState(currentMillis, previousState);
            if (logicalTimeState.compareAndSet(previousState, nextState)) {
                return nextState;
            }
        }
    }

    private long calculateNextState(long currentMillis, long previousState) {
        long previousMillis = extractMillis(previousState);
        long previousSequence = extractSequence(previousState);
        if (currentMillis + rollbackToleranceMillis < previousMillis) {
            String message = "系统时钟回拨超过允许范围: currentMillis=" + currentMillis
                + ", previousMillis=" + previousMillis;
            throw new IllegalStateException(
                message
            );
        }
        if (currentMillis > previousMillis) {
            return currentMillis << SEQUENCE_BITS;
        }
        long nextSequence = (previousSequence + 1) & SEQUENCE_MASK;
        if (nextSequence == 0L) {
            return (previousMillis + 1) << SEQUENCE_BITS;
        }
        return (previousMillis << SEQUENCE_BITS) | nextSequence;
    }

    private long composeId(long logicalMillis, SnowflakeNodeAssignment assignment, long sequence) {
        if (logicalMillis < epoch) {
            throw new IllegalStateException("Snowflake 时间戳早于起始时间: " + logicalMillis);
        }
        long timestampDelta = logicalMillis - epoch;
        if (timestampDelta > MAX_TIMESTAMP_DELTA) {
            throw new IllegalStateException("Snowflake 时间戳已超出可用范围: " + logicalMillis);
        }
        return (timestampDelta << TIMESTAMP_SHIFT)
            | ((long) assignment.datacenterId() << DATACENTER_SHIFT)
            | ((long) assignment.workerId() << WORKER_SHIFT)
            | sequence;
    }

    private long extractMillis(long stateValue) {
        return stateValue >>> SEQUENCE_BITS;
    }

    private long extractSequence(long stateValue) {
        return stateValue & SEQUENCE_MASK;
    }

    private void validateAssignment(SnowflakeNodeAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Snowflake 节点分配不能为空");
        }
        if (assignment.workerId() < 0 || assignment.workerId() > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId 超出范围: " + assignment.workerId());
        }
        if (assignment.datacenterId() < 0 || assignment.datacenterId() > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException("datacenterId 超出范围: " + assignment.datacenterId());
        }
    }

    private String resolveUnavailableReason() {
        String reason = unavailableReason.get();
        return reason != null ? reason : "Snowflake 节点尚未初始化";
    }
}
