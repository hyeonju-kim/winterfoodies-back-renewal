package com.winter.winter_back.domain.store.dto;

import com.winter.winter_back.common.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * description    : 매장 DTO
 * fileName       : StoreDto
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDto extends BaseDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private String status; // OPEN, CLOSED, TEMPORARILY_CLOSED
    private String imageUrl;
    private String category;
    private Integer minOrderAmount;
    private Integer deliveryFee;
    private Integer deliveryTime; // 분 단위
    private Double averageRating;
    private Long reviewCount;
    private Integer orderCount;
    private Boolean isOpen;
    private Double distance; // km
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}