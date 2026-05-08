package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细表实体类
 */
@Data
@TableName("order_detail")
public class OrderDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID，关联order_header.id
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 商品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 数量，≥1整数
     */
    private Integer quantity;

    /**
     * 单价，≥0，两位小数
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 小计 = 数量×单价
     */
    private BigDecimal subtotal;

    /**
     * 排序序号，保持明细顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 初始化方法
     */
    public void initForCreate(Long orderId, Integer sortOrder) {
        this.orderId = orderId;
        this.sortOrder = sortOrder;
        this.calculateSubtotal();
    }

    /**
     * 计算小计
     */
    public void calculateSubtotal() {
        if (this.quantity != null && this.unitPrice != null) {
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * 更新商品信息
     */
    public void updateProductInfo(String productName, Integer quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.calculateSubtotal();
    }

    /**
     * 验证数据有效性
     */
    public boolean isValid() {
        if (this.productName == null || this.productName.trim().isEmpty()) {
            return false;
        }
        if (this.quantity == null || this.quantity < 1) {
            return false;
        }
        if (this.unitPrice == null || this.unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }

    /**
     * 获取小计金额（防止null）
     */
    public BigDecimal getSafeSubtotal() {
        return this.subtotal != null ? this.subtotal : BigDecimal.ZERO;
    }
}