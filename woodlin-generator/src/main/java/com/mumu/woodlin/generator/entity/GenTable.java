package com.mumu.woodlin.generator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 代码生成业务表实体
 * 
 * @author mumu
 * @description 代码生成业务表信息实体类
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("gen_table")
@Schema(description = "代码生成业务表")
public class GenTable extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 表ID
     */
    @TableId(value = "table_id", type = IdType.ASSIGN_ID)
    @Schema(description = "表ID")
    private Long tableId;
    
    /**
     * 表名称
     */
    @TableField("table_name")
    @Schema(description = "表名称")
    private String tableName;
    
    /**
     * 表描述
     */
    @TableField("table_comment")
    @Schema(description = "表描述")
    private String tableComment;
    
    /**
     * 实体类名称
     */
    @TableField("class_name")
    @Schema(description = "实体类名称")
    private String className;
    
    /**
     * 生成包路径
     */
    @TableField("package_name")
    @Schema(description = "生成包路径")
    private String packageName;
    
    /**
     * 生成模块名
     */
    @TableField("module_name")
    @Schema(description = "生成模块名")
    private String moduleName;
    
    /**
     * 生成业务名
     */
    @TableField("business_name")
    @Schema(description = "生成业务名")
    private String businessName;
    
    /**
     * 生成功能名
     */
    @TableField("function_name")
    @Schema(description = "生成功能名")
    private String functionName;
    
    /**
     * 生成功能作者
     */
    @TableField("function_author")
    @Schema(description = "生成功能作者")
    private String functionAuthor;
    
    /**
     * 生成代码方式（zip-压缩包，project-直接生成到项目）
     */
    @TableField("gen_type")
    @Schema(description = "生成代码方式")
    private String genType;
    
    /**
     * 生成路径（不填默认项目路径）
     */
    @TableField("gen_path")
    @Schema(description = "生成路径")
    private String genPath;
    
    /**
     * 主键信息
     */
    @TableField("pk_column")
    @Schema(description = "主键信息")
    private String pkColumn;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}