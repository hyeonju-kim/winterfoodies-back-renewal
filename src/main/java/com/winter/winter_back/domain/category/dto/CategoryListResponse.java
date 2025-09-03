package com.winter.winter_back.domain.category.dto;

import com.winter.winter_back.common.dto.ListResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * description    :
 * fileName       : CategoryListResponse
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryListResponse extends ListResponseDto<CategoryDto> {

    public CategoryListResponse(List<CategoryDto> categories) {
        this.setTotalCount(categories.size());
        this.setDataList(categories);
    }
}