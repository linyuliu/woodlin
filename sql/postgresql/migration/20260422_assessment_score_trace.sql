ALTER TABLE sys_assessment_result
  ADD COLUMN IF NOT EXISTS score_trace_json text DEFAULT NULL;
