package com.demo.order.service;

import com.demo.order.dto.request.OrderCreateRequest;
import com.demo.order.dto.request.OrderQueryRequest;
import com.demo.order.dto.request.OrderUpdateRequest;
import com.demo.order.dto.response.OrderDetailResponse;
import com.demo.order.dto.response.OrderResponse;
import com.demo.order.dto.response.PageResponse;
import com.demo.order.common.constants.OrderStatus;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    OrderResponse createOrder(OrderCreateRequest request);

    /**
     * 更新订单
     */
    OrderResponse updateOrder(Long orderId, OrderUpdateRequest request);

    /**
     * 删除订单（软删除）
     */
    void deleteOrder(Long orderId);

    /**
     * 获取订单详情
     */
    OrderDetailResponse getOrderDetail(Long orderId);

    /**
     * 分页查询订单列表
     */
    PageResponse<OrderResponse> queryOrders(OrderQueryRequest request);

    /**
     * 变更订单状态
     */
    void changeOrderStatus(Long orderId, OrderStatus newStatus);

    /**
     * 获取订单状态流转选项
     */
    List<OrderStatus> getAvailableStatusTransitions(Long orderId);

    /**
     * 批量删除订单
     */
    void batchDeleteOrders(List<Long> orderIds);

    /**
     * 恢复已删除的订单
     */
    void restoreOrder(Long orderId);

    /**
     * 获取订单统计信息
     */
    Object getOrderStatistics();
}