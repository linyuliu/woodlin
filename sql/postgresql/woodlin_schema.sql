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
                              show_in_tabs    char(1)      DEFAULT '1',
                              active_menu     varchar(255) DEFAULT NULL,
                              redirect        varchar(255) DEFAULT NULL,
                              remarkvarchar(500) DEFAULT '',
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

DROP TABLE IF EXISTS sys_open_app CASCADE;
CREATE TABLE sys_open_app
(
  app_id       bigint       NOT NULL,
  app_code     varchar(100) NOT NULL,
  app_name     varchar(100) NOT NULL,
  status       char(1)       DEFAULT '1',
  tenant_id    varchar(64)   DEFAULT NULL,
  owner_name   varchar(64)   DEFAULT NULL,
  ip_whitelist varchar(1000) DEFAULT NULL,
  remark       varchar(500)  DEFAULT NULL,
  create_by    varchar(64)   DEFAULT NULL,
  create_time  timestamp     DEFAULT CURRENT_TIMESTAMP,
  update_by    varchar(64)   DEFAULT NULL,
  update_time  timestamp     DEFAULT CURRENT_TIMESTAMP,
  deleted      char(1)       DEFAULT '0',
  PRIMARY KEY (app_id),
  UNIQUE (app_code)
);

DROP TABLE IF EXISTS sys_open_app_credential CASCADE;
CREATE TABLE sys_open_app_credential
(
  credential_id                bigint       NOT NULL,
  app_id                       bigint       NOT NULL,
  credential_name              varchar(100) NOT NULL,
  access_key                   varchar(100) NOT NULL,
  secret_key_encrypted         varchar(2000) DEFAULT NULL,
  secret_key_fingerprint       varchar(64)   DEFAULT NULL,
  signature_public_key         text,
  encryption_public_key        text,
  server_public_key            text,
  server_private_key_encrypted text,
  signature_algorithm          varchar(50)   DEFAULT NULL,
  encryption_algorithm         varchar(50)   DEFAULT NULL,
  security_mode                varchar(50)   DEFAULT NULL,
  active_from                  timestamp     DEFAULT NULL,
  active_to                    timestamp     DEFAULT NULL,
  last_rotated_time            timestamp     DEFAULT NULL,
  status                       char(1)       DEFAULT '1',
  remark                       varchar(500)  DEFAULT NULL,
  create_by                    varchar(64)   DEFAULT NULL,
  create_time                  timestamp     DEFAULT CURRENT_TIMESTAMP,
  update_by                    varchar(64)   DEFAULT NULL,
  update_time                  timestamp     DEFAULT CURRENT_TIMESTAMP,
  deleted                      char(1)       DEFAULT '0',
  PRIMARY KEY (credential_id),
  UNIQUE (access_key)
);

DROP TABLE IF EXISTS sys_open_api_policy CASCADE;
CREATE TABLE sys_open_api_policy
(
  policy_id                bigint       NOT NULL,
  policy_name              varchar(100) NOT NULL,
  path_pattern             varchar(255) NOT NULL,
  http_method              varchar(20)  NOT NULL,
  security_mode            varchar(50)  DEFAULT NULL,
  signature_algorithm      varchar(50)  DEFAULT NULL,
  encryption_algorithm     varchar(50)  DEFAULT NULL,
  timestamp_window_seconds integer      DEFAULT 300,
  nonce_enabled            char(1)      DEFAULT '1',
  nonce_ttl_seconds        integer      DEFAULT 300,
  tenant_required          char(1)      DEFAULT '0',
  enabled                  char(1)      DEFAULT '1',
  remark                   varchar(500) DEFAULT NULL,
  create_by                varchar(64)  DEFAULT NULL,
  create_time              timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_by                varchar(64)  DEFAULT NULL,
  update_time              timestamp    DEFAULT CURRENT_TIMESTAMP,
  deleted                  char(1)      DEFAULT '0',
  PRIMARY KEY (policy_id)
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
  transform_params    text         DEFAULT NULL,
  constant_value      varchar(500) DEFAULT NULL,
  default_value       varchar(500) DEFAULT NULL,
  empty_value_policy  varchar(50)  DEFAULT NULL,
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

-- =============================================
-- Assessment Module（测评模块）
-- =============================================

-- sys_assessment_form
DROP TABLE IF EXISTS sys_assessment_form;
CREATE TABLE sys_assessment_form
(
    form_id            bigint       NOT NULL,
    form_code          varchar(100) NOT NULL,
    form_name          varchar(200) NOT NULL,
    assessment_type    varchar(30)  DEFAULT 'scale',
    category_code      varchar(100) DEFAULT NULL,
    description        text         DEFAULT NULL,
    cover_url          varchar(500) DEFAULT NULL,
    tags               varchar(500) DEFAULT NULL,
    current_version_id bigint       DEFAULT NULL,
    status             smallint     DEFAULT 1,
    sort_order         int          DEFAULT 0,
    tenant_id          varchar(64)  DEFAULT NULL,
    remark             varchar(500) DEFAULT NULL,
    create_by          varchar(64)  DEFAULT NULL,
    create_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by          varchar(64)  DEFAULT NULL,
    update_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted            char(1)      DEFAULT '0',
    PRIMARY KEY (form_id)
);
COMMENT ON TABLE sys_assessment_form IS '测评主体表（量表/试卷/问卷）';
COMMENT ON COLUMN sys_assessment_form.form_id IS '测评ID';
COMMENT ON COLUMN sys_assessment_form.form_code IS '唯一编码（业务标识）';
COMMENT ON COLUMN sys_assessment_form.assessment_type IS '测评类型: scale/exam/survey';
COMMENT ON COLUMN sys_assessment_form.status IS '启用状态: 1=启用 0=禁用';
COMMENT ON COLUMN sys_assessment_form.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_form_version
DROP TABLE IF EXISTS sys_assessment_form_version;
CREATE TABLE sys_assessment_form_version
(
    version_id     bigint       NOT NULL,
    form_id        bigint       NOT NULL,
    version_no     varchar(30)  NOT NULL,
    version_tag    varchar(100) DEFAULT NULL,
    schema_id      bigint       DEFAULT NULL,
    schema_hash    varchar(128) DEFAULT NULL,
    dsl_hash       varchar(128) DEFAULT NULL,
    status         varchar(30)  DEFAULT 'draft',
    published_at   timestamp    DEFAULT NULL,
    published_by   varchar(64)  DEFAULT NULL,
    change_summary text         DEFAULT NULL,
    tenant_id      varchar(64)  DEFAULT NULL,
    create_by      varchar(64)  DEFAULT NULL,
    create_time    timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by      varchar(64)  DEFAULT NULL,
    update_time    timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted        char(1)      DEFAULT '0',
    PRIMARY KEY (version_id)
);
COMMENT ON TABLE sys_assessment_form_version IS '测评版本表';
COMMENT ON COLUMN sys_assessment_form_version.status IS '版本状态: draft/compiled/published/deprecated/archived';
COMMENT ON COLUMN sys_assessment_form_version.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_schema
DROP TABLE IF EXISTS sys_assessment_schema;
CREATE TABLE sys_assessment_schema
(
    schema_id        bigint       NOT NULL,
    form_id          bigint       NOT NULL,
    version_id       bigint       DEFAULT NULL,
    status           varchar(30)  DEFAULT 'draft',
    canonical_schema text         DEFAULT NULL,
    dsl_source       text         DEFAULT NULL,
    compiled_schema  text         DEFAULT NULL,
    compile_error    text         DEFAULT NULL,
    schema_hash      varchar(128) DEFAULT NULL,
    tenant_id        varchar(64)  DEFAULT NULL,
    create_by        varchar(64)  DEFAULT NULL,
    create_time      timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by        varchar(64)  DEFAULT NULL,
    update_time      timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted          char(1)      DEFAULT '0',
    PRIMARY KEY (schema_id)
);
COMMENT ON TABLE sys_assessment_schema IS '测评 Schema 表（结构定义）';
COMMENT ON COLUMN sys_assessment_schema.canonical_schema IS '规范 JSON Schema（后台配置主存）';
COMMENT ON COLUMN sys_assessment_schema.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_section
DROP TABLE IF EXISTS sys_assessment_section;
CREATE TABLE sys_assessment_section
(
    section_id    bigint       NOT NULL,
    version_id    bigint       NOT NULL,
    form_id       bigint       NOT NULL,
    section_code  varchar(100) NOT NULL,
    section_title varchar(200) DEFAULT NULL,
    section_desc  text         DEFAULT NULL,
    display_mode  varchar(30)  DEFAULT 'paged',
    random_strategy varchar(50) DEFAULT 'none',
    sort_order    int          DEFAULT 0,
    is_required   smallint     DEFAULT 1,
    anchor_code   varchar(100) DEFAULT NULL,
    tenant_id     varchar(64)  DEFAULT NULL,
    create_by     varchar(64)  DEFAULT NULL,
    create_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by     varchar(64)  DEFAULT NULL,
    update_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted       char(1)      DEFAULT '0',
    PRIMARY KEY (section_id)
);
COMMENT ON TABLE sys_assessment_section IS '测评章节/页面表';
COMMENT ON COLUMN sys_assessment_section.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_item
DROP TABLE IF EXISTS sys_assessment_item;
CREATE TABLE sys_assessment_item
(
    item_id             bigint        NOT NULL,
    version_id          bigint        NOT NULL,
    section_id          bigint        DEFAULT NULL,
    form_id             bigint        NOT NULL,
    item_code           varchar(100)  NOT NULL,
    item_type           varchar(50)   DEFAULT 'single_choice',
    stem                text          DEFAULT NULL,
    stem_media_url      varchar(500)  DEFAULT NULL,
    help_text           varchar(500)  DEFAULT NULL,
    sort_order          int           DEFAULT 0,
    is_required         smallint      DEFAULT 1,
    is_scored           smallint      DEFAULT 1,
    is_anchor           smallint      DEFAULT 0,
    is_reverse          smallint      DEFAULT 0,
    is_demographic      smallint      DEFAULT 0,
    max_score           numeric(10,4) DEFAULT NULL,
    min_score           numeric(10,4) DEFAULT NULL,
    time_limit_seconds  int           DEFAULT 0,
    demographic_field   varchar(100)  DEFAULT NULL,
    tenant_id           varchar(64)   DEFAULT NULL,
    create_by           varchar(64)   DEFAULT NULL,
    create_time         timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by           varchar(64)   DEFAULT NULL,
    update_time         timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted             char(1)       DEFAULT '0',
    PRIMARY KEY (item_id)
);
COMMENT ON TABLE sys_assessment_item IS '题目/条目表';
COMMENT ON COLUMN sys_assessment_item.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_option
DROP TABLE IF EXISTS sys_assessment_option;
CREATE TABLE sys_assessment_option
(
    option_id           bigint        NOT NULL,
    item_id             bigint        NOT NULL,
    option_code         varchar(30)   NOT NULL,
    display_text        text          DEFAULT NULL,
    media_url           varchar(500)  DEFAULT NULL,
    raw_value           varchar(255)  DEFAULT NULL,
    score_value         numeric(10,4) DEFAULT NULL,
    score_reverse_value numeric(10,4) DEFAULT NULL,
    is_exclusive        smallint      DEFAULT 0,
    is_correct          smallint      DEFAULT 0,
    sort_order          int           DEFAULT 0,
    tenant_id           varchar(64)   DEFAULT NULL,
    create_by           varchar(64)   DEFAULT NULL,
    create_time         timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by           varchar(64)   DEFAULT NULL,
    update_time         timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted             char(1)       DEFAULT '0',
    PRIMARY KEY (option_id)
);
COMMENT ON TABLE sys_assessment_option IS '题目选项表';
COMMENT ON COLUMN sys_assessment_option.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_dimension
DROP TABLE IF EXISTS sys_assessment_dimension;
CREATE TABLE sys_assessment_dimension
(
    dimension_id        bigint       NOT NULL,
    form_id             bigint       NOT NULL,
    version_id          bigint       NOT NULL,
    parent_dimension_id bigint       DEFAULT NULL,
    dimension_code      varchar(100) NOT NULL,
    dimension_name      varchar(200) NOT NULL,
    dimension_desc      text         DEFAULT NULL,
    score_mode          varchar(50)  DEFAULT 'sum',
    score_dsl           text         DEFAULT NULL,
    norm_set_id         bigint       DEFAULT NULL,
    sort_order          int          DEFAULT 0,
    tenant_id           varchar(64)  DEFAULT NULL,
    create_by           varchar(64)  DEFAULT NULL,
    create_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by           varchar(64)  DEFAULT NULL,
    update_time         timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted             char(1)      DEFAULT '0',
    PRIMARY KEY (dimension_id)
);
COMMENT ON TABLE sys_assessment_dimension IS '维度/因子/分量表';
COMMENT ON COLUMN sys_assessment_dimension.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_dimension_item
DROP TABLE IF EXISTS sys_assessment_dimension_item;
CREATE TABLE sys_assessment_dimension_item
(
    id           bigint        NOT NULL,
    dimension_id bigint        NOT NULL,
    item_id      bigint        NOT NULL,
    version_id   bigint        NOT NULL,
    weight       numeric(10,4) DEFAULT 1.0000,
    score_mode   varchar(50)   DEFAULT NULL,
    reverse_mode varchar(20)   DEFAULT 'none',
    tenant_id    varchar(64)   DEFAULT NULL,
    create_by    varchar(64)   DEFAULT NULL,
    create_time  timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by    varchar(64)   DEFAULT NULL,
    update_time  timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted      char(1)       DEFAULT '0',
    PRIMARY KEY (id)
);
COMMENT ON TABLE sys_assessment_dimension_item IS '题目-维度映射表';
COMMENT ON COLUMN sys_assessment_dimension_item.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_rule
DROP TABLE IF EXISTS sys_assessment_rule;
CREATE TABLE sys_assessment_rule
(
    rule_id       bigint       NOT NULL,
    form_id       bigint       NOT NULL,
    version_id    bigint       NOT NULL,
    rule_code     varchar(100) NOT NULL,
    rule_name     varchar(200) DEFAULT NULL,
    rule_type     varchar(50)  NOT NULL,
    target_type   varchar(30)  DEFAULT NULL,
    target_code   varchar(200) DEFAULT NULL,
    dsl_source    text         DEFAULT NULL,
    compiled_rule text         DEFAULT NULL,
    priority      int          DEFAULT 100,
    is_active     smallint     DEFAULT 1,
    compile_error text         DEFAULT NULL,
    tenant_id     varchar(64)  DEFAULT NULL,
    create_by     varchar(64)  DEFAULT NULL,
    create_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by     varchar(64)  DEFAULT NULL,
    update_time   timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted       char(1)      DEFAULT '0',
    PRIMARY KEY (rule_id)
);
COMMENT ON TABLE sys_assessment_rule IS '规则定义表';
COMMENT ON COLUMN sys_assessment_rule.rule_type IS '规则类型: display/branch/validation/score/norm_match/report/eligibility/terminate';
COMMENT ON COLUMN sys_assessment_rule.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_anchor_item
DROP TABLE IF EXISTS sys_assessment_anchor_item;
CREATE TABLE sys_assessment_anchor_item
(
    anchor_id   bigint       NOT NULL,
    form_id     bigint       NOT NULL,
    anchor_code varchar(100) NOT NULL,
    item_id     bigint       NOT NULL,
    version_id  bigint       NOT NULL,
    anchor_type varchar(30)  DEFAULT 'common',
    note        varchar(500) DEFAULT NULL,
    tenant_id   varchar(64)  DEFAULT NULL,
    create_by   varchar(64)  DEFAULT NULL,
    create_time timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by   varchar(64)  DEFAULT NULL,
    update_time timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted     char(1)      DEFAULT '0',
    PRIMARY KEY (anchor_id)
);
COMMENT ON TABLE sys_assessment_anchor_item IS '锚题定义表';
COMMENT ON COLUMN sys_assessment_anchor_item.anchor_type IS '锚题类型: common/equating/calibration';
COMMENT ON COLUMN sys_assessment_anchor_item.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_publish
DROP TABLE IF EXISTS sys_assessment_publish;
CREATE TABLE sys_assessment_publish
(
    publish_id              bigint       NOT NULL,
    form_id                 bigint       NOT NULL,
    version_id              bigint       NOT NULL,
    publish_code            varchar(100) DEFAULT NULL,
    publish_name            varchar(200) NOT NULL,
    status                  varchar(30)  DEFAULT 'draft',
    start_time              timestamp    DEFAULT NULL,
    end_time                timestamp    DEFAULT NULL,
    time_limit_minutes      int          DEFAULT 0,
    max_attempts            int          DEFAULT 1,
    allow_anonymous         smallint     DEFAULT 0,
    allow_resume            smallint     DEFAULT 1,
    random_strategy         varchar(50)  DEFAULT 'none',
    access_policy           text         DEFAULT NULL,
    device_restriction      text         DEFAULT NULL,
    show_result_immediately smallint     DEFAULT 0,
    result_visibility       varchar(30)  DEFAULT 'self',
    tenant_id               varchar(64)  DEFAULT NULL,
    remark                  varchar(500) DEFAULT NULL,
    create_by               varchar(64)  DEFAULT NULL,
    create_time             timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by               varchar(64)  DEFAULT NULL,
    update_time             timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted                 char(1)      DEFAULT '0',
    PRIMARY KEY (publish_id)
);
COMMENT ON TABLE sys_assessment_publish IS '发布实例表';
COMMENT ON COLUMN sys_assessment_publish.status IS '发布状态: draft/under_review/published/paused/closed/archived';
COMMENT ON COLUMN sys_assessment_publish.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_session
DROP TABLE IF EXISTS sys_assessment_session;
CREATE TABLE sys_assessment_session
(
    session_id      bigint       NOT NULL,
    publish_id      bigint       NOT NULL,
    form_id         bigint       NOT NULL,
    version_id      bigint       NOT NULL,
    user_id         bigint       DEFAULT NULL,
    anonymous_token varchar(200) DEFAULT NULL,
    status          varchar(30)  DEFAULT 'not_started',
    display_seed    bigint       DEFAULT NULL,
    started_at      timestamp    DEFAULT NULL,
    completed_at    timestamp    DEFAULT NULL,
    elapsed_seconds int          DEFAULT 0,
    client_ip       varchar(128) DEFAULT NULL,
    user_agent      varchar(500) DEFAULT NULL,
    device_type     varchar(20)  DEFAULT NULL,
    attempt_number  int          DEFAULT 1,
    tenant_id       varchar(64)  DEFAULT NULL,
    create_by       varchar(64)  DEFAULT NULL,
    create_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by       varchar(64)  DEFAULT NULL,
    update_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted         char(1)      DEFAULT '0',
    PRIMARY KEY (session_id)
);
COMMENT ON TABLE sys_assessment_session IS '作答会话表';
COMMENT ON COLUMN sys_assessment_session.status IS '会话状态: not_started/in_progress/paused/completed/expired/abandoned/invalidated';
COMMENT ON COLUMN sys_assessment_session.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_session_snapshot
DROP TABLE IF EXISTS sys_assessment_session_snapshot;
CREATE TABLE sys_assessment_session_snapshot
(
    snapshot_id            bigint       NOT NULL,
    session_id             bigint       NOT NULL,
    current_section_code   varchar(100) DEFAULT NULL,
    current_item_code      varchar(100) DEFAULT NULL,
    item_order_snapshot    text         DEFAULT NULL,
    option_order_snapshot  text         DEFAULT NULL,
    answered_cache         text         DEFAULT NULL,
    elapsed_seconds        int          DEFAULT 0,
    snapshot_at            timestamp    DEFAULT CURRENT_TIMESTAMP,
    tenant_id              varchar(64)  DEFAULT NULL,
    create_by              varchar(64)  DEFAULT NULL,
    create_time            timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by              varchar(64)  DEFAULT NULL,
    update_time            timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted                char(1)      DEFAULT '0',
    PRIMARY KEY (snapshot_id)
);
COMMENT ON TABLE sys_assessment_session_snapshot IS '作答断点快照表';
COMMENT ON COLUMN sys_assessment_session_snapshot.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_response
DROP TABLE IF EXISTS sys_assessment_response;
CREATE TABLE sys_assessment_response
(
    response_id                    bigint       NOT NULL,
    session_id                     bigint       NOT NULL,
    form_id                        bigint       NOT NULL,
    item_id                        bigint       NOT NULL,
    item_code                      varchar(100) NOT NULL,
    display_order                  int          DEFAULT NULL,
    raw_answer                     text         DEFAULT NULL,
    selected_option_codes          text         DEFAULT NULL,
    selected_option_display_orders text         DEFAULT NULL,
    text_answer                    text         DEFAULT NULL,
    answered_at                    timestamp    DEFAULT NULL,
    time_spent_seconds             int          DEFAULT NULL,
    is_skipped                     smallint     DEFAULT 0,
    tenant_id                      varchar(64)  DEFAULT NULL,
    create_by                      varchar(64)  DEFAULT NULL,
    create_time                    timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by                      varchar(64)  DEFAULT NULL,
    update_time                    timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted                        char(1)      DEFAULT '0',
    PRIMARY KEY (response_id)
);
COMMENT ON TABLE sys_assessment_response IS '逐题作答记录表';
COMMENT ON COLUMN sys_assessment_response.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_result
DROP TABLE IF EXISTS sys_assessment_result;
CREATE TABLE sys_assessment_result
(
    result_id            bigint        NOT NULL,
    session_id           bigint        NOT NULL,
    form_id              bigint        NOT NULL,
    publish_id           bigint        NOT NULL,
    user_id              bigint        DEFAULT NULL,
    raw_total_score      numeric(10,4) DEFAULT NULL,
    weighted_total_score numeric(10,4) DEFAULT NULL,
    standard_score       numeric(10,4) DEFAULT NULL,
    norm_score_type      varchar(30)   DEFAULT NULL,
    percentile           numeric(6,2)  DEFAULT NULL,
    grade_label          varchar(100)  DEFAULT NULL,
    norm_set_id          bigint        DEFAULT NULL,
    norm_segment_id      bigint        DEFAULT NULL,
    risk_level           varchar(20)   DEFAULT 'none',
    answered_count       int           DEFAULT 0,
    total_item_count     int           DEFAULT 0,
    report_json          text          DEFAULT NULL,
    score_trace_json text DEFAULT NULL,
    tenant_id            varchar(64)   DEFAULT NULL,
    create_by            varchar(64)   DEFAULT NULL,
    create_time          timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by            varchar(64)   DEFAULT NULL,
    update_time          timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted              char(1)       DEFAULT '0',
    PRIMARY KEY (result_id)
);
COMMENT ON TABLE sys_assessment_result IS '测评总结果表';
COMMENT ON COLUMN sys_assessment_result.risk_level IS '综合风险等级: none/low/medium/high/confirmed';
COMMENT ON COLUMN sys_assessment_result.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_result_dimension
DROP TABLE IF EXISTS sys_assessment_result_dimension;
CREATE TABLE sys_assessment_result_dimension
(
    id              bigint        NOT NULL,
    result_id       bigint        NOT NULL,
    session_id      bigint        NOT NULL,
    dimension_id    bigint        NOT NULL,
    dimension_code  varchar(100)  DEFAULT NULL,
    raw_score       numeric(10,4) DEFAULT NULL,
    mean_score      numeric(10,4) DEFAULT NULL,
    standard_score  numeric(10,4) DEFAULT NULL,
    percentile      numeric(6,2)  DEFAULT NULL,
    grade_label     varchar(100)  DEFAULT NULL,
    norm_segment_id bigint        DEFAULT NULL,
    item_count      int           DEFAULT 0,
    tenant_id       varchar(64)   DEFAULT NULL,
    create_by       varchar(64)   DEFAULT NULL,
    create_time     timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by       varchar(64)   DEFAULT NULL,
    update_time     timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted         char(1)       DEFAULT '0',
    PRIMARY KEY (id)
);
COMMENT ON TABLE sys_assessment_result_dimension IS '维度得分结果表';
COMMENT ON COLUMN sys_assessment_result_dimension.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_demographic_profile
DROP TABLE IF EXISTS sys_assessment_demographic_profile;
CREATE TABLE sys_assessment_demographic_profile
(
    profile_id      bigint        NOT NULL,
    session_id      bigint        NOT NULL,
    user_id         bigint        DEFAULT NULL,
    gender          varchar(10)   DEFAULT NULL,
    birth_year      int           DEFAULT NULL,
    age             int           DEFAULT NULL,
    age_group       varchar(20)   DEFAULT NULL,
    education_level varchar(30)   DEFAULT NULL,
    occupation      varchar(50)   DEFAULT NULL,
    region_code     varchar(30)   DEFAULT NULL,
    province_code   varchar(10)   DEFAULT NULL,
    marital_status  varchar(20)   DEFAULT NULL,
    ethnicity       varchar(30)   DEFAULT NULL,
    extra_fields    text          DEFAULT NULL,
    norm_weight     numeric(10,4) DEFAULT 1.0000,
    tenant_id       varchar(64)   DEFAULT NULL,
    create_by       varchar(64)   DEFAULT NULL,
    create_time     timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by       varchar(64)   DEFAULT NULL,
    update_time     timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted         char(1)       DEFAULT '0',
    PRIMARY KEY (profile_id)
);
COMMENT ON TABLE sys_assessment_demographic_profile IS '人口学档案快照表';
COMMENT ON COLUMN sys_assessment_demographic_profile.region_code IS '地区编码（关联 sys_region.region_code）';
COMMENT ON COLUMN sys_assessment_demographic_profile.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_event_log
DROP TABLE IF EXISTS sys_assessment_event_log;
CREATE TABLE sys_assessment_event_log
(
    event_id        bigint       NOT NULL,
    session_id      bigint       NOT NULL,
    event_type      varchar(60)  NOT NULL,
    item_code       varchar(100) DEFAULT NULL,
    event_payload   text         DEFAULT NULL,
    occurred_at     timestamp(3) DEFAULT NULL,
    elapsed_seconds int          DEFAULT NULL,
    tenant_id       varchar(64)  DEFAULT NULL,
    create_by       varchar(64)  DEFAULT NULL,
    create_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by       varchar(64)  DEFAULT NULL,
    update_time     timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted         char(1)      DEFAULT '0',
    PRIMARY KEY (event_id)
);
COMMENT ON TABLE sys_assessment_event_log IS '作答事件日志表（行为埋点）';
COMMENT ON COLUMN sys_assessment_event_log.event_type IS '事件类型: focus/blur/paste/copy_blocked/fullscreen_exit/…';
COMMENT ON COLUMN sys_assessment_event_log.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_cheat_flag
DROP TABLE IF EXISTS sys_assessment_cheat_flag;
CREATE TABLE sys_assessment_cheat_flag
(
    flag_id           bigint       NOT NULL,
    session_id        bigint       NOT NULL,
    rule_code         varchar(100) NOT NULL,
    rule_desc         varchar(500) DEFAULT NULL,
    risk_level        varchar(20)  DEFAULT 'low',
    evidence          text         DEFAULT NULL,
    detected_at       timestamp    DEFAULT CURRENT_TIMESTAMP,
    reviewed_by       varchar(64)  DEFAULT NULL,
    reviewed_at       timestamp    DEFAULT NULL,
    review_conclusion varchar(30)  DEFAULT 'pending',
    review_note       varchar(500) DEFAULT NULL,
    tenant_id         varchar(64)  DEFAULT NULL,
    create_by         varchar(64)  DEFAULT NULL,
    create_time       timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by         varchar(64)  DEFAULT NULL,
    update_time       timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted           char(1)      DEFAULT '0',
    PRIMARY KEY (flag_id)
);
COMMENT ON TABLE sys_assessment_cheat_flag IS '作弊/风险标记表';
COMMENT ON COLUMN sys_assessment_cheat_flag.review_conclusion IS '复核结论: confirmed_cheat/false_positive/pending';
COMMENT ON COLUMN sys_assessment_cheat_flag.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_norm_set
DROP TABLE IF EXISTS sys_assessment_norm_set;
CREATE TABLE sys_assessment_norm_set
(
    norm_set_id       bigint       NOT NULL,
    form_id           bigint       NOT NULL,
    norm_set_name     varchar(200) NOT NULL,
    norm_set_code     varchar(100) NOT NULL,
    sample_size       int          DEFAULT NULL,
    collection_start  date         DEFAULT NULL,
    collection_end    date         DEFAULT NULL,
    source_desc       text         DEFAULT NULL,
    applicability_desc text        DEFAULT NULL,
    is_default        smallint     DEFAULT 0,
    status            smallint     DEFAULT 1,
    tenant_id         varchar(64)  DEFAULT NULL,
    create_by         varchar(64)  DEFAULT NULL,
    create_time       timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by         varchar(64)  DEFAULT NULL,
    update_time       timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted           char(1)      DEFAULT '0',
    PRIMARY KEY (norm_set_id)
);
COMMENT ON TABLE sys_assessment_norm_set IS '常模集表';
COMMENT ON COLUMN sys_assessment_norm_set.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_norm_segment
DROP TABLE IF EXISTS sys_assessment_norm_segment;
CREATE TABLE sys_assessment_norm_segment
(
    segment_id         bigint       NOT NULL,
    norm_set_id        bigint       NOT NULL,
    segment_code       varchar(100) NOT NULL,
    segment_name       varchar(200) NOT NULL,
    gender_filter      varchar(10)  DEFAULT NULL,
    age_min            int          DEFAULT NULL,
    age_max            int          DEFAULT NULL,
    education_filter   varchar(30)  DEFAULT NULL,
    region_code_filter varchar(30)  DEFAULT NULL,
    sample_size        int          DEFAULT NULL,
    extra_filter       text         DEFAULT NULL,
    sort_priority      int          DEFAULT 100,
    tenant_id          varchar(64)  DEFAULT NULL,
    create_by          varchar(64)  DEFAULT NULL,
    create_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
    update_by          varchar(64)  DEFAULT NULL,
    update_time        timestamp    DEFAULT CURRENT_TIMESTAMP,
    deleted            char(1)      DEFAULT '0',
    PRIMARY KEY (segment_id)
);
COMMENT ON TABLE sys_assessment_norm_segment IS '常模分层表';
COMMENT ON COLUMN sys_assessment_norm_segment.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_norm_conversion
DROP TABLE IF EXISTS sys_assessment_norm_conversion;
CREATE TABLE sys_assessment_norm_conversion
(
    conversion_id   bigint        NOT NULL,
    segment_id      bigint        NOT NULL,
    dimension_id    bigint        DEFAULT NULL,
    norm_score_type varchar(30)   NOT NULL,
    raw_score_min   numeric(10,4) DEFAULT NULL,
    raw_score_max   numeric(10,4) DEFAULT NULL,
    raw_score_exact numeric(10,4) DEFAULT NULL,
    standard_score  numeric(10,4) DEFAULT NULL,
    percentile      numeric(6,2)  DEFAULT NULL,
    grade_label     varchar(100)  DEFAULT NULL,
    sort_order      int           DEFAULT 0,
    tenant_id       varchar(64)   DEFAULT NULL,
    create_by       varchar(64)   DEFAULT NULL,
    create_time     timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by       varchar(64)   DEFAULT NULL,
    update_time     timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted         char(1)       DEFAULT '0',
    PRIMARY KEY (conversion_id)
);
COMMENT ON TABLE sys_assessment_norm_conversion IS '常模转换映射表';
COMMENT ON COLUMN sys_assessment_norm_conversion.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_item_stat
DROP TABLE IF EXISTS sys_assessment_item_stat;
CREATE TABLE sys_assessment_item_stat
(
    stat_id             bigint        NOT NULL,
    item_id             bigint        NOT NULL,
    version_id          bigint        NOT NULL,
    publish_id          bigint        DEFAULT NULL,
    sample_size         int           DEFAULT 0,
    item_difficulty     numeric(8,6)  DEFAULT NULL,
    item_discrimination numeric(8,6)  DEFAULT NULL,
    missing_rate        numeric(8,6)  DEFAULT NULL,
    ceiling_rate        numeric(8,6)  DEFAULT NULL,
    floor_rate          numeric(8,6)  DEFAULT NULL,
    alpha_if_deleted    numeric(8,6)  DEFAULT NULL,
    omega_if_deleted    numeric(8,6)  DEFAULT NULL,
    dif_flag            varchar(10)   DEFAULT 'none',
    dif_reference_group varchar(100)  DEFAULT NULL,
    stat_json           text          DEFAULT NULL,
    computed_at         timestamp     DEFAULT NULL,
    tenant_id           varchar(64)   DEFAULT NULL,
    create_by           varchar(64)   DEFAULT NULL,
    create_time         timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by           varchar(64)   DEFAULT NULL,
    update_time         timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted             char(1)       DEFAULT '0',
    PRIMARY KEY (stat_id)
);
COMMENT ON TABLE sys_assessment_item_stat IS '题目项目分析统计表';
COMMENT ON COLUMN sys_assessment_item_stat.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_equating_record
DROP TABLE IF EXISTS sys_assessment_equating_record;
CREATE TABLE sys_assessment_equating_record
(
    equating_id          bigint        NOT NULL,
    form_id              bigint        NOT NULL,
    source_version_id    bigint        NOT NULL,
    target_version_id    bigint        NOT NULL,
    equating_method      varchar(50)   NOT NULL,
    anchor_item_codes    text          DEFAULT NULL,
    anchor_count         int           DEFAULT 0,
    slope                numeric(12,6) DEFAULT NULL,
    intercept            numeric(12,6) DEFAULT NULL,
    rmsea                numeric(10,6) DEFAULT NULL,
    equating_result_json text          DEFAULT NULL,
    computed_at          timestamp     DEFAULT NULL,
    computed_by          varchar(64)   DEFAULT NULL,
    tenant_id            varchar(64)   DEFAULT NULL,
    create_by            varchar(64)   DEFAULT NULL,
    create_time          timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by            varchar(64)   DEFAULT NULL,
    update_time          timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted              char(1)       DEFAULT '0',
    PRIMARY KEY (equating_id)
);
COMMENT ON TABLE sys_assessment_equating_record IS '等值/链接记录表';
COMMENT ON COLUMN sys_assessment_equating_record.equating_method IS '等值方法: mean_sigma/irt_linking/equipercentile/…';
COMMENT ON COLUMN sys_assessment_equating_record.deleted IS '删除标识（0-正常，1-删除）';

-- sys_assessment_bi_snapshot
DROP TABLE IF EXISTS sys_assessment_bi_snapshot;
CREATE TABLE sys_assessment_bi_snapshot
(
    snapshot_id            bigint        NOT NULL,
    form_id                bigint        NOT NULL,
    publish_id             bigint        DEFAULT NULL,
    snapshot_type          varchar(30)   NOT NULL,
    snapshot_date          timestamp     NOT NULL,
    total_sessions         bigint        DEFAULT 0,
    completed_sessions     bigint        DEFAULT 0,
    avg_score              numeric(10,4) DEFAULT NULL,
    metrics_json           text          DEFAULT NULL,
    dimension_stats_json   text          DEFAULT NULL,
    demographic_stats_json text          DEFAULT NULL,
    tenant_id              varchar(64)   DEFAULT NULL,
    create_by              varchar(64)   DEFAULT NULL,
    create_time            timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by              varchar(64)   DEFAULT NULL,
    update_time            timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted                char(1)       DEFAULT '0',
    PRIMARY KEY (snapshot_id)
);
COMMENT ON TABLE sys_assessment_bi_snapshot IS 'BI 预聚合快照表';
COMMENT ON COLUMN sys_assessment_bi_snapshot.snapshot_type IS '快照类型: daily/weekly/publish_close/manual';
COMMENT ON COLUMN sys_assessment_bi_snapshot.deleted IS '删除标识（0-正常，1-删除）';

-- =============================================
-- Assessment indexes
-- =============================================
CREATE UNIQUE INDEX uk_assessment_form_code ON sys_assessment_form (form_code, tenant_id, deleted);
CREATE INDEX idx_assessment_form_tenant ON sys_assessment_form (tenant_id);
CREATE INDEX idx_assessment_form_status ON sys_assessment_form (status);
CREATE INDEX idx_assessment_form_type ON sys_assessment_form (assessment_type);
CREATE INDEX idx_assessment_form_create_time ON sys_assessment_form (create_time);

CREATE INDEX idx_assessment_version_form ON sys_assessment_form_version (form_id);
CREATE INDEX idx_assessment_version_status ON sys_assessment_form_version (status);
CREATE INDEX idx_assessment_version_tenant ON sys_assessment_form_version (tenant_id);
CREATE INDEX idx_assessment_version_published_at ON sys_assessment_form_version (published_at);

CREATE INDEX idx_assessment_schema_form ON sys_assessment_schema (form_id);
CREATE INDEX idx_assessment_schema_version ON sys_assessment_schema (version_id);
CREATE INDEX idx_assessment_schema_status ON sys_assessment_schema (status);
CREATE INDEX idx_assessment_schema_tenant ON sys_assessment_schema (tenant_id);

CREATE INDEX idx_assessment_section_version ON sys_assessment_section (version_id);
CREATE INDEX idx_assessment_section_form ON sys_assessment_section (form_id);
CREATE INDEX idx_assessment_section_tenant ON sys_assessment_section (tenant_id);
CREATE INDEX idx_assessment_section_sort ON sys_assessment_section (version_id, sort_order);

CREATE INDEX idx_assessment_item_version ON sys_assessment_item (version_id);
CREATE INDEX idx_assessment_item_section ON sys_assessment_item (section_id);
CREATE INDEX idx_assessment_item_form ON sys_assessment_item (form_id);
CREATE INDEX idx_assessment_item_code ON sys_assessment_item (version_id, item_code);
CREATE INDEX idx_assessment_item_tenant ON sys_assessment_item (tenant_id);
CREATE INDEX idx_assessment_item_sort ON sys_assessment_item (section_id, sort_order);

CREATE INDEX idx_assessment_option_item ON sys_assessment_option (item_id);
CREATE INDEX idx_assessment_option_sort ON sys_assessment_option (item_id, sort_order);
CREATE INDEX idx_assessment_option_tenant ON sys_assessment_option (tenant_id);

CREATE INDEX idx_assessment_dimension_form ON sys_assessment_dimension (form_id);
CREATE INDEX idx_assessment_dimension_version ON sys_assessment_dimension (version_id);
CREATE INDEX idx_assessment_dimension_parent ON sys_assessment_dimension (parent_dimension_id);
CREATE INDEX idx_assessment_dimension_tenant ON sys_assessment_dimension (tenant_id);

CREATE UNIQUE INDEX uk_assessment_dim_item ON sys_assessment_dimension_item (dimension_id, item_id, deleted);
CREATE INDEX idx_assessment_dim_item_version ON sys_assessment_dimension_item (version_id);
CREATE INDEX idx_assessment_dim_item_item ON sys_assessment_dimension_item (item_id);
CREATE INDEX idx_assessment_dim_item_tenant ON sys_assessment_dimension_item (tenant_id);

CREATE INDEX idx_assessment_rule_form ON sys_assessment_rule (form_id);
CREATE INDEX idx_assessment_rule_version ON sys_assessment_rule (version_id);
CREATE INDEX idx_assessment_rule_type ON sys_assessment_rule (rule_type);
CREATE INDEX idx_assessment_rule_active ON sys_assessment_rule (is_active);
CREATE INDEX idx_assessment_rule_tenant ON sys_assessment_rule (tenant_id);
CREATE INDEX idx_assessment_rule_priority ON sys_assessment_rule (version_id, rule_type, priority);

CREATE INDEX idx_assessment_anchor_form ON sys_assessment_anchor_item (form_id);
CREATE INDEX idx_assessment_anchor_version ON sys_assessment_anchor_item (version_id);
CREATE INDEX idx_assessment_anchor_code ON sys_assessment_anchor_item (anchor_code);
CREATE INDEX idx_assessment_anchor_item ON sys_assessment_anchor_item (item_id);
CREATE INDEX idx_assessment_anchor_tenant ON sys_assessment_anchor_item (tenant_id);

CREATE UNIQUE INDEX uk_assessment_publish_code ON sys_assessment_publish (publish_code, deleted);
CREATE INDEX idx_assessment_publish_form ON sys_assessment_publish (form_id);
CREATE INDEX idx_assessment_publish_version ON sys_assessment_publish (version_id);
CREATE INDEX idx_assessment_publish_status ON sys_assessment_publish (status);
CREATE INDEX idx_assessment_publish_tenant ON sys_assessment_publish (tenant_id);
CREATE INDEX idx_assessment_publish_time ON sys_assessment_publish (start_time, end_time);

CREATE INDEX idx_assessment_session_publish ON sys_assessment_session (publish_id);
CREATE INDEX idx_assessment_session_form ON sys_assessment_session (form_id);
CREATE INDEX idx_assessment_session_user ON sys_assessment_session (user_id);
CREATE INDEX idx_assessment_session_status ON sys_assessment_session (status);
CREATE INDEX idx_assessment_session_tenant ON sys_assessment_session (tenant_id);
CREATE INDEX idx_assessment_session_started_at ON sys_assessment_session (started_at);
CREATE INDEX idx_assessment_session_completed_at ON sys_assessment_session (completed_at);

CREATE INDEX idx_assessment_snapshot_session ON sys_assessment_session_snapshot (session_id);
CREATE INDEX idx_assessment_snapshot_at ON sys_assessment_session_snapshot (snapshot_at);
CREATE INDEX idx_assessment_snapshot_tenant ON sys_assessment_session_snapshot (tenant_id);

CREATE INDEX idx_assessment_response_session ON sys_assessment_response (session_id);
CREATE INDEX idx_assessment_response_form ON sys_assessment_response (form_id);
CREATE INDEX idx_assessment_response_item ON sys_assessment_response (item_id);
CREATE INDEX idx_assessment_response_tenant ON sys_assessment_response (tenant_id);
CREATE INDEX idx_assessment_response_answered_at ON sys_assessment_response (answered_at);

CREATE UNIQUE INDEX uk_assessment_result_session ON sys_assessment_result (session_id, deleted);
CREATE INDEX idx_assessment_result_form ON sys_assessment_result (form_id);
CREATE INDEX idx_assessment_result_publish ON sys_assessment_result (publish_id);
CREATE INDEX idx_assessment_result_user ON sys_assessment_result (user_id);
CREATE INDEX idx_assessment_result_risk ON sys_assessment_result (risk_level);
CREATE INDEX idx_assessment_result_tenant ON sys_assessment_result (tenant_id);
CREATE INDEX idx_assessment_result_create_time ON sys_assessment_result (create_time);

CREATE INDEX idx_assessment_result_dim_result ON sys_assessment_result_dimension (result_id);
CREATE INDEX idx_assessment_result_dim_session ON sys_assessment_result_dimension (session_id);
CREATE INDEX idx_assessment_result_dim_dimension ON sys_assessment_result_dimension (dimension_id);
CREATE INDEX idx_assessment_result_dim_tenant ON sys_assessment_result_dimension (tenant_id);

CREATE UNIQUE INDEX uk_assessment_demographic_session ON sys_assessment_demographic_profile (session_id, deleted);
CREATE INDEX idx_assessment_demographic_user ON sys_assessment_demographic_profile (user_id);
CREATE INDEX idx_assessment_demographic_region ON sys_assessment_demographic_profile (region_code);
CREATE INDEX idx_assessment_demographic_tenant ON sys_assessment_demographic_profile (tenant_id);

CREATE INDEX idx_assessment_event_session ON sys_assessment_event_log (session_id);
CREATE INDEX idx_assessment_event_type ON sys_assessment_event_log (event_type);
CREATE INDEX idx_assessment_event_occurred_at ON sys_assessment_event_log (occurred_at);
CREATE INDEX idx_assessment_event_tenant ON sys_assessment_event_log (tenant_id);

CREATE INDEX idx_assessment_cheat_session ON sys_assessment_cheat_flag (session_id);
CREATE INDEX idx_assessment_cheat_risk ON sys_assessment_cheat_flag (risk_level);
CREATE INDEX idx_assessment_cheat_conclusion ON sys_assessment_cheat_flag (review_conclusion);
CREATE INDEX idx_assessment_cheat_detected_at ON sys_assessment_cheat_flag (detected_at);
CREATE INDEX idx_assessment_cheat_tenant ON sys_assessment_cheat_flag (tenant_id);

CREATE INDEX idx_assessment_norm_set_form ON sys_assessment_norm_set (form_id);
CREATE INDEX idx_assessment_norm_set_status ON sys_assessment_norm_set (status);
CREATE INDEX idx_assessment_norm_set_tenant ON sys_assessment_norm_set (tenant_id);

CREATE INDEX idx_assessment_norm_segment_set ON sys_assessment_norm_segment (norm_set_id);
CREATE INDEX idx_assessment_norm_segment_priority ON sys_assessment_norm_segment (norm_set_id, sort_priority);
CREATE INDEX idx_assessment_norm_segment_tenant ON sys_assessment_norm_segment (tenant_id);

CREATE INDEX idx_assessment_norm_conv_segment ON sys_assessment_norm_conversion (segment_id);
CREATE INDEX idx_assessment_norm_conv_dimension ON sys_assessment_norm_conversion (dimension_id);
CREATE INDEX idx_assessment_norm_conv_type ON sys_assessment_norm_conversion (segment_id, norm_score_type);
CREATE INDEX idx_assessment_norm_conv_tenant ON sys_assessment_norm_conversion (tenant_id);

CREATE INDEX idx_assessment_item_stat_item ON sys_assessment_item_stat (item_id);
CREATE INDEX idx_assessment_item_stat_version ON sys_assessment_item_stat (version_id);
CREATE INDEX idx_assessment_item_stat_publish ON sys_assessment_item_stat (publish_id);
CREATE INDEX idx_assessment_item_stat_tenant ON sys_assessment_item_stat (tenant_id);

CREATE INDEX idx_assessment_equating_form ON sys_assessment_equating_record (form_id);
CREATE INDEX idx_assessment_equating_source_ver ON sys_assessment_equating_record (source_version_id);
CREATE INDEX idx_assessment_equating_target_ver ON sys_assessment_equating_record (target_version_id);
CREATE INDEX idx_assessment_equating_tenant ON sys_assessment_equating_record (tenant_id);

CREATE INDEX idx_assessment_bi_snapshot_form ON sys_assessment_bi_snapshot (form_id);
CREATE INDEX idx_assessment_bi_snapshot_publish ON sys_assessment_bi_snapshot (publish_id);
CREATE INDEX idx_assessment_bi_snapshot_type ON sys_assessment_bi_snapshot (snapshot_type);
CREATE INDEX idx_assessment_bi_snapshot_date ON sys_assessment_bi_snapshot (snapshot_date);
CREATE INDEX idx_assessment_bi_snapshot_tenant ON sys_assessment_bi_snapshot (tenant_id);
