package com.mumu.woodlin.assessment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.assessment.model.dto.SaveSnapshotDTO;
import com.mumu.woodlin.assessment.model.dto.StartSessionDTO;
import com.mumu.woodlin.assessment.model.dto.SubmitAnswersDTO;
import com.mumu.woodlin.assessment.model.vo.RuntimePayloadVO;
import com.mumu.woodlin.assessment.model.vo.RuntimePublishVO;
import com.mumu.woodlin.assessment.service.IAssessmentRuntimeService;
import com.mumu.woodlin.common.response.R;

/**
 * 作答运行时接口
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/assessment/runtime")
@RequiredArgsConstructor
@Validated
@Tag(name = "作答运行时", description = "面向受试者/考生的答题运行时接口")
public class AssessmentRuntimeController {

    private final IAssessmentRuntimeService runtimeService;

    @GetMapping("/publish/{publishId}")
    @Operation(summary = "获取发布运行时信息")
    public R<RuntimePublishVO> getPublishInfo(
            @Parameter(description = "发布ID", required = true) @PathVariable Long publishId) {
        return R.ok(runtimeService.getPublishInfo(publishId));
    }

    @PostMapping("/session/start")
    @Operation(summary = "启动或续答会话")
    public R<RuntimePayloadVO> startOrResumeSession(
            @Valid @RequestBody StartSessionDTO dto,
            @RequestParam(required = false) Long userId) {
        return R.ok(runtimeService.startOrResumeSession(dto, resolveUserId(userId)));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "加载会话运行时载荷")
    public R<RuntimePayloadVO> loadSessionPayload(
            @Parameter(description = "会话ID", required = true) @PathVariable Long sessionId,
            @RequestParam(required = false) Long userId) {
        return R.ok(runtimeService.loadSessionPayload(sessionId, resolveUserId(userId)));
    }

    @PostMapping("/snapshot")
    @Operation(summary = "保存作答快照")
    public R<Void> saveSnapshot(
            @Valid @RequestBody SaveSnapshotDTO dto,
            @RequestParam(required = false) Long userId) {
        runtimeService.saveSnapshot(dto, resolveUserId(userId));
        return R.ok();
    }

    @PostMapping("/submit")
    @Operation(summary = "提交作答并完成会话")
    public R<Void> submitSession(
            @Valid @RequestBody SubmitAnswersDTO dto,
            @RequestParam(required = false) Long userId) {
        runtimeService.submitSession(dto, resolveUserId(userId));
        return R.ok();
    }

    private Long resolveUserId(Long requestUserId) {
        Long userId = requestUserId;
        // TODO: 从 SecurityContext 中提取已认证用户ID
        return userId;
    }
}
