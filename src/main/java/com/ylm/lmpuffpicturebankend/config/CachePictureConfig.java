package com.ylm.lmpuffpicturebankend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CachePictureConfig {

    @Bean
    public Cache<String, String> pictureCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10000)
                .expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES)
                .build();
    }
}
