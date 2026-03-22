package com.mumu.woodlin.etl.constant;

/**
 * ETL 权限编码常量。
 *
 * <p>统一维护 ETL 离线向导和兼容接口的权限码，避免控制器中散落硬编码字符串。</p>
 *
 * @author mumu
 * @since 1.0.0
 */
public final class EtlPermissionConstants {

    /**
     * ETL 离线任务列表权限。
     */
    public static final String OFFLINE_LIST = "etl:offline:list";

    /**
     * ETL 离线任务创建权限。
     */
    public static final String OFFLINE_CREATE = "etl:offline:create";

    /**
     * ETL 离线任务编辑权限。
     */
    public static final String OFFLINE_EDIT = "etl:offline:edit";

    /**
     * ETL 离线任务删除权限。
     */
    public static final String OFFLINE_DELETE = "etl:offline:delete";

    /**
     * ETL 离线任务立即执行权限。
     */
    public static final String OFFLINE_EXECUTE = "etl:offline:execute";

    /**
     * ETL 离线任务启用权限。
     */
    public static final String OFFLINE_ENABLE = "etl:offline:enable";

    /**
     * ETL 离线任务禁用权限。
     */
    public static final String OFFLINE_DISABLE = "etl:offline:disable";

    /**
     * ETL 离线任务预校验权限。
     */
    public static final String OFFLINE_VALIDATE = "etl:offline:validate";

    /**
     * ETL 离线任务详情权限。
     */
    public static final String OFFLINE_DETAIL = "etl:offline:detail";

    /**
     * ETL 离线任务表列表权限。
     */
    public static final String OFFLINE_TABLE_LIST = "etl:offline:table:list";

    /**
     * ETL 离线任务字段列表权限。
     */
    public static final String OFFLINE_COLUMN_LIST = "etl:offline:column:list";

    /**
     * ETL 执行日志列表权限。
     */
    public static final String OFFLINE_LOG_LIST = "etl:offline:log:list";

    /**
     * ETL 执行日志详情权限。
     */
    public static final String OFFLINE_LOG_DETAIL = "etl:offline:log:detail";

    private EtlPermissionConstants() {
    }
}
