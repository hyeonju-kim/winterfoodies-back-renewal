package com.winter.winter_back.common.security.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Base64;

/**
 * description    : JWT 키 생성기
 * fileName       : KeyGenerator
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
public class KeyGenerator {
    public static void main(String[] args) {
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        System.out.println(Base64.getEncoder().encodeToString(keyBytes));
    }
}