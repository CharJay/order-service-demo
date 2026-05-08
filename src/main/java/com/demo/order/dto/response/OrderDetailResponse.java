package com.demo.order.dto.response;

import com.demo.order.common.constants.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情响应DTO
 */
@Data
@Schema(description = "订单详情响应")
public class OrderDetailResponse {

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

    @Schema(description = "可流转到的状态列表")
    private List<OrderStatus> availableStatusTransitions;

    @Schema(description = "订单明细列表")
    private List<OrderItemResponse> items;

    /**
     * 订单明细项响应DTO
     */
    @Data
    @Schema(description = "订单明细项")
    public static class OrderItemResponse {

        @Schema(description = "明细ID", example = "1")
        private Long id;

        @Schema(description = "商品名称", example = "iPhone 15 Pro")
        private String productName;

        @Schema(description = "数量", example = "1")
        private Integer quantity;

        @Schema(description = "单价", example = "8999.00")
        private BigDecimal unitPrice;

        @Schema(description = "小计", example = "8999.00")
        private BigDecimal subtotal;

        @Schema(description = "排序序号", example = "1")
        private Integer sortOrder;

        @Schema(description = "创建时间", example = "2026-05-08 10:30:00")
        private LocalDateTime createTime;

        @Schema(description = "更新时间", example = "2026-05-08 10:30:00")
        private LocalDateTime updateTime;

        /**
         * 计算小计（如果未提供）
         */
        public void calculateSubtotal() {
            if (this.unitPrice != null && this.quantity != null) {
                this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
            }
        }
    }

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
     * 计算订单统计信息
     */
    public void calculateStatistics() {
        if (items != null && !items.isEmpty()) {
            // 计算商品总数
            this.totalItems = items.stream()
                    .mapToInt(OrderItemResponse::getQuantity)
                    .sum();
            
            // 计算总金额
            this.totalAmount = items.stream()
                    .map(item -> {
                        if (item.getSubtotal() != null) {
                            return item.getSubtotal();
                        } else if (item.getUnitPrice() != null && item.getQuantity() != null) {
                            return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalItems = 0;
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    /**
     * 获取最贵的商品
     */
    public OrderItemResponse getMostExpensiveItem() {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.stream()
                .max((a, b) -> {
                    BigDecimal subtotalA = a.getSubtotal() != null ? a.getSubtotal() : BigDecimal.ZERO;
                    BigDecimal subtotalB = b.getSubtotal() != null ? b.getSubtotal() : BigDecimal.ZERO;
                    return subtotalA.compareTo(subtotalB);
                })
                .orElse(null);
    }

    /**
     * 获取最便宜的商品
     */
    public OrderItemResponse getCheapestItem() {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.stream()
                .min((a, b) -> {
                    BigDecimal subtotalA = a.getSubtotal() != null ? a.getSubtotal() : BigDecimal.ZERO;
                    BigDecimal subtotalB = b.getSubtotal() != null ? b.getSubtotal() : BigDecimal.ZERO;
                    return subtotalA.compareTo(subtotalB);
                })
                .orElse(null);
    }

    /**
     * 获取商品种类数
     */
    public Integer getProductVarietyCount() {
        if (items == null) {
            return 0;
        }
        return (int) items.stream()
                .map(OrderItemResponse::getProductName)
                .distinct()
                .count();
    }

    /**
     * 获取平均单价
     */
    public BigDecimal getAverageUnitPrice() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalPrice = items.stream()
                .map(item -> {
                    if (item.getUnitPrice() != null) {
                        return item.getUnitPrice();
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalPrice.divide(BigDecimal.valueOf(items.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
}