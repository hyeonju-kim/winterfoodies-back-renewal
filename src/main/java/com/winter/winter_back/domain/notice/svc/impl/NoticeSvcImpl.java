package com.winter.winter_back.domain.notice.svc.impl;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.domain.notice.dao.NoticeDao;
import com.winter.winter_back.domain.notice.dto.NoticeDto;
import com.winter.winter_back.domain.notice.svc.NoticeSvc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description    : 공지사항 서비스 구현 (조회 전용, 등록은 당분간 DB 에 직접)
 * fileName       : NoticeSvcImpl
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeSvcImpl implements NoticeSvc {

    private static final int MAX_LIMIT = 50;

    private final NoticeDao noticeDao;

    /**
     * Author      : hj kim
     * Description : 공개 공지 목록 (고정 공지 먼저, 그다음 최신순). page 는 1부터, limit 은 1~50
     * Date        : 26. 10. 26.
     */
    @Override
    public ListResponseDto<NoticeDto> getNoticeList(int page, int limit) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);

        ListResponseDto<NoticeDto> response = new ListResponseDto<>();
        response.setDataList(noticeDao.getNoticeList((safePage - 1) * safeLimit, safeLimit));
        response.setTotalCount(noticeDao.getNoticeCount());
        return response;
    }

    /**
     * Author      : hj kim
     * Description : 공개 공지 상세 (없거나 비공개면 notice_not_found)
     * Date        : 26. 10. 26.
     */
    @Override
    public NoticeDto getNotice(Long noticeId) {
        NoticeDto notice = noticeDao.getNotice(noticeId);
        if (notice == null) {
            throw new BizException("notice_not_found");
        }
        return notice;
    }
}
