package com.example.elkmonitoring.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * HTTP 요청마다 고유한 추적 ID를 생성하고 MDC에 저장하는 인터셉터
 * MDC(Mapped Diagnostic Context)는 로그에 컨텍스트 정보를 추가하는 메커니즘
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String CLIENT_IP = "clientIp";
    private static final String API_PATH = "api_path";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 요청 ID 생성 (고유 추적용)
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);

        // 클라이언트 IP 추출
        String clientIp = getClientIp(request);
        MDC.put(CLIENT_IP, clientIp);

        // API 경로 추가 (메서드 + URI)
        String apiPath = request.getMethod() + " " + request.getRequestURI();
        MDC.put(API_PATH, apiPath);

        // 사용자 ID (실제로는 인증 정보에서 추출)
        // 현재는 예제를 위해 하드코딩
        MDC.put(USER_ID, "anonymous");

        // 요청 정보 로깅
        log.info("HTTP 요청 - 메서드: {}, URI: {}, IP: {}",
                request.getMethod(),
                request.getRequestURI(),
                clientIp);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 응답 정보 로깅
        log.info("HTTP 응답 - 상태: {}, URI: {}",
                response.getStatus(),
                request.getRequestURI());

        // MDC 클리어 (메모리 누수 방지)
        MDC.clear();
    }

    /**
     * 클라이언트 IP 추출 (프록시 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
