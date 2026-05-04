package com.mumu.woodlin.admin;

import com.mumu.woodlin.system.codegen.CodeGenController;
import com.mumu.woodlin.system.controller.MonitorController;
import com.mumu.woodlin.system.controller.SysNoticeController;
import com.mumu.woodlin.system.controller.SysRegionController;
import com.mumu.woodlin.task.controller.ScheduleJobController;
import com.mumu.woodlin.task.controller.ScheduleJobLogController;
import com.mumu.woodlin.tenant.controller.SysTenantPackageController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight integration smoke test that verifies the controller surface area
 * exposed by the new system / task / tenant / generator modules without
 * requiring a Spring context or backing database.
 *
 * @author yulin
 * @since 2026-06
 */
class IntegrationSmokeTest {

    @Test
    void monitorControllerHasExpectedMapping() {
        RequestMapping mapping = MonitorController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/monitor");
    }

    @Test
    void noticeControllerHasExpectedMapping() {
        RequestMapping mapping = SysNoticeController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/system/notice");
    }

    @Test
    void regionControllerHasExpectedMapping() {
        RequestMapping mapping = SysRegionController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/system/region");
    }

    @Test
    void scheduleJobControllerHasExpectedMapping() {
        RequestMapping mapping = ScheduleJobController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/schedule/job");
    }

    @Test
    void scheduleJobLogControllerHasExpectedMapping() {
        RequestMapping mapping = ScheduleJobLogController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/schedule/log");
    }

    @Test
    void tenantPackageControllerHasExpectedMapping() {
        RequestMapping mapping = SysTenantPackageController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/system/tenant/package");
    }

    @Test
    void codeGenControllerHasExpectedMapping() {
        RequestMapping mapping = CodeGenController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/gen");
    }
}
