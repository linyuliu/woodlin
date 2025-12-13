package com.mumu.woodlin.admin.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mumu.woodlin.common.service.BuildInfoService;

/**
 * 应用就绪事件监听器
 * 
 * @author mumu
 * @description 监听应用启动完成事件，打印构建信息
 * @since 2025-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationReadyListener {
    
    private final BuildInfoService buildInfoService;
    
    /**
     * 应用启动完成事件处理
     * 
     * @param event 应用就绪事件
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        // 打印构建信息
        String buildInfoBanner = buildInfoService.formatBuildInfoBanner();
        log.info(buildInfoBanner);
        
        // 同时输出到控制台
        System.out.println(buildInfoBanner);
    }
}
