package com.example.onboarding.mq;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(RocketMqProperties.class)
public class RocketMqLifecycle implements SmartLifecycle {

    private final RocketMqOrderProducer producer;
    private final RocketMqOrderConsumer consumer;
    private boolean running;

    public RocketMqLifecycle(RocketMqOrderProducer producer, RocketMqOrderConsumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    @Override
    public void start() {
        try {
            producer.start();
            consumer.start();
            running = true;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to start RocketMQ client. Check app.rocketmq.* config.", ex);
        }
    }

    @Override
    public void stop() {
        consumer.shutdown();
        producer.shutdown();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
