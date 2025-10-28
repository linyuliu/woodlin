package com.mumu.woodlin.sql2api.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL DSL解析器
 * 
 * @author mumu
 * @description 解析简化的SQL DSL语法，提取参数并生成可执行的SQL
 * @since 2025-01-01
 */
@Slf4j
@Data
@Builder
public class SqlDslParser {
    
    /**
     * 原始SQL模板
     */
    private String sqlTemplate;
    
    /**
     * 参数定义列表
     */
    private List<SqlParam> params;
    
    /**
     * 参数匹配模式: #{paramName}, ${paramName}, :paramName
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile("#\\{([^}]+)}|\\$\\{([^}]+)}|:([a-zA-Z0-9_]+)");
    
    /**
     * 条件标签模式: <if test="condition">...</if>
     */
    private static final Pattern IF_PATTERN = Pattern.compile("<if\\s+test=\"([^\"]+)\">(.*?)</if>", Pattern.DOTALL);
    
    /**
     * 解析SQL并绑定参数
     * 
     * @param requestParams 请求参数
     * @return 解析结果
     */
    public ParsedSql parse(Map<String, Object> requestParams) {
        log.debug("开始解析SQL模板，参数: {}", requestParams);
        
        // 1. 提取并验证参数
        Map<String, Object> boundParams = extractAndValidateParams(requestParams);
        
        // 2. 处理条件标签
        String processedSql = processConditionalTags(sqlTemplate, boundParams);
        
        // 3. 替换参数占位符
        String finalSql = replaceParams(processedSql, boundParams);
        
        log.debug("SQL解析完成: {}", finalSql);
        
        return ParsedSql.builder()
                .sql(finalSql)
                .params(boundParams)
                .build();
    }
    
    /**
     * 提取并验证参数
     */
    private Map<String, Object> extractAndValidateParams(Map<String, Object> requestParams) {
        Map<String, Object> boundParams = new HashMap<>();
        
        if (params != null) {
            for (SqlParam param : params) {
                Object value = param.extractValue(requestParams);
                
                // 验证参数
                if (!param.validate(value)) {
                    throw new IllegalArgumentException("参数 " + param.getName() + " 验证失败");
                }
                
                boundParams.put(param.getName(), value);
            }
        }
        
        return boundParams;
    }
    
    /**
     * 处理条件标签 (简化版MyBatis动态SQL)
     */
    private String processConditionalTags(String sql, Map<String, Object> params) {
        Matcher matcher = IF_PATTERN.matcher(sql);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String condition = matcher.group(1);
            String content = matcher.group(2);
            
            // 评估条件
            boolean conditionMet = evaluateCondition(condition, params);
            
            // 如果条件满足，保留内容；否则移除
            matcher.appendReplacement(result, conditionMet ? content : "");
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Map<String, Object> params) {
        // 简单的条件评估: paramName != null, paramName == 'value', etc.
        condition = condition.trim();
        
        // 处理 != null
        if (condition.endsWith("!= null")) {
            String paramName = condition.replace("!= null", "").trim();
            return params.get(paramName) != null;
        }
        
        // 处理 == null
        if (condition.endsWith("== null")) {
            String paramName = condition.replace("== null", "").trim();
            return params.get(paramName) == null;
        }
        
        // 处理布尔值
        Object value = params.get(condition);
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        
        // 默认：参数存在且不为null即为true
        return params.containsKey(condition) && params.get(condition) != null;
    }
    
    /**
     * 替换参数占位符
     */
    private String replaceParams(String sql, Map<String, Object> params) {
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String paramName = null;
            
            // 找到匹配的参数名
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    paramName = matcher.group(i);
                    break;
                }
            }
            
            if (paramName != null && params.containsKey(paramName)) {
                Object value = params.get(paramName);
                String replacement = formatValue(value);
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 格式化参数值
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        }
        
        return value.toString();
    }
    
    /**
     * 从SQL模板中自动提取参数
     */
    public static List<String> extractParamNames(String sqlTemplate) {
        List<String> paramNames = new ArrayList<>();
        Matcher matcher = PARAM_PATTERN.matcher(sqlTemplate);
        
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    String paramName = matcher.group(i);
                    if (!paramNames.contains(paramName)) {
                        paramNames.add(paramName);
                    }
                }
            }
        }
        
        return paramNames;
    }
    
    /**
     * 解析结果
     */
    @Data
    @Builder
    public static class ParsedSql {
        /**
         * 最终SQL
         */
        private String sql;
        
        /**
         * 绑定的参数
         */
        private Map<String, Object> params;
    }
}
