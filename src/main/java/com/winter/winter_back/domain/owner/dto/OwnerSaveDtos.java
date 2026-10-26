package com.winter.winter_back.domain.owner.dto;

import lombok.Builder;
import lombok.Data;

/**
 * description    : 사장님 메뉴·메뉴 카테고리 저장용 DTO (INSERT 후 생성된 ID 를 돌려받는다)
 * fileName       : OwnerSaveDtos
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
public final class OwnerSaveDtos {

    private OwnerSaveDtos() {
    }

    /** 메뉴 저장 */
    @Data
    @Builder
    public static class MenuSave {
        private Long menuId;       // INSERT 후 채워진다
        private Long storeId;
        private Long categoryId;
        private String name;
        private String description;
        private Integer price;
    }

    /** 메뉴 카테고리 저장 (없는 이름이면 새로 만든다) */
    @Data
    @Builder
    public static class MenuCategorySave {
        private Long categoryId;   // INSERT 후 채워진다
        private Long storeId;
        private String name;
    }
}
