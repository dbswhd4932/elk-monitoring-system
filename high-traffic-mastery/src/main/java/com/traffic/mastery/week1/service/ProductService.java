package com.traffic.mastery.week1.service;

import com.traffic.mastery.week1.domain.Product;
import com.traffic.mastery.week1.dto.ProductResponse;
import com.traffic.mastery.week1.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Week 1-2: 기본 상품 조회 (DB 직접 조회)
     * 성능 측정 대상 API
     */
    public ProductResponse getProduct(Long id) {
        log.debug("DB에서 상품 조회: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 의도적으로 처리 시간 추가 (DB 쿼리 시뮬레이션)
        simulateProcessing(50);

        return ProductResponse.from(product);
    }

    /**
     * 전체 상품 조회
     */
    public List<ProductResponse> getAllProducts() {
        log.debug("DB에서 전체 상품 조회");
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상품 생성
     */
    @Transactional
    public ProductResponse createProduct(String name, Integer price, String description, Integer stock) {
        Product product = new Product(name, price, description, stock);
        Product saved = productRepository.save(product);
        log.info("상품 생성: {}", saved.getId());
        return ProductResponse.from(saved);
    }

    /**
     * 재고 감소 (동시성 제어 없음 - Week 9에서 개선 예정)
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.decreaseStock(quantity);
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
