package com.demo.order.dto.response;

import com.demo.order.common.constants.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应DTO
 */
@Data
@Schema(description = "订单响应")
public class OrderResponse {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单编号", example = "ORD-20260508-0001")
    private String orderNo;

    @Schema(description = "客户姓名", example = "张三")
    private String customerName;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "收货地址", example = "北京市朝阳区建国门外大街")
    private String address;

    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    @Schema(description = "订单状态", example = "pending")
    private OrderStatus status;

    @Schema(description = "状态描述", example = "待处理")
    private String statusDesc;

    @Schema(description = "状态颜色", example = "orange")
    private String statusColor;

    @Schema(description = "状态图标", example = "clock-circle")
    private String statusIcon;

    @Schema(description = "商品总数", example = "3")
    private Integer totalItems;

    @Schema(description = "总金额", example = "11997.00")
    private BigDecimal totalAmount;

    @Schema(description = "下单时间", example = "2026-05-08 10:30:00")
    private LocalDateTime orderTime;

    @Schema(description = "最后更新时间", example = "2026-05-08 10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "是否可编辑", example = "true")
    private Boolean editable;

    @Schema(description = "是否可删除", example = "true")
    private Boolean deletable;

    /**
     * 设置状态相关属性
     */
    public void setStatus(OrderStatus status) {
        this.status = status;
        if (status != null) {
            this.statusDesc = status.getDescription();
            this.statusColor = status.getColor();
            this.statusIcon = status.getIcon();
        }
    }

    /**
     * 设置可操作状态
     */
    public void setOperable(Boolean editable, Boolean deletable) {
        this.editable = editable;
        this.deletable = deletable;
    }

    /**
     * 简化的订单信息（用于列表显示）
     */
    @Data
    @Schema(description = "简化订单信息")
    public static class SimpleOrder {
        @Schema(description = "订单ID", example = "1")
        private Long id;

        @Schema(description = "订单编号", example = "ORD-20260508-0001")
        private String orderNo;

        @Schema(description = "客户姓名", example = "张三")
        private String customerName;

        @Schema(description = "订单状态", example = "pending")
        private OrderStatus status;

        @Schema(description = "商品总数", example = "3")
        private Integer totalItems;

        @Schema(description = "总金额", example = "11997.00")
        private BigDecimal totalAmount;

        @Schema(description = "下单时间", example = "2026-05-08 10:30:00")
        private LocalDateTime orderTime;
    }

    /**
     * 统计信息
     */
    @Data
    @Schema(description = "订单统计信息")
    public static class Statistics {
        @Schema(description = "总订单数", example = "100")
        private Long totalOrders;

        @Schema(description = "总金额", example = "500000.00")
        private BigDecimal totalAmount;

        @Schema(description = "平均订单金额", example = "5000.00")
        private BigDecimal avgOrderAmount;

        @Schema(description = "待处理订单数", example = "10")
        private Long pendingCount;

        @Schema(description = "处理中订单数", example = "20")
        private Long processingCount;

        @Schema(description = "已完成订单数", example = "60")
        private Long completedCount;

        @Schema(description = "已取消订单数", example = "10")
        private Long cancelledCount;
    }

    /**
     * 时间统计信息
     */
    @Data
    @Schema(description = "时间统计信息")
    public static class TimeStatistics {
        @Schema(description = "日期", example = "2026-05-08")
        private String date;

        @Schema(description = "订单数", example = "5")
        private Integer orderCount;

        @Schema(description = "总金额", example = "25000.00")
        private BigDecimal totalAmount;

        @Schema(description = "平均订单金额", example = "5000.00")
        private BigDecimal avgOrderAmount;
    }
}