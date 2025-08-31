package com.winter.winter_back.domain.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * description    : 메뉴 옵션 그룹 DTO
 * fileName       : MenuOptionGroupDto
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
public class MenuOptionGroupDto {
    private Long id;
    private Long menuId;
    private String name;
    private Boolean isRequired;
    private Integer maxSelectCount;
    private Integer minSelectCount;
    private Integer sortOrder;
    private List<MenuOptionDto> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}