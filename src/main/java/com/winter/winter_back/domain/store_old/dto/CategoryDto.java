package com.winter.winter_back.domain.store_old.dto;

import lombok.Data;

/**
 * description    :
 * fileName       : CategoryDto
 * author         : hyeonju
 * date           : 25. 8. 25.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 25.        hyeonju             최초 생성
 */
@Data
public class CategoryDto {
    private Long categoryId;   // 카테고리 PK
    private String categoryCode; // 카테고리 코드 (BUNGEOPPANG, EOMUK ...)
    private String categoryName; // 카테고리명
}
