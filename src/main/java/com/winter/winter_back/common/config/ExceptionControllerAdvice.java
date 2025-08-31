package com.winter.winter_back.common.config;


import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @Autowired
    private ExceptionWrapperUtil exceptionWrapperUtil;


    // BizException이 발생한 경우의 예외 처리 메서드
    @ExceptionHandler(BizException.class)
    public ResponseBaseDto<Object> bizException(Exception ex, HttpServletRequest req, HttpServletResponse response) throws Exception {
        BizException bizException = (BizException) ex;

        return exceptionWrapperUtil.makeResponse(bizException.getType(), bizException.getOptions().toArray());
    }

    // 파라미터가 적절하지 않은 경우의 예외처리 메서드
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseBaseDto<Object> bindException(BindException ex, HttpServletRequest req, HttpServletResponse response) {
        log.error(ex.getClass().getName());
        log.error(ex.getMessage());
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        return exceptionWrapperUtil.makeResponse(ex.getClass().getName(), new String[]{errorMessage});
    }

    // 지원되지 않는 HTTP 메서드를 사용한 경우의 예외 처리 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseBaseDto<Object> methodNotSupportedException(Exception ex, HttpServletRequest req, HttpServletResponse response) throws Exception {
        log.error(ex.getClass().getName());
        log.error(ex.getMessage());

        return exceptionWrapperUtil.makeResponse(ex.getClass().getName(), new String[]{ex.getClass().toString()});
    }

    // 요청된 URL에 대한 핸들러가 없는 경우의 예외 처리 메서드
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseBaseDto<Object> notFoundException(Exception ex, HttpServletRequest req, HttpServletResponse response) throws Exception {
        log.error(ex.getClass().getName());
        log.error(ex.getMessage());

        return exceptionWrapperUtil.makeResponse(ex.getClass().getName(), new String[]{ex.getClass().toString()});
    }

    // 기타 모든 예외에 대한 기본 예외 처리 메서드
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseBaseDto<Object> baseException(Exception ex, HttpServletRequest req, HttpServletResponse response) throws Exception {
        log.error(ex.getClass().getName());
        log.error(ex.getMessage());

        return exceptionWrapperUtil.makeResponse(ex.getClass().getName(), new String[]{ex.getClass().toString()});
    }
}