package com.mumu.woodlin.assessment.service;

import com.mumu.woodlin.assessment.model.dto.SaveSnapshotDTO;
import com.mumu.woodlin.assessment.model.dto.StartSessionDTO;
import com.mumu.woodlin.assessment.model.dto.SubmitAnswersDTO;
import com.mumu.woodlin.assessment.model.vo.RuntimePayloadVO;
import com.mumu.woodlin.assessment.model.vo.RuntimePublishVO;

/**
 * 作答运行时服务
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentRuntimeService {

    /**
     * 获取运行时发布信息
     *
     * @param publishId 发布ID
     * @return 发布信息
     */
    RuntimePublishVO getPublishInfo(Long publishId);

    /**
     * 启动或续答会话
     *
     * @param dto 启动参数
     * @param userId 当前用户ID
     * @return 运行时完整载荷
     */
    RuntimePayloadVO startOrResumeSession(StartSessionDTO dto, Long userId);

    /**
     * 加载会话运行时载荷
     *
     * @param sessionId 会话ID
     * @param userId 当前用户ID
     * @return 运行时完整载荷
     */
    RuntimePayloadVO loadSessionPayload(Long sessionId, Long userId);

    /**
     * 保存会话快照
     *
     * @param dto 快照参数
     * @param userId 当前用户ID
     */
    void saveSnapshot(SaveSnapshotDTO dto, Long userId);

    /**
     * 提交会话
     *
     * @param dto 提交参数
     * @param userId 当前用户ID
     */
    void submitSession(SubmitAnswersDTO dto, Long userId);
}
