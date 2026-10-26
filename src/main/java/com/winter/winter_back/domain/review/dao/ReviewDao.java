package com.winter.winter_back.domain.review.dao;

import com.winter.winter_back.domain.review.dto.ReviewDto;
import com.winter.winter_back.domain.review.dto.ReviewStatsResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    :
 * fileName       : ReviewDao
 * author         : hj kim
 * date           : 25. 9. 2.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 2.         hj kim             최초 생성
 */

@Mapper
public interface ReviewDao {

    /**
     * 매장 리뷰 목록 조회 (매장 및 사용자 정보 포함)
     * @param storeId 매장 ID
     * @param offset 페이지 오프셋
     * @param limit 페이지 크기
     * @return 리뷰 목록
     */
    List<ReviewDto> selectStoreReviews(@Param("storeId") Long storeId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 사용자 리뷰 목록 조회 (매장 정보 포함)
     * @param userId 사용자 ID
     * @param offset 페이지 오프셋
     * @param limit 페이지 크기
     * @return 리뷰 목록
     */
    List<ReviewDto> selectUserReviews(@Param("userId") String userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 매장 리뷰 수 조회
     * @param storeId 매장 ID
     * @return 리뷰 수
     */
    Long countStoreReviews(@Param("storeId") Long storeId);

    /**
     * 사용자 리뷰 수 조회
     * @param userId 사용자 ID
     * @return 리뷰 수
     */
    Long countUserReviews(@Param("userId") String userId);

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 ID
     * @return 리뷰 정보
     */
    ReviewDto selectReviewById(@Param("reviewId") Long reviewId);

    /**
     * 리뷰 작성
     * @param reviewDto 리뷰 정보
     * @return 작성된 행 수
     */
    int insertReview(ReviewDto reviewDto);

    /**
     * 리뷰 수정
     * @param reviewDto 리뷰 정보
     * @return 수정된 행 수
     */
    int updateReview(ReviewDto reviewDto);

    /**
     * 리뷰 삭제
     * @param reviewId 리뷰 ID
     * @return 삭제된 행 수
     */
    int deleteReview(@Param("reviewId") Long reviewId);

    /**
     * 매장 평균 평점 조회
     * @param storeId 매장 ID
     * @return 평균 평점
     */
    Double getStoreAverageRating(@Param("storeId") Long storeId);

    /**
     * 매장 리뷰 통계 조회
     * @param storeId 매장 ID
     * @return 리뷰 통계
     */
    ReviewStatsResponse selectStoreReviewStats(@Param("storeId") Long storeId);

    /**
     * 주문 리뷰 존재 여부 확인
     * @param userId 사용자 ID
     * @param orderOrderId 주문 ID
     * @return 존재 여부
     */
    boolean existsReviewByOrderId(@Param("userId") String userId, @Param("orderOrderId") String orderOrderId);

    /**
     * 매장의 모든 리뷰 삭제 (매장 삭제 시 사용)
     * @param storeId 매장 ID
     * @return 삭제된 행 수
     */
    int deleteAllReviewsByStoreId(@Param("storeId") Long storeId);

    /**
     * 사용자의 모든 리뷰 삭제 (사용자 삭제 시 사용)
     * @param userId 사용자 ID
     * @return 삭제된 행 수
     */
    int deleteAllReviewsByUserId(@Param("userId") String userId);
}