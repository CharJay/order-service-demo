package com.demo.order.common.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {

    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    private static final Map<String, OrderStatus> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(OrderStatus::getCode, Function.identity()));

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static OrderStatus fromCode(String code) {
        return CODE_MAP.get(code);
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValidCode(String code) {
        return CODE_MAP.containsKey(code);
    }

    /**
     * 判断是否为终态（不可再变更）
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * 判断是否可以流转到目标状态
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (this.isTerminal()) {
            return false; // 终态不可流转
        }

        switch (this) {
            case PENDING:
                return target == PROCESSING || target == CANCELLED;
            case PROCESSING:
                return target == COMPLETED || target == CANCELLED;
            default:
                return false;
        }
    }

    /**
     * 获取可流转到的状态列表
     */
    public OrderStatus[] getAvailableTransitions() {
        switch (this) {
            case PENDING:
                return new OrderStatus[]{PROCESSING, CANCELLED};
            case PROCESSING:
                return new OrderStatus[]{COMPLETED, CANCELLED};
            default:
                return new OrderStatus[0];
        }
    }

    /**
     * 获取状态标签颜色（用于前端显示）
     */
    public String getColor() {
        switch (this) {
            case PENDING:
                return "orange";
            case PROCESSING:
                return "blue";
            case COMPLETED:
                return "green";
            case CANCELLED:
                return "red";
            default:
                return "default";
        }
    }

    /**
     * 获取状态图标（用于前端显示）
     */
    public String getIcon() {
        switch (this) {
            case PENDING:
                return "clock-circle";
            case PROCESSING:
                return "sync";
            case COMPLETED:
                return "check-circle";
            case CANCELLED:
                return "close-circle";
            default:
                return "question-circle";
        }
    }

    @Override
    public String toString() {
        return this.code;
    }
}