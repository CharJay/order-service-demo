package com.demo.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 更新订单请求DTO
 */
@Data
@Schema(description = "更新订单请求")
public class OrderUpdateRequest {

    @Size(max = 100, message = "客户姓名长度不能超过100个字符")
    @Schema(description = "客户姓名", example = "张三")
    private String customerName;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Size(max = 500, message = "收货地址长度不能超过500个字符")
    @Schema(description = "收货地址", example = "北京市朝阳区建国门外大街")
    private String address;

    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    @Valid
    @Schema(description = "订单明细列表")
    private List<OrderItemRequest> items;

    /**
     * 订单明细项请求DTO
     */
    @Data
    @Schema(description = "订单明细项")
    public static class OrderItemRequest {

        @Schema(description = "明细ID（更新时使用，新增时为空）", example = "1")
        private Long id;

        @Size(max = 200, message = "商品名称长度不能超过200个字符")
        @Schema(description = "商品名称", example = "iPhone 15 Pro")
        private String productName;

        @Schema(description = "数量，≥1整数", example = "1")
        private Integer quantity;

        @Schema(description = "单价，≥0，两位小数", example = "8999.00")
        private Double unitPrice;

        /**
         * 验证明细项数据
         */
        public boolean isValid() {
            if (productName == null || productName.trim().isEmpty()) {
                return false;
            }
            if (quantity == null || quantity < 1) {
                return false;
            }
            if (unitPrice == null || unitPrice < 0) {
                return false;
            }
            return true;
        }
    }

    /**
     * 验证请求数据
     */
    public boolean isValid() {
        // 如果没有提供任何更新字段，则无效
        if (customerName == null && phone == null && address == null && 
            remark == null && (items == null || items.isEmpty())) {
            return false;
        }

        // 如果提供了明细，验证每个明细项
        if (items != null && !items.isEmpty()) {
            for (OrderItemRequest item : items) {
                if (!item.isValid()) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 判断是否更新了订单头信息
     */
    public boolean hasHeaderUpdate() {
        return customerName != null || phone != null || address != null || remark != null;
    }

    /**
     * 判断是否更新了订单明细
     */
    public boolean hasDetailUpdate() {
        return items != null && !items.isEmpty();
    }

    /**
     * 获取商品总数
     */
    public Integer getTotalItems() {
        if (items == null) {
            return null;
        }
        return items.stream()
                .mapToInt(OrderItemRequest::getQuantity)
                .sum();
    }

    /**
     * 获取总金额
     */
    public Double getTotalAmount() {
        if (items == null) {
            return null;
        }
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }
}