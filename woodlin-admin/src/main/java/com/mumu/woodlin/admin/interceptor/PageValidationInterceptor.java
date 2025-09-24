package com.mumu.woodlin.admin.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 分页参数校验拦截器
 * 
 * @author mumu
 * @description 防止分页参数溢出，确保系统安全性和性能
 * @since 2025-01-01
 */
@Slf4j
@Component
public class PageValidationInterceptor implements HandlerInterceptor {
    
    @Value("${page.max-page-size:500}")
    private int maxPageSize;
    
    @Value("${page.max-page-num:10000}")
    private int maxPageNum;
    
    @Value("${page.default-page-size:20}")
    private int defaultPageSize;
    
    @Value("${page.default-page-num:1}")
    private int defaultPageNum;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 检查页码参数
        String pageNumStr = request.getParameter("pageNum");
        if (pageNumStr != null) {
            try {
                int pageNum = Integer.parseInt(pageNumStr);
                if (pageNum < 1) {
                    log.warn("Invalid pageNum: {}, reset to {}", pageNum, defaultPageNum);
                    request.setAttribute("pageNum", defaultPageNum);
                } else if (pageNum > maxPageNum) {
                    log.warn("PageNum overflow: {}, reset to {}", pageNum, maxPageNum);
                    request.setAttribute("pageNum", maxPageNum);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid pageNum format: {}, reset to {}", pageNumStr, defaultPageNum);
                request.setAttribute("pageNum", defaultPageNum);
            }
        }
        
        // 检查页面大小参数
        String pageSizeStr = request.getParameter("pageSize");
        if (pageSizeStr != null) {
            try {
                int pageSize = Integer.parseInt(pageSizeStr);
                if (pageSize < 1) {
                    log.warn("Invalid pageSize: {}, reset to {}", pageSize, defaultPageSize);
                    request.setAttribute("pageSize", defaultPageSize);
                } else if (pageSize > maxPageSize) {
                    log.warn("PageSize overflow: {}, reset to {}", pageSize, maxPageSize);
                    request.setAttribute("pageSize", maxPageSize);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid pageSize format: {}, reset to {}", pageSizeStr, defaultPageSize);
                request.setAttribute("pageSize", defaultPageSize);
            }
        }
        
        return true;
    }
}