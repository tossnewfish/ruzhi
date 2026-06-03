package com.example.onboarding.dto;

import com.example.onboarding.domain.OrderRecord;
import com.example.onboarding.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderNo,
        String productName,
        BigDecimal amount,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static OrderResponse from(OrderRecord order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getProductName(),
                order.getAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
