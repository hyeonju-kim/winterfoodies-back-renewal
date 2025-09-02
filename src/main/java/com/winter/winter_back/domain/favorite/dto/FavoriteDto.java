package com.winter.winter_back.domain.favorite.dto;

import com.winter.winter_back.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * description    :
 * fileName       : FavoriteDto
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FavoriteDto extends BaseDto {
    private Long id;                    // tb_favorites.id
    private String userId;              // tb_favorites.user_id (varchar)
    private Long storeId;               // tb_favorites.store_id
    private LocalDateTime createdAt;    // tb_favorites.created_at
    private LocalDateTime updatedAt;    // tb_favorites.updated_at
    private String createdBy;           // tb_favorites.created_by
    private String updatedBy;           // tb_favorites.updated_by
    private String useYn;               // tb_favorites.use_yn

    // 조인용 매장 정보 (tb_store)
    private String storeName;           // tb_store.name
    private String storeDescription;    // tb_store.description
    private String category;            // tb_store.category
    private String phone;               // tb_store.phone
    private String address;             // tb_store.address
    private Double latitude;            // tb_store.latitude
    private Double longitude;           // tb_store.longitude
    private String status;              // tb_store.status
    private String imageUrl;            // tb_store.image_url
    private Integer minOrderAmount;     // tb_store.min_order_amount
    private Integer deliveryFee;        // tb_store.delivery_fee
    private Integer deliveryTime;       // tb_store.delivery_time
    private Long ownerId;               // tb_store.owner_id
    private String businessNumber;      // tb_store.business_number
    private Boolean isFeatured;         // tb_store.is_featured
}