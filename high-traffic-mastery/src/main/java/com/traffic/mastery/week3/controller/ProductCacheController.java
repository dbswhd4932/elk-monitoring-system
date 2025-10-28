package com.traffic.mastery.week3.controller;

import com.traffic.mastery.week1.dto.ProductResponse;
import com.traffic.mastery.week3.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Week 3-4: Redis 캐시 성능 비교용 Controller
 *
 * 테스트 시나리오:
 * 1. /api/v3/products/cache/{id}    - Redis 캐시 사용 (빠름)
 * 2. /api/v3/products/no-cache/{id} - 캐시 없이 DB 직접 조회 (느림)
 */
@RestController
@RequestMapping("/api/v3/products")
@RequiredArgsConstructor
public class ProductCacheController {

    private final ProductCacheService productCacheService;

    /**
     * Redis 캐시 적용된 상품 조회
     *
     * nGrinder 테스트:
     * - URL: http://host.docker.internal:8080/api/v3/products/cache/{id}
     * - 예상 성능: TPS 2000+, 평균 응답시간 50ms 이하
     */
    @GetMapping("/cache/{id}")
    public ResponseEntity<ProductResponse> getProductWithCache(@PathVariable Long id) {
        ProductResponse product = productCacheService.getProductWithCache(id);
        return ResponseEntity.ok()
                .header("X-Cache-Type", "REDIS")
                .body(product);
    }

    /**
     * 캐시 없이 DB 직접 조회 (Week 1-2 방식)
     *
     * nGrinder 테스트:
     * - URL: http://host.docker.internal:8080/api/v3/products/no-cache/{id}
     * - 예상 성능: TPS 500, 평균 응답시간 200ms
     */
    @GetMapping("/no-cache/{id}")
    public ResponseEntity<ProductResponse> getProductWithoutCache(@PathVariable Long id) {
        ProductResponse product = productCacheService.getProductWithoutCache(id);
        return ResponseEntity.ok()
                .header("X-Cache-Type", "NONE")
                .body(product);
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Week 3-4: Redis Cache OK");
    }
}
