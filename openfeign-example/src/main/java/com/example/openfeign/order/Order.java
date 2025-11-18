package com.example.openfeign.order;

import com.example.openfeign.common.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private String productName;
    private Integer quantity;
    private Double price;
    private User user;  // Feign으로 조회한 사용자 정보
}
