package com.winter.winter_back.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

/**
 * description    : JSON 가공 유틸리티 클래스
 * fileName       : JsonUtils
 * author         : hj kim
 * date           : 25. 8. 15.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 15.        hj kim             최초 생성
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON → Object 변환
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("[JsonUtils] JSON deserialization failed: {}", e.getMessage());
            throw new RuntimeException("JSON deserialization failed: " + truncateJson(json), e);
        }
    }

    /**
     * Object → JSON 변환
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[JsonUtils] JSON serialization failed: {}", e.getMessage());
            throw new RuntimeException("JSON serialization failed: " + obj, e);
        }
    }

    /**
     * JSON 문자열 유효성 검사
     * (빠른 검증용, 구조/스키마까지 검사하지 않음)
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 긴 JSON 문자열 잘라서 로깅
     */
    private static String truncateJson(String json) {
        return json.length() > 200 ? json.substring(0, 200) + "..." : json;
    }
}
