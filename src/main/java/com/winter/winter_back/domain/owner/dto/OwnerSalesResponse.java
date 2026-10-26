package com.winter.winter_back.domain.owner.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * description    : 사장님 매출 - 기간(일별·주별·월별) 매출 추이와 인기 메뉴. 매출은 받아간(DELIVERED) 주문 기준
 * fileName       : OwnerSalesResponse
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Data
@Builder
public class OwnerSalesResponse {
    private String period;              // DAY / WEEK / MONTH
    private LocalDate fromDate;         // 집계 시작일
    private LocalDate toDate;           // 집계 마지막 날(오늘)
    private BigDecimal totalSales;      // 기간 매출 합계
    private Integer orderCount;         // 기간 받아간 주문 수
    private Integer cancelCount;        // 기간 취소 주문 수
    private List<SalesPoint> points;    // 날짜·주·달별 매출 (오래된 것부터, 매출 없는 칸도 0 으로)
    private List<TopMenu> topMenus;     // 기간 많이 팔린 메뉴 (최대 5개)

    /** 매출 추이 한 칸 */
    @Data
    @Builder
    public static class SalesPoint {
        private String label;           // 예) 10/26(월), 10/20 주, 10월
        private LocalDate startDate;    // 그 칸의 첫날
        private BigDecimal sales;
        private Integer orderCount;
    }

    /** 인기 메뉴 */
    @Data
    public static class TopMenu {
        private String menuName;
        private Integer quantity;       // 팔린 개수
        private BigDecimal sales;       // 그 메뉴 매출 (단가 x 수량)
    }

    /** 날짜별 매출 (쿼리 결과, 서비스에서 주·달로 묶는다) */
    @Data
    public static class DailySales {
        private LocalDate salesDate;
        private BigDecimal sales;
        private Integer orderCount;
    }
}
