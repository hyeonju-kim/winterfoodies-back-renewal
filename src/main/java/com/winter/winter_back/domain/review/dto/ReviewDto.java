package com.winter.winter_back.domain.review.dto;

import com.winter.winter_back.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * description    :
 * fileName       : ReviewDto
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewDto extends BaseDto {
    private Long idx;                       // tb_review.idx (PK)
    private Long storeId;                   // tb_review.store_id
    private String userId;                    // tb_review.user_id
    private String orderOrderId;              // tb_review.order_order_id
    private Long rating;                    // tb_review.rating (1~5)
    private String images;                  // tb_review.images
    private String content;                 // tb_review.content
    private LocalDateTime createdDatetime;  // tb_review.created_datetime
    private LocalDateTime modifiedDatetime; // tb_review.modified_datetime
    private String replyContent;            // tb_review.reply_content (사장님 답글)
    private LocalDateTime replyDatetime;    // tb_review.reply_datetime

    // 조인용 매장 정보
    private String storeName;               // tb_store.name
    private String storeCategory;           // tb_store.category
    private String storeImageUrl;           // tb_store.image_url

    // 조인용 사용자 정보 (필요 시)
    private String userName;                // tb_user.name
}
