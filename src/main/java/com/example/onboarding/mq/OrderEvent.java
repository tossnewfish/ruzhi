package com.example.onboarding.mq;

public record OrderEvent(
        Long orderId,
        String orderNo,
        String status
) {
}
