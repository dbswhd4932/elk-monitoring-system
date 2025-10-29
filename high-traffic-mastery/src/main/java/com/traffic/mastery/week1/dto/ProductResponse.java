package com.traffic.mastery.week1.dto;

import com.traffic.mastery.week1.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private Integer stock;

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStock()
        );
    }
}
