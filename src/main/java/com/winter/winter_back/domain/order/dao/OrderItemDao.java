package com.winter.winter_back.domain.order.dao;

import com.winter.winter_back.domain.order.dto.OrderDetailResponse;
import com.winter.winter_back.domain.order.dto.OrderItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : OrderItemDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */

@Mapper
public interface OrderItemDao {

    void insertOrderItem(OrderItemDto orderItemDto);

    List<OrderDetailResponse.OrderItem> selectOrderItems(@Param("orderId") String orderId);

    List<OrderItemDto> selectOrderItemsByOrderId(@Param("orderId") String orderId);

    void updateOrderItem(OrderItemDto orderItemDto);

    void deleteOrderItem(@Param("orderItemId") Long orderItemId);

    void deleteOrderItemsByOrderId(@Param("orderId") String orderId);
}