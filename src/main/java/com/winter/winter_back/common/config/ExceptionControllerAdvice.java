package com.winter.winter_back.common.config;


import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.BizException;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 예외를 공통 응답(ResponseBaseDto)으로 바꾸고, 응답 본문의 status 를 실제 HTTP 상태 코드로도 내려준다
 */
@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @Autowired
    private ExceptionWrapperUtil exceptionWrapperUtil;


    // BizException이 발생한 경우의 예외 처리 메서드
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ResponseBaseDto<Object>> bizException(BizException bizException) {
        log.warn("[BizException] type={}, options={}", bizException.getType(), bizException.getOptions());

        if (bizException.getOptions() != null) {
            return toResponseEntity(exceptionWrapperUtil.makeResponse(bizException.getType(), bizException.getOptions().toArray()));
        }
        return toResponseEntity(exceptionWrapperUtil.makeResponse(bizException.getType()));
    }

    // 파라미터가 적절하지 않은 경우의 예외처리 메서드
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ResponseBaseDto<Object>> bindException(BindException ex) {
        String errorMessage = ex.getBindingResult().getFieldError() == null
                ? ex.getMessage()
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("[{}] {}", ex.getClass().getSimpleName(), errorMessage);

        return toResponseEntity(exceptionWrapperUtil.makeResponse(ex.getClass().getName(), new String[]{errorMessage}));
    }

    // 지원되지 않는 HTTP 메서드를 사용한 경우의 예외 처리 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseBaseDto<Object>> methodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("[{}] {}", ex.getClass().getSimpleName(), ex.getMessage());

        return toResponseEntity(exceptionWrapperUtil.makeResponse(ex.getClass().getName()));
    }

    // 요청된 URL에 대한 핸들러가 없는 경우의 예외 처리 메서드 (Spring 6.1+ 는 NoResourceFoundException)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ResponseBaseDto<Object>> notFoundException(Exception ex) {
        log.warn("[{}] {}", ex.getClass().getSimpleName(), ex.getMessage());

        return toResponseEntity(exceptionWrapperUtil.makeResponse(NoHandlerFoundException.class.getName()));
    }

    // 기타 모든 예외에 대한 기본 예외 처리 메서드 (원인 추적을 위해 스택트레이스 로그, 응답에는 내부 정보 노출하지 않음)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBaseDto<Object>> baseException(Exception ex) {
        log.error("[{}] 처리되지 않은 예외", ex.getClass().getName(), ex);

        return toResponseEntity(exceptionWrapperUtil.makeResponse(ex.getClass().getName()));
    }

    private ResponseEntity<ResponseBaseDto<Object>> toResponseEntity(ResponseBaseDto<Object> responseBaseDto) {
        return ResponseEntity.status(responseBaseDto.getStatus()).body(responseBaseDto);
    }
}
