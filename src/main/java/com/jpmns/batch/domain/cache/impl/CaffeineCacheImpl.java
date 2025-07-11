package com.jpmns.batch.domain.cache.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.jpmns.batch.domain.cache.interfaces.Cache;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CaffeineCacheImpl<K, V> implements Cache<K, V> {

    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;

    public CaffeineCacheImpl() {
        this.cache = Caffeine.newBuilder()
                .build();
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public void set(K key, V value) {
        cache.put(key, value);
    }
}
