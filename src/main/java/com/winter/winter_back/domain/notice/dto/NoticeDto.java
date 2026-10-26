package com.winter.winter_back.domain.notice.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * description    : 공지사항 DTO (목록은 content 없이, 상세는 content 포함)
 * fileName       : NoticeDto
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Data
public class NoticeDto {
    private Long idx;                       // 공지 ID
    private String title;                   // 제목
    private String content;                 // 내용 (목록 조회에서는 null)
    private String isPinned;                // 상단 고정 여부(Y/N)
    private LocalDateTime createdDatetime;  // 등록일시
    private LocalDateTime updatedDatetime;  // 수정일시
}
