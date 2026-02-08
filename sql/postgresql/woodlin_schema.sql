-- =============================================
-- Woodlin 多租户中后台管理系统数据库脚本 (PostgreSQL)
-- 描述: 完整模块表结构（含字典、文件、任务、SQL2API、ETL等）
-- 版本: 2.0.0
-- 说明: 由 MySQL 脚本转换，已适配 PostgreSQL 语法
-- =============================================

-- 连接到 woodlin 数据库
-- \c woodlin;

DROP TABLE IF EXISTS sys_tenant CASCADE;
CREATE TABLE sys_tenant (
                          tenant_id     varchar(64)  NOT NULL,
                          tenant_name   varchar(100) NOT NULL,
                          tenant_code   varchar(50)  NOT NULL,
                          contact_name  varchar(50)  DEFAULT NULL,
                          contact_phone varchar(20)  DEFAULT NULL,
                          contact_email varchar(100) DEFAULT NULL,
                          status        char(1)      DEFAULT '1',
                          expire_time   timestamp    DEFAULT NULL,
                          user_limit    integer      DEFAULT 100,
                          remark        varchar(500) DEFAULT NULL,
                          create_by     varchar(64)  DEFAULT NULL,
                          create_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
                          update_by     varchar(64)  DEFAULT NULL,
                          update_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
                          deleted       char(1)      DEFAULT '0',
                          PRIMARY KEY (tenant_id)
);

DROP TABLE IF EXISTS sys_dept CASCADE;
CREATE TABLE sys_dept (
                        dept_id     bigint      NOT NULL,
                        parent_id   bigint       DEFAULT 0,
                        ancestors   varchar(500) DEFAULT '',
                        dept_name   varchar(30) NOT NULL,
                        dept_code   varchar(50)  DEFAULT NULL,
                        sort_order  integer      DEFAULT 0,
                        leader      varchar(20)  DEFAULT NULL,
                        phone       varchar(11)  DEFAULT NULL,
                        email       varchar(50)  DEFAULT NULL,
                        status      char(1)      DEFAULT '1',
                        tenant_id   varchar(64)  DEFAULT NULL,
                        remark      varchar(500) DEFAULT NULL,
                        create_by   varchar(64)  DEFAULT NULL,
                        create_time timestamp    DEFAULT CURRENT_TIMESTAMP,
                        update_by   varchar(64)  DEFAULT NULL,
                        update_time timestamp    DEFAULT CURRENT_TIMESTAMP,
                        deleted     char(1)      DEFAULT '0',
    PRIMARY KEY (dept_id)
);

DROP TABLE IF EXISTS sys_user CASCADE;
CREATE TABLE sys_user (
                        user_id         bigint      NOT NULL,
                        username        varchar(30) NOT NULL,
                        nickname        varchar(30) NOT NULL,
                        real_name       varchar(30)  DEFAULT NULL,
                        password        varchar(100) DEFAULT '',
                        email           varchar(50)  DEFAULT '',
                        mobile          varchar(11)  DEFAULT '',
                        avatar          varchar(200) DEFAULT '',
                        gender          smallint     DEFAULT 0,
                        birthday        timestamp    DEFAULT NULL,
                        status          char(1)      DEFAULT '1',
                        tenant_id       varchar(64)  DEFAULT NULL,
                        dept_id         bigint       DEFAULT NULL,
                        last_login_time timestamp    DEFAULT NULL,
                        last_login_ip   varchar(128) DEFAULT '',
                        login_count     integer      DEFAULT 0,
                        pwd_error_count integer      DEFAULT 0,
                        lock_time       timestamp    DEFAULT NULL,
                        pwd_change_time timestamp    DEFAULT NULL,
                        is_first_login  smallint     DEFAULT 1,
                        pwd_expire_days integer      DEFAULT NULL,
                        remark          varchar(500) DEFAULT NULL,
                        create_by       varchar(64)  DEFAULT NULL,
                        create_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
                        update_by       varchar(64)  DEFAULT NULL,
                        update_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
                        deleted         char(1)      DEFAULT '0',
                        PRIMARY KEY (user_id)
);

DROP TABLE IF EXISTS sys_role CASCADE;
CREATE TABLE sys_role (
                        role_id        bigint       NOT NULL,
                        parent_role_id bigint       DEFAULT NULL,
                        role_level     integer      DEFAULT 0,
                        role_path      varchar(500) DEFAULT '',
                        role_name      varchar(30)  NOT NULL,
                        role_code      varchar(100) NOT NULL,
                        sort_order     integer      DEFAULT 0,
                        data_scope     char(1)      DEFAULT '1',
                        is_inheritable char(1)      DEFAULT '1',
                        status         char(1)      DEFAULT '1',
                        tenant_id      varchar(64)  DEFAULT NULL,
                        remark         varchar(500) DEFAULT NULL,
                        create_by      varchar(64)  DEFAULT NULL,
                        create_time    timestamp    DEFAULT CURRENT_TIMESTAMP,
                        update_by      varchar(64)  DEFAULT NULL,
                        update_time    timestamp    DEFAULT CURRENT_TIMESTAMP,
                        deleted        char(1)      DEFAULT '0',
    PRIMARY KEY (role_id)
);

DROP TABLE IF EXISTS sys_permission CASCADE;
CREATE TABLE sys_permission (
                              permission_id   bigint      NOT NULL,
                              parent_id       bigint       DEFAULT 0,
                              permission_name varchar(50) NOT NULL,
                              permission_code varchar(100) DEFAULT NULL,
                              permission_type char(1)      DEFAULT 'M',
                              path            varchar(200) DEFAULT '',
                              component       varchar(255) DEFAULT NULL,
                              icon            varchar(100) DEFAULT '#',
                              sort_order      integer      DEFAULT 0,
                              status          char(1)      DEFAULT '1',
                              is_frame        char(1)      DEFAULT '0',
                              is_cache        char(1)      DEFAULT '0',
                              visible         char(1)      DEFAULT '1',
                              remark          varchar(500) DEFAULT '',
                              create_by       varchar(64)  DEFAULT NULL,
                              create_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
                              update_by       varchar(64)  DEFAULT NULL,
                              update_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
                              deleted         char(1)      DEFAULT '0',
    PRIMARY KEY (permission_id)
);

DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
                             user_id bigint NOT NULL,
                             role_id bigint NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

DROP TABLE IF EXISTS sys_role_permission CASCADE;
CREATE TABLE sys_role_permission (
                                   role_id       bigint NOT NULL,
                                   permission_id bigint NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

DROP TABLE IF EXISTS sys_role_dept CASCADE;
CREATE TABLE sys_role_dept
(
  role_id bigint NOT NULL,
  dept_id bigint NOT NULL,
  PRIMARY KEY (role_id, dept_id)
);

DROP TABLE IF EXISTS sys_role_hierarchy CASCADE;
CREATE TABLE sys_role_hierarchy
(
  ancestor_role_id   bigint  NOT NULL,
  descendant_role_id bigint  NOT NULL,
  distance           integer NOT NULL DEFAULT 0,
  tenant_id          varchar(64)      DEFAULT NULL,
  create_time        timestamp        DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ancestor_role_id, descendant_role_id)
);

DROP TABLE IF EXISTS sys_role_inherited_permission CASCADE;
CREATE TABLE sys_role_inherited_permission
(
  role_id        bigint NOT NULL,
  permission_id  bigint NOT NULL,
  is_inherited   char(1)     DEFAULT '0',
  inherited_from bigint      DEFAULT NULL,
  tenant_id      varchar(64) DEFAULT NULL,
  update_time    timestamp   DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, permission_id)
);

DROP TABLE IF EXISTS sys_config CASCADE;
CREATE TABLE sys_config (
                          config_id    bigint NOT NULL,
                          config_name  varchar(100) DEFAULT '',
                          config_key   varchar(100) DEFAULT '',
                          config_value varchar(500) DEFAULT '',
                          config_type  char(1)      DEFAULT 'N',
                          tenant_id    varchar(64)  DEFAULT NULL,
                          remark       varchar(500) DEFAULT NULL,
                          create_by    varchar(64)  DEFAULT NULL,
                          create_time  timestamp    DEFAULT CURRENT_TIMESTAMP,
                          update_by    varchar(64)  DEFAULT NULL,
                          update_time  timestamp    DEFAULT CURRENT_TIMESTAMP,
                          deleted      char(1)      DEFAULT '0',
                          PRIMARY KEY (config_id)
);

DROP TABLE IF EXISTS sys_oper_log CASCADE;
CREATE TABLE sys_oper_log (
                            oper_id        bigint NOT NULL,
                            title          varchar(50)   DEFAULT '',
                            business_type  smallint      DEFAULT 0,
                            method         varchar(200)  DEFAULT '',
                            request_method varchar(10)   DEFAULT '',
                            oper_url       varchar(255)  DEFAULT '',
                            oper_ip        varchar(128)  DEFAULT '',
                            oper_location  varchar(255)  DEFAULT '',
                            oper_param     varchar(2000) DEFAULT '',
                            json_result    varchar(2000) DEFAULT '',
                            status         smallint      DEFAULT 0,
                            error_msg      varchar(2000) DEFAULT '',
                            oper_time      timestamp     DEFAULT CURRENT_TIMESTAMP,
                            cost_time      bigint        DEFAULT 0,
                            tenant_id      varchar(64)   DEFAULT NULL,
                            create_by      varchar(64)   DEFAULT NULL,
                            create_time    timestamp     DEFAULT CURRENT_TIMESTAMP,
                            update_by      varchar(64)   DEFAULT NULL,
                            update_time    timestamp     DEFAULT CURRENT_TIMESTAMP,
                            deleted        char(1)       DEFAULT '0',
    PRIMARY KEY (oper_id)
);

DROP TABLE IF EXISTS sys_storage_config CASCADE;
CREATE TABLE sys_storage_config
(
  config_id          bigint       NOT NULL,
  config_name        varchar(100) NOT NULL,
  storage_type       varchar(20)  NOT NULL,
  access_key         varchar(200) DEFAULT NULL,
  secret_key         varchar(500) DEFAULT NULL,
  endpoint           varchar(500) DEFAULT NULL,
  bucket_name        varchar(100) DEFAULT NULL,
  region             varchar(50)  DEFAULT NULL,
  base_path          varchar(200) DEFAULT '/',
  domain             varchar(500) DEFAULT NULL,
  is_default         char(1)      DEFAULT '0',
  is_public          char(1)      DEFAULT '0',
  status             char(1)      DEFAULT '1',
  max_file_size      bigint       DEFAULT 104857600,
  allowed_extensions varchar(500) DEFAULT NULL,
  tenant_id          varchar(64)  DEFAULT NULL,
  remark             varchar(500) DEFAULT NULL,
  create_by          varchar(64)  DEFAULT NULL,
  create_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by          varchar(64)  DEFAULT NULL,
  update_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted            char(1)      DEFAULT '0',
  PRIMARY KEY (config_id)
);

DROP TABLE IF EXISTS sys_upload_policy CASCADE;
CREATE TABLE sys_upload_policy
(
  policy_id          bigint       NOT NULL,
  policy_name        varchar(100) NOT NULL,
  policy_code        varchar(50)  NOT NULL,
  storage_config_id  bigint       NOT NULL,
  detect_file_type   char(1)      DEFAULT '0',
  check_file_size    char(1)      DEFAULT '1',
  max_file_size      bigint       DEFAULT 104857600,
  allowed_extensions varchar(500) DEFAULT NULL,
  allowed_mime_types varchar(500) DEFAULT NULL,
  check_md5          char(1)      DEFAULT '1',
  allow_duplicate    char(1)      DEFAULT '1',
  generate_thumbnail char(1)      DEFAULT '0',
  thumbnail_width    integer      DEFAULT 200,
  thumbnail_height   integer      DEFAULT 200,
  path_pattern       varchar(200) DEFAULT '/{yyyy}/{MM}/{dd}/',
  file_name_pattern  varchar(100) DEFAULT '{timestamp}_{random}',
  signature_expires  integer      DEFAULT 3600,
  callback_url       varchar(500) DEFAULT NULL,
  status             char(1)      DEFAULT '1',
  tenant_id          varchar(64)  DEFAULT NULL,
  remark             varchar(500) DEFAULT NULL,
  create_by          varchar(64)  DEFAULT NULL,
  create_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by          varchar(64)  DEFAULT NULL,
  update_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted            char(1)      DEFAULT '0',
  PRIMARY KEY (policy_id)
);

DROP TABLE IF EXISTS sys_file CASCADE;
CREATE TABLE sys_file (
                        file_id            bigint       NOT NULL,
                        file_name          varchar(100) NOT NULL,
                        original_name      varchar(100) DEFAULT NULL,
                        file_path          varchar(255) NOT NULL,
                        file_url           varchar(500) DEFAULT NULL,
                        file_size          bigint       DEFAULT 0,
                        file_extension     varchar(20)  DEFAULT NULL,
                        file_type          varchar(50)  DEFAULT NULL,
                        mime_type          varchar(100) DEFAULT NULL,
                        detected_mime_type varchar(100) DEFAULT NULL,
                        file_md5           varchar(32)  DEFAULT NULL,
                        file_sha256        varchar(64)  DEFAULT NULL,
                        storage_type       varchar(20)  DEFAULT 'local',
                        storage_config_id  bigint       DEFAULT NULL,
                        upload_policy_id   bigint       DEFAULT NULL,
                        bucket_name        varchar(100) DEFAULT NULL,
                        object_key         varchar(500) DEFAULT NULL,
                        is_image           char(1)      DEFAULT '0',
                        image_width        integer      DEFAULT NULL,
                        image_height       integer      DEFAULT NULL,
                        thumbnail_path     varchar(255) DEFAULT NULL,
                        thumbnail_url      varchar(500) DEFAULT NULL,
                        is_public          char(1)      DEFAULT '0',
                        access_count       integer      DEFAULT 0,
                        last_access_time   timestamp    DEFAULT NULL,
                        expired_time       timestamp    DEFAULT NULL,
                        upload_ip          varchar(128) DEFAULT NULL,
                        user_agent         varchar(500) DEFAULT NULL,
                        tenant_id          varchar(64)  DEFAULT NULL,
                        business_type      varchar(50)  DEFAULT NULL,
                        business_id        varchar(100) DEFAULT NULL,
                        remark             varchar(500) DEFAULT NULL,
                        create_by          varchar(64)  DEFAULT NULL,
                        create_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
                        update_by          varchar(64)  DEFAULT NULL,
                        update_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
                        deleted            char(1)      DEFAULT '0',
    PRIMARY KEY (file_id)
);

DROP TABLE IF EXISTS sys_upload_token CASCADE;
CREATE TABLE sys_upload_token
(
  token_id           bigint       NOT NULL,
  token              varchar(200) NOT NULL,
  policy_id          bigint       NOT NULL,
  user_id            bigint       DEFAULT NULL,
  signature          varchar(500) NOT NULL,
  max_file_size      bigint       DEFAULT NULL,
  allowed_extensions varchar(500) DEFAULT NULL,
  expire_time        timestamp    NOT NULL,
  is_used            char(1)      DEFAULT '0',
  used_time          timestamp    DEFAULT NULL,
  file_id            bigint       DEFAULT NULL,
  tenant_id          varchar(64)  DEFAULT NULL,
  create_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (token_id)
);

DROP TABLE IF EXISTS sys_job CASCADE;
CREATE TABLE sys_job (
                       job_id            bigint       NOT NULL,
                       job_name          varchar(64)  NOT NULL DEFAULT '',
                       job_group         varchar(64)  NOT NULL DEFAULT 'DEFAULT',
                       invoke_target     varchar(500) NOT NULL,
                       cron_expression   varchar(255)          DEFAULT '',
                       misfire_policy    varchar(20)           DEFAULT '3',
                       concurrent        char(1)               DEFAULT '1',
                       status            char(1)               DEFAULT '0',
                       next_execute_time timestamp             DEFAULT NULL,
                       last_execute_time timestamp             DEFAULT NULL,
                       tenant_id         varchar(64)           DEFAULT NULL,
                       remark            varchar(500)          DEFAULT '',
                       create_by         varchar(64)           DEFAULT NULL,
                       create_time       timestamp             DEFAULT CURRENT_TIMESTAMP,
                       update_by         varchar(64)           DEFAULT NULL,
                       update_time       timestamp             DEFAULT CURRENT_TIMESTAMP,
                       deleted           char(1)               DEFAULT '0',
    PRIMARY KEY (job_id, job_name, job_group)
);

DROP TABLE IF EXISTS gen_table CASCADE;
CREATE TABLE gen_table (
                         table_id        bigint NOT NULL,
                         table_name      varchar(200) DEFAULT '',
                         table_comment   varchar(500) DEFAULT '',
                         class_name      varchar(100) DEFAULT '',
                         package_name    varchar(100) DEFAULT NULL,
                         module_name     varchar(30)  DEFAULT NULL,
                         business_name   varchar(30)  DEFAULT NULL,
                         function_name   varchar(50)  DEFAULT NULL,
                         function_author varchar(50)  DEFAULT NULL,
                         gen_type        char(1)      DEFAULT '0',
                         gen_path        varchar(200) DEFAULT '/',
                         pk_column       varchar(100) DEFAULT NULL,
                         remark          varchar(500) DEFAULT NULL,
                         create_by       varchar(64)  DEFAULT NULL,
                         create_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
                         update_by       varchar(64)  DEFAULT NULL,
                         update_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
                         deleted         char(1)      DEFAULT '0',
    PRIMARY KEY (table_id)
);

DROP TABLE IF EXISTS sql2api_config CASCADE;
CREATE TABLE sql2api_config
(
  api_id            BIGINT       NOT NULL,
  api_name          VARCHAR(100) NOT NULL,
  api_path          VARCHAR(200) NOT NULL,
  http_method       VARCHAR(10)  NOT NULL DEFAULT 'GET',
  datasource_name   VARCHAR(50)  NOT NULL DEFAULT 'master',
  sql_type          VARCHAR(20)  NOT NULL,
  sql_content       TEXT         NOT NULL,
  params_config     TEXT,
  result_type       VARCHAR(20)  NOT NULL DEFAULT 'list',
  cache_enabled     smallint              DEFAULT 0,
  cache_expire      INT                   DEFAULT 300,
  encrypt_enabled   smallint              DEFAULT 0,
  encrypt_algorithm VARCHAR(20),
  auth_required     smallint              DEFAULT 1,
  auth_type         VARCHAR(20)           DEFAULT 'TOKEN',
  flow_limit        INT                   DEFAULT 0,
  api_desc          VARCHAR(500),
  enabled           smallint              DEFAULT 1,
  status            INT                   DEFAULT 0,
  create_by         VARCHAR(64),
  create_time       timestamp,
  update_by         VARCHAR(64),
  update_time       timestamp,
  PRIMARY KEY (api_id)
);

DROP TABLE IF EXISTS sql2api_orchestration CASCADE;
CREATE TABLE sql2api_orchestration
(
  orchestration_id     BIGINT       NOT NULL,
  orchestration_name   VARCHAR(100) NOT NULL,
  orchestration_path   VARCHAR(200) NOT NULL,
  orchestration_config TEXT,
  execution_order      TEXT,
  field_mapping        TEXT,
  validation_rules     TEXT,
  error_strategy       VARCHAR(20) DEFAULT 'STOP',
  timeout              INT         DEFAULT 30000,
  enabled              smallint    DEFAULT 1,
  orchestration_desc   VARCHAR(500),
  create_by            VARCHAR(64),
  create_time          timestamp,
  update_by            VARCHAR(64),
  update_time          timestamp,
  PRIMARY KEY (orchestration_id)
);

DROP TABLE IF EXISTS infra_datasource CASCADE;
CREATE TABLE infra_datasource
(
  id              BIGINT       NOT NULL,
  datasource_code VARCHAR(64)  NOT NULL,
  datasource_name VARCHAR(128) NOT NULL,
  datasource_type VARCHAR(32)  NOT NULL,
  driver_class    VARCHAR(256) NOT NULL,
  jdbc_url        VARCHAR(512) NOT NULL,
  username        VARCHAR(128) NOT NULL,
  password        VARCHAR(256) NOT NULL,
  test_sql        VARCHAR(512)          DEFAULT NULL,
  status          TINYINT      NOT NULL DEFAULT 1,
  owner           VARCHAR(64)           DEFAULT NULL,
  biz_tags        VARCHAR(256)          DEFAULT NULL,
  remark          VARCHAR(512)          DEFAULT NULL,
  ext_config      JSON                  DEFAULT NULL,
  create_by       VARCHAR(64)           DEFAULT NULL,
  create_time     timestamp             DEFAULT CURRENT_TIMESTAMP,
  update_by       VARCHAR(64)           DEFAULT NULL,
  update_time     timestamp             DEFAULT CURRENT_TIMESTAMP,
  deleted         CHAR(1)               DEFAULT '0',
  version         INT                   DEFAULT 1,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS sql2api_execute_log CASCADE;
CREATE TABLE sql2api_execute_log
(
  log_id           bigserial NOT NULL,
  api_id           BIGINT,
  orchestration_id BIGINT,
  request_path     VARCHAR(200),
  request_method   VARCHAR(10),
  request_params   TEXT,
  response_result  TEXT,
  execute_status   VARCHAR(20),
  execute_time     BIGINT,
  error_message    TEXT,
  client_ip        VARCHAR(50),
  user_agent       VARCHAR(500),
  create_time      timestamp,
  PRIMARY KEY (log_id)
);

DROP TABLE IF EXISTS sys_sensitive_data CASCADE;
CREATE TABLE sys_sensitive_data
(
  data_id                    bigint NOT NULL,
  real_name                  VARCHAR(500)  DEFAULT NULL,
  real_name_search_index     TEXT          DEFAULT NULL,
  id_card                    VARCHAR(500)  DEFAULT NULL,
  mobile                     VARCHAR(500)  DEFAULT NULL,
  mobile_search_index        TEXT          DEFAULT NULL,
  email_address              VARCHAR(500)  DEFAULT NULL,
  email_address_search_index TEXT          DEFAULT NULL,
  home_address               VARCHAR(1000) DEFAULT NULL,
  home_address_search_index  TEXT          DEFAULT NULL,
  bank_card                  VARCHAR(500)  DEFAULT NULL,
  data_type                  VARCHAR(50)   DEFAULT NULL,
  status                     CHAR(1)       DEFAULT '1',
  tenant_id                  VARCHAR(20)   DEFAULT NULL,
  remark                     VARCHAR(500)  DEFAULT NULL,
  create_by                  VARCHAR(64)   DEFAULT NULL,
  create_time                timestamp     DEFAULT CURRENT_TIMESTAMP,
  update_by                  VARCHAR(64)   DEFAULT NULL,
  update_time                timestamp     DEFAULT CURRENT_TIMESTAMP,
  del_flag                   smallint      DEFAULT 0,
  PRIMARY KEY (data_id)
);

DROP TABLE IF EXISTS sys_dict_type CASCADE;
CREATE TABLE sys_dict_type
(
  dict_id       bigserial    NOT NULL,
  dict_name     varchar(100) NOT NULL,
  dict_type     varchar(100) NOT NULL,
  dict_category varchar(50)  DEFAULT 'system',
  status        char(1)      DEFAULT '1',
  remark        varchar(500) DEFAULT NULL,
  tenant_id     varchar(64)  DEFAULT NULL,
  create_by     varchar(64)  DEFAULT NULL,
  create_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by     varchar(64)  DEFAULT NULL,
  update_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted       char(1)      DEFAULT '0',
  PRIMARY KEY (dict_id)
);

DROP TABLE IF EXISTS sys_dict_data CASCADE;
CREATE TABLE sys_dict_data
(
  data_id     bigserial    NOT NULL,
  dict_type   varchar(100) NOT NULL,
  dict_label  varchar(100) NOT NULL,
  dict_value  varchar(100) NOT NULL,
  dict_desc   varchar(500) DEFAULT NULL,
  dict_sort   integer      DEFAULT 0,
  css_class   varchar(100) DEFAULT NULL,
  list_class  varchar(100) DEFAULT NULL,
  is_default  char(1)      DEFAULT '0',
  status      char(1)      DEFAULT '1',
  extra_data  text         DEFAULT NULL,
  tenant_id   varchar(64)  DEFAULT NULL,
  create_by   varchar(64)  DEFAULT NULL,
  create_time timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by   varchar(64)  DEFAULT NULL,
  update_time timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted     char(1)      DEFAULT '0',
  PRIMARY KEY (data_id)
);

DROP TABLE IF EXISTS sys_region CASCADE;
CREATE TABLE sys_region
(
  region_id       bigserial    NOT NULL,
  region_code     varchar(20)  NOT NULL,
  region_name     varchar(100) NOT NULL,
  parent_code     varchar(20)    DEFAULT NULL,
  region_level    integer        DEFAULT 1,
  region_type     varchar(20)    DEFAULT NULL,
  short_name      varchar(50)    DEFAULT NULL,
  pinyin          varchar(100)   DEFAULT NULL,
  pinyin_abbr     varchar(20)    DEFAULT NULL,
  longitude       numeric(10, 6) DEFAULT NULL,
  latitude        numeric(10, 6) DEFAULT NULL,
  sort_order      integer        DEFAULT 0,
  is_municipality char(1)        DEFAULT '0',
  status          char(1)        DEFAULT '1',
  remark          varchar(500)   DEFAULT NULL,
  create_by       varchar(64)    DEFAULT NULL,
  create_time     timestamp      DEFAULT CURRENT_TIMESTAMP,
  update_by       varchar(64)    DEFAULT NULL,
  update_time     timestamp      DEFAULT CURRENT_TIMESTAMP,
  deleted         char(1)        DEFAULT '0',
  PRIMARY KEY (region_id)
);

DROP TABLE IF EXISTS sys_etl_job CASCADE;
CREATE TABLE sys_etl_job
(
  job_id              bigint       NOT NULL,
  job_name            varchar(100) NOT NULL,
  job_group           varchar(50)  DEFAULT 'DEFAULT',
  job_description     varchar(500) DEFAULT NULL,
  source_datasource   varchar(100) NOT NULL,
  source_table        varchar(100) DEFAULT NULL,
  source_schema       varchar(100) DEFAULT NULL,
  source_query        text         DEFAULT NULL,
  target_datasource   varchar(100) NOT NULL,
  target_table        varchar(100) NOT NULL,
  target_schema       varchar(100) DEFAULT NULL,
  sync_mode           varchar(20)  DEFAULT 'FULL',
  incremental_column  varchar(100) DEFAULT NULL,
  column_mapping      text         DEFAULT NULL,
  transform_rules     text         DEFAULT NULL,
  filter_condition    varchar(500) DEFAULT NULL,
  batch_size          integer      DEFAULT 1000,
  cron_expression     varchar(100) NOT NULL,
  status              char(1)      DEFAULT '0',
  concurrent          char(1)      DEFAULT '0',
  retry_count         integer      DEFAULT 3,
  retry_interval      integer      DEFAULT 60,
  next_execute_time   timestamp    DEFAULT NULL,
  last_execute_time   timestamp    DEFAULT NULL,
  last_execute_status varchar(20)  DEFAULT NULL,
  tenant_id           varchar(64)  DEFAULT NULL,
  remark              varchar(500) DEFAULT NULL,
  create_by           varchar(64)  DEFAULT NULL,
  create_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by           varchar(64)  DEFAULT NULL,
  update_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted             char(1)      DEFAULT '0',
  PRIMARY KEY (job_id)
);

DROP TABLE IF EXISTS sys_etl_execution_log CASCADE;
CREATE TABLE sys_etl_execution_log
(
  log_id           bigint       NOT NULL,
  job_id           bigint       NOT NULL,
  job_name         varchar(100) NOT NULL,
  execution_status varchar(20)  NOT NULL,
  start_time       timestamp    NOT NULL,
  end_time         timestamp   DEFAULT NULL,
  duration         bigint      DEFAULT NULL,
  extracted_rows   bigint      DEFAULT 0,
  transformed_rows bigint      DEFAULT 0,
  loaded_rows      bigint      DEFAULT 0,
  failed_rows      bigint      DEFAULT 0,
  error_message    text        DEFAULT NULL,
  execution_detail text        DEFAULT NULL,
  tenant_id        varchar(64) DEFAULT NULL,
  create_by        varchar(64) DEFAULT NULL,
  create_time      timestamp   DEFAULT CURRENT_TIMESTAMP,
  update_by        varchar(64) DEFAULT NULL,
  update_time      timestamp   DEFAULT CURRENT_TIMESTAMP,
  deleted          char(1)     DEFAULT '0',
  PRIMARY KEY (log_id)
);

DROP TABLE IF EXISTS sys_etl_column_mapping_rule CASCADE;
CREATE TABLE sys_etl_column_mapping_rule
(
  mapping_id          bigint       NOT NULL,
  job_id              bigint       NOT NULL,
  source_schema_name  varchar(100) DEFAULT NULL,
  source_table_name   varchar(100) DEFAULT NULL,
  source_column_name  varchar(100) NOT NULL,
  source_column_type  varchar(100) DEFAULT NULL,
  target_schema_name  varchar(100) DEFAULT NULL,
  target_table_name   varchar(100) DEFAULT NULL,
  target_column_name  varchar(100) NOT NULL,
  target_column_type  varchar(100) DEFAULT NULL,
  mapping_action      varchar(20)  DEFAULT 'INSERT',
  ordinal_position    integer      DEFAULT 0,
  enabled             char(1)      DEFAULT '1',
  tenant_id           varchar(64)  DEFAULT NULL,
  remark              varchar(500) DEFAULT NULL,
  create_by           varchar(64)  DEFAULT NULL,
  create_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by           varchar(64)  DEFAULT NULL,
  update_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted             char(1)      DEFAULT '0',
  PRIMARY KEY (mapping_id)
);

DROP TABLE IF EXISTS sys_etl_sync_checkpoint CASCADE;
CREATE TABLE sys_etl_sync_checkpoint
(
  checkpoint_id         bigint       NOT NULL,
  job_id                bigint       NOT NULL,
  sync_mode             varchar(20)  DEFAULT NULL,
  incremental_column    varchar(100) DEFAULT NULL,
  last_incremental_value varchar(255) DEFAULT NULL,
  last_sync_time        timestamp    DEFAULT NULL,
  source_row_count      bigint       DEFAULT 0,
  target_row_count      bigint       DEFAULT 0,
  applied_bucket_count  integer      DEFAULT 0,
  skipped_bucket_count  integer      DEFAULT 0,
  validation_status     varchar(20)  DEFAULT NULL,
  last_execution_log_id bigint       DEFAULT NULL,
  tenant_id             varchar(64)  DEFAULT NULL,
  remark                varchar(500) DEFAULT NULL,
  create_by             varchar(64)  DEFAULT NULL,
  create_time           timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by             varchar(64)  DEFAULT NULL,
  update_time           timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted               char(1)      DEFAULT '0',
  PRIMARY KEY (checkpoint_id),
  UNIQUE (job_id)
);

DROP TABLE IF EXISTS sys_etl_table_structure_snapshot CASCADE;
CREATE TABLE sys_etl_table_structure_snapshot
(
  snapshot_id         bigint       NOT NULL,
  job_id              bigint       NOT NULL,
  datasource_name     varchar(100) NOT NULL,
  schema_name         varchar(100) DEFAULT NULL,
  table_name          varchar(100) NOT NULL,
  column_count        integer      DEFAULT 0,
  primary_key_columns varchar(500) DEFAULT NULL,
  structure_digest    varchar(128) NOT NULL,
  snapshot_time       timestamp    DEFAULT CURRENT_TIMESTAMP,
  tenant_id           varchar(64)  DEFAULT NULL,
  remark              varchar(500) DEFAULT NULL,
  create_by           varchar(64)  DEFAULT NULL,
  create_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by           varchar(64)  DEFAULT NULL,
  update_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted             char(1)      DEFAULT '0',
  PRIMARY KEY (snapshot_id)
);

DROP TABLE IF EXISTS sys_etl_data_bucket_checksum CASCADE;
CREATE TABLE sys_etl_data_bucket_checksum
(
  bucket_checksum_id   bigint       NOT NULL,
  job_id               bigint       NOT NULL,
  execution_log_id     bigint       NOT NULL,
  bucket_number        integer      NOT NULL,
  bucket_boundary_start varchar(255) DEFAULT NULL,
  bucket_boundary_end  varchar(255) DEFAULT NULL,
  source_row_count     bigint       DEFAULT 0,
  target_row_count     bigint       DEFAULT 0,
  source_checksum      varchar(128) DEFAULT NULL,
  target_checksum      varchar(128) DEFAULT NULL,
  retry_count          integer      DEFAULT 0,
  retry_success        char(1)      DEFAULT '0',
  last_retry_time      timestamp    DEFAULT NULL,
  needs_sync           char(1)      DEFAULT '0',
  skip_reason          varchar(255) DEFAULT NULL,
  compared_at          timestamp    DEFAULT CURRENT_TIMESTAMP,
  tenant_id            varchar(64)  DEFAULT NULL,
  create_by            varchar(64)  DEFAULT NULL,
  create_time          timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by            varchar(64)  DEFAULT NULL,
  update_time          timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted              char(1)      DEFAULT '0',
  PRIMARY KEY (bucket_checksum_id)
);

DROP TABLE IF EXISTS sys_etl_data_validation_log CASCADE;
CREATE TABLE sys_etl_data_validation_log
(
  validation_log_id     bigint       NOT NULL,
  job_id                bigint       NOT NULL,
  execution_log_id      bigint       NOT NULL,
  validation_type       varchar(20)  DEFAULT NULL,
  source_row_count      bigint       DEFAULT 0,
  target_row_count      bigint       DEFAULT 0,
  source_checksum       varchar(128) DEFAULT NULL,
  target_checksum       varchar(128) DEFAULT NULL,
  bucket_count          integer      DEFAULT 0,
  mismatch_bucket_count integer      DEFAULT 0,
  validation_status     varchar(20)  DEFAULT NULL,
  validation_message    text         DEFAULT NULL,
  validated_at          timestamp    DEFAULT CURRENT_TIMESTAMP,
  tenant_id             varchar(64)  DEFAULT NULL,
  create_by             varchar(64)  DEFAULT NULL,
  create_time           timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by             varchar(64)  DEFAULT NULL,
  update_time           timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted               char(1)      DEFAULT '0',
  PRIMARY KEY (validation_log_id)
);

-- =============================================
-- Indexes
-- =============================================

CREATE UNIQUE INDEX uk_sys_tenant_tenant_code ON sys_tenant (tenant_code);
CREATE INDEX idx_sys_tenant_tenant_status ON sys_tenant (status);
CREATE INDEX idx_sys_tenant_create_time ON sys_tenant (create_time);
CREATE INDEX idx_sys_dept_parent_id ON sys_dept (parent_id);
CREATE INDEX idx_sys_dept_tenant_id ON sys_dept (tenant_id);
CREATE INDEX idx_sys_dept_status ON sys_dept (status);
CREATE UNIQUE INDEX uk_sys_user_username ON sys_user (username);
CREATE INDEX idx_sys_user_dept_id ON sys_user (dept_id);
CREATE INDEX idx_sys_user_tenant_id ON sys_user (tenant_id);
CREATE INDEX idx_sys_user_status ON sys_user (status);
CREATE INDEX idx_sys_user_pwd_change_time ON sys_user (pwd_change_time);
CREATE INDEX idx_sys_user_is_first_login ON sys_user (is_first_login);
CREATE UNIQUE INDEX uk_sys_role_role_code ON sys_role (role_code);
CREATE INDEX idx_sys_role_parent_role_id ON sys_role (parent_role_id);
CREATE INDEX idx_sys_role_role_level ON sys_role (role_level);
CREATE INDEX idx_sys_role_tenant_id ON sys_role (tenant_id);
CREATE INDEX idx_sys_role_status ON sys_role (status);
CREATE INDEX idx_sys_permission_parent_id ON sys_permission (parent_id);
CREATE INDEX idx_sys_permission_status ON sys_permission (status);
CREATE INDEX idx_sys_role_hierarchy_descendant ON sys_role_hierarchy (descendant_role_id);
CREATE INDEX idx_sys_role_hierarchy_tenant_id ON sys_role_hierarchy (tenant_id);
CREATE INDEX idx_sys_role_hierarchy_distance ON sys_role_hierarchy (distance);
CREATE INDEX idx_sys_role_inherited_permission_permission_id ON sys_role_inherited_permission (permission_id);
CREATE INDEX idx_sys_role_inherited_permission_inherited_from ON sys_role_inherited_permission (inherited_from);
CREATE INDEX idx_sys_role_inherited_permission_tenant_id ON sys_role_inherited_permission (tenant_id);
CREATE UNIQUE INDEX uk_sys_config_config_key ON sys_config (config_key);
CREATE INDEX idx_sys_config_tenant_id ON sys_config (tenant_id);
CREATE INDEX idx_sys_oper_log_business_type ON sys_oper_log (business_type);
CREATE INDEX idx_sys_oper_log_status ON sys_oper_log (status);
CREATE INDEX idx_sys_oper_log_oper_time ON sys_oper_log (oper_time);
CREATE INDEX idx_sys_oper_log_tenant_id ON sys_oper_log (tenant_id);
CREATE INDEX idx_sys_storage_config_storage_type ON sys_storage_config (storage_type);
CREATE INDEX idx_sys_storage_config_tenant_id ON sys_storage_config (tenant_id);
CREATE INDEX idx_sys_storage_config_is_default ON sys_storage_config (is_default);
CREATE UNIQUE INDEX uk_sys_upload_policy_policy_code ON sys_upload_policy (policy_code);
CREATE INDEX idx_sys_upload_policy_storage_config_id ON sys_upload_policy (storage_config_id);
CREATE INDEX idx_sys_upload_policy_tenant_id ON sys_upload_policy (tenant_id);
CREATE INDEX idx_sys_file_file_md5 ON sys_file (file_md5);
CREATE INDEX idx_sys_file_file_sha256 ON sys_file (file_sha256);
CREATE INDEX idx_sys_file_tenant_id ON sys_file (tenant_id);
CREATE INDEX idx_sys_file_storage_config_id ON sys_file (storage_config_id);
CREATE INDEX idx_sys_file_upload_policy_id ON sys_file (upload_policy_id);
CREATE INDEX idx_sys_file_business ON sys_file (business_type, business_id);
CREATE INDEX idx_sys_file_create_time ON sys_file (create_time);
CREATE UNIQUE INDEX uk_sys_upload_token_token ON sys_upload_token (token);
CREATE INDEX idx_sys_upload_token_policy_id ON sys_upload_token (policy_id);
CREATE INDEX idx_sys_upload_token_user_id ON sys_upload_token (user_id);
CREATE INDEX idx_sys_upload_token_expire_time ON sys_upload_token (expire_time);
CREATE INDEX idx_sys_upload_token_tenant_id ON sys_upload_token (tenant_id);
CREATE INDEX idx_sys_job_status ON sys_job (status);
CREATE INDEX idx_sys_job_tenant_id ON sys_job (tenant_id);
CREATE UNIQUE INDEX uk_sql2api_config_api_path ON sql2api_config (api_path);
CREATE INDEX idx_sql2api_config_datasource ON sql2api_config (datasource_name);
CREATE INDEX idx_sql2api_config_enabled ON sql2api_config (enabled);
CREATE INDEX idx_sql2api_config_create_time ON sql2api_config (create_time);
CREATE UNIQUE INDEX uk_sql2api_orchestration_orchestration_path ON sql2api_orchestration (orchestration_path);
CREATE INDEX idx_sql2api_orchestration_enabled ON sql2api_orchestration (enabled);
CREATE INDEX idx_sql2api_orchestration_create_time ON sql2api_orchestration (create_time);
CREATE UNIQUE INDEX uk_infra_datasource_datasource_code ON infra_datasource (datasource_code);
CREATE INDEX idx_infra_datasource_status ON infra_datasource (status);
CREATE INDEX idx_infra_datasource_create_time ON infra_datasource (create_time);
CREATE INDEX idx_sql2api_execute_log_api_id ON sql2api_execute_log (api_id);
CREATE INDEX idx_sql2api_execute_log_orchestration_id ON sql2api_execute_log (orchestration_id);
CREATE INDEX idx_sql2api_execute_log_execute_status ON sql2api_execute_log (execute_status);
CREATE INDEX idx_sql2api_execute_log_create_time ON sql2api_execute_log (create_time);
CREATE INDEX idx_sys_sensitive_data_tenant_id ON sys_sensitive_data (tenant_id);
CREATE INDEX idx_sys_sensitive_data_status ON sys_sensitive_data (status);
CREATE INDEX idx_sys_sensitive_data_data_type ON sys_sensitive_data (data_type);
CREATE INDEX idx_sys_sensitive_data_create_time ON sys_sensitive_data (create_time);
CREATE UNIQUE INDEX uk_sys_dict_type_dict_type ON sys_dict_type (dict_type, tenant_id, deleted);
CREATE INDEX idx_sys_dict_type_dict_category ON sys_dict_type (dict_category);
CREATE INDEX idx_sys_dict_type_status ON sys_dict_type (status);
CREATE INDEX idx_sys_dict_type_tenant_id ON sys_dict_type (tenant_id);
CREATE INDEX idx_sys_dict_data_dict_type ON sys_dict_data (dict_type);
CREATE INDEX idx_sys_dict_data_dict_sort ON sys_dict_data (dict_sort);
CREATE INDEX idx_sys_dict_data_status ON sys_dict_data (status);
CREATE INDEX idx_sys_dict_data_tenant_id ON sys_dict_data (tenant_id);
CREATE UNIQUE INDEX uk_sys_region_region_code ON sys_region (region_code, deleted);
CREATE INDEX idx_sys_region_parent_code ON sys_region (parent_code);
CREATE INDEX idx_sys_region_region_level ON sys_region (region_level);
CREATE INDEX idx_sys_region_region_type ON sys_region (region_type);
CREATE INDEX idx_sys_region_status ON sys_region (status);
CREATE INDEX idx_sys_etl_job_job_name ON sys_etl_job (job_name);
CREATE INDEX idx_sys_etl_job_job_group ON sys_etl_job (job_group);
CREATE INDEX idx_sys_etl_job_status ON sys_etl_job (status);
CREATE INDEX idx_sys_etl_job_tenant_id ON sys_etl_job (tenant_id);
CREATE INDEX idx_sys_etl_job_next_execute_time ON sys_etl_job (next_execute_time);
CREATE INDEX idx_sys_etl_execution_log_job_id ON sys_etl_execution_log (job_id);
CREATE INDEX idx_sys_etl_execution_log_execution_status ON sys_etl_execution_log (execution_status);
CREATE INDEX idx_sys_etl_execution_log_start_time ON sys_etl_execution_log (start_time);
CREATE INDEX idx_sys_etl_execution_log_tenant_id ON sys_etl_execution_log (tenant_id);
CREATE INDEX idx_sys_etl_column_mapping_rule_job
  ON sys_etl_column_mapping_rule (job_id, enabled, ordinal_position);
CREATE INDEX idx_sys_etl_structure_snapshot_lookup
  ON sys_etl_table_structure_snapshot (job_id, datasource_name, schema_name, table_name, snapshot_time);
CREATE INDEX idx_sys_etl_bucket_checksum_execution
  ON sys_etl_data_bucket_checksum (execution_log_id, bucket_number);
CREATE INDEX idx_sys_etl_validation_log_execution
  ON sys_etl_data_validation_log (execution_log_id, validation_status);
