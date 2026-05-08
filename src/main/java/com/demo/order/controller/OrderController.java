package com.demo.order.controller;

import com.demo.order.common.constants.OrderStatus;
import com.demo.order.dto.request.OrderCreateRequest;
import com.demo.order.dto.request.OrderQueryRequest;
import com.demo.order.dto.request.OrderUpdateRequest;
import com.demo.order.dto.response.OrderDetailResponse;
import com.demo.order.dto.response.OrderResponse;
import com.demo.order.dto.response.PageResponse;
import com.demo.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "订单管理", description = "订单的增删改查等操作")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "创建订单", description = "创建新的订单")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        log.info("收到创建订单请求：{}", request);
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "更新订单", description = "更新订单信息")
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "订单ID", required = true, example = "1")
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateRequest request) {
        log.info("收到更新订单请求，订单ID：{}，请求：{}", orderId, request);
        OrderResponse response = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "删除订单", description = "软删除订单")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "订单ID", required = true, example = "1")
            @PathVariable Long orderId) {
        log.info("收到删除订单请求，订单ID：{}", orderId);
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "获取订单的详细信息，包括明细")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @Parameter(description = "订单ID", required = true, example = "1")
            @PathVariable Long orderId) {
        log.info("收到获取订单详情请求，订单ID：{}", orderId);
        OrderDetailResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "查询订单列表", description = "分页查询订单列表，支持多条件筛选")
    public ResponseEntity<PageResponse<OrderResponse>> queryOrders(
            @Valid OrderQueryRequest request) {
        log.info("收到查询订单列表请求：{}", request);
        PageResponse<OrderResponse> response = orderService.queryOrders(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "变更订单状态", description = "变更订单的状态")
    public ResponseEntity<Void> changeOrderStatus(
            @Parameter(description = "订单ID", required = true, example = "1")
            @PathVariable Long orderId,
            @Parameter(description = "新状态", required = true, example = "processing")
            @RequestParam OrderStatus newStatus) {
        log.info("收到变更订单状态请求，订单ID：{}，新状态：{}", orderId, newStatus);
        orderService.changeOrderStatus(orderId, newStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/available-status")
    @Operation(summary = "获取可流转状态", description = "获取订单可流转到的状态列表")
    public ResponseEntity<List<OrderStatus>> getAvailableStatusTransitions(
            @Parameter(description = "订单ID", required = true, example = "1")
            @PathVariable Long orderId) {
        log.info("收到获取可流转状态请求，订单ID：{}", orderId);
        List<OrderStatus> transitions = orderService.getAvailableStatusTransitions(orderId);
        return ResponseEntity.ok(transitions);
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除订单", description = "批量软删除多个订单")
    public ResponseEntity<Void> batchDeleteOrders(
            @Parameter(description = "订单ID列表", required = true)
            @RequestBody List<Long> orderIds) {
        log.info("收到批量删除订单请求，订单ID列表：{}", orderIds);
        orderService.batchDeleteOrders(orderIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/restore")
    @Operation(summary = "恢复订单", description = "恢复已软删除的订单")
    public ResponseEntity<Void> restoreOrder(
            @Parameter(description = "订单ID", required = true, example = "1")
            @PathVariable Long orderId) {
        log.info("收到恢复订单请求，订单ID：{}", orderId);
        orderService.restoreOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计", description = "获取订单的统计信息")
    public ResponseEntity<Object> getOrderStatistics() {
        log.info("收到获取订单统计请求");
        Object statistics = orderService.getOrderStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "服务健康检查接口")
    public ResponseEntity<String> healthCheck() {
        log.debug("健康检查请求");
        return ResponseEntity.ok("Order Service is healthy");
    }

    @GetMapping("/test")
    @Operation(summary = "测试接口", description = "用于测试的连接接口")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Order Service API is working");
    }
}