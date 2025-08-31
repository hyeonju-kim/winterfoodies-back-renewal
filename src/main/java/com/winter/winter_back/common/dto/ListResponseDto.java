package com.winter.winter_back.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListResponseDto<T> {
    private Integer totalCount; // 전체 리스트 개수
    private List<T> dataList;   // 출력되는 데이터 리스트
}
