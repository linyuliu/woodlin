package com.mumu.woodlin.datasource.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import com.mumu.woodlin.datasource.mapper.InfraDatasourceMapper;
import com.mumu.woodlin.datasource.model.request.DatasourceRequest;
import com.mumu.woodlin.datasource.service.InfraDatasourceService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * 基础设施数据源管理控制器
 * <p>
 * 提供数据源的完整生命周期管理，包括增删改查、连接测试等功能。
 * 该控制器是平台级基础设施能力，为sql2api、ETL、报表等业务模块提供统一的数据源管理入口。
 * </p>
 * 
 * @author mumu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/infra/datasource")
@RequiredArgsConstructor
@Tag(name = "数据源管理", description = "基础设施数据源的增删改查、测试连接接口")
public class InfraDatasourceController {

    private final InfraDatasourceMapper datasourceMapper;
    private final InfraDatasourceService datasourceService;

    /**
     * 查询所有数据源列表
     * 
     * @return 数据源配置列表
     */
    @GetMapping
    @Operation(summary = "查询数据源列表", description = "获取系统中所有已配置的数据源信息")
    public R<List<InfraDatasourceConfig>> list() {
        return R.ok(datasourceMapper.selectList(new QueryWrapper<>()));
    }

    /**
     * 根据数据源编码查询详情
     * 
     * @param code 数据源唯一编码
     * @return 数据源配置详情
     */
    @GetMapping("/{code}")
    @Operation(summary = "查询数据源详情", description = "根据数据源编码获取详细配置信息")
    public R<InfraDatasourceConfig> detail(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code) {
        InfraDatasourceConfig config = datasourceMapper.selectOne(new QueryWrapper<InfraDatasourceConfig>()
                .eq("datasource_code", code));
        return config == null ? R.fail("数据源不存在") : R.ok(config);
    }

    /**
     * 创建新数据源
     * 
     * @param request 数据源配置信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "创建数据源", description = "添加新的数据源配置，系统会自动验证数据源编码的唯一性")
    public R<Void> create(
            @Parameter(description = "数据源配置信息", required = true)
            @Valid @RequestBody DatasourceRequest request) {
        saveOrUpdate(request, false);
        return R.ok();
    }

    /**
     * 更新数据源配置
     * 
     * @param request 数据源配置信息
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "更新数据源", description = "修改已有数据源的配置信息")
    public R<Void> update(
            @Parameter(description = "数据源配置信息", required = true)
            @Valid @RequestBody DatasourceRequest request) {
        saveOrUpdate(request, true);
        return R.ok();
    }

    /**
     * 删除数据源
     * 
     * @param code 数据源唯一编码
     * @return 操作结果
     */
    @DeleteMapping("/{code}")
    @Operation(summary = "删除数据源", description = "根据数据源编码删除指定的数据源配置")
    public R<Void> delete(
            @Parameter(description = "数据源唯一编码", required = true)
            @PathVariable("code") String code) {
        datasourceMapper.delete(new QueryWrapper<InfraDatasourceConfig>().eq("datasource_code", code));
        return R.ok();
    }

    /**
     * 测试数据源连接
     * 
     * @param request 数据源配置信息
     * @return 测试结果
     */
    @PostMapping("/test")
    @Operation(summary = "测试数据源连接", description = "验证数据源配置是否正确，能否成功建立数据库连接")
    public R<Void> test(
            @Parameter(description = "数据源配置信息", required = true)
            @Valid @RequestBody DatasourceRequest request) {
        datasourceService.validateConnectivity(
                datasourceService.buildTemporaryDataSource(request),
                request.getTestSql(),
                request.getDatasourceType()
        );
        return R.ok();
    }

    /**
     * 保存或更新数据源配置
     * 
     * @param request 数据源配置请求
     * @param isUpdate 是否为更新操作
     */
    private void saveOrUpdate(DatasourceRequest request, boolean isUpdate) {
        QueryWrapper<InfraDatasourceConfig> wrapper = new QueryWrapper<InfraDatasourceConfig>()
                .eq("datasource_code", request.getDatasourceCode());
        InfraDatasourceConfig exist = datasourceMapper.selectOne(wrapper);
        
        if (!isUpdate && exist != null) {
            throw new BusinessException("数据源编码已存在");
        }
        if (isUpdate && exist == null) {
            throw new BusinessException("数据源不存在，无法更新");
        }
        
        InfraDatasourceConfig record = exist == null ? new InfraDatasourceConfig() : exist;
        record.setDatasourceCode(request.getDatasourceCode());
        record.setDatasourceName(request.getDatasourceName());
        record.setDatasourceType(request.getDatasourceType());
        record.setDriverClass(request.getDriverClass());
        record.setJdbcUrl(request.getJdbcUrl());
        record.setUsername(request.getUsername());
        record.setPassword(request.getPassword());
        record.setTestSql(StrUtil.emptyToDefault(request.getTestSql(), 
                datasourceService.defaultTestQuery(request.getDatasourceType())));
        record.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        record.setOwner(request.getOwner());
        record.setBizTags(request.getBizTags());
        record.setRemark(request.getRemark());
        record.setExtConfig(request.getExtConfig());

        if (!isUpdate) {
            datasourceMapper.insert(record);
        } else {
            datasourceMapper.updateById(record);
        }
    }
}
