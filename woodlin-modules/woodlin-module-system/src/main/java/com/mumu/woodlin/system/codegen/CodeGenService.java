package com.mumu.woodlin.system.codegen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 代码生成服务
 * <p>
 * 基于 information_schema 提取表与字段元数据，使用字符串模板生成 Java 实体、Mapper、
 * Service 接口与实现、Controller 以及前端 api.ts 文件，最后以 zip 形式输出。
 * 不依赖 Velocity / Freemarker 等模板引擎，保持模块零额外依赖。
 * </p>
 *
 * @author yulin
 * @since 2026-06
 */
@Slf4j
@Service
public class CodeGenService {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * 分页查询当前数据源中的数据库表
     *
     * @param tableName    表名（模糊匹配，可选）
     * @param tableComment 表注释（模糊匹配，可选）
     * @param pageNum      页码，从 1 开始
     * @param pageSize     每页数量
     * @return 表信息列表
     */
    public List<GenTableInfo> getTables(String tableName, String tableComment, int pageNum, int pageSize) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate 未配置，无法读取数据库表元数据");
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT TABLE_NAME, TABLE_COMMENT, CREATE_TIME, UPDATE_TIME FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = (SELECT DATABASE())");
        List<Object> args = new ArrayList<>();
        if (tableName != null && !tableName.isBlank()) {
            sql.append(" AND TABLE_NAME LIKE ?");
            args.add("%" + tableName + "%");
        }
        if (tableComment != null && !tableComment.isBlank()) {
            sql.append(" AND TABLE_COMMENT LIKE ?");
            args.add("%" + tableComment + "%");
        }
        sql.append(" ORDER BY CREATE_TIME DESC LIMIT ? OFFSET ?");
        int safePage = Math.max(1, pageNum);
        int safeSize = Math.max(1, pageSize);
        args.add(safeSize);
        args.add((safePage - 1) * safeSize);

        try {
            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                GenTableInfo info = new GenTableInfo();
                info.setTableName(rs.getString("TABLE_NAME"));
                info.setTableComment(rs.getString("TABLE_COMMENT"));
                info.setCreateTime(rs.getString("CREATE_TIME"));
                info.setUpdateTime(rs.getString("UPDATE_TIME"));
                return info;
            }, args.toArray());
        } catch (Exception e) {
            log.warn("查询表元数据失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 统计当前数据源中的数据库表数量
     *
     * @param tableName    表名（模糊匹配，可选）
     * @param tableComment 表注释（模糊匹配，可选）
     * @return 表数量
     */
    public long countTables(String tableName, String tableComment) {
        if (jdbcTemplate == null) {
            return 0L;
        }
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = (SELECT DATABASE())");
        List<Object> args = new ArrayList<>();
        if (tableName != null && !tableName.isBlank()) {
            sql.append(" AND TABLE_NAME LIKE ?");
            args.add("%" + tableName + "%");
        }
        if (tableComment != null && !tableComment.isBlank()) {
            sql.append(" AND TABLE_COMMENT LIKE ?");
            args.add("%" + tableComment + "%");
        }
        try {
            Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, args.toArray());
            return count == null ? 0L : count;
        } catch (Exception e) {
            log.warn("统计表数量失败: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 查询指定表的字段信息
     *
     * @param tableName 表名
     * @return 字段信息列表
     */
    public List<GenColumnInfo> getColumns(String tableName) {
        if (jdbcTemplate == null || tableName == null || tableName.isBlank()) {
            return Collections.emptyList();
        }
        String sql = "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE "
                + "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ? "
                + "ORDER BY ORDINAL_POSITION";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                GenColumnInfo c = new GenColumnInfo();
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("DATA_TYPE");
                String columnKey = rs.getString("COLUMN_KEY");
                String isNullable = rs.getString("IS_NULLABLE");
                c.setColumnName(columnName);
                c.setColumnComment(rs.getString("COLUMN_COMMENT"));
                c.setColumnType(rs.getString("COLUMN_TYPE"));
                c.setJavaType(toJavaType(dataType));
                c.setJavaField(toCamelCase(columnName));
                boolean pk = "PRI".equalsIgnoreCase(columnKey);
                c.setIsPk(pk ? "1" : "0");
                c.setIsRequired("NO".equalsIgnoreCase(isNullable) ? "1" : "0");
                c.setIsInsert("1");
                c.setIsEdit(pk ? "0" : "1");
                c.setIsList("1");
                c.setIsQuery("0");
                c.setQueryType("EQ");
                c.setHtmlType(toHtmlType(dataType));
                return c;
            }, tableName);
        } catch (Exception e) {
            log.warn("查询表 {} 的字段信息失败: {}", tableName, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 预览生成的代码模板文件
     *
     * @param config 代码生成配置
     * @return 模板文件列表
     */
    public List<TemplateFile> preview(GenConfig config) {
        List<GenColumnInfo> columns = getColumns(config.getTableName());
        String className = toPascalCase(safe(config.getBusinessName(), config.getTableName()));
        String basePackage = safe(config.getPackageName(), "com.mumu.woodlin.generated");
        String moduleName = safe(config.getModuleName(), "system");
        String basePath = basePackage.replace('.', '/');

        List<TemplateFile> files = new ArrayList<>();
        files.add(new TemplateFile(
                "src/main/java/" + basePath + "/entity/" + className + ".java",
                renderEntity(basePackage, className, config, columns)));
        files.add(new TemplateFile(
                "src/main/java/" + basePath + "/mapper/" + className + "Mapper.java",
                renderMapper(basePackage, className, config)));
        files.add(new TemplateFile(
                "src/main/java/" + basePath + "/service/" + className + "Service.java",
                renderServiceInterface(basePackage, className, config)));
        files.add(new TemplateFile(
                "src/main/java/" + basePath + "/service/impl/" + className + "ServiceImpl.java",
                renderServiceImpl(basePackage, className, config)));
        files.add(new TemplateFile(
                "src/main/java/" + basePath + "/controller/" + className + "Controller.java",
                renderController(basePackage, className, moduleName, config)));
        files.add(new TemplateFile(
                "src/api/" + moduleName + "/" + lowerFirst(className) + ".ts",
                renderApiTs(className, moduleName, config)));
        return files;
    }

    /**
     * 将预览得到的模板文件打包为 zip
     *
     * @param config 代码生成配置
     * @return zip 字节内容
     * @throws IOException 写入失败
     */
    public byte[] download(GenConfig config) throws IOException {
        List<TemplateFile> files = preview(config);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (TemplateFile f : files) {
                zos.putNextEntry(new ZipEntry(f.getName()));
                zos.write(f.getContent().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private String renderEntity(String basePackage, String className, GenConfig config, List<GenColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(basePackage).append(".entity;\n\n");
        sb.append("import java.io.Serializable;\n");
        sb.append("import java.math.BigDecimal;\n");
        sb.append("import java.time.LocalDate;\n");
        sb.append("import java.time.LocalDateTime;\n\n");
        sb.append("import lombok.Data;\n\n");
        sb.append("/**\n");
        sb.append(" * ").append(safe(config.getFunctionName(), className)).append("\n");
        sb.append(" *\n");
        sb.append(" * @author ").append(safe(config.getAuthor(), "yulin")).append("\n");
        sb.append(" */\n");
        sb.append("@Data\n");
        sb.append("public class ").append(className).append(" implements Serializable {\n\n");
        sb.append("    private static final long serialVersionUID = 1L;\n\n");
        for (GenColumnInfo col : columns) {
            if (col.getColumnComment() != null && !col.getColumnComment().isBlank()) {
                sb.append("    /** ").append(col.getColumnComment()).append(" */\n");
            }
            sb.append("    private ").append(col.getJavaType()).append(' ').append(col.getJavaField()).append(";\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String renderMapper(String basePackage, String className, GenConfig config) {
        return "package " + basePackage + ".mapper;\n\n"
                + "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n"
                + "import org.apache.ibatis.annotations.Mapper;\n\n"
                + "import " + basePackage + ".entity." + className + ";\n\n"
                + "/**\n"
                + " * " + safe(config.getFunctionName(), className) + " Mapper\n"
                + " *\n"
                + " * @author " + safe(config.getAuthor(), "yulin") + "\n"
                + " */\n"
                + "@Mapper\n"
                + "public interface " + className + "Mapper extends BaseMapper<" + className + "> {\n"
                + "}\n";
    }

    private String renderServiceInterface(String basePackage, String className, GenConfig config) {
        return "package " + basePackage + ".service;\n\n"
                + "import com.baomidou.mybatisplus.extension.service.IService;\n\n"
                + "import " + basePackage + ".entity." + className + ";\n\n"
                + "/**\n"
                + " * " + safe(config.getFunctionName(), className) + " Service\n"
                + " *\n"
                + " * @author " + safe(config.getAuthor(), "yulin") + "\n"
                + " */\n"
                + "public interface " + className + "Service extends IService<" + className + "> {\n"
                + "}\n";
    }

    private String renderServiceImpl(String basePackage, String className, GenConfig config) {
        return "package " + basePackage + ".service.impl;\n\n"
                + "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n"
                + "import org.springframework.stereotype.Service;\n\n"
                + "import " + basePackage + ".entity." + className + ";\n"
                + "import " + basePackage + ".mapper." + className + "Mapper;\n"
                + "import " + basePackage + ".service." + className + "Service;\n\n"
                + "/**\n"
                + " * " + safe(config.getFunctionName(), className) + " Service 实现\n"
                + " *\n"
                + " * @author " + safe(config.getAuthor(), "yulin") + "\n"
                + " */\n"
                + "@Service\n"
                + "public class " + className + "ServiceImpl extends ServiceImpl<" + className + "Mapper, "
                + className + "> implements " + className + "Service {\n"
                + "}\n";
    }

    private String renderController(String basePackage, String className, String moduleName, GenConfig config) {
        String path = "/" + moduleName + "/" + lowerFirst(className);
        String var = lowerFirst(className);
        return "package " + basePackage + ".controller;\n\n"
                + "import java.util.List;\n\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.web.bind.annotation.*;\n\n"
                + "import com.mumu.woodlin.common.response.R;\n"
                + "import " + basePackage + ".entity." + className + ";\n"
                + "import " + basePackage + ".service." + className + "Service;\n\n"
                + "/**\n"
                + " * " + safe(config.getFunctionName(), className) + " Controller\n"
                + " *\n"
                + " * @author " + safe(config.getAuthor(), "yulin") + "\n"
                + " */\n"
                + "@RestController\n"
                + "@RequestMapping(\"" + path + "\")\n"
                + "@RequiredArgsConstructor\n"
                + "public class " + className + "Controller {\n\n"
                + "    private final " + className + "Service " + var + "Service;\n\n"
                + "    @GetMapping(\"/list\")\n"
                + "    public R<List<" + className + ">> list() {\n"
                + "        return R.ok(" + var + "Service.list());\n"
                + "    }\n\n"
                + "    @GetMapping(\"/{id}\")\n"
                + "    public R<" + className + "> get(@PathVariable Long id) {\n"
                + "        return R.ok(" + var + "Service.getById(id));\n"
                + "    }\n\n"
                + "    @PostMapping\n"
                + "    public R<Boolean> save(@RequestBody " + className + " entity) {\n"
                + "        return R.ok(" + var + "Service.save(entity));\n"
                + "    }\n\n"
                + "    @PutMapping\n"
                + "    public R<Boolean> update(@RequestBody " + className + " entity) {\n"
                + "        return R.ok(" + var + "Service.updateById(entity));\n"
                + "    }\n\n"
                + "    @DeleteMapping(\"/{id}\")\n"
                + "    public R<Boolean> remove(@PathVariable Long id) {\n"
                + "        return R.ok(" + var + "Service.removeById(id));\n"
                + "    }\n"
                + "}\n";
    }

    private String renderApiTs(String className, String moduleName, GenConfig config) {
        String var = lowerFirst(className);
        String path = "/" + moduleName + "/" + var;
        return "import request from '@/utils/request'\n\n"
                + "// " + safe(config.getFunctionName(), className) + " API\n"
                + "export function list" + className + "() {\n"
                + "  return request.get('" + path + "/list')\n"
                + "}\n\n"
                + "export function get" + className + "(id: number | string) {\n"
                + "  return request.get(`" + path + "/${id}`)\n"
                + "}\n\n"
                + "export function save" + className + "(data: any) {\n"
                + "  return request.post('" + path + "', data)\n"
                + "}\n\n"
                + "export function update" + className + "(data: any) {\n"
                + "  return request.put('" + path + "', data)\n"
                + "}\n\n"
                + "export function remove" + className + "(id: number | string) {\n"
                + "  return request.delete(`" + path + "/${id}`)\n"
                + "}\n";
    }

    private String toJavaType(String dbType) {
        if (dbType == null) {
            return "String";
        }
        return switch (dbType.toLowerCase()) {
            case "bigint" -> "Long";
            case "int", "integer", "tinyint", "smallint", "mediumint" -> "Integer";
            case "varchar", "char", "text", "longtext", "mediumtext", "tinytext", "json" -> "String";
            case "datetime", "timestamp" -> "LocalDateTime";
            case "date" -> "LocalDate";
            case "decimal", "numeric" -> "BigDecimal";
            case "double", "float" -> "Double";
            case "bit", "boolean", "bool" -> "Boolean";
            default -> "String";
        };
    }

    private String toHtmlType(String dbType) {
        if (dbType == null) {
            return "input";
        }
        return switch (dbType.toLowerCase()) {
            case "text", "longtext", "mediumtext", "tinytext" -> "textarea";
            case "datetime", "timestamp", "date" -> "datetime";
            case "bit", "boolean", "bool", "tinyint" -> "select";
            default -> "input";
        };
    }

    private String toCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_' || c == '-') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private String toPascalCase(String name) {
        String camel = toCamelCase(name);
        if (camel.isEmpty()) {
            return camel;
        }
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    private String lowerFirst(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
