package com.winter.winter_back.domain.owner.dto;

import lombok.Data;

/**
 * description    : 사장님 영업시간 - 요일 하나 (시각은 "HH:mm")
 * fileName       : OwnerHoursResponse
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Data
public class OwnerHoursResponse {
    private String dayOfWeek;   // MONDAY ~ SUNDAY
    private String openTime;    // 예) 10:00 (휴무면 null)
    private String closeTime;   // 예) 22:00 (휴무면 null)
    private Boolean isHoliday;  // 휴무
}
