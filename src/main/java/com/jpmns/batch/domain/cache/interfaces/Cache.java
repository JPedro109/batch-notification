package com.jpmns.batch.domain.cache.interfaces;

import java.util.Optional;

public interface Cache<K, V> {

    Optional<V> get(K key);

    void set(K key, V value);
}
