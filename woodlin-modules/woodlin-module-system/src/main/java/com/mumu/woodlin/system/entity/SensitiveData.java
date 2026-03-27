package com.mumu.woodlin.system.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.annotation.SearchableField;
import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 敏感数据实体（示例）
 * 
 * @author mumu
 * @description 演示可搜索加密功能的示例实体，包含需要加密的敏感字段
 *              此实体用于展示如何使用@SearchableField注解实现数据加密和模糊搜索
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sensitive_data")
@Schema(description = "敏感数据示例")
public class SensitiveData extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 数据ID
     */
    @TableId(value = "data_id", type = IdType.ASSIGN_ID)
    @Schema(description = "数据ID")
    private Long dataId;
    
    /**
     * 真实姓名（加密存储，支持模糊搜索）
     * 使用@SearchableField注解标记，数据会自动加密
     * fuzzySearch=true表示支持模糊查询
     */
    @TableField("real_name")
    @SearchableField(fuzzySearch = true)
    @Schema(description = "真实姓名")
    private String realName;
    
    /**
     * 真实姓名搜索索引（存储加密的N-gram）
     * 用于支持模糊搜索，无需手动维护
     */
    @TableField("real_name_search_index")
    @Schema(description = "真实姓名搜索索引")
    private String realNameSearchIndex;
    
    /**
     * 身份证号（加密存储，仅支持精确匹配）
     * fuzzySearch=false表示仅支持精确查询，不生成N-gram索引
     */
    @TableField("id_card")
    @SearchableField(fuzzySearch = false)
    @Schema(description = "身份证号")
    private String idCard;
    
    /**
     * 手机号（加密存储，支持模糊搜索）
     */
    @TableField("mobile")
    @SearchableField(fuzzySearch = true)
    @Schema(description = "手机号")
    private String mobile;
    
    /**
     * 手机号搜索索引
     */
    @TableField("mobile_search_index")
    @Schema(description = "手机号搜索索引")
    private String mobileSearchIndex;
    
    /**
     * 邮箱地址（加密存储，支持模糊搜索）
     */
    @TableField("email_address")
    @SearchableField(fuzzySearch = true)
    @Schema(description = "邮箱地址")
    private String emailAddress;
    
    /**
     * 邮箱搜索索引
     */
    @TableField("email_address_search_index")
    @Schema(description = "邮箱搜索索引")
    private String emailAddressSearchIndex;
    
    /**
     * 家庭住址（加密存储，支持模糊搜索）
     */
    @TableField("home_address")
    @SearchableField(fuzzySearch = true, ngramSize = 3)
    @Schema(description = "家庭住址")
    private String homeAddress;
    
    /**
     * 家庭住址搜索索引
     */
    @TableField("home_address_search_index")
    @Schema(description = "家庭住址搜索索引")
    private String homeAddressSearchIndex;
    
    /**
     * 银行卡号（加密存储，仅支持精确匹配）
     */
    @TableField("bank_card")
    @SearchableField(fuzzySearch = false)
    @Schema(description = "银行卡号")
    private String bankCard;
    
    /**
     * 数据类型
     */
    @TableField("data_type")
    @Schema(description = "数据类型")
    private String dataType;
    
    /**
     * 状态（1-正常，0-禁用）
     */
    @TableField("status")
    @Schema(description = "状态")
    private String status;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
