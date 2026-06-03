package com.example.onboarding.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RocketMqOrderProducer producer;

    public OrderEventPublisher(RocketMqOrderProducer producer) {
        this.producer = producer;
    }

    public void publish(OrderEvent event) {
        if (!producer.isEnabled()) {
            log.info("RocketMQ disabled, would publish order event: {}", event);
            return;
        }
        producer.send(event);
    }
}
