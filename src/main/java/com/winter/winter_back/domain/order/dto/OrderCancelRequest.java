package com.winter.winter_back.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * description    :
 * fileName       : OrderCancelRequest
 * author         : hj kim
 * date           : 25. 9. 3.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 3.         hj kim             최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelRequest {
    @NotBlank(message = "취소 사유는 필수입니다")
    private String reason;
}