package com.winter.winter_back.domain.review.service.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.file.ImageStorage;
import com.winter.winter_back.domain.order.dao.OrderDao;
import com.winter.winter_back.domain.order.dto.OrderDto;
import com.winter.winter_back.domain.review.dao.ReviewDao;
import com.winter.winter_back.domain.review.dto.ReviewDto;
import com.winter.winter_back.domain.review.dto.ReviewRequest;
import com.winter.winter_back.domain.review.dto.ReviewUpdateRequest;
import com.winter.winter_back.domain.store.dao.StoreDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * description    : 리뷰 서비스 단위 테스트 - 작성 조건, 리뷰 사진 검사, 수정 시 사진 유지·삭제
 * fileName       : ReviewServiceImplTest
 * author         : hj kim
 * date           : 26. 10. 28.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 28.       hj kim             최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    private static final String USER_ID = "user1";
    private static final String ORDER_ID = "ORD1";
    private static final String OWN_PHOTO = "/uploads/reviews/user1/a.png";

    @Mock
    private ReviewDao reviewDao;
    @Mock
    private OrderDao orderDao;
    @Mock
    private StoreDao storeDao;
    @Mock
    private ImageStorage imageStorage;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private void givenOrder(String userId, String status) {
        when(orderDao.selectOrderById(ORDER_ID)).thenReturn(
                OrderDto.builder().orderId(ORDER_ID).storeId(1L).userId(userId).status(status).build());
    }

    private ReviewRequest reviewRequest(String images) {
        ReviewRequest request = new ReviewRequest();
        request.setStoreId(1L);
        request.setOrderOrderId(ORDER_ID);
        request.setRating(5L);
        request.setContent("맛있어요");
        request.setImages(images);
        return request;
    }

    private static String errorType(Throwable error) {
        return ((BizException) error).getType();
    }

    @Test
    @DisplayName("받아간 주문이면 리뷰를 저장하고, 사진 경로는 공백을 정리해 쉼표로 잇는다")
    void createReview() {
        givenOrder(USER_ID, "DELIVERED");
        when(reviewDao.insertReview(any())).thenReturn(1);

        reviewService.createReview(USER_ID, reviewRequest(" " + OWN_PHOTO + " , /uploads/reviews/user1/b.png "));

        ArgumentCaptor<ReviewDto> saved = ArgumentCaptor.forClass(ReviewDto.class);
        verify(reviewDao).insertReview(saved.capture());
        assertThat(saved.getValue().getImages()).isEqualTo(OWN_PHOTO + ",/uploads/reviews/user1/b.png");
        verify(storeDao).mergeStoreStats(1L);
    }

    @Test
    @DisplayName("진행 중이거나 취소된 주문은 리뷰를 쓸 수 없다")
    void createReviewNotCompleted() {
        givenOrder(USER_ID, "PREPARING");

        assertThatThrownBy(() -> reviewService.createReview(USER_ID, reviewRequest(null)))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("review_order_not_completed"));
        verify(reviewDao, never()).insertReview(any());
    }

    @Test
    @DisplayName("남의 주문으로는 리뷰를 쓸 수 없다")
    void createReviewOtherUsersOrder() {
        givenOrder("someone", "DELIVERED");

        assertThatThrownBy(() -> reviewService.createReview(USER_ID, reviewRequest(null)))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("review_not_allowed"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/uploads/reviews/someone/a.png",                                   // 남이 올린 사진
            "/uploads/reviews/user1/../someone/a.png",                          // 경로 거슬러 올라가기
            "/uploads/reviews/user1/sub/a.png",                                 // 하위 폴더
            "https://example.com/a.png",                                        // 외부 주소
            "/uploads/reviews/user1/1.png,/uploads/reviews/user1/2.png,"
                    + "/uploads/reviews/user1/3.png,/uploads/reviews/user1/4.png" // 4장
    })
    @DisplayName("본인이 올린 사진 3장까지만 붙일 수 있다")
    void createReviewInvalidImages(String images) {
        givenOrder(USER_ID, "DELIVERED");
        when(reviewDao.existsReviewByOrderId(USER_ID, ORDER_ID)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(USER_ID, reviewRequest(images)))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("invalid_review_images"));
        verify(reviewDao, never()).insertReview(any());
    }

    private ReviewDto existingReview(String images) {
        ReviewDto review = new ReviewDto();
        review.setIdx(7L);
        review.setUserId(USER_ID);
        review.setStoreId(1L);
        review.setImages(images);
        return review;
    }

    private ReviewUpdateRequest updateRequest(String images) {
        ReviewUpdateRequest request = new ReviewUpdateRequest();
        request.setRating(4L);
        request.setContent("수정");
        request.setImages(images);
        return request;
    }

    @Test
    @DisplayName("수정할 때 사진을 보내지 않으면 기존 사진을 그대로 둔다")
    void updateReviewKeepsImages() {
        when(reviewDao.selectReviewById(7L)).thenReturn(existingReview(OWN_PHOTO));
        when(reviewDao.updateReview(any())).thenReturn(1);

        reviewService.updateReview(USER_ID, 7L, updateRequest(null));

        ArgumentCaptor<ReviewDto> saved = ArgumentCaptor.forClass(ReviewDto.class);
        verify(reviewDao).updateReview(saved.capture());
        assertThat(saved.getValue().getImages()).isEqualTo(OWN_PHOTO);
        verify(imageStorage, never()).deleteImage(anyString());
    }

    @Test
    @DisplayName("수정할 때 사진을 비우면 사진을 지우고 파일도 삭제한다")
    void updateReviewRemovesImages() {
        when(reviewDao.selectReviewById(7L)).thenReturn(existingReview(OWN_PHOTO));
        when(reviewDao.updateReview(any())).thenReturn(1);

        reviewService.updateReview(USER_ID, 7L, updateRequest(""));

        ArgumentCaptor<ReviewDto> saved = ArgumentCaptor.forClass(ReviewDto.class);
        verify(reviewDao).updateReview(saved.capture());
        assertThat(saved.getValue().getImages()).isNull();
        verify(imageStorage).deleteImage(OWN_PHOTO);
    }

    @Test
    @DisplayName("남의 리뷰는 고칠 수 없다")
    void updateOtherUsersReview() {
        when(reviewDao.selectReviewById(7L)).thenReturn(existingReview(null));

        assertThatThrownBy(() -> reviewService.updateReview("someone", 7L, updateRequest(null)))
                .satisfies(error -> assertThat(errorType(error)).isEqualTo("permission_denied"));
        verify(reviewDao, never()).updateReview(any());
    }
}
