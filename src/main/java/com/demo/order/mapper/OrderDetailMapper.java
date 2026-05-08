package com.demo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.order.entity.OrderDetail;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单明细表 Mapper 接口
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    /**
     * 根据订单ID查询明细列表
     */
    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId} ORDER BY sort_order ASC")
    List<OrderDetail> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单ID列表批量查询明细
     */
    @Select({
        "<script>",
        "SELECT * FROM order_detail WHERE order_id IN ",
        "<foreach collection='orderIds' item='orderId' open='(' separator=',' close=')'>",
        "#{orderId}",
        "</foreach>",
        " ORDER BY order_id, sort_order ASC",
        "</script>"
    })
    List<OrderDetail> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 批量插入订单明细
     */
    @Insert({
        "<script>",
        "INSERT INTO order_detail (order_id, product_name, quantity, unit_price, subtotal, sort_order, create_time, update_time) VALUES ",
        "<foreach collection='details' item='detail' separator=','>",
        "(#{detail.orderId}, #{detail.productName}, #{detail.quantity}, #{detail.unitPrice}, #{detail.subtotal}, #{detail.sortOrder}, NOW(), NOW())",
        "</foreach>",
        "</script>"
    })
    int batchInsert(@Param("details") List<OrderDetail> details);

    /**
     * 根据订单ID删除所有明细
     */
    @Delete("DELETE FROM order_detail WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单ID统计商品总数和总金额
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) as total_items, COALESCE(SUM(subtotal), 0) as total_amount FROM order_detail WHERE order_id = #{orderId}")
    OrderStatistics selectStatisticsByOrderId(@Param("orderId") Long orderId);

    /**
     * 更新订单明细的排序序号
     */
    @Update("UPDATE order_detail SET sort_order = #{sortOrder}, update_time = NOW() WHERE id = #{detailId}")
    int updateSortOrder(@Param("detailId") Long detailId, @Param("sortOrder") Integer sortOrder);

    /**
     * 根据商品名称模糊查询
     */
    @Select("SELECT * FROM order_detail WHERE product_name LIKE CONCAT('%', #{productName}, '%') ORDER BY create_time DESC")
    List<OrderDetail> selectByProductName(@Param("productName") String productName);

    /**
     * 获取订单中最贵的商品
     */
    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId} ORDER BY subtotal DESC LIMIT 1")
    OrderDetail selectMostExpensiveByOrderId(@Param("orderId") Long orderId);

    /**
     * 获取订单中最便宜的商品
     */
    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId} ORDER BY subtotal ASC LIMIT 1")
    OrderDetail selectCheapestByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计接口
     */
    interface OrderStatistics {
        Integer getTotalItems();
        BigDecimal getTotalAmount();
    }

    /**
     * 检查订单明细是否存在
     */
    @Select("SELECT COUNT(*) FROM order_detail WHERE id = #{detailId} AND order_id = #{orderId}")
    int existsByIdAndOrderId(@Param("detailId") Long detailId, @Param("orderId") Long orderId);

    /**
     * 更新商品信息
     */
    @Update("UPDATE order_detail SET product_name = #{productName}, quantity = #{quantity}, unit_price = #{unitPrice}, subtotal = #{subtotal}, update_time = NOW() WHERE id = #{detailId}")
    int updateProductInfo(@Param("detailId") Long detailId, 
                          @Param("productName") String productName,
                          @Param("quantity") Integer quantity,
                          @Param("unitPrice") BigDecimal unitPrice,
                          @Param("subtotal") BigDecimal subtotal);
}