package com.winter.winter_back.domain.cart.dao;

import com.winter.winter_back.domain.cart.dto.CartDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * description    :
 * fileName       : CartDao
 * author         : hj kim
 * date           : 25. 8. 29.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 29.        hj kim             최초 생성
 */
@Mapper
public interface CartDao {
    CartDto findById(Long id);
    CartDto findByUserId(String userId);
    Long insert(CartDto cart);
    void updateUpdatedAt(Long cartId);
    void deleteById(Long id);
    void deleteByUserId(String userId);
}