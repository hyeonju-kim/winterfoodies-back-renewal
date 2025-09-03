package com.winter.winter_back.domain.order.dao;

import com.winter.winter_back.domain.order.dto.OrderDetailResponse;
import com.winter.winter_back.domain.order.dto.OrderDto;
import com.winter.winter_back.domain.order.dto.OrderListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : OrderDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */

@Mapper
public interface OrderDao {

    void insertOrder(OrderDto orderDto);

    OrderDto selectOrderById(@Param("orderId") String orderId);

    List<OrderListResponse> selectOrderList(@Param("userId") String userId,
                                            @Param("status") String status,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    int selectOrderCount(@Param("userId") String userId, @Param("status") String status);

    OrderDetailResponse selectOrderDetail(@Param("orderId") String orderId);

    void updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status);

    void updateOrder(OrderDto orderDto);

    void deleteOrder(@Param("orderId") String orderId);
}