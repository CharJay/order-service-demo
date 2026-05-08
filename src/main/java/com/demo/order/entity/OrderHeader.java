package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.demo.order.common.constants.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体类
 */
@Data
@TableName("order_header")
public class OrderHeader {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号 ORD-yyyyMMdd-xxxx
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 客户姓名
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 收货地址
     */
    private String address;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 商品总数
     */
    @TableField("total_items")
    private Integer totalItems;

    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 下单时间
     */
    @TableField("order_time")
    private LocalDateTime orderTime;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 删除标记：0=未删除，1=已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 删除时间
     */
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 创建订单时的初始化方法
     */
    public void initForCreate() {
        this.status = OrderStatus.PENDING;
        this.totalItems = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.orderTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.deleted = 0;
    }

    /**
     * 更新订单统计信息
     */
    public void updateStatistics(Integer itemCount, BigDecimal amount) {
        this.totalItems = itemCount;
        this.totalAmount = amount;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断订单是否可编辑（非终态）
     */
    public boolean isEditable() {
        return !this.status.isTerminal();
    }

    /**
     * 判断订单是否可删除（非终态）
     */
    public boolean isDeletable() {
        return !this.status.isTerminal();
    }

    /**
     * 判断订单是否可变更状态
     */
    public boolean canChangeStatus(OrderStatus newStatus) {
        if (this.status.isTerminal()) {
            return false; // 终态不可变更
        }
        return this.status.canTransitionTo(newStatus);
    }
}