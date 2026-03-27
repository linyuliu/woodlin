package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 基于网卡和主机信息的兜底节点提供者。
 */
public class NetworkSnowflakeLeaseProvider implements SnowflakeLeaseProvider {

    public static final int ORDER = 400;
    private static final String SOURCE = "NETWORK";

    private final SnowflakeIdProperties properties;

    public NetworkSnowflakeLeaseProvider(SnowflakeIdProperties properties) {
        this.properties = properties;
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
        int slot = calculateStableSlot();
        return Optional.of(
            new FixedSnowflakeLease(
                new SnowflakeNodeAssignment(SOURCE, slot, slot & 31, (slot >>> 5) & 31)
            )
        );
    }

    private int calculateStableSlot() {
        int slotCount = properties.getSlotCount();
        String fingerprint = resolveFingerprint();
        byte[] digest = sha256(fingerprint);
        int hash = ((digest[0] & 0xFF) << 24)
            | ((digest[1] & 0xFF) << 16)
            | ((digest[2] & 0xFF) << 8)
            | (digest[3] & 0xFF);
        return Math.floorMod(hash, slotCount);
    }

    private String resolveFingerprint() {
        List<String> macAddresses = loadMacAddresses();
        if (!macAddresses.isEmpty()) {
            Collections.sort(macAddresses);
            return String.join(",", macAddresses);
        }
        String host = resolveHost();
        if (StringUtils.hasText(host)) {
            return host;
        }
        return resolveProcessId();
    }

    private List<String> loadMacAddresses() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return List.of();
            }
            List<String> addresses = new ArrayList<>();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress == null || hardwareAddress.length == 0) {
                    continue;
                }
                addresses.add(toHex(hardwareAddress));
            }
            return addresses;
        } catch (Exception exception) {
            return List.of();
        }
    }

    private String resolveHost() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (localHost == null) {
                return null;
            }
            if (StringUtils.hasText(localHost.getHostName())) {
                return localHost.getHostName();
            }
            return localHost.getHostAddress();
        } catch (Exception exception) {
            return null;
        }
    }

    private String resolveProcessId() {
        try {
            return Long.toString(ProcessHandle.current().pid());
        } catch (Exception exception) {
            return ManagementFactory.getRuntimeMXBean().getName();
        }
    }

    private byte[] sha256(String source) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 算法不可用", exception);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
