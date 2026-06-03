package com.example.onboarding.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class RocketMqOrderProducer {

    private static final Logger log = LoggerFactory.getLogger(RocketMqOrderProducer.class);

    private final RocketMqProperties properties;
    private final ObjectMapper objectMapper;
    private DefaultMQProducer producer;

    public RocketMqOrderProducer(RocketMqProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public void start() throws MQClientException {
        if (!properties.isEnabled()) {
            return;
        }
        producer = new DefaultMQProducer(properties.getProducerGroup());
        producer.setNamesrvAddr(properties.getNamesrvAddr());
        producer.start();
        log.info("RocketMQ producer started, namesrv={}", properties.getNamesrvAddr());
    }

    public void send(OrderEvent event) {
        if (producer == null) {
            log.info("RocketMQ producer is not started, skip event: {}", event);
            return;
        }
        try {
            byte[] body = objectMapper.writeValueAsString(event).getBytes(StandardCharsets.UTF_8);
            Message message = new Message(properties.getOrderTopic(), "ORDER_STATUS", event.orderNo(), body);
            SendResult result = producer.send(message);
            log.info("RocketMQ send result: {}", result);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize order event", ex);
        } catch (Exception ex) {
            log.warn("Failed to send RocketMQ event {}: {}", event, ex.getMessage());
        }
    }

    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
