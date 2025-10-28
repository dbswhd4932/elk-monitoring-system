package com.traffic.mastery.week3.service;

import com.traffic.mastery.week1.domain.Product;
import com.traffic.mastery.week1.dto.ProductResponse;
import com.traffic.mastery.week1.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Week 3-4: Redis 캐싱이 적용된 상품 조회 서비스
 *
 * 비교 대상:
 * - Week 1-2: ProductService (캐시 없음, DB 직접 조회)
 * - Week 3-4: ProductCacheService (Redis 캐시 적용)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductCacheService {

    private final ProductRepository productRepository;

    /**
     * Redis 캐시 적용된 상품 조회
     *
     * @Cacheable 동작:
     * 1. 먼저 Redis에서 캐시 확인
     * 2. 캐시에 있으면 → 즉시 반환 (DB 조회 X)
     * 3. 캐시에 없으면 → DB 조회 후 Redis에 저장
     *
     * 캐시 키: "product::{id}"
     * TTL: 5분 (RedisConfig에서 설정)
     */
    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductWithCache(Long id) {
        log.info("🔍 [CACHE MISS] DB에서 상품 조회 시작: productId={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // Week 1-2와 동일한 처리 시간 시뮬레이션
        simulateProcessing(50);

        log.info("✅ [CACHE MISS] DB 조회 완료, Redis에 캐시 저장: productId={}", id);
        return ProductResponse.from(product);
    }

    /**
     * 캐시 없이 직접 DB 조회 (비교용)
     * Week 1-2와 동일한 방식
     */
    public ProductResponse getProductWithoutCache(Long id) {
        log.info("🔍 [NO CACHE] DB에서 상품 직접 조회: productId={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        simulateProcessing(50);

        log.info("✅ [NO CACHE] DB 조회 완료: productId={}", id);
        return ProductResponse.from(product);
    }

    /**
     * 처리 시간 시뮬레이션 (DB 쿼리, 비즈니스 로직 등)
     */
    private void simulateProcessing(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
