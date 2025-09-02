package com.winter.winter_back.domain.favorite.dto;

import com.winter.winter_back.common.dto.ListResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * description    :
 * fileName       : FavoriteListResponse
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FavoriteListResponse extends ListResponseDto<FavoriteResponse> {
}