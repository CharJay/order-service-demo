package com.demo.order.dto.request;

import com.demo.order.common.constants.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 订单查询请求DTO
 */
@Data
@Schema(description = "订单查询请求")
public class OrderQueryRequest {

    @Schema(description = "页码，从1开始", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;

    @Schema(description = "订单状态", example = "pending")
    private OrderStatus status;

    @Schema(description = "客户姓名关键词", example = "张三")
    private String customerName;

    @Schema(description = "订单号关键词", example = "ORD-20260508")
    private String orderNo;

    @Schema(description = "开始时间", example = "2026-05-01 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-05-08 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "排序字段", example = "order_time")
    private String sortField = "order_time";

    @Schema(description = "排序方向：asc/desc", example = "desc")
    private String sortDirection = "desc";

    /**
     * 获取排序字符串
     */
    public String getOrderBy() {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "order_time desc";
        }
        
        // 防止SQL注入，只允许特定的字段排序
        String safeField = getSafeSortField(sortField);
        String direction = "desc".equalsIgnoreCase(sortDirection) ? "desc" : "asc";
        
        return safeField + " " + direction;
    }

    /**
     * 安全的排序字段验证
     */
    private String getSafeSortField(String field) {
        switch (field.toLowerCase()) {
            case "order_time":
                return "order_time";
            case "update_time":
                return "update_time";
            case "total_amount":
                return "total_amount";
            case "customer_name":
                return "customer_name";
            case "status":
                return "status";
            default:
                return "order_time";
        }
    }

    /**
     * 判断是否按状态筛选
     */
    public boolean hasStatusFilter() {
        return status != null;
    }

    /**
     * 判断是否按客户名筛选
     */
    public boolean hasCustomerNameFilter() {
        return customerName != null && !customerName.trim().isEmpty();
    }

    /**
     * 判断是否按订单号筛选
     */
    public boolean hasOrderNoFilter() {
        return orderNo != null && !orderNo.trim().isEmpty();
    }

    /**
     * 判断是否按时间范围筛选
     */
    public boolean hasTimeRangeFilter() {
        return startTime != null && endTime != null;
    }

    /**
     * 验证时间范围是否有效
     */
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // 没有时间范围限制，认为是有效的
        }
        return !startTime.isAfter(endTime);
    }

    /**
     * 获取查询偏移量
     */
    public Integer getOffset() {
        return (page - 1) * size;
    }

    /**
     * 获取查询限制
     */
    public Integer getLimit() {
        return size;
    }

    /**
     * 构建模糊查询的客户名
     */
    public String getCustomerNameLike() {
        if (!hasCustomerNameFilter()) {
            return null;
        }
        return "%" + customerName.trim() + "%";
    }

    /**
     * 构建模糊查询的订单号
     */
    public String getOrderNoLike() {
        if (!hasOrderNoFilter()) {
            return null;
        }
        return "%" + orderNo.trim() + "%";
    }
}