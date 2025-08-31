package com.winter.winter_back.domain.product_old.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * description    : 음식 카테고리
 * fileName       : CategoryEnum
 * author         : hyeonju
 * date           : 25. 8. 25.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 25.        hyeonju             최초 생성
 */
@Getter
@RequiredArgsConstructor
public enum CategoryEnum {
    BUNGEOPPANG("붕어빵", "fish_bread.png"),
    EOMUK("어묵", "fish_cake.png"),
    GUNBAP("군밤", "roasted_chestnut.png"),
    HODDEOK("호떡", "sweet_pancake.png"),
    GYERANPPANG("계란빵", "egg_bread.png"),
    GUNGOGUMA("군고구마", "roasted_sweet_potato.png"),
    TAKOYAKI("다코야키", "takoyaki.png"),
    HODUGWAJA("호두과자", "walnut_cookie.png"),
    GUKHWAPPANG("국화빵", "chrysanthemum_bread.png");

    private final String displayName;  // 한글 이름
    private final String imageFile;    // 이미지 파일명 (또는 static 리소스 경로)
}
