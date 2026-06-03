package com.example.onboarding.service;

import com.example.onboarding.domain.OrderRecord;
import com.example.onboarding.domain.OrderStatus;
import com.example.onboarding.dto.CreateOrderRequest;
import com.example.onboarding.dto.OrderResponse;
import com.example.onboarding.mapper.OrderMapper;
import com.example.onboarding.mq.OrderEvent;
import com.example.onboarding.mq.OrderEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final RedisDistributedLock distributedLock;
    private final OrderCacheService orderCacheService;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(
            OrderMapper orderMapper,
            RedisDistributedLock distributedLock,
            OrderCacheService orderCacheService,
            OrderEventPublisher orderEventPublisher
    ) {
        this.orderMapper = orderMapper;
        this.distributedLock = distributedLock;
        this.orderCacheService = orderCacheService;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String lockKey = "lock:order:create:" + request.productName();
        RedisDistributedLock.LockToken lockToken = distributedLock
                .tryLock(lockKey, Duration.ofSeconds(5))
                .orElseThrow(() -> new IllegalStateException("The product is busy, please retry later."));

        try {
            OrderRecord order = new OrderRecord();
            order.setOrderNo("OD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            order.setProductName(request.productName());
            order.setAmount(request.amount());
            order.setStatus(OrderStatus.CREATED);

            orderMapper.insert(order);
            OrderResponse response = loadOrderFromDatabase(order.getId());
            orderCacheService.saveOrder(response);
            orderEventPublisher.publish(new OrderEvent(response.id(), response.orderNo(), response.status().name()));
            return response;
        } finally {
            distributedLock.unlock(lockToken);
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return orderCacheService.findOrder(id)
                .orElseGet(() -> {
                    OrderResponse response = loadOrderFromDatabase(id);
                    orderCacheService.saveOrder(response);
                    return response;
                });
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> recentCreatedOrders(int limit) {
        return orderMapper.selectRecentCreated(Math.min(limit, 50))
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse payOrder(Long id) {
        int updated = orderMapper.updateStatus(id, OrderStatus.PAID);
        if (updated == 0) {
            throw new OrderNotFoundException(id);
        }
        orderCacheService.removeOrder(id);
        OrderResponse response = loadOrderFromDatabase(id);
        orderCacheService.saveOrder(response);
        orderEventPublisher.publish(new OrderEvent(response.id(), response.orderNo(), response.status().name()));
        return response;
    }

    private OrderResponse loadOrderFromDatabase(Long id) {
        OrderRecord order = orderMapper.selectById(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        return OrderResponse.from(order);
    }
}
