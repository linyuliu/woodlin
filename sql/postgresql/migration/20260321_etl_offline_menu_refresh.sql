UPDATE sys_permission
SET permission_name = '同步任务',
    icon = 'git-compare-outline',
    remark = 'ETL同步任务目录',
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE permission_id = 7000;

UPDATE sys_permission
SET permission_name = '离线同步',
    permission_code = 'etl:offline',
    path = 'offline',
    component = 'etl/offline',
    icon = 'swap-horizontal-outline',
    remark = '离线同步页面',
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE permission_id = 7001;

UPDATE sys_permission
SET path = 'offline/logs',
    component = 'etl/offline/logs',
    remark = '离线同步执行历史页面',
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE permission_id = 7002;
