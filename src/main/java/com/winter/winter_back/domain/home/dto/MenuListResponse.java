package com.winter.winter_back.domain.home.dto;

import com.winter.winter_back.domain.menu.dto.MenuCategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description    : 메뉴 리스트 응답 객체
 * fileName       : MenuListResponse
 * author         : hj kim
 * date           : 25. 8. 31.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 31.        hj kim             최초 생성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuListResponse {
    private Long storeId;
    private String storeName;
    private List<MenuCategoryResponse> categories;
}