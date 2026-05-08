package com.demo.order.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误详情
     */
    private final Object data;

    /**
     * 构造业务异常
     */
    public BusinessException(String code, String message) {
        this(code, message, null);
    }

    /**
     * 构造业务异常（带数据）
     */
    public BusinessException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    /**
     * 构造业务异常（带原因）
     */
    public BusinessException(String code, String message, Throwable cause) {
        this(code, message, cause, null);
    }

    /**
     * 构造业务异常（带原因和数据）
     */
    public BusinessException(String code, String message, Throwable cause, Object data) {
        super(message, cause);
        this.code = code;
        this.data = data;
    }

    /**
     * 创建订单不存在异常
     */
    public static BusinessException orderNotFound(Long orderId) {
        return new BusinessException("ORDER_NOT_FOUND", 
                String.format("订单不存在，订单ID：%d", orderId), orderId);
    }

    /**
     * 创建订单已删除异常
     */
    public static BusinessException orderDeleted(Long orderId) {
        return new BusinessException("ORDER_DELETED", 
                String.format("订单已被删除，订单ID：%d", orderId), orderId);
    }

    /**
     * 创建订单不可编辑异常
     */
    public static BusinessException orderNotEditable(Long orderId) {
        return new BusinessException("ORDER_NOT_EDITABLE", 
                String.format("订单不可编辑（终态订单），订单ID：%d", orderId), orderId);
    }

    /**
     * 创建订单不可删除异常
     */
    public static BusinessException orderNotDeletable(Long orderId) {
        return new BusinessException("ORDER_NOT_DELETABLE", 
                String.format("订单不可删除（终态订单），订单ID：%d", orderId), orderId);
    }

    /**
     * 创建状态流转不允许异常
     */
    public static BusinessException statusTransitionNotAllowed(Long orderId, String fromStatus, String toStatus) {
        return new BusinessException("STATUS_TRANSITION_NOT_ALLOWED", 
                String.format("不允许的状态流转：从 %s 到 %s，订单ID：%d", fromStatus, toStatus, orderId),
                new StatusTransitionData(orderId, fromStatus, toStatus));
    }

    /**
     * 创建订单号已存在异常
     */
    public static BusinessException orderNoExists(String orderNo) {
        return new BusinessException("ORDER_NO_EXISTS", 
                String.format("订单号已存在：%s", orderNo), orderNo);
    }

    /**
     * 创建数据验证异常
     */
    public static BusinessException validationError(String field, String message) {
        return new BusinessException("VALIDATION_ERROR", 
                String.format("字段 %s 验证失败：%s", field, message),
                new ValidationErrorData(field, message));
    }

    /**
     * 状态流转数据类
     */
    @Getter
    public static class StatusTransitionData {
        private final Long orderId;
        private final String fromStatus;
        private final String toStatus;

        public StatusTransitionData(Long orderId, String fromStatus, String toStatus) {
            this.orderId = orderId;
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
        }
    }

    /**
     * 验证错误数据类
     */
    @Getter
    public static class ValidationErrorData {
        private final String field;
        private final String message;

        public ValidationErrorData(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}