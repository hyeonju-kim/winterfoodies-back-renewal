package com.winter.winter_back.domain.order.dao;

import com.winter.winter_back.domain.order.dto.OrderHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : OrderHistoryDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */

@Mapper
public interface OrderHistoryDao {

    void insertOrderHistory(OrderHistoryDto orderHistoryDto);

    List<OrderHistoryDto> selectOrderHistories(@Param("orderId") String orderId);

    OrderHistoryDto selectLatestOrderHistory(@Param("orderId") String orderId);

    void deleteOrderHistories(@Param("orderId") String orderId);
}