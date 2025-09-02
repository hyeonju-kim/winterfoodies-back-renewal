package com.winter.winter_back.domain.cart.dao;

import com.winter.winter_back.domain.cart.dto.CartItemDto;
import com.winter.winter_back.domain.cart.dto.CartItemOptionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : CartItemDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Mapper
public interface CartItemDao {
    CartItemDto findById(Long id);
    List<CartItemDto> findByCartId(Long cartId);
    List<CartItemOptionDto> findOptionsByCartItemId(Long cartItemId);
    Long insert(CartItemDto cartItem);
    void insertOption(CartItemOptionDto cartItemOption);
    void updateQuantityAndPrice(@Param("id") Long id,
                                @Param("quantity") Integer quantity,
                                @Param("totalPrice") Integer totalPrice);
    void deleteById(Long id);
    void deleteByCartId(Long cartId);
    void deleteOptionsByCartItemId(Long cartItemId);
}