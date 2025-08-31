package com.winter.winter_back.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ResponseBaseDto<T> {

    private String code;
    private String desc;
    private Integer status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public ResponseBaseDto(T data) {
        this.data = data;
    }

    public ResponseBaseDto(String code) {
        this.code = code;
    }
    public ResponseBaseDto(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public ResponseBaseDto(String code, String desc, Integer status) {
        this.code = code;
        this.desc = desc;
        this.status = status;
    }

    public ResponseBaseDto(String code, String desc, Integer status, T data) {
        this.code = code;
        this.desc = desc;
        this.status = status;
        this.data = data;
    }
}
