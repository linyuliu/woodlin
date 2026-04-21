package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 开放 API 概览数据。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Schema(description = "开放API概览")
public class OpenApiOverviewDto {

    /**
     * 应用数量。
     */
    private long appCount;

    /**
     * 凭证数量。
     */
    private long credentialCount;

    /**
     * 策略数量。
     */
    private long policyCount;
}
