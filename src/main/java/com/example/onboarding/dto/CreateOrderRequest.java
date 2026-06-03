package com.example.onboarding.dto;

import java.math.BigDecimal;

public record CreateOrderRequest(
        String productName,
        BigDecimal amount
) {
}
