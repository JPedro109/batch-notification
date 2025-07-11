package com.jpmns.batch.domain.cache.impl;

import com.jpmns.batch.domain.cache.interfaces.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CaffeineCacheImplTest {

    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new CaffeineCacheImpl<>();
    }

    @Test
    void shouldReturnEmptyIfKeyNotPresent() {
        Optional<String> result = cache.get("missing-key");
        assertTrue(result.isEmpty(), "Expected empty result for missing key");
    }

    @Test
    void shouldStoreAndRetrieveValueSuccessfully() {
        cache.set("greeting", "hello");

        Optional<String> result = cache.get("greeting");
        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
    }

    @Test
    void shouldOverrideValueIfKeyAlreadyExists() {
        cache.set("key", "value1");
        cache.set("key", "value2");

        Optional<String> result = cache.get("key");
        assertTrue(result.isPresent());
        assertEquals("value2", result.get());
    }
}
