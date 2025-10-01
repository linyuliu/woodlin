package com.mumu.woodlin.sql2api.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.mumu.woodlin.sql2api.dsl.SqlDslParser;
import com.mumu.woodlin.sql2api.dsl.SqlParam;
import com.mumu.woodlin.sql2api.entity.SqlApiConfig;
import com.mumu.woodlin.sql2api.service.DynamicSqlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * 动态SQL执行服务实现
 * 
 * @author mumu
 * @description 基于SQL配置动态执行SQL，支持多数据源、缓存和流控
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicSqlServiceImpl implements DynamicSqlService {
    
    private final DataSource dataSource;
    
    @Override
    @Cacheable(value = "sqlQuerySingle", key = "#config.apiId + ':' + #params.hashCode()", 
               condition = "#config.cacheEnabled != null && #config.cacheEnabled")
    public Map<String, Object> executeSingleQuery(SqlApiConfig config, Map<String, Object> params) {
        log.info("执行单条查询，API: {}", config.getApiName());
        
        JdbcTemplate jdbcTemplate = createJdbcTemplate(config.getDatasourceName());
        SqlDslParser.ParsedSql parsedSql = parseSql(config, params);
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(parsedSql.getSql());
        
        return results.isEmpty() ? new HashMap<>() : results.get(0);
    }
    
    @Override
    @Cacheable(value = "sqlQueryList", key = "#config.apiId + ':' + #params.hashCode()",
               condition = "#config.cacheEnabled != null && #config.cacheEnabled")
    public List<Map<String, Object>> executeListQuery(SqlApiConfig config, Map<String, Object> params) {
        log.info("执行列表查询，API: {}", config.getApiName());
        
        JdbcTemplate jdbcTemplate = createJdbcTemplate(config.getDatasourceName());
        SqlDslParser.ParsedSql parsedSql = parseSql(config, params);
        
        return jdbcTemplate.queryForList(parsedSql.getSql());
    }
    
    @Override
    @Cacheable(value = "sqlQueryPage", key = "#config.apiId + ':' + #params.hashCode() + ':' + #pageNum + ':' + #pageSize",
               condition = "#config.cacheEnabled != null && #config.cacheEnabled")
    public Map<String, Object> executePageQuery(SqlApiConfig config, Map<String, Object> params, int pageNum, int pageSize) {
        log.info("执行分页查询，API: {}, 页码: {}, 每页: {}", config.getApiName(), pageNum, pageSize);
        
        JdbcTemplate jdbcTemplate = createJdbcTemplate(config.getDatasourceName());
        SqlDslParser.ParsedSql parsedSql = parseSql(config, params);
        
        // 查询总数
        String countSql = "SELECT COUNT(*) FROM (" + parsedSql.getSql() + ") AS temp_count";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        
        // 查询数据
        int offset = (pageNum - 1) * pageSize;
        String pageSql = parsedSql.getSql() + " LIMIT " + offset + ", " + pageSize;
        List<Map<String, Object>> records = jdbcTemplate.queryForList(pageSql);
        
        // 构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total != null ? total : 0L);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("pages", total != null ? (total + pageSize - 1) / pageSize : 0);
        result.put("records", records);
        
        return result;
    }
    
    @Override
    public int executeUpdate(SqlApiConfig config, Map<String, Object> params) {
        log.info("执行更新操作，API: {}", config.getApiName());
        
        JdbcTemplate jdbcTemplate = createJdbcTemplate(config.getDatasourceName());
        SqlDslParser.ParsedSql parsedSql = parseSql(config, params);
        
        return jdbcTemplate.update(parsedSql.getSql());
    }
    
    @Override
    public int[] executeBatchUpdate(SqlApiConfig config, List<Map<String, Object>> batchParams) {
        log.info("执行批量更新操作，API: {}, 批量数: {}", config.getApiName(), batchParams.size());
        
        JdbcTemplate jdbcTemplate = createJdbcTemplate(config.getDatasourceName());
        
        List<String> sqlList = new ArrayList<>();
        for (Map<String, Object> params : batchParams) {
            SqlDslParser.ParsedSql parsedSql = parseSql(config, params);
            sqlList.add(parsedSql.getSql());
        }
        
        return jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]));
    }
    
    /**
     * 创建JdbcTemplate
     */
    private JdbcTemplate createJdbcTemplate(String datasourceName) {
        DataSource targetDataSource = getTargetDataSource(datasourceName);
        return new JdbcTemplate(targetDataSource);
    }
    
    /**
     * 获取目标数据源
     */
    private DataSource getTargetDataSource(String datasourceName) {
        if (dataSource instanceof DynamicRoutingDataSource dynamicDataSource) {
            DataSource targetDs = dynamicDataSource.getDataSource(datasourceName);
            if (targetDs == null) {
                throw new RuntimeException("数据源不存在: " + datasourceName);
            }
            return targetDs;
        }
        return dataSource;
    }
    
    /**
     * 解析SQL
     */
    private SqlDslParser.ParsedSql parseSql(SqlApiConfig config, Map<String, Object> params) {
        // 解析参数配置
        List<SqlParam> sqlParams = parseParamsConfig(config.getParamsConfig());
        
        // 创建DSL解析器
        SqlDslParser parser = SqlDslParser.builder()
                .sqlTemplate(config.getSqlContent())
                .params(sqlParams)
                .build();
        
        // 解析并绑定参数
        return parser.parse(params);
    }
    
    /**
     * 解析参数配置JSON
     */
    private List<SqlParam> parseParamsConfig(String paramsConfigJson) {
        if (paramsConfigJson == null || paramsConfigJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<SqlParam> sqlParams = new ArrayList<>();
            List<?> paramsArray = JSONUtil.toList(paramsConfigJson, Map.class);
            
            for (Object item : paramsArray) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> paramMap = (Map<String, Object>) item;
                    
                    SqlParam sqlParam = SqlParam.builder()
                            .name((String) paramMap.get("name"))
                            .type((String) paramMap.getOrDefault("type", "String"))
                            .required((Boolean) paramMap.getOrDefault("required", false))
                            .defaultValue(paramMap.get("defaultValue"))
                            .description((String) paramMap.get("description"))
                            .validation((String) paramMap.get("validation"))
                            .example(paramMap.get("example"))
                            .build();
                    
                    sqlParams.add(sqlParam);
                }
            }
            
            return sqlParams;
            
        } catch (Exception e) {
            log.error("解析参数配置失败: {}", paramsConfigJson, e);
            return new ArrayList<>();
        }
    }
}
