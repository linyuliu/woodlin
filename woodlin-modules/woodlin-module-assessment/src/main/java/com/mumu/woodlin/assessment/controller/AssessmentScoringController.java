package com.mumu.woodlin.assessment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.assessment.model.dto.ScoringRequest;
import com.mumu.woodlin.assessment.model.vo.ScoringResultVO;
import com.mumu.woodlin.assessment.service.IAssessmentScoringService;
import com.mumu.woodlin.common.response.R;

/**
 * 计分接口
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/assessment/scoring")
@RequiredArgsConstructor
@Validated
@Tag(name = "计分管理", description = "测评计分与常模转换接口")
public class AssessmentScoringController {

    private final IAssessmentScoringService scoringService;

    /**
     * 触发计分：对指定会话执行完整计分流程（原始分 → 维度分 → 常模转换）。
     *
     * <p>若会话已有计分结果且 {@code forceRescore=false}，则直接返回缓存结果。
     */
    @PostMapping("/session/{sessionId}")
    @Operation(summary = "触发会话计分",
            description = "对指定会话执行计分流程，包括原始分、维度分计算和地区/人口学常模转换。")
    public R<ScoringResultVO> score(
            @Parameter(description = "作答会话ID", required = true) @PathVariable Long sessionId,
            @Valid @RequestBody(required = false) ScoringRequest request) {

        if (request == null) {
            request = new ScoringRequest();
        }
        request.setSessionId(sessionId);
        return R.ok(scoringService.scoreSession(request));
    }

    /**
     * 强制重新计分（覆盖已有结果）。
     */
    @PostMapping("/session/{sessionId}/rescore")
    @Operation(summary = "强制重新计分", description = "忽略已有计分结果，重新执行完整计分流程。")
    public R<ScoringResultVO> rescore(
            @Parameter(description = "作答会话ID", required = true) @PathVariable Long sessionId) {

        ScoringRequest request = new ScoringRequest();
        request.setSessionId(sessionId);
        request.setForceRescore(true);
        return R.ok(scoringService.scoreSession(request));
    }
}
