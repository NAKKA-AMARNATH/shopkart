package com.amarnath.shopkart.utils;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderNumberGenerator {

    private static final AtomicInteger counter = new AtomicInteger(0);

    private OrderNumberGenerator() {}

    public static String generate() {
        int count = counter.incrementAndGet();
        return String.format("SK-%d-%05d",
                LocalDateTime.now().getYear(), count);
    }
}