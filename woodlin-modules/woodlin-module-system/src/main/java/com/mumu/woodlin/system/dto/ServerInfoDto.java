package com.mumu.woodlin.system.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 服务器信息DTO
 *
 * @author yulin
 * @description 监控页使用的服务器/JVM/磁盘聚合信息
 * @since 2026-06
 */
@Data
@Accessors(chain = true)
@Schema(description = "服务器信息")
public class ServerInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "CPU信息")
    private CpuInfo cpu;

    @Schema(description = "内存信息")
    private MemInfo mem;

    @Schema(description = "JVM信息")
    private JvmInfo jvm;

    @Schema(description = "系统信息")
    private SysInfo sys;

    @Schema(description = "磁盘信息")
    private List<DiskInfo> disk;

    /**
     * CPU 信息
     */
    @Data
    @Accessors(chain = true)
    public static class CpuInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Integer cpuNum;
        private Double total;
        private Double sys;
        private Double used;
        private Double wait;
        private Double free;
    }

    /**
     * 内存信息
     */
    @Data
    @Accessors(chain = true)
    public static class MemInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Double total;
        private Double used;
        private Double free;
        private Double usage;
    }

    /**
     * JVM 信息
     */
    @Data
    @Accessors(chain = true)
    public static class JvmInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Double total;
        private Double max;
        private Double free;
        private Double used;
        private Double usage;
        private String version;
        private String home;
        private String name;
        private String startTime;
        private String runTime;
        private String inputArgs;
    }

    /**
     * 系统信息
     */
    @Data
    @Accessors(chain = true)
    public static class SysInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String computerName;
        private String computerIp;
        private String userDir;
        private String osName;
        private String osArch;
    }

    /**
     * 磁盘信息
     */
    @Data
    @Accessors(chain = true)
    public static class DiskInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String dirName;
        private String sysTypeName;
        private String typeName;
        private String total;
        private String free;
        private String used;
        private Double usage;
    }
}
