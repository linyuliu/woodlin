package com.mumu.woodlin.common.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.util.List;

/**
 * 分页响应结果封装
 * 
 * @author mumu
 * @description 分页查询响应结果封装，包含分页信息和数据列表
 * @param <T> 数据类型
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "分页响应结果")
public class PageResult<T> extends Result<List<T>> {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Long current;
    
    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数", example = "20")
    private Long size;
    
    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;
    
    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "5")
    private Long pages;
    
    /**
     * 是否有上一页
     */
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;
    
    /**
     * 私有构造函数
     */
    private PageResult() {
        super();
    }
    
    /**
     * 私有构造函数
     */
    private PageResult(Long current, Long size, Long total, List<T> records) {
        super();
        this.current = current;
        this.size = size;
        this.total = total;
        this.setData(records);
        this.calculatePages();
    }
    
    /**
     * 成功分页响应
     * 
     * @param current 当前页码
     * @param size 每页记录数
     * @param total 总记录数
     * @param records 数据列表
     * @param <T> 数据类型
     * @return 分页响应结果
     */
    public static <T> PageResult<T> success(Long current, Long size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>(current, size, total, records);
        result.setCode(200);
        result.setMessage("查询成功");
        return result;
    }
    
    /**
     * 成功分页响应（自定义消息）
     * 
     * @param message 响应消息
     * @param current 当前页码
     * @param size 每页记录数
     * @param total 总记录数
     * @param records 数据列表
     * @param <T> 数据类型
     * @return 分页响应结果
     */
    public static <T> PageResult<T> success(String message, Long current, Long size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>(current, size, total, records);
        result.setCode(200);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 空分页响应
     * 
     * @param current 当前页码
     * @param size 每页记录数
     * @param <T> 数据类型
     * @return 空分页响应结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        return success(current, size, 0L, List.of());
    }
    
    /**
     * 失败分页响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败分页响应结果
     */
    public static <T> PageResult<T> failPage(String message) {
        PageResult<T> result = new PageResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 计算分页信息
     */
    private void calculatePages() {
        if (size != null && size > 0 && total != null) {
            this.pages = (total + size - 1) / size;
            this.hasPrevious = current != null && current > 1;
            this.hasNext = current != null && pages != null && current < pages;
        } else {
            this.pages = 0L;
            this.hasPrevious = false;
            this.hasNext = false;
        }
    }
    
    /**
     * 是否为空页
     * 
     * @return 是否为空页
     */
    public boolean isEmpty() {
        return getData() == null || getData().isEmpty();
    }
    
    /**
     * 获取记录数量
     * 
     * @return 记录数量
     */
    public int getRecordCount() {
        return getData() == null ? 0 : getData().size();
    }
    
}