package com.mumu.woodlin.etl.service;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobCreateRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineJobPageRequest;
import com.mumu.woodlin.etl.model.request.EtlOfflineValidationRequest;
import com.mumu.woodlin.etl.model.response.EtlOfflineCreateJobResponse;
import com.mumu.woodlin.etl.model.response.EtlOfflineDatasourceOption;
import com.mumu.woodlin.etl.model.response.EtlOfflineValidationResult;
import com.mumu.woodlin.etl.model.response.EtlOfflineWizardConfigResponse;

import java.util.List;

/**
 * ETL离线任务向导服务。
 *
 * @author mumu
 * @since 1.0.0
 */
public interface IEtlOfflineService {

    /**
     * 获取向导初始化配置。
     *
     * @return 向导配置
     */
    EtlOfflineWizardConfigResponse getWizardConfig();

    /**
     * 查询可选数据源。
     *
     * @param datasourceType 数据源类型
     * @param keyword        关键字
     * @param enabledOnly    是否仅返回启用数据源
     * @return 数据源选项
     */
    List<EtlOfflineDatasourceOption> listDatasourceOptions(String datasourceType, String keyword, boolean enabledOnly);

    /**
     * 查询离线任务分页。
     *
     * @param request 分页请求
     * @return 分页结果
     */
    PageResult<EtlJob> pageJobs(EtlOfflineJobPageRequest request);

    /**
     * 校验离线任务配置。
     *
     * @param request 校验请求
     * @return 校验结果
     */
    EtlOfflineValidationResult validate(EtlOfflineValidationRequest request);

    /**
     * 创建离线任务。
     *
     * @param request 创建请求
     * @return 创建结果
     */
    EtlOfflineCreateJobResponse createOfflineJob(EtlOfflineJobCreateRequest request);

    /**
     * 更新离线任务。
     *
     * @param jobId   任务ID
     * @param request 更新请求
     * @return 更新结果
     */
    EtlOfflineCreateJobResponse updateOfflineJob(Long jobId, EtlOfflineJobCreateRequest request);

    /**
     * 删除离线任务。
     *
     * @param jobId 任务ID
     */
    void deleteOfflineJob(Long jobId);

    /**
     * 查询表列表（支持关键字过滤）。
     *
     * @param datasourceCode 数据源编码
     * @param schemaName     schema
     * @param keyword        关键字
     * @param limit          返回上限
     * @return 表列表
     */
    List<TableMetadata> listTables(String datasourceCode, String schemaName, String keyword, Integer limit);

    /**
     * 查询字段列表（支持关键字过滤）。
     *
     * @param datasourceCode 数据源编码
     * @param schemaName     schema
     * @param tableName      表名
     * @param keyword        关键字
     * @param limit          返回上限
     * @return 字段列表
     */
    List<ColumnMetadata> listColumns(String datasourceCode, String schemaName, String tableName, String keyword,
                                     Integer limit);
}
