package com.example.onboarding.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodLogAspect {

    private static final Logger log = LoggerFactory.getLogger(MethodLogAspect.class);

    @Around("within(com.example.onboarding.controller..*) || within(com.example.onboarding.service..*)")
    public Object logCost(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("method cost: {} {} ms", joinPoint.getSignature().toShortString(), cost);
        }
    }
}
