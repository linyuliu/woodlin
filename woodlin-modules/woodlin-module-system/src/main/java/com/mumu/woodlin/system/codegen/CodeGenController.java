package com.mumu.woodlin.system.codegen;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;

/**
 * 代码生成 Controller
 *
 * @author yulin
 * @since 2026-06
 */
@Slf4j
@RestController
@RequestMapping("/gen")
@RequiredArgsConstructor
@Tag(name = "代码生成", description = "代码生成管理接口")
public class CodeGenController {

    private final CodeGenService codeGenService;

    /**
     * 分页查询数据库表
     */
    @GetMapping("/table")
    public R<PageResult<GenTableInfo>> listTables(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String tableComment,
            @RequestParam(required = false) Long dataSourceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<GenTableInfo> records = codeGenService.getTables(tableName, tableComment, pageNum, pageSize);
        long total = codeGenService.countTables(tableName, tableComment);
        PageResult<GenTableInfo> page = PageResult.success((long) pageNum, (long) pageSize, total, records);
        return R.ok(page);
    }

    /**
     * 查询表字段信息
     */
    @GetMapping("/column/{tableName}")
    public R<List<GenColumnInfo>> listColumns(@PathVariable String tableName,
            @RequestParam(required = false) Long dataSourceId) {
        return R.ok(codeGenService.getColumns(tableName));
    }

    /**
     * 预览生成代码
     */
    @PostMapping("/preview")
    public R<Map<String, Object>> preview(@RequestBody GenConfig config) {
        List<TemplateFile> files = codeGenService.preview(config);
        Map<String, Object> result = new HashMap<>(2);
        result.put("templateFiles", files);
        return R.ok(result);
    }

    /**
     * 下载生成代码 zip
     */
    @PostMapping("/download")
    public void download(@RequestBody GenConfig config, HttpServletResponse response) throws IOException {
        byte[] zip = codeGenService.download(config);
        String fileName = (config.getTableName() == null || config.getTableName().isBlank()
                ? "code" : config.getTableName()) + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.getOutputStream().write(zip);
        response.getOutputStream().flush();
    }

    /**
     * 导入（写入项目）：当前实现仅做预览校验
     */
    @PostMapping("/import")
    public R<Void> importCode(@RequestBody GenConfig config) {
        List<TemplateFile> files = codeGenService.preview(config);
        log.info("代码生成导入，模板文件数: {}", files.size());
        return R.ok("代码预览成功，实际写入需要配置目标路径");
    }
}
