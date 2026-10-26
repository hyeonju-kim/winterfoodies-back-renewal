package com.winter.winter_back.domain.notice.dao;

import com.winter.winter_back.domain.notice.dto.NoticeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description    : 공지사항 DAO
 * fileName       : NoticeDao
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Mapper
public interface NoticeDao {

    List<NoticeDto> getNoticeList(@Param("offset") int offset, @Param("limit") int limit); // 공개 공지 목록 (고정 공지 먼저, 최신순)

    int getNoticeCount(); // 공개 공지 개수

    NoticeDto getNotice(@Param("noticeId") Long noticeId); // 공개 공지 상세
}
