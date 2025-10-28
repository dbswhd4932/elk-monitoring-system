package com.traffic.mastery.config;

import com.traffic.mastery.week1.domain.Product;
import com.traffic.mastery.week1.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        log.info("초기 데이터 생성 시작...");

        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            products.add(new Product(
                    "상품 " + i,
                    (i * 1000),
                    "상품 설명 " + i,
                    1000
            ));
        }

        productRepository.saveAll(products);
        log.info("초기 데이터 생성 완료: {} 개", products.size());
    }
}
