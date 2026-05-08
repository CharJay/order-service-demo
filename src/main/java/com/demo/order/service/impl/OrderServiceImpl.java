package com.demo.order.service.impl;

import com.demo.order.common.constants.OrderStatus;
import com.demo.order.dto.request.OrderCreateRequest;
import com.demo.order.dto.request.OrderQueryRequest;
import com.demo.order.dto.request.OrderUpdateRequest;
import com.demo.order.dto.response.OrderDetailResponse;
import com.demo.order.dto.response.OrderResponse;
import com.demo.order.dto.response.PageResponse;
import com.demo.order.entity.OrderDetail;
import com.demo.order.entity.OrderHeader;
import com.demo.order.mapper.OrderDetailMapper;
import com.demo.order.mapper.OrderHeaderMapper;
import com.demo.order.service.OrderService;
import com.demo.order.utils.OrderNoGenerator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderHeaderMapper orderHeaderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderNoGenerator orderNoGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("创建订单，客户：{}", request.getCustomerName());

        // 1. 验证请求数据
        if (!request.isValid()) {
            throw new IllegalArgumentException("订单数据无效");
        }

        // 2. 生成订单号
        String orderNo = orderNoGenerator.generate();

        // 3. 创建订单头
        OrderHeader orderHeader = new OrderHeader();
        orderHeader.setOrderNo(orderNo);
        orderHeader.setCustomerName(request.getCustomerName());
        orderHeader.setPhone(request.getPhone());
        orderHeader.setAddress(request.getAddress());
        orderHeader.setRemark(request.getRemark());
        orderHeader.initForCreate();

        // 保存订单头
        orderHeaderMapper.insert(orderHeader);
        Long orderId = orderHeader.getId();
        log.info("订单头创建成功，订单ID：{}，订单号：{}", orderId, orderNo);

        // 4. 创建订单明细
        List<OrderDetail> orderDetails = new ArrayList<>();
        int sortOrder = 1;
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (OrderCreateRequest.OrderItemRequest itemRequest : request.getItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setProductName(itemRequest.getProductName());
            orderDetail.setQuantity(itemRequest.getQuantity());
            orderDetail.setUnitPrice(BigDecimal.valueOf(itemRequest.getUnitPrice()));
            orderDetail.calculateSubtotal();
            orderDetail.setSortOrder(sortOrder++);

            orderDetails.add(orderDetail);
            totalItems += itemRequest.getQuantity();
            totalAmount = totalAmount.add(orderDetail.getSubtotal());
        }

        // 批量插入订单明细
        if (!orderDetails.isEmpty()) {
            orderDetailMapper.batchInsert(orderDetails);
            log.info("订单明细创建成功，共{}条明细", orderDetails.size());
        }

        // 5. 更新订单统计信息
        orderHeader.updateStatistics(totalItems, totalAmount);
        orderHeaderMapper.updateStatistics(orderId, totalItems, totalAmount);

        // 6. 返回响应
        return convertToOrderResponse(orderHeader);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request) {
        log.info("更新订单，订单ID：{}", orderId);

        // 1. 检查订单是否存在且可编辑
        OrderHeader orderHeader = orderHeaderMapper.selectById(orderId);
        if (orderHeader == null || orderHeader.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在或已被删除");
        }
        if (!orderHeader.isEditable()) {
            throw new IllegalStateException("订单不可编辑（终态订单）");
        }

        // 2. 更新订单头信息
        if (request.getCustomerName() != null) {
            orderHeader.setCustomerName(request.getCustomerName());
        }
        if (request.getPhone() != null) {
            orderHeader.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            orderHeader.setAddress(request.getAddress());
        }
        if (request.getRemark() != null) {
            orderHeader.setRemark(request.getRemark());
        }
        orderHeader.setUpdateTime(LocalDateTime.now());

        // 3. 如果提供了明细，则更新明细
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // 删除原有明细
            orderDetailMapper.deleteByOrderId(orderId);

            // 创建新的明细
            List<OrderDetail> orderDetails = new ArrayList<>();
            int sortOrder = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalItems = 0;

            for (OrderUpdateRequest.OrderItemRequest itemRequest : request.getItems()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(orderId);
                orderDetail.setProductName(itemRequest.getProductName());
                orderDetail.setQuantity(itemRequest.getQuantity());
                orderDetail.setUnitPrice(BigDecimal.valueOf(itemRequest.getUnitPrice()));
                orderDetail.calculateSubtotal();
                orderDetail.setSortOrder(sortOrder++);

                orderDetails.add(orderDetail);
                totalItems += itemRequest.getQuantity();
                totalAmount = totalAmount.add(orderDetail.getSubtotal());
            }

            // 批量插入新明细
            if (!orderDetails.isEmpty()) {
                orderDetailMapper.batchInsert(orderDetails);
            }

            // 更新订单统计
            orderHeader.updateStatistics(totalItems, totalAmount);
        }

        // 4. 保存更新
        orderHeaderMapper.updateById(orderHeader);

        // 5. 返回响应
        return convertToOrderResponse(orderHeader);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId) {
        log.info("删除订单，订单ID：{}", orderId);

        // 1. 检查订单是否存在且可删除
        OrderHeader orderHeader = orderHeaderMapper.selectById(orderId);
        if (orderHeader == null || orderHeader.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在或已被删除");
        }
        if (!orderHeader.isDeletable()) {
            throw new IllegalStateException("订单不可删除（终态订单）");
        }

        // 2. 执行软删除
        orderHeaderMapper.softDelete(orderId);
        log.info("订单删除成功，订单ID：{}", orderId);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderId) {
        log.info("获取订单详情，订单ID：{}", orderId);

        // 1. 查询订单头
        OrderHeader orderHeader = orderHeaderMapper.selectById(orderId);
        if (orderHeader == null || orderHeader.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在或已被删除");
        }

        // 2. 查询订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orderId);

        // 3. 构建响应
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(orderHeader.getId());
        response.setOrderNo(orderHeader.getOrderNo());
        response.setCustomerName(orderHeader.getCustomerName());
        response.setPhone(orderHeader.getPhone());
        response.setAddress(orderHeader.getAddress());
        response.setRemark(orderHeader.getRemark());
        response.setStatus(orderHeader.getStatus());
        response.setStatusDesc(orderHeader.getStatus().getDescription());
        response.setStatusColor(orderHeader.getStatus().getColor());
        response.setStatusIcon(orderHeader.getStatus().getIcon());
        response.setTotalItems(orderHeader.getTotalItems());
        response.setTotalAmount(orderHeader.getTotalAmount());
        response.setOrderTime(orderHeader.getOrderTime());
        response.setUpdateTime(orderHeader.getUpdateTime());
        response.setOperable(orderHeader.isEditable(), orderHeader.isDeletable());

        // 4. 设置可流转状态
        response.setAvailableStatusTransitions(getAvailableStatusTransitions(orderId));

        // 5. 转换明细
        List<OrderDetailResponse.OrderItemResponse> itemResponses = orderDetails.stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    @Override
    public PageResponse<OrderResponse> queryOrders(OrderQueryRequest request) {
        log.info("查询订单列表，参数：{}", request);

        // 1. 验证查询参数
        if (!request.isValidTimeRange()) {
            throw new IllegalArgumentException("时间范围无效");
        }

        // 2. 使用 PageHelper 分页
        PageHelper.startPage(request.getPage(), request.getSize(), request.getOrderBy());

        // 3. 构建查询条件（这里简化处理，实际应根据条件动态构建SQL）
        List<OrderHeader> orderHeaders;
        if (request.hasStatusFilter() && request.hasCustomerNameFilter()) {
            // 状态+客户名查询
            orderHeaders = orderHeaderMapper.selectByStatus(request.getStatus().getCode());
            orderHeaders = orderHeaders.stream()
                    .filter(order -> order.getCustomerName().contains(request.getCustomerName()))
                    .collect(Collectors.toList());
        } else if (request.hasStatusFilter()) {
            // 仅状态查询
            orderHeaders = orderHeaderMapper.selectByStatus(request.getStatus().getCode());
        } else if (request.hasCustomerNameFilter()) {
            // 仅客户名查询
            orderHeaders = orderHeaderMapper.selectByCustomerName(request.getCustomerName());
        } else {
            // 查询所有
            orderHeaders = orderHeaderMapper.selectList(null);
        }

        // 4. 过滤已删除的订单
        orderHeaders = orderHeaders.stream()
                .filter(order -> order.getDeleted() == 0)
                .collect(Collectors.toList());

        // 5. 应用时间范围过滤
        if (request.hasTimeRangeFilter()) {
            orderHeaders = orderHeaders.stream()
                    .filter(order -> !order.getOrderTime().isBefore(request.getStartTime()) &&
                            !order.getOrderTime().isAfter(request.getEndTime()))
                    .collect(Collectors.toList());
        }

        // 6. 转换为响应对象
        List<OrderResponse> orderResponses = orderHeaders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());

        // 7. 构建分页响应
        PageInfo<OrderHeader> pageInfo = new PageInfo<>(orderHeaders);
        PageResponse<OrderResponse> pageResponse = new PageResponse<>();
        pageResponse.setPage(request.getPage());
        pageResponse.setSize(request.getSize());
        pageResponse.setTotal(pageInfo.getTotal());
        pageResponse.setTotalPages(pageInfo.getPages());
        pageResponse.setData(orderResponses);

        return pageResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("变更订单状态，订单ID：{}，新状态：{}", orderId, newStatus);

        // 1. 检查订单是否存在
        OrderHeader orderHeader = orderHeaderMapper.selectById(orderId);
        if (orderHeader == null || orderHeader.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在或已被删除");
        }

        // 2. 检查状态流转是否允许
        if (!orderHeader.canChangeStatus(newStatus)) {
            throw new IllegalStateException("不允许的状态流转");
        }

        // 3. 更新状态
        orderHeader.setStatus(newStatus);
        orderHeader.setUpdateTime(LocalDateTime.now());
        orderHeaderMapper.updateStatus(orderId, newStatus.getCode());

        log.info("订单状态变更成功，订单ID：{}，旧状态：{}，新状态：{}", 
                orderId, orderHeader.getStatus(), newStatus);
    }

    @Override
    public List<OrderStatus> getAvailableStatusTransitions(Long orderId) {
        OrderHeader orderHeader = orderHeaderMapper.selectById(orderId);
        if (orderHeader == null || orderHeader.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在或已被删除");
        }
        return List.of(orderHeader.getStatus().getAvailableTransitions());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteOrders(List<Long> orderIds) {
        log.info("批量删除订单，订单ID列表：{}", orderIds);

        for (Long orderId : orderIds) {
            try {
                deleteOrder(orderId);
            } catch (Exception e) {
                log.error("删除订单失败，订单ID：{}，错误：{}", orderId, e.getMessage());
                // 继续处理其他订单
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreOrder(Long orderId) {
        log.info("恢复订单，订单ID：{}", orderId);

        OrderHeader orderHeader = orderHeaderMapper.selectById(orderId);
        if (orderHeader == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (orderHeader.getDeleted() == 0) {
            throw new IllegalStateException("订单未被删除");
        }

        orderHeaderMapper.restore(orderId);
        log.info("订单恢复成功，订单ID：{}", orderId);
    }

    @Override
    public Object getOrderStatistics() {
        log.info("获取订单统计信息");

        // 1. 获取各状态订单数量
        List<Map<String, Object>> statusCounts = orderHeaderMapper.countByStatus();

        // 2. 获取总统计
        Map<String, Object> summary = orderHeaderMapper.getSummaryStatistics();

        // 3. 构建响应
        OrderResponse.Statistics statistics = new OrderResponse.Statistics();
        statistics.setTotalOrders(((Number) summary.get("total_orders")).longValue());
        statistics.setTotalAmount(new BigDecimal(summary.get("total_amount").toString()));

        // 计算平均订单金额
        if (statistics.getTotalOrders() > 0) {
            BigDecimal avgAmount = statistics.getTotalAmount()
                    .divide(BigDecimal.valueOf(statistics.getTotalOrders()), 2, BigDecimal.ROUND_HALF_UP);
            statistics.setAvgOrderAmount(avgAmount);
        } else {
            statistics.setAvgOrderAmount(BigDecimal.ZERO);
        }

        // 设置各状态数量
        for (Map<String, Object> statusCount : statusCounts) {
            String status = (String) statusCount.get("status");
            Long count = ((Number) statusCount.get("count")).longValue();

            switch (OrderStatus.fromCode(status)) {
                case PENDING:
                    statistics.setPendingCount(count);
                    break;
                case PROCESSING:
                    statistics.setProcessingCount(count);
                    break;
                case COMPLETED:
                    statistics.setCompletedCount(count);
                    break;
                case CANCELLED:
                    statistics.setCancelledCount(count);
                    break;
            }
        }

        return statistics;
    }

    /**
     * 转换订单头为响应对象
     */
    private OrderResponse convertToOrderResponse(OrderHeader orderHeader) {
        OrderResponse response = new OrderResponse();
        response.setId(orderHeader.getId());
        response.setOrderNo(orderHeader.getOrderNo());
        response.setCustomerName(orderHeader.getCustomerName());
        response.setPhone(orderHeader.getPhone());
        response.setAddress(orderHeader.getAddress());
        response.setRemark(orderHeader.getRemark());
        response.setStatus(orderHeader.getStatus());
        response.setStatusDesc(orderHeader.getStatus().getDescription());
        response.setStatusColor(orderHeader.getStatus().getColor());
        response.setStatusIcon(orderHeader.getStatus().getIcon());
        response.setTotalItems(orderHeader.getTotalItems());
        response.setTotalAmount(orderHeader.getTotalAmount());
        response.setOrderTime(orderHeader.getOrderTime());
        response.setUpdateTime(orderHeader.getUpdateTime());
        response.setOperable(orderHeader.isEditable(), orderHeader.isDeletable());
        return response;
    }

    /**
     * 转换订单明细为响应对象
     */
    private OrderDetailResponse.OrderItemResponse convertToOrderItemResponse(OrderDetail orderDetail) {
        OrderDetailResponse.OrderItemResponse response = new OrderDetailResponse.OrderItemResponse();
        response.setId(orderDetail.getId());
        response.setProductName(orderDetail.getProductName());
        response.setQuantity(orderDetail.getQuantity());
        response.setUnitPrice(orderDetail.getUnitPrice());
        response.setSubtotal(orderDetail.getSubtotal());
        response.setSortOrder(orderDetail.getSortOrder());
        response.setCreateTime(orderDetail.getCreateTime());
        response.setUpdateTime(orderDetail.getUpdateTime());
        return response;
    }
}