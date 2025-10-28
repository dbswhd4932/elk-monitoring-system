package com.traffic.mastery.week1.controller;

import com.traffic.mastery.week1.dto.ProductResponse;
import com.traffic.mastery.week1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Week 1-2: 기본 성능 측정용 API
     * nGrinder로 이 엔드포인트를 테스트합니다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    /**
     * 전체 상품 조회
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 상품 생성 (테스트 데이터용)
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestParam String name,
            @RequestParam Integer price,
            @RequestParam String description,
            @RequestParam Integer stock
    ) {
        ProductResponse product = productService.createProduct(name, price, description, stock);
        return ResponseEntity.ok(product);
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
