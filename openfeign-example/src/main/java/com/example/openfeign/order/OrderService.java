package com.example.openfeign.order;

import com.example.openfeign.common.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserClient userClient;
    private final Map<Long, Order> orderDatabase = new HashMap<>();

    public OrderService(UserClient userClient) {
        this.userClient = userClient;

        // 초기 주문 데이터
        orderDatabase.put(1L, new Order(1L, 1L, "노트북", 1, 1500000.0, null));
        orderDatabase.put(2L, new Order(2L, 2L, "마우스", 2, 30000.0, null));
        orderDatabase.put(3L, new Order(3L, 1L, "키보드", 1, 120000.0, null));
    }

    /**
     * 주문 정보 조회 - Feign으로 사용자 정보도 함께 가져옴
     */
    public Order getOrderById(Long id) {
        Order order = orderDatabase.get(id);
        if (order == null) {
            throw new RuntimeException("Order not found: " + id);
        }

        // Feign Client를 사용하여 사용자 정보 조회
        User user = userClient.getUserById(order.getUserId());
        order.setUser(user);

        return order;
    }

    /**
     * 모든 주문 조회 - 각 주문에 사용자 정보 포함
     */
    public List<Order> getAllOrders() {
        return orderDatabase.values().stream()
            .map(order -> {
                User user = userClient.getUserById(order.getUserId());
                order.setUser(user);
                return order;
            })
            .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 주문 조회
     */
    public List<Order> getOrdersByUserId(Long userId) {
        // 먼저 사용자가 존재하는지 확인 (Feign으로 조회)
        User user = userClient.getUserById(userId);

        return orderDatabase.values().stream()
            .filter(order -> order.getUserId().equals(userId))
            .peek(order -> order.setUser(user))
            .collect(Collectors.toList());
    }

    /**
     * 주문 생성
     */
    public Order createOrder(Order order) {
        // Feign으로 사용자가 존재하는지 확인
        User user = userClient.getUserById(order.getUserId());

        orderDatabase.put(order.getId(), order);
        order.setUser(user);
        return order;
    }
}
