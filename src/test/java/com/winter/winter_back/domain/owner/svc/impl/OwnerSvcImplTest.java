package com.winter.winter_back.domain.owner.svc.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.file.ImageStorage;
import com.winter.winter_back.domain.order.dao.OrderHistoryDao;
import com.winter.winter_back.domain.order.dto.OrderHistoryDto;
import com.winter.winter_back.domain.owner.dao.OwnerDao;
import com.winter.winter_back.domain.owner.dto.OwnerOrderResponse;
import com.winter.winter_back.domain.owner.dto.OwnerRequests;
import com.winter.winter_back.domain.owner.dto.OwnerReviewResponse;
import com.winter.winter_back.domain.store.dao.StoreDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * description    : 사장님 서비스 단위 테스트 - 주문 단계 규칙, 리뷰 답글
 * fileName       : OwnerSvcImplTest
 * author         : hj kim
 * date           : 26. 10. 28.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 28.       hj kim             최초 생성
 */
@ExtendWith(MockitoExtension.class)
class OwnerSvcImplTest {

    private static final String OWNER_ID = "owner1";
    private static final Long STORE_ID = 1L;
    private static final String ORDER_ID = "ORD1";

    @Mock
    private OwnerDao ownerDao;
    @Mock
    private OrderHistoryDao orderHistoryDao;
    @Mock
    private StoreDao storeDao;
    @Mock
    private ImageStorage imageStorage;

    @InjectMocks
    private OwnerSvcImpl ownerSvc;

    @BeforeEach
    void setUp() {
        when(ownerDao.getOwnerStoreId(OWNER_ID)).thenReturn(STORE_ID);
    }

    private void givenOrder(String status) {
        OwnerOrderResponse order = new OwnerOrderResponse();
        order.setOrderId(ORDER_ID);
        order.setStatus(status);
        when(ownerDao.getOwnerOrder(STORE_ID, ORDER_ID)).thenReturn(order);
    }

    private OwnerRequests.OrderStatusChangeRequest statusRequest(String status, String reason) {
        OwnerRequests.OrderStatusChangeRequest request = new OwnerRequests.OrderStatusChangeRequest();
        request.setStatus(status);
        request.setReason(reason);
        return request;
    }

    private static String errorType(Throwable error) {
        return ((BizException) error).getType();
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"PENDING, CONFIRMED", "CONFIRMED, PREPARING", "PREPARING, DELIVERING", "DELIVERING, DELIVERED"})
    @DisplayName("주문은 정해진 다음 단계로 바뀌고 손님 진행 이력이 남는다")
    void modOrderStatusNextStep(String from, String to) {
        givenOrder(from);
        when(ownerDao.modOrderStatus(STORE_ID, ORDER_ID, from, to)).thenReturn(1);

        ownerSvc.modOrderStatus(OWNER_ID, ORDER_ID, statusRequest(to, null));

        ArgumentCaptor<OrderHistoryDto> history = ArgumentCaptor.forClass(OrderHistoryDto.class);
        verify(orderHistoryDao).insertOrderHistory(history.capture());
        assertThat(history.getValue().getStatus()).isEqualTo("ORDER_" + to);
        assertThat(history.getValue().getCreatedBy()).isEqualTo(OWNER_ID);
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"PENDING, PREPARING", "PENDING, DELIVERED", "PREPARING, CANCELLED", "DELIVERED, CANCELLED",
            "CANCELLED, CONFIRMED", "PREPARING, PENDING"})
    @DisplayName("단계를 건너뛰거나 되돌리거나, 조리 시작 뒤 거절하면 거부한다")
    void modOrderStatusInvalidStep(String from, String to) {
        givenOrder(from);

        assertThatThrownBy(() -> ownerSvc.modOrderStatus(OWNER_ID, ORDER_ID, statusRequest(to, "재료 소진")))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("invalid_order_status_change"));
        verify(ownerDao, never()).modOrderStatus(any(), any(), any(), any());
        verify(orderHistoryDao, never()).insertOrderHistory(any());
    }

    @Test
    @DisplayName("거절은 사유가 있어야 하고, 사유는 이력에 함께 남는다")
    void rejectNeedsReason() {
        givenOrder("PENDING");
        assertThatThrownBy(() -> ownerSvc.modOrderStatus(OWNER_ID, ORDER_ID, statusRequest("CANCELLED", "   ")))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("reject_reason_required"));

        when(ownerDao.modOrderStatus(STORE_ID, ORDER_ID, "PENDING", "CANCELLED")).thenReturn(1);
        ownerSvc.modOrderStatus(OWNER_ID, ORDER_ID, statusRequest("CANCELLED", " 재료 소진 "));

        ArgumentCaptor<OrderHistoryDto> history = ArgumentCaptor.forClass(OrderHistoryDto.class);
        verify(orderHistoryDao).insertOrderHistory(history.capture());
        assertThat(history.getValue().getDescription()).endsWith("사유: 재료 소진");
    }

    @Test
    @DisplayName("손님이 같은 순간 취소해 상태가 바뀌었으면(변경 0건) 거부하고 이력을 남기지 않는다")
    void concurrentChangeRejected() {
        givenOrder("PENDING");
        when(ownerDao.modOrderStatus(STORE_ID, ORDER_ID, "PENDING", "CONFIRMED")).thenReturn(0);

        assertThatThrownBy(() -> ownerSvc.modOrderStatus(OWNER_ID, ORDER_ID, statusRequest("CONFIRMED", null)))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("invalid_order_status_change"));
        verify(orderHistoryDao, never()).insertOrderHistory(any());
    }

    @Test
    @DisplayName("픽업 완료되면 가게 통계(받아간 주문 수)를 다시 계산한다")
    void deliveredUpdatesStoreStats() {
        givenOrder("DELIVERING");
        when(ownerDao.modOrderStatus(STORE_ID, ORDER_ID, "DELIVERING", "DELIVERED")).thenReturn(1);

        ownerSvc.modOrderStatus(OWNER_ID, ORDER_ID, statusRequest("DELIVERED", null));

        verify(storeDao).mergeStoreStats(STORE_ID);
    }

    @Test
    @DisplayName("다른 가게 주문은 없는 주문으로 응답한다")
    void otherStoreOrderNotFound() {
        when(ownerDao.getOwnerOrder(STORE_ID, ORDER_ID)).thenReturn(null);

        assertThatThrownBy(() -> ownerSvc.modOrderStatus(OWNER_ID, ORDER_ID, statusRequest("CONFIRMED", null)))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("order_not_found"));
    }

    private OwnerRequests.ReviewReplyRequest replyRequest(String content) {
        OwnerRequests.ReviewReplyRequest request = new OwnerRequests.ReviewReplyRequest();
        request.setContent(content);
        return request;
    }

    @Test
    @DisplayName("답글은 앞뒤 공백을 정리해 저장하고, 응답의 손님 이름은 첫 글자만 보인다")
    void modReviewReply() {
        OwnerReviewResponse review = new OwnerReviewResponse();
        review.setReviewId(7L);
        review.setUserName("김현주");
        when(ownerDao.getOwnerReview(STORE_ID, 7L)).thenReturn(review);

        OwnerReviewResponse response = ownerSvc.modReviewReply(OWNER_ID, 7L, replyRequest("  감사합니다  "));

        verify(ownerDao).modReviewReply(STORE_ID, 7L, "감사합니다");
        assertThat(response.getUserName()).isEqualTo("김**");
    }

    @Test
    @DisplayName("다른 가게 리뷰에는 답글을 달 수 없다 (없는 리뷰로 응답)")
    void otherStoreReviewNotFound() {
        when(ownerDao.getOwnerReview(STORE_ID, 7L)).thenReturn(null);

        assertThatThrownBy(() -> ownerSvc.modReviewReply(OWNER_ID, 7L, replyRequest("감사합니다")))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("review_not_found"));
        verify(ownerDao, never()).modReviewReply(any(), any(), anyString());
    }

    @Test
    @DisplayName("답글 삭제는 내용을 비운다")
    void delReviewReply() {
        when(ownerDao.getOwnerReview(STORE_ID, 7L)).thenReturn(new OwnerReviewResponse());

        ownerSvc.delReviewReply(OWNER_ID, 7L);

        verify(ownerDao).modReviewReply(eq(STORE_ID), eq(7L), eq(null));
    }
}
