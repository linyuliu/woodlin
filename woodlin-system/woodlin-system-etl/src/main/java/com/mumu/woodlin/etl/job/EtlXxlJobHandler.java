package com.mumu.woodlin.etl.job;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.etl.service.IEtlJobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * ETL XXL-Job 执行入口。
 *
 * @author mumu
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "xxl.job.enabled", havingValue = "true")
public class EtlXxlJobHandler {

    private final IEtlJobService etlJobService;
    private final ObjectMapper objectMapper;

    /**
     * ETL 同步执行器。
     * <p>
     * 支持参数格式：
     * <ul>
     *     <li>纯数字：{@code 10001}</li>
     *     <li>键值形式：{@code jobId=10001}</li>
     *     <li>JSON：{@code {"jobId":10001}}</li>
     * </ul>
     * </p>
     */
    @XxlJob("woodlinEtlSyncHandler")
    public void woodlinEtlSyncHandler() {
        String jobParam = XxlJobHelper.getJobParam();
        Long jobId = resolveJobId(jobParam);
        if (jobId == null) {
            String message = "XXL-Job 参数中未解析到有效 jobId: " + jobParam;
            XxlJobHelper.log(message);
            throw new BusinessException(message);
        }
        XxlJobHelper.log("开始执行 ETL 任务: jobId=" + jobId);
        boolean success = etlJobService.executeJob(jobId);
        if (!success) {
            String message = "执行 ETL 任务失败: jobId=" + jobId;
            XxlJobHelper.log(message);
            throw new BusinessException(message);
        }
        XxlJobHelper.log("执行 ETL 任务完成: jobId=" + jobId);
    }

    private Long resolveJobId(String jobParam) {
        if (!StringUtils.hasText(jobParam)) {
            return null;
        }
        String trimmed = jobParam.trim();
        Long direct = parseLong(trimmed);
        if (direct != null) {
            return direct;
        }
        if (trimmed.startsWith("jobId=")) {
            return parseLong(trimmed.substring("jobId=".length()));
        }
        if (trimmed.startsWith("job_id=")) {
            return parseLong(trimmed.substring("job_id=".length()));
        }
        try {
            Map<String, Object> json = objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {
            });
            Object jobIdValue = json.get("jobId");
            if (jobIdValue == null) {
                jobIdValue = json.get("job_id");
            }
            return parseLong(jobIdValue);
        } catch (Exception exception) {
            log.warn("解析 XXL-Job 参数失败: {}", jobParam, exception);
            return null;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (Exception exception) {
            return null;
        }
    }
}
