package com.mumu.woodlin.assessment.service;

import com.mumu.woodlin.assessment.model.dto.ScoringRequest;
import com.mumu.woodlin.assessment.model.vo.ScoringResultVO;

/**
 * 计分服务接口
 *
 * <p>将数据库中的作答记录、题目元数据和常模数据转换为计分引擎输入，完成以下流程：
 * <ol>
 *   <li>加载会话、题目、选项、维度、作答记录</li>
 *   <li>构建 DSL {@code Questionnaire} 模型</li>
 *   <li>调用 {@code ScoringEngine} 计算原始分、有效分、维度分</li>
 *   <li>通过 {@code NormMatchEngine} 进行地区/人口学常模分层匹配（含多级降级兜底）</li>
 *   <li>查询 {@code NormConversion} 表完成 T分/百分位/等级转换</li>
 *   <li>持久化 {@code AssessmentResult} + {@code AssessmentResultDimension}</li>
 * </ol>
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentScoringService {

    /**
     * 对指定会话进行计分，持久化结果并返回得分视图。
     *
     * <p>若会话已有结果且 {@code forceRescore=false}，则直接返回已有结果。
     *
     * @param request 计分请求，包含 sessionId 及可选的强制重算标志
     * @return 完整计分结果视图
     * @throws com.mumu.woodlin.common.exception.BusinessException 当会话不存在或版本下没有题目时
     */
    ScoringResultVO scoreSession(ScoringRequest request);
}
