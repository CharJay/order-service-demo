package com.demo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.order.entity.OrderHeader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单主表 Mapper 接口
 */
@Mapper
public interface OrderHeaderMapper extends BaseMapper<OrderHeader> {

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM order_header WHERE order_no = #{orderNo} AND deleted = 0")
    OrderHeader selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据客户姓名模糊查询
     */
    @Select("SELECT * FROM order_header WHERE customer_name LIKE CONCAT('%', #{customerName}, '%') AND deleted = 0 ORDER BY order_time DESC")
    List<OrderHeader> selectByCustomerName(@Param("customerName") String customerName);

    /**
     * 根据状态查询订单列表
     */
    @Select("SELECT * FROM order_header WHERE status = #{status} AND deleted = 0 ORDER BY order_time DESC")
    List<OrderHeader> selectByStatus(@Param("status") String status);

    /**
     * 查询指定时间范围内的订单
     */
    @Select("SELECT * FROM order_header WHERE order_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY order_time DESC")
    List<OrderHeader> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 更新订单统计信息
     */
    @Select("UPDATE order_header SET total_items = #{totalItems}, total_amount = #{totalAmount}, update_time = NOW() WHERE id = #{orderId}")
    int updateStatistics(@Param("orderId") Long orderId, 
                         @Param("totalItems") Integer totalItems, 
                         @Param("totalAmount") BigDecimal totalAmount);

    /**
     * 软删除订单
     */
    @Select("UPDATE order_header SET deleted = 1, delete_time = NOW(), update_time = NOW() WHERE id = #{orderId}")
    int softDelete(@Param("orderId") Long orderId);

    /**
     * 恢复软删除的订单
     */
    @Select("UPDATE order_header SET deleted = 0, delete_time = NULL, update_time = NOW() WHERE id = #{orderId}")
    int restore(@Param("orderId") Long orderId);

    /**
     * 更新订单状态
     */
    @Select("UPDATE order_header SET status = #{status}, update_time = NOW() WHERE id = #{orderId} AND deleted = 0")
    int updateStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /**
     * 统计各状态订单数量
     */
    @Select("SELECT status, COUNT(*) as count FROM order_header WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计总订单数和总金额
     */
    @Select("SELECT COUNT(*) as total_orders, COALESCE(SUM(total_amount), 0) as total_amount FROM order_header WHERE deleted = 0")
    Map<String, Object> getSummaryStatistics();

    /**
     * 获取最近N天的订单统计
     */
    @Select("SELECT DATE(order_time) as date, COUNT(*) as order_count, COALESCE(SUM(total_amount), 0) as total_amount " +
            "FROM order_header WHERE deleted = 0 AND order_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(order_time) ORDER BY date DESC")
    List<Map<String, Object>> getRecentStatistics(@Param("days") Integer days);

    /**
     * 检查订单是否存在且未删除
     */
    @Select("SELECT COUNT(*) FROM order_header WHERE id = #{orderId} AND deleted = 0")
    int existsAndNotDeleted(@Param("orderId") Long orderId);

    /**
     * 检查订单号是否已存在
     */
    @Select("SELECT COUNT(*) FROM order_header WHERE order_no = #{orderNo} AND deleted = 0")
    int existsByOrderNo(@Param("orderNo") String orderNo);
}