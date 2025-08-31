package com.winter.winter_back.common.exception;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class BizException extends RuntimeException{
    private String type;
    private List<String> options;

    public BizException(String type){
        this.type = type;
    }

    public BizException(String type, String options){
        this.type = type;
        this.options = Arrays.asList(options);
    }

    public BizException(String type, List<String> options){
        this.type = type;
        this.options = options;
    }
}
