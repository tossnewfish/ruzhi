package com.example.onboarding.mq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RocketMqOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(RocketMqOrderConsumer.class);

    private final RocketMqProperties properties;
    private DefaultMQPushConsumer consumer;

    public RocketMqOrderConsumer(RocketMqProperties properties) {
        this.properties = properties;
    }

    public void start() throws MQClientException {
        if (!properties.isEnabled()) {
            return;
        }
        consumer = new DefaultMQPushConsumer(properties.getConsumerGroup());
        consumer.setNamesrvAddr(properties.getNamesrvAddr());
        consumer.subscribe(properties.getOrderTopic(), "*");
        consumer.registerMessageListener(new OrderMessageListener());
        consumer.start();
        log.info("RocketMQ consumer started, topic={}", properties.getOrderTopic());
    }

    public void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
        }
    }

    private static class OrderMessageListener implements MessageListenerConcurrently {
        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
            for (MessageExt message : messages) {
                String body = new String(message.getBody(), StandardCharsets.UTF_8);
                log.info("RocketMQ consumed message. topic={}, keys={}, body={}",
                        message.getTopic(),
                        message.getKeys(),
                        body
                );
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }
}
