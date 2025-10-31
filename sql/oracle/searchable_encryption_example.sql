-- =============================================
-- Oracle version - Auto-converted from MySQL
-- Source: searchable_encryption_example.sql
-- Database: Oracle 12c+
-- =============================================

-- 可搜索加密示例表
-- 演示如何使用可搜索加密功能存储敏感数据

-- 创建敏感数据表
CREATE TABLE IF NOT EXISTS sys_sensitive_data (
  data_id BIGNUMBER(10) NOT NULL COMMENT '数据ID',
  real_name VARCHAR2(500) DEFAULT NULL COMMENT '真实姓名（加密）',
  real_name_search_index CLOB DEFAULT NULL COMMENT '真实姓名搜索索引',
  id_card VARCHAR2(500) DEFAULT NULL COMMENT '身份证号（加密）',
  mobile VARCHAR2(500) DEFAULT NULL COMMENT '手机号（加密）',
  mobile_search_index CLOB DEFAULT NULL COMMENT '手机号搜索索引',
  email_address VARCHAR2(500) DEFAULT NULL COMMENT '邮箱地址（加密）',
  email_address_search_index CLOB DEFAULT NULL COMMENT '邮箱搜索索引',
  home_address VARCHAR2(1000) DEFAULT NULL COMMENT '家庭住址（加密）',
  home_address_search_index CLOB DEFAULT NULL COMMENT '家庭住址搜索索引',
  bank_card VARCHAR2(500) DEFAULT NULL COMMENT '银行卡号（加密）',
  data_type VARCHAR2(50) DEFAULT NULL COMMENT '数据类型',
  status CHAR(1) DEFAULT '1' COMMENT '状态（1-正常，0-禁用）',
  tenant_id VARCHAR2(20) DEFAULT NULL COMMENT '租户ID',
  remark VARCHAR2(500) DEFAULT NULL COMMENT '备注',
  create_by VARCHAR2(64) DEFAULT NULL COMMENT '创建者',
  create_time TIMESTAMP DEFAULT SYSTIMESTAMP COMMENT '创建时间',
  update_by VARCHAR2(64) DEFAULT NULL COMMENT '更新者',
  update_time TIMESTAMP DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
  del_flag NUMBER(3) DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (data_id),
  KEY idx_tenant_id (tenant_id),
  KEY idx_status (status),
  KEY idx_data_type (data_type),
  KEY idx_create_time (create_time)
) -- Comment: 敏感数据表（可搜索加密示例）;

-- 插入示例数据说明
-- 注意：实际使用时，这些数据应该通过应用程序插入，以便自动加密
-- 下面是未加密的示例数据结构，用于理解数据模型

-- INSERT INTO sys_sensitive_data (data_id, real_name, id_card, mobile, email_address, home_address, bank_card, data_type, status, tenant_id, remark)
-- VALUES 
-- (1, '张三', '110101199001011234', '13800138000', 'zhangsan@example.com', '北京市朝阳区建国路1号', '6222021234567890123', 'PERSONAL', '1', '000000', '测试数据1'),
-- (2, '李四', '310101199002021234', '13900139000', 'lisi@example.com', '上海市浦东新区世纪大道1号', '6222021234567890124', 'PERSONAL', '1', '000000', '测试数据2'),
-- (3, '王五', '440101199003031234', '13700137000', 'wangwu@example.com', '广州市天河区体育西路1号', '6222021234567890125', 'PERSONAL', '1', '000000', '测试数据3');

-- 使用说明：
-- 1. 配置加密密钥：在 application.yml 中配置 woodlin.searchable-encryption.encryption-key
-- 2. 启用加密功能：设置 woodlin.searchable-encryption.enabled=true
-- 3. 使用 API 插入数据：通过 /api/system/sensitive-data/add 接口插入数据，系统会自动加密
-- 4. 模糊搜索：通过 /api/system/sensitive-data/search/name?keyword=张 进行模糊搜索
-- 5. 精确搜索：通过 /api/system/sensitive-data/search/idcard?idCard=110101199001011234 进行精确搜索

-- 性能优化建议：
-- 1. 为搜索索引字段创建全文索引（MySQL 5.7+）：
-- ALTER TABLE sys_sensitive_data ADD FULLCLOB INDEX ft_real_name_search (real_name_search_index);
-- ALTER TABLE sys_sensitive_data ADD FULLCLOB INDEX ft_mobile_search (mobile_search_index);

-- 2. 如果使用 PostgreSQL，可以使用 GIN 索引：
-- CREATE INDEX idx_gin_real_name_search ON sys_sensitive_data USING gin(to_tsvector('simple', real_name_search_index));

-- 3. 定期清理和优化表：
-- OPTIMIZE TABLE sys_sensitive_data;
