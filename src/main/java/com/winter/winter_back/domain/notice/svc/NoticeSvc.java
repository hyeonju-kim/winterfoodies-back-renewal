package com.winter.winter_back.domain.notice.svc;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.domain.notice.dto.NoticeDto;

/**
 * description    : 공지사항 서비스 인터페이스
 * fileName       : NoticeSvc
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
public interface NoticeSvc {

    ListResponseDto<NoticeDto> getNoticeList(int page, int limit); // 공지 목록 (page 는 1부터)

    NoticeDto getNotice(Long noticeId); // 공지 상세 (없거나 비공개면 notice_not_found)
}
