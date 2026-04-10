package com.amarnath.shopkart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@EnableCaching
@Configuration
public class CacheConfig {

    public static final String CACHE_CATEGORIES   = "categories";
    public static final String CACHE_PRODUCTS     = "products";
    public static final String CACHE_PRODUCT_LIST = "product-list";
    public static final String CACHE_USERS        = "users";
    public static final String CACHE_CART         = "cart";

    private GenericJackson2JsonRedisSerializer buildSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return GenericJackson2JsonRedisSerializer.builder()
                .objectMapper(mapper)
                .build();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        GenericJackson2JsonRedisSerializer serializer = buildSerializer();

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CACHE_CATEGORIES,   base.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put(CACHE_PRODUCTS,     base.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put(CACHE_PRODUCT_LIST, base.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put(CACHE_USERS,        base.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put(CACHE_CART,         base.entryTtl(Duration.ofDays(7)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(base.entryTtl(Duration.ofMinutes(30)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}