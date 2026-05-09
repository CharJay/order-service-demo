package com.demo.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateRequest {

    @NotBlank(message = "客户姓名不能为空")
    @Size(max = 100, message = "客户姓名长度不能超过100个字符")
    @Schema(description = "客户姓名", required = true, example = "张三")
    private String customerName;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Size(max = 500, message = "收货地址长度不能超过500个字符")
    @Schema(description = "收货地址", example = "北京市朝阳区建国门外大街")
    private String address;

    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    @NotNull(message = "订单明细不能为空")
    @Size(min = 1, message = "至少需要一个商品明细")
    @Valid
    @Schema(description = "订单明细列表", required = true)
    private List<OrderItemRequest> items;

    /**
     * 订单明细项请求DTO
     */
    @Data
    @Schema(description = "订单明细项")
    public static class OrderItemRequest {

        @NotBlank(message = "商品名称不能为空")
        @Size(max = 200, message = "商品名称长度不能超过200个字符")
        @Schema(description = "商品名称", required = true, example = "iPhone 15 Pro")
        private String productName;

        @NotNull(message = "数量不能为空")
        @Schema(description = "数量，≥1整数", required = true, example = "1")
        private Integer quantity;

        @NotNull(message = "单价不能为空")
        @Schema(description = "单价，≥0，两位小数", required = true, example = "8999.00")
        private Double unitPrice;
    }

    /**
     * 验证请求数据
     */
    public boolean isValid() {
        if (items == null || items.isEmpty()) {
            return false;
        }
        
        // 验证每个明细项
        for (OrderItemRequest item : items) {
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                return false;
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                return false;
            }
            if (item.getUnitPrice() == null || item.getUnitPrice() < 0) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 获取商品总数
     */
    public Integer getTotalItems() {
        if (items == null) {
            return 0;
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
            return 0.0;
        }
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }
}