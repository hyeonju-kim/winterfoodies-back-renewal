package com.winter.winter_back.domain.notice.ctrl;

import com.winter.winter_back.common.dto.ListResponseDto;
import com.winter.winter_back.domain.notice.dto.NoticeDto;
import com.winter.winter_back.domain.notice.svc.NoticeSvc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * description    : 공지사항 컨트롤러 (비로그인 조회 가능)
 * fileName       : NoticeCtrl
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Slf4j
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeCtrl {

    private final NoticeSvc noticeSvc;

    /**
     * 공지 목록 조회
     */
    @GetMapping
    public ListResponseDto<NoticeDto> getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return noticeSvc.getNoticeList(page, limit);
    }

    /**
     * 공지 상세 조회
     */
    @GetMapping("/{noticeId}")
    public NoticeDto getNotice(@PathVariable Long noticeId) {
        return noticeSvc.getNotice(noticeId);
    }
}
