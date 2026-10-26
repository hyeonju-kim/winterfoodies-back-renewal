package com.winter.winter_back.domain.favorite.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * description    :
 * fileName       : FavoriteRequest
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
public class FavoriteRequest {
    @NotNull(message = "매장 ID는 필수입니다.")
    private Long storeId;
}