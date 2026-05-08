package com.demo.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页响应DTO
 */
@Data
@Schema(description = "分页响应")
public class PageResponse<T> {

    @Schema(description = "当前页码", example = "1")
    private Integer page;

    @Schema(description = "每页大小", example = "10")
    private Integer size;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "总页数", example = "10")
    private Integer totalPages;

    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;

    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    @Schema(description = "数据列表")
    private List<T> data;

    /**
     * 设置分页信息
     */
    public void setPageInfo(Integer page, Integer size, Long total) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.hasPrevious = page > 1;
        this.hasNext = page < totalPages;
    }

    /**
     * 静态工厂方法：创建成功响应
     */
    public static <T> PageResponse<T> success(Integer page, Integer size, Long total, List<T> data) {
        PageResponse<T> response = new PageResponse<>();
        response.setPageInfo(page, size, total);
        response.setData(data);
        return response;
    }

    /**
     * 静态工厂方法：创建空响应
     */
    public static <T> PageResponse<T> empty(Integer page, Integer size) {
        PageResponse<T> response = new PageResponse<>();
        response.setPageInfo(page, size, 0L);
        response.setData(List.of());
        return response;
    }

    /**
     * 获取起始索引（用于数据库查询）
     */
    public Integer getStartIndex() {
        return (page - 1) * size;
    }

    /**
     * 获取结束索引（用于数据库查询）
     */
    public Integer getEndIndex() {
        return page * size;
    }

    /**
     * 获取当前页的起始序号（用于前端显示）
     */
    public Integer getStartNumber() {
        return (page - 1) * size + 1;
    }

    /**
     * 获取当前页的结束序号（用于前端显示）
     */
    public Integer getEndNumber() {
        long end = (long) page * size;
        return (int) Math.min(end, total);
    }

    /**
     * 获取分页参数摘要
     */
    public String getSummary() {
        return String.format("第%d页，共%d条记录，每页%d条，共%d页", 
                page, total, size, totalPages);
    }

    /**
     * 判断是否为空页
     */
    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    /**
     * 判断是否为第一页
     */
    public boolean isFirstPage() {
        return page <= 1;
    }

    /**
     * 判断是否为最后一页
     */
    public boolean isLastPage() {
        return page >= totalPages;
    }

    /**
     * 获取上一页页码
     */
    public Integer getPreviousPage() {
        return isFirstPage() ? null : page - 1;
    }

    /**
     * 获取下一页页码
     */
    public Integer getNextPage() {
        return isLastPage() ? null : page + 1;
    }

    /**
     * 获取页码范围（用于分页器）
     */
    public List<Integer> getPageRange() {
        int start = Math.max(1, page - 2);
        int end = Math.min(totalPages, page + 2);
        
        List<Integer> pages = new java.util.ArrayList<>();
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
}