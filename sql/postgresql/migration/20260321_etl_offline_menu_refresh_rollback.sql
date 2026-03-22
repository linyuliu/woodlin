UPDATE sys_permission
SET permission_name = 'ETL管理',
    icon = 'shuffle-outline',
    remark = 'ETL管理目录',
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE permission_id = 7000;

UPDATE sys_permission
SET permission_name = '离线任务',
    permission_code = 'etl:offline:list',
    path = 'offline',
    component = 'etl/OfflineJob',
    icon = 'layers-outline',
    remark = '离线任务页面',
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE permission_id = 7001;

UPDATE sys_permission
SET path = 'log',
    component = 'etl/OfflineLog',
    remark = '执行历史页面',
    update_by = 'admin',
    update_time = CURRENT_TIMESTAMP
WHERE permission_id = 7002;
