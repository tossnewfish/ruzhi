package com.example.onboarding.service;

import com.example.onboarding.domain.OrderStatus;
import com.example.onboarding.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderCacheService {

    private static final Logger log = LoggerFactory.getLogger(OrderCacheService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public OrderCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<OrderResponse> findOrder(Long id) {
        try {
            Map<Object, Object> cached = redisTemplate.opsForHash().entries(cacheKey(id));
            if (cached.isEmpty()) {
                return Optional.empty();
            }
            log.info("order cache hit, id={}", id);
            return Optional.of(toOrderResponse(cached));
        } catch (RuntimeException ex) {
            log.info("order cache read skipped, id={}, reason={}", id, ex.getMessage());
            return Optional.empty();
        }
    }

    public void saveOrder(OrderResponse order) {
        try {
            String key = cacheKey(order.id());
            Map<String, String> data = new HashMap<>();
            data.put("id", String.valueOf(order.id()));
            data.put("orderNo", order.orderNo());
            data.put("productName", order.productName());
            data.put("amount", order.amount().toPlainString());
            data.put("status", order.status().name());
            data.put("createdAt", order.createdAt().toString());
            data.put("updatedAt", order.updatedAt().toString());

            redisTemplate.opsForHash().putAll(key, data);
            redisTemplate.expire(key, CACHE_TTL);
        } catch (DataAccessException ex) {
            log.info("order cache write skipped, id={}, reason={}", order.id(), ex.getMessage());
        }
    }

    public void removeOrder(Long id) {
        try {
            redisTemplate.delete(cacheKey(id));
        } catch (DataAccessException ex) {
            log.info("order cache delete skipped, id={}, reason={}", id, ex.getMessage());
        }
    }

    private OrderResponse toOrderResponse(Map<Object, Object> value) {
        return new OrderResponse(
                Long.valueOf(required(value, "id")),
                required(value, "orderNo"),
                required(value, "productName"),
                new BigDecimal(required(value, "amount")),
                OrderStatus.valueOf(required(value, "status")),
                LocalDateTime.parse(required(value, "createdAt")),
                LocalDateTime.parse(required(value, "updatedAt"))
        );
    }

    private String required(Map<Object, Object> value, String field) {
        Object fieldValue = value.get(field);
        if (fieldValue == null) {
            throw new IllegalStateException("Missing order cache field: " + field);
        }
        return fieldValue.toString();
    }

    private String cacheKey(Long id) {
        return "cache:order:" + id;
    }
}
