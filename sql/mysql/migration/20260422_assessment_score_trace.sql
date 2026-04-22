ALTER TABLE `sys_assessment_result`
  ADD COLUMN `score_trace_json` longtext DEFAULT NULL COMMENT '计分审计轨迹 JSON'
    AFTER `report_json`;
