package com.winter.winter_back.common.config;

import com.winter.winter_back.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;

import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * description    : RestTemplate 인터셉터
 * fileName       : RestTemplateInterceptor
 * author         : 문다은
 * date           : 2024-07-12
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-07-12        문다은              최초 생성
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
    private final RestTemplateLogExcludes restTemplateLogExcludes;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest httpRequest,
            byte[] body,
            ClientHttpRequestExecution clientHttpRequestExecution) {
        // Content-Type header를 application/json으로 설정
        httpRequest.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        try {
            // API 요청 로그 출력
            requestLog(httpRequest, body);

            // HTTP 요청 실행 → 결과 반환
            ClientHttpResponse clientHttpResponse = clientHttpRequestExecution.execute(httpRequest, body);

            // 응답 본문을 미리 읽고 API 응답 로그 출력 후, 새로운 InputStream을 생성
            byte[] responseBody = StreamUtils.copyToByteArray(clientHttpResponse.getBody());
            if (!restTemplateLogExcludes.getPath().contains(httpRequest.getURI().getPath()))
                responseLog(clientHttpResponse, responseBody);

            return new BufferingClientHttpResponseWrapper(clientHttpResponse, responseBody);
        } catch (IOException e) {
            log.error("[RestTemplateInterceptor.java] REST API 요청 실패 > URI - {}", httpRequest.getURI());
            log.error("[RestTemplateInterceptor.java] intercept 메서드 > 에러 메시지 - {}", e.getMessage());
            throw new BizException("");
        }
    }

    private void requestLog(HttpRequest httpRequest, byte[] body) {
        URI uri = httpRequest.getURI();
        HttpMethod method = httpRequest.getMethod();
        String requestBody = new String(body, StandardCharsets.UTF_8);

        log.info("[RestTemplateInterceptor.java] REST API 요청 > URI - {}, Method - {}, Request Body - {}", uri, method, requestBody);
    }

    private void responseLog(ClientHttpResponse response, byte[] body) {
        try {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            log.info("[RestTemplateInterceptor.java] REST API 응답 > Status Code - {}, Response Body - {}", response.getStatusCode(), bodyStr);
        } catch (IOException e) {
            log.error("[RestTemplatInterceptor.java] responseLog > 응답 Log 생성 실패");
            log.error("[RestTemplateInterceptor.java] responseLog > 에러 메시지 - {}", e.getMessage());
            throw new BizException("");
        }
    }

    /**
     * HTTP 응답을 읽고, 읽은 내용을 저장하여 이후에 다시 읽을 수 있도록 하는 Wrapper
     */
    private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private final byte[] body;

        public BufferingClientHttpResponseWrapper(ClientHttpResponse response, byte[] body) {
            this.response = response;
            this.body = body;
        }

        // 저장된 바이트 배열을 ByteArrayInputStream으로 감싸서 반환
        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return (HttpStatus) response.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return 0;
        }


        // TODO 컴파일 에러로 잠시 주석 250814
//        @Override
//        public int getRawStatusCode() throws IOException {
//            return response.getRawStatusCode();
//        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }
    }
}
