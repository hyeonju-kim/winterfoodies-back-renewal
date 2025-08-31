package com.winter.winter_back.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * description    : 매장 시간용 DTO
 * fileName       : StoreHoursDto
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
public class StoreHoursDto {
    private Long id;
    private Long storeId;
    private String dayOfWeek; // MONDAY, TUESDAY, ...
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isHoliday;
}