package com.winter.winter_back.common.config;

import com.winter.winter_back.common.dto.BaseConstant;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> { // 공통으로 응답처리 하는 응답 래퍼 클래스

    // application.yml에서 설정한 URL 패턴 중 변환을 제외할 패턴들
    @Value("${cnf.url.auto-convert.excludes}")
    private List<String> excludeList;

    @Autowired
    private ExceptionWrapperUtil exceptionWrapperUtil;

    // AntPathMatcher를 사용하여 URL 패턴을 매칭하는데 사용
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 반환 타입이 JSON 타입이 아니면(ex. String) beforeBodyWrite 메서드 SKIP
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    // ResponseBodyAdvice 인터페이스의 메서드 구현: 응답 데이터를 조작하는 로직을 담당
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        boolean isConvert = true;

        // body가 ResponseBaseDto의 인스턴스가 아니면서, 특정 URL 패턴에 매치되지 않으면 isConvert = true 여서 아래 if(isConvert) 블럭으로 넘어가 변환 수행
        if (!(body instanceof ResponseBaseDto)) {
            String url = ((ServletServerHttpRequest) request).getServletRequest().getServletPath();
            for (String pattern : excludeList) {
                if (pathMatcher.match(pattern, url)) {
                    isConvert = false;
                    break;
                }
            }
        } else { // body가 ResponseBaseDto의 인스턴스이면 변환할 필요가 없어서 그대로 return
            isConvert = false;
        }

        // 변환을 수행할 경우, 성공적인 응답을 나타내는 "success" 코드를 사용하여 ResponseBaseDto로 감싸준다.
        if (isConvert) {
            Map<String, Object> expInfo = exceptionWrapperUtil.getExceptionInfo("success");
            return new ResponseBaseDto<>(
                    String.valueOf(expInfo.get(BaseConstant.RES_CODE)),
                    String.valueOf(expInfo.get(BaseConstant.RES_DESC)),
                    Integer.valueOf(String.valueOf(expInfo.get(BaseConstant.RES_STATUS))),
                    BaseConstant.RES_SUCCESS_DATA.equals(body) ? null : body
            );
        } else {
            return body;
        }
    }
}