package com.winter.winter_back.domain.review.service.impl;

import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.order.dao.OrderDao;
import com.winter.winter_back.domain.order.dto.OrderDto;
import com.winter.winter_back.domain.review.dao.ReviewDao;
import com.winter.winter_back.domain.review.dto.*;
import com.winter.winter_back.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final OrderDao orderDao;

    @Override
    public ReviewListResponse getStoreReviews(Long storeId, int page, int limit) {
        int offset = (page - 1) * limit;

        List<ReviewDto> reviews = reviewDao.selectStoreReviews(storeId, offset, limit);
        Long totalCount = reviewDao.countStoreReviews(storeId);

        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::convertToResponse)
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
            throw new BizException("리뷰를 찾을 수 없습니다.", new ArrayList<>());
        }
        return convertToResponse(review);
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
            throw new BizException("리뷰 작성에 실패했습니다.", new ArrayList<>());
        }

        log.info("리뷰 작성 완료 - userId: {}, storeId: {}, reviewId: {}", userId, request.getStoreId(), reviewDto.getIdx());

        // TODO tb_order 에 review_id 업데이트 추가
    }

    @Override
    @Transactional
    public void updateReview(String userId, Long reviewId, ReviewUpdateRequest request) {
        ReviewDto existingReview = reviewDao.selectReviewById(reviewId);
        if (existingReview == null) {
            throw new BizException("리뷰를 찾을 수 없습니다.", new ArrayList<>());
        }

        if (!existingReview.getUserId().equals(userId)) {
            throw new BizException("본인의 리뷰만 수정할 수 있습니다.", new ArrayList<>());
        }

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setIdx(reviewId);
        reviewDto.setRating(request.getRating());
        reviewDto.setContent(request.getContent());
        reviewDto.setImages(request.getImages());
        reviewDto.setModifiedDatetime(LocalDateTime.now());

        int result = reviewDao.updateReview(reviewDto);
        if (result == 0) {
            throw new BizException("리뷰 수정에 실패했습니다.", new ArrayList<>());
        }

        log.info("리뷰 수정 완료 - userId: {}, reviewId: {}", userId, reviewId);
    }

    @Override
    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        ReviewDto existingReview = reviewDao.selectReviewById(reviewId);
        if (existingReview == null) {
            throw new BizException("리뷰를 찾을 수 없습니다.", new ArrayList<>());
        }

        if (!existingReview.getUserId().equals(userId)) {
            throw new BizException("본인의 리뷰만 삭제할 수 있습니다.", new ArrayList<>());
        }

        int result = reviewDao.deleteReview(reviewId);
        if (result == 0) {
            throw new BizException("리뷰 삭제에 실패했습니다.", new ArrayList<>());
        }

        log.info("리뷰 삭제 완료 - userId: {}, reviewId: {}", userId, reviewId);
    }

    @Override
    public ReviewStatsResponse getStoreReviewStats(Long storeId) {
        return reviewDao.selectStoreReviewStats(storeId);
    }

    @Override
    public boolean isReviewWritable(String userId, String orderOrderId) {
        return !reviewDao.existsReviewByOrderId(userId, orderOrderId);
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