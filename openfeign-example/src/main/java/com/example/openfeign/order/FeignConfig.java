package com.example.openfeign.order;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign 클라이언트 설정
 */
@Configuration
public class FeignConfig {

    /**
     * Feign 로깅 레벨 설정
     * NONE: 로깅 안함
     * BASIC: 요청 메서드, URL, 응답 상태, 실행 시간만 로깅
     * HEADERS: BASIC + 요청/응답 헤더
     * FULL: 모든 요청/응답 데이터 로깅
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 요청 옵션 설정 (타임아웃 등)
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5000, TimeUnit.MILLISECONDS,  // 연결 타임아웃
            10000, TimeUnit.MILLISECONDS  // 읽기 타임아웃
        );
    }

    /**
     * 재시도 정책 설정
     * period: 재시도 간격
     * maxPeriod: 최대 재시도 간격
     * maxAttempts: 최대 재시도 횟수
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
            100,    // 시작 간격 (ms)
            1000,   // 최대 간격 (ms)
            3       // 최대 시도 횟수
        );
    }
}
