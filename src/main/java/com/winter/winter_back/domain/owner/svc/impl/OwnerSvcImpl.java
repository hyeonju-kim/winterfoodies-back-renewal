package com.winter.winter_back.domain.owner.svc.impl;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.order.dao.OrderHistoryDao;
import com.winter.winter_back.domain.order.dto.OrderHistoryDto;
import com.winter.winter_back.domain.order.dto.OrderStatus;
import com.winter.winter_back.domain.owner.dao.OwnerDao;
import com.winter.winter_back.domain.owner.dto.OwnerHoursResponse;
import com.winter.winter_back.domain.owner.dto.OwnerMenuResponse;
import com.winter.winter_back.domain.owner.dto.OwnerOrderResponse;
import com.winter.winter_back.domain.owner.dto.OwnerRequests;
import com.winter.winter_back.domain.owner.dto.OwnerSalesResponse;
import com.winter.winter_back.domain.owner.dto.OwnerSaveDtos;
import com.winter.winter_back.domain.owner.dto.OwnerStoreResponse;
import com.winter.winter_back.domain.owner.svc.OwnerSvc;
import com.winter.winter_back.domain.store.dao.StoreDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * description    : 사장님 서비스 구현 - 영업 상태·대기시간, 주문 단계 처리, 메뉴 품절
 * fileName       : OwnerSvcImpl
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 * 26. 10. 26.       hj kim             메뉴 추가·수정·삭제, 가게 정보 수정, 영업시간 조회·수정 추가
 * 26. 10. 26.       hj kim             매출 조회(일별·주별·월별, 인기 메뉴) 추가
 * 26. 10. 26.       hj kim             픽업 완료 시 가게 통계(받아간 주문 수) 다시 계산
 * 26. 10. 26.       hj kim             가게 등록 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerSvcImpl implements OwnerSvc {

    private static final Set<String> STORE_STATUSES = Set.of("OPEN", "TEMPORARILY_CLOSED", "CLOSED");
    private static final int MAX_LIMIT = 50;
    private static final String DEFAULT_MENU_CATEGORY = "기본";
    private static final int DEFAULT_WAIT_MINUTES = 10;
    private static final String DEFAULT_OPEN_TIME = "10:00";
    private static final String DEFAULT_CLOSE_TIME = "21:00";
    // 간식 종류 코드 → 한글 이름 (앱 홈 카테고리와 같은 목록)
    private static final Map<String, String> STORE_CATEGORIES = Map.of(
            "BUNGEOPPANG", "붕어빵", "HOTTEOK", "호떡", "EOMUK", "어묵", "GUNBAM", "군밤",
            "GUNGOGUMA", "군고구마", "GYERANPPANG", "계란빵", "TAKOYAKI", "타코야키",
            "HODUGWAJA", "호두과자", "GUKHWAPPANG", "국화빵", "TTEOKBOKKI", "떡볶이");
    private static final int TOP_MENU_COUNT = 5;

    // 주문 단계는 이 순서로만 바뀐다: 접수 → 확인 → 조리 → 픽업대기 → 픽업완료, 확인 단계까지는 거절(취소) 가능
    private static final Map<OrderStatus, Set<OrderStatus>> NEXT_STATUSES = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.DELIVERING),
            OrderStatus.DELIVERING, Set.of(OrderStatus.DELIVERED));

    // 손님 화면 진행 상황에 쌓이는 이력 문구 (포장 주문 기준)
    private static final Map<OrderStatus, String> HISTORY_DESCRIPTIONS = Map.of(
            OrderStatus.CONFIRMED, "가게에서 주문을 확인했습니다.",
            OrderStatus.PREPARING, "조리를 시작했습니다.",
            OrderStatus.DELIVERING, "준비가 끝났어요. 가게에서 받아가세요.",
            OrderStatus.DELIVERED, "픽업이 완료되었습니다.",
            OrderStatus.CANCELLED, "가게에서 주문을 거절했습니다.");

    private final OwnerDao ownerDao;
    private final OrderHistoryDao orderHistoryDao;
    private final StoreDao storeDao;

    @Override
    @Transactional(readOnly = true)
    public OwnerStoreResponse getMyStore(String userId) {
        return ownerDao.getOwnerStore(getStoreId(userId));
    }

    /**
     * Author      : hj kim
     * Description : 가게 등록. 사장님 한 명당 한 가게. 영업 종료 상태로 만들고
     *               기본 영업시간(매일 10:00~21:00)·통계 행·검색용 간식 종류(한글)를 같이 만든다
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerStoreResponse regStore(String userId, OwnerRequests.StoreRegisterRequest request) {
        if (ownerDao.getOwnerStoreId(userId) != null) {
            throw new BizException("owner_store_exists");
        }
        String categoryName = STORE_CATEGORIES.get(request.getCategory());
        if (categoryName == null) {
            throw new BizException("invalid_store_category");
        }

        OwnerSaveDtos.StoreSave store = OwnerSaveDtos.StoreSave.builder()
                .ownerId(userId)
                .name(request.getName().trim())
                .category(request.getCategory())
                .description(request.getDescription() == null || request.getDescription().isBlank() ? null : request.getDescription().trim())
                .phone(request.getPhone() == null || request.getPhone().isBlank() ? null : request.getPhone())
                .address(request.getAddress().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .minOrderAmount(request.getMinOrderAmount() == null ? 0 : request.getMinOrderAmount())
                .waitMinutes(DEFAULT_WAIT_MINUTES)
                .build();
        ownerDao.regStore(store);
        Long storeId = store.getStoreId();

        ownerDao.regStoreCategory(storeId, request.getCategory(), categoryName);
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            OwnerRequests.DayHours day = new OwnerRequests.DayHours();
            day.setDayOfWeek(dayOfWeek.name());
            day.setOpenTime(DEFAULT_OPEN_TIME);
            day.setCloseTime(DEFAULT_CLOSE_TIME);
            day.setIsHoliday(false);
            ownerDao.mergeStoreHours(storeId, day);
        }
        storeDao.mergeStoreStats(storeId);
        log.info("[regStore] 사장님 {} 가게 등록 : {} ({})", userId, storeId, store.getName());

        return ownerDao.getOwnerStore(storeId);
    }

    /**
     * Author      : hj kim
     * Description : 영업 상태 변경. 영업중이 아니면 손님 주문이 거부된다(주문 서비스의 restaurant_closed)
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerStoreResponse modStoreStatus(String userId, OwnerRequests.StoreStatusRequest request) {
        if (!STORE_STATUSES.contains(request.getStatus())) {
            throw new BizException("invalid_store_status");
        }
        Long storeId = getStoreId(userId);
        ownerDao.modStoreStatus(storeId, request.getStatus());
        return ownerDao.getOwnerStore(storeId);
    }

    /**
     * Author      : hj kim
     * Description : 대기시간 변경. 손님 화면의 "조리 약 N분"(tb_store.delivery_time)으로 바로 보인다
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerStoreResponse modWaitTime(String userId, OwnerRequests.WaitTimeRequest request) {
        Long storeId = getStoreId(userId);
        ownerDao.modStoreWaitTime(storeId, request.getMinutes());
        return ownerDao.getOwnerStore(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponseDto<OwnerOrderResponse> getOrderList(String userId, List<String> statuses, int page, int limit) {
        Long storeId = getStoreId(userId);
        List<String> statusFilter = statuses == null || statuses.isEmpty() ? null : statuses;
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        int offset = (Math.max(page, 1) - 1) * safeLimit;

        ListResponseDto<OwnerOrderResponse> response = new ListResponseDto<>();
        response.setDataList(ownerDao.getOwnerOrderList(storeId, statusFilter, offset, safeLimit));
        response.setTotalCount(ownerDao.getOwnerOrderCount(storeId, statusFilter));
        return response;
    }

    /**
     * Author      : hj kim
     * Description : 주문 단계 변경. 정해진 다음 단계로만 바꿀 수 있고, 거절은 사유가 필요하다.
     *               손님이 같은 순간 취소하는 경우를 막기 위해 "지금 상태가 확인한 상태일 때만" 바꾸고(0건이면 거부),
     *               바뀌면 손님 진행 상황에 보일 이력(ORDER_상태)을 남긴다
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerOrderResponse modOrderStatus(String userId, String orderId, OwnerRequests.OrderStatusChangeRequest request) {
        Long storeId = getStoreId(userId);
        OwnerOrderResponse order = ownerDao.getOwnerOrder(storeId, orderId);
        if (order == null) {
            // 다른 가게 주문도 "없는 주문"으로 응답 (주문번호 존재 여부를 알려주지 않는다)
            throw new BizException("order_not_found");
        }

        OrderStatus current = OrderStatus.from(order.getStatus());
        OrderStatus next = OrderStatus.from(request.getStatus());
        if (current == null || next == null || !NEXT_STATUSES.getOrDefault(current, Set.of()).contains(next)) {
            throw new BizException("invalid_order_status_change");
        }
        String reason = request.getReason() == null ? "" : request.getReason().trim();
        if (next == OrderStatus.CANCELLED && reason.isEmpty()) {
            throw new BizException("reject_reason_required");
        }

        if (ownerDao.modOrderStatus(storeId, orderId, current.name(), next.name()) == 0) {
            throw new BizException("invalid_order_status_change");
        }

        String description = HISTORY_DESCRIPTIONS.get(next) + (reason.isEmpty() ? "" : " 사유: " + reason);
        orderHistoryDao.insertOrderHistory(OrderHistoryDto.builder()
                .orderId(orderId)
                .status("ORDER_" + next.name())
                .description(description)
                .createdBy(userId)
                .createdDatetime(LocalDateTime.now())
                .build());
        if (next == OrderStatus.DELIVERED) {
            // 받아간 주문 수 = "주문 많은 순" 정렬 기준
            storeDao.mergeStoreStats(storeId);
        }
        log.info("[modOrderStatus] 가게 {} 주문 {} : {} → {}", storeId, orderId, current, next);

        return ownerDao.getOwnerOrder(storeId, orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerMenuResponse> getMenuList(String userId) {
        return ownerDao.getOwnerMenuList(getStoreId(userId));
    }

    @Override
    public void modMenuSoldOut(String userId, Long menuId, OwnerRequests.SoldOutRequest request) {
        if (ownerDao.modMenuSoldOut(getStoreId(userId), menuId, request.getIsSoldOut()) == 0) {
            throw new BizException("menu_not_found");
        }
    }

    /**
     * Author      : hj kim
     * Description : 메뉴 추가. 카테고리는 이름으로 찾고 없으면 새로 만든다(비우면 "기본")
     * Date        : 26. 10. 26.
     */
    @Override
    public OwnerMenuResponse regMenu(String userId, OwnerRequests.MenuRequest request) {
        Long storeId = getStoreId(userId);
        OwnerSaveDtos.MenuSave menu = toMenuSave(storeId, request);
        ownerDao.regMenu(menu);
        return ownerDao.getOwnerMenu(storeId, menu.getMenuId());
    }

    @Override
    public OwnerMenuResponse modMenu(String userId, Long menuId, OwnerRequests.MenuRequest request) {
        Long storeId = getStoreId(userId);
        OwnerSaveDtos.MenuSave menu = toMenuSave(storeId, request);
        menu.setMenuId(menuId);
        if (ownerDao.modMenu(menu) == 0) {
            throw new BizException("menu_not_found");
        }
        return ownerDao.getOwnerMenu(storeId, menuId);
    }

    /**
     * Author      : hj kim
     * Description : 메뉴 삭제. 지난 주문·리뷰가 메뉴를 참조해서 지우지 않고 사용 안 함(is_active = 0)으로 숨긴다
     * Date        : 26. 10. 26.
     */
    @Override
    public void delMenu(String userId, Long menuId) {
        if (ownerDao.delMenu(getStoreId(userId), menuId) == 0) {
            throw new BizException("menu_not_found");
        }
    }

    @Override
    public OwnerStoreResponse modStoreInfo(String userId, OwnerRequests.StoreInfoRequest request) {
        Long storeId = getStoreId(userId);
        request.setName(request.getName().trim());
        if (request.getPhone() != null && request.getPhone().isBlank()) {
            request.setPhone(null);
        }
        ownerDao.modStoreInfo(storeId, request);
        return ownerDao.getOwnerStore(storeId);
    }

    /**
     * Author      : hj kim
     * Description : 영업시간 월~일 7개. 아직 저장하지 않은 요일은 시각 없이(휴무 아님) 돌려준다
     * Date        : 26. 10. 26.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OwnerHoursResponse> getHours(String userId) {
        List<OwnerHoursResponse> saved = ownerDao.getStoreHours(getStoreId(userId));
        List<OwnerHoursResponse> week = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            OwnerHoursResponse hours = saved.stream()
                    .filter(item -> day.name().equals(item.getDayOfWeek()))
                    .findFirst()
                    .orElseGet(() -> {
                        OwnerHoursResponse empty = new OwnerHoursResponse();
                        empty.setDayOfWeek(day.name());
                        empty.setIsHoliday(false);
                        return empty;
                    });
            week.add(hours);
        }
        return week;
    }

    /**
     * Author      : hj kim
     * Description : 영업시간 수정. 요일은 MONDAY~SUNDAY 중복 없이, 휴무가 아니면 여는·닫는 시각 필수이고 같으면 안 된다
     *               (닫는 시각이 여는 시각보다 이르면 자정을 넘겨 영업하는 것으로 본다). 요일마다 있으면 수정, 없으면 추가
     * Date        : 26. 10. 26.
     */
    @Override
    public List<OwnerHoursResponse> modHours(String userId, OwnerRequests.HoursRequest request) {
        Long storeId = getStoreId(userId);
        Set<String> seenDays = new HashSet<>();
        for (OwnerRequests.DayHours day : request.getDays()) {
            boolean isValidDay = Arrays.stream(DayOfWeek.values()).anyMatch(value -> value.name().equals(day.getDayOfWeek()));
            boolean isOpenDay = !Boolean.TRUE.equals(day.getIsHoliday());
            boolean hasValidTimes = day.getOpenTime() != null && day.getCloseTime() != null
                    && !day.getOpenTime().equals(day.getCloseTime());
            if (!isValidDay || !seenDays.add(day.getDayOfWeek()) || (isOpenDay && !hasValidTimes)) {
                throw new BizException("invalid_business_hours");
            }
            if (!isOpenDay) {
                day.setOpenTime(null);
                day.setCloseTime(null);
            }
        }
        for (OwnerRequests.DayHours day : request.getDays()) {
            ownerDao.mergeStoreHours(storeId, day);
        }
        return getHours(userId);
    }

    private OwnerSaveDtos.MenuSave toMenuSave(Long storeId, OwnerRequests.MenuRequest request) {
        String categoryName = request.getCategoryName() == null || request.getCategoryName().isBlank()
                ? DEFAULT_MENU_CATEGORY
                : request.getCategoryName().trim();
        Long categoryId = ownerDao.getMenuCategoryId(storeId, categoryName);
        if (categoryId == null) {
            OwnerSaveDtos.MenuCategorySave category = OwnerSaveDtos.MenuCategorySave.builder()
                    .storeId(storeId)
                    .name(categoryName)
                    .build();
            ownerDao.regMenuCategory(category);
            categoryId = category.getCategoryId();
        }
        String description = request.getDescription() == null || request.getDescription().isBlank()
                ? null
                : request.getDescription().trim();
        return OwnerSaveDtos.MenuSave.builder()
                .storeId(storeId)
                .categoryId(categoryId)
                .name(request.getName().trim())
                .description(description)
                .price(request.getPrice())
                .build();
    }

    /**
     * Author      : hj kim
     * Description : 매출. 받아간(DELIVERED) 주문을 날짜별로 가져와 기간 단위로 묶는다.
     *               DAY = 오늘 포함 최근 7일, WEEK = 이번 주(월요일 시작) 포함 최근 8주, MONTH = 이번 달 포함 최근 6개월.
     *               매출이 없는 칸도 0 으로 채워 그래프가 비지 않게 한다
     * Date        : 26. 10. 26.
     */
    @Override
    @Transactional(readOnly = true)
    public OwnerSalesResponse getSales(String userId, String period) {
        Long storeId = getStoreId(userId);
        String unit = period == null ? "DAY" : period.toUpperCase();
        LocalDate today = LocalDate.now();

        List<LocalDate> starts = new ArrayList<>();
        switch (unit) {
            case "DAY" -> {
                for (int back = 6; back >= 0; back--) {
                    starts.add(today.minusDays(back));
                }
            }
            case "WEEK" -> {
                LocalDate thisWeek = today.with(DayOfWeek.MONDAY);
                for (int back = 7; back >= 0; back--) {
                    starts.add(thisWeek.minusWeeks(back));
                }
            }
            case "MONTH" -> {
                LocalDate thisMonth = today.withDayOfMonth(1);
                for (int back = 5; back >= 0; back--) {
                    starts.add(thisMonth.minusMonths(back));
                }
            }
            default -> throw new BizException("invalid_sales_period");
        }
        LocalDate fromDate = starts.get(0);

        List<OwnerSalesResponse.DailySales> daily = ownerDao.getDailySales(storeId, fromDate, today);
        List<OwnerSalesResponse.SalesPoint> points = new ArrayList<>();
        for (int index = 0; index < starts.size(); index++) {
            LocalDate start = starts.get(index);
            LocalDate nextStart = index + 1 < starts.size() ? starts.get(index + 1) : today.plusDays(1);
            BigDecimal sales = BigDecimal.ZERO;
            int orderCount = 0;
            for (OwnerSalesResponse.DailySales day : daily) {
                if (!day.getSalesDate().isBefore(start) && day.getSalesDate().isBefore(nextStart)) {
                    sales = sales.add(day.getSales());
                    orderCount += day.getOrderCount();
                }
            }
            points.add(OwnerSalesResponse.SalesPoint.builder()
                    .label(toSalesLabel(unit, start))
                    .startDate(start)
                    .sales(sales)
                    .orderCount(orderCount)
                    .build());
        }

        return OwnerSalesResponse.builder()
                .period(unit)
                .fromDate(fromDate)
                .toDate(today)
                .totalSales(points.stream().map(OwnerSalesResponse.SalesPoint::getSales).reduce(BigDecimal.ZERO, BigDecimal::add))
                .orderCount(points.stream().mapToInt(OwnerSalesResponse.SalesPoint::getOrderCount).sum())
                .cancelCount(ownerDao.getCancelCount(storeId, fromDate, today))
                .points(points)
                .topMenus(ownerDao.getTopMenus(storeId, fromDate, today, TOP_MENU_COUNT))
                .build();
    }

    // 그래프 아래 이름: 일별 10/26(월), 주별 10/20~, 월별 10월
    private String toSalesLabel(String unit, LocalDate start) {
        return switch (unit) {
            case "DAY" -> start.getMonthValue() + "/" + start.getDayOfMonth()
                    + "(" + start.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + ")";
            case "WEEK" -> start.getMonthValue() + "/" + start.getDayOfMonth() + "~";
            default -> start.getMonthValue() + "월";
        };
    }

    private Long getStoreId(String userId) {
        Long storeId = ownerDao.getOwnerStoreId(userId);
        if (storeId == null) {
            throw new BizException("owner_store_not_found");
        }
        return storeId;
    }
}
