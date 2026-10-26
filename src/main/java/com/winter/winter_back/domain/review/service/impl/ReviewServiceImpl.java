package com.winter.winter_back.domain.review.service.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.order.dao.OrderDao;
import com.winter.winter_back.domain.order.dto.OrderDto;
import com.winter.winter_back.domain.order.dto.OrderStatus;
import com.winter.winter_back.domain.review.dao.ReviewDao;
import com.winter.winter_back.domain.review.dto.*;
import com.winter.winter_back.domain.review.service.ReviewService;
import com.winter.winter_back.domain.store.dao.StoreDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description    :
 * fileName       : ReviewServiceImpl
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 * 26. 10. 26.       hj kim             받아간 주문만 리뷰 작성·작성 가능 여부에 본인·상태 확인, 공개 목록 작성자 아이디 숨김·이름 가림
 * 26. 10. 26.       hj kim             리뷰 작성·수정·삭제 시 가게 통계(별점·리뷰 수) 다시 계산
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final OrderDao orderDao;
    private final StoreDao storeDao;

    @Override
    public ReviewListResponse getStoreReviews(Long storeId, int page, int limit) {
        int offset = (page - 1) * limit;

        List<ReviewDto> reviews = reviewDao.selectStoreReviews(storeId, offset, limit);
        Long totalCount = reviewDao.countStoreReviews(storeId);

        // 누구나 보는 목록이라 작성자 아이디는 빼고 이름은 일부 가린다
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::convertToResponse)
                .map(this::hideAuthor)
                .collect(Collectors.toList());

        ReviewListResponse response = new ReviewListResponse();
        response.setDataList(reviewResponses);
        response.setTotalCount(totalCount.intValue());

        // 평균 평점 계산
        Double averageRating = reviewDao.getStoreAverageRating(storeId);
        response.setAverageRating(averageRating);
        response.setTotalReviews(totalCount);

        return response;
    }

    @Override
    public ReviewListResponse getUserReviews(String userId, int page, int limit) {
        int offset = (page - 1) * limit;

        List<ReviewDto> reviews = reviewDao.selectUserReviews(userId, offset, limit);
        log.info("[getUserReviews] > [reviews] === {}", reviews);
        log.info("[getUserReviews] > [userId] === {}", userId);

        Long totalCount = reviewDao.countUserReviews(userId);
        log.info("[getUserReviews] > [totalCount] === {}", totalCount);

        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        log.info("[getUserReviews] > [reviewResponses] === {}", reviewResponses);

        ReviewListResponse response = new ReviewListResponse();
        response.setDataList(reviewResponses);
        response.setTotalCount(totalCount.intValue());
        response.setTotalReviews(totalCount);

        return response;
    }

    @Override
    public ReviewResponse getReview(Long reviewId) {
        ReviewDto review = reviewDao.selectReviewById(reviewId);
        if (review == null) {
            throw new BizException("review_not_found");
        }
        return hideAuthor(convertToResponse(review));
    }

    @Override
    @Transactional
    public void createReview(String userId, ReviewRequest request) {
        log.info("[createReview] > [userId] === {}", userId);
        log.info("[createReview] > [request] === {}", request);
        // 본인 주문이고, 리뷰하려는 매장의 주문인지 확인
        OrderDto order = orderDao.selectOrderById(request.getOrderOrderId());
        if (order == null) {
            throw new BizException("order_not_found");
        }
        if (!userId.equals(order.getUserId()) || !request.getStoreId().equals(order.getStoreId())) {
            throw new BizException("review_not_allowed");
        }
        // 받아간(픽업완료·배달완료) 주문만 리뷰 가능 - 취소·진행 중 주문 거부
        if (!OrderStatus.DELIVERED.name().equals(order.getStatus())) {
            throw new BizException("review_order_not_completed");
        }

        // 이미 해당 주문에 대한 리뷰가 있는지 확인
        if (reviewDao.existsReviewByOrderId(userId, request.getOrderOrderId())) {
            throw new BizException("review_already_exists");
        }


        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setStoreId(request.getStoreId());
        reviewDto.setUserId(userId);
        reviewDto.setOrderOrderId(request.getOrderOrderId());
        reviewDto.setRating(request.getRating());
        reviewDto.setContent(request.getContent());
        reviewDto.setImages(request.getImages());
        reviewDto.setCreatedDatetime(LocalDateTime.now());
        reviewDto.setModifiedDatetime(LocalDateTime.now());

        int result = reviewDao.insertReview(reviewDto);
        log.info("[createReview] > [result] === {}", result);
        log.info("[createReview] > [reviewDto] === {}", reviewDto);

        if (result == 0) {
            throw new BizException("fail_add");
        }

        storeDao.mergeStoreStats(request.getStoreId());
        log.info("리뷰 작성 완료 - userId: {}, storeId: {}, reviewId: {}", userId, request.getStoreId(), reviewDto.getIdx());

        // TODO tb_order 에 review_id 업데이트 추가
    }

    @Override
    @Transactional
    public void updateReview(String userId, Long reviewId, ReviewUpdateRequest request) {
        ReviewDto existingReview = reviewDao.selectReviewById(reviewId);
        if (existingReview == null) {
            throw new BizException("review_not_found");
        }

        if (!existingReview.getUserId().equals(userId)) {
            throw new BizException("permission_denied");
        }

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setIdx(reviewId);
        reviewDto.setRating(request.getRating());
        reviewDto.setContent(request.getContent());
        reviewDto.setImages(request.getImages());
        reviewDto.setModifiedDatetime(LocalDateTime.now());

        int result = reviewDao.updateReview(reviewDto);
        if (result == 0) {
            throw new BizException("fail_update");
        }

        storeDao.mergeStoreStats(existingReview.getStoreId());
        log.info("리뷰 수정 완료 - userId: {}, reviewId: {}", userId, reviewId);
    }

    @Override
    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        ReviewDto existingReview = reviewDao.selectReviewById(reviewId);
        if (existingReview == null) {
            throw new BizException("review_not_found");
        }

        if (!existingReview.getUserId().equals(userId)) {
            throw new BizException("permission_denied");
        }

        int result = reviewDao.deleteReview(reviewId);
        if (result == 0) {
            throw new BizException("fail_remove");
        }

        storeDao.mergeStoreStats(existingReview.getStoreId());
        log.info("리뷰 삭제 완료 - userId: {}, reviewId: {}", userId, reviewId);
    }

    @Override
    public ReviewStatsResponse getStoreReviewStats(Long storeId) {
        return reviewDao.selectStoreReviewStats(storeId);
    }

    @Override
    public boolean isReviewWritable(String userId, String orderOrderId) {
        // 본인 주문 + 받아간 주문 + 아직 리뷰 없음
        OrderDto order = orderDao.selectOrderById(orderOrderId);
        return order != null
                && userId.equals(order.getUserId())
                && OrderStatus.DELIVERED.name().equals(order.getStatus())
                && !reviewDao.existsReviewByOrderId(userId, orderOrderId);
    }

    /**
     * 공개 응답용: 작성자 아이디를 빼고 이름은 첫 글자만 남긴다 (예: 김현주 → 김**)
     */
    private ReviewResponse hideAuthor(ReviewResponse response) {
        response.setUserId(null);
        String userName = response.getUserName();
        if (userName != null && !userName.isEmpty()) {
            response.setUserName(userName.charAt(0) + "*".repeat(Math.max(userName.length() - 1, 1)));
        }
        return response;
    }

    /**
     * ReviewDto를 ReviewResponse로 변환
     */
    private ReviewResponse convertToResponse(ReviewDto dto) {
        ReviewResponse response = new ReviewResponse();
        response.setIdx(dto.getIdx());
        response.setStoreId(dto.getStoreId());
        response.setUserId(dto.getUserId());
        response.setOrderOrderId(dto.getOrderOrderId());
        response.setRating(dto.getRating());
        response.setContent(dto.getContent());
        response.setImages(dto.getImages());
        response.setCreatedDatetime(dto.getCreatedDatetime());
        response.setModifiedDatetime(dto.getModifiedDatetime());
        response.setStoreName(dto.getStoreName());
        response.setStoreCategory(dto.getStoreCategory());
        response.setStoreImageUrl(dto.getStoreImageUrl());
        response.setUserName(dto.getUserName());
        return response;
    }
}