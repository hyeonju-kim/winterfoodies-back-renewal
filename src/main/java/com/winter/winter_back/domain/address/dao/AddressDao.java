package com.winter.winter_back.domain.address.dao;

import com.winter.winter_back.domain.address.dto.AddressDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : AddressDao
 * author         : hyeonju
 * date           : 25. 8. 30.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 30.        hyeonju             최초 생성
 */

@Mapper
public interface AddressDao {

    /**
     * 사용자 주소 목록 조회
     */
    List<AddressDto> getAddressList(@Param("userId") String userId);

    /**
     * 주소 상세 조회
     */
    AddressDto getAddress(@Param("addressId") Long addressId, @Param("userId") String userId);

    /**
     * 주소 등록
     */
    int regAddress(AddressDto addressDto);

    /**
     * 주소 수정
     */
    int modAddress(AddressDto addressDto);

    /**
     * 주소 삭제
     */
    int delAddress(@Param("addressId") Long addressId, @Param("userId") String userId);

    /**
     * 기본 주소 해제 (모든 주소의 is_default를 N으로 변경)
     */
    int modAllAddressDefaultN(@Param("userId") String userId);

    /**
     * 기본 주소 설정
     */
    int modAddressDefaultY(@Param("addressId") Long addressId, @Param("userId") String userId);

    /**
     * 사용자의 주소 개수 조회
     */
    int getAddressCount(@Param("userId") String userId);

}