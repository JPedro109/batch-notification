package com.jpmns.batch.domain.strategies.factories;

import com.jpmns.batch.domain.strategies.interfaces.NotificationStrategy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationStrategyFactory {
    private final Map<String, NotificationStrategy> strategies;

    public NotificationStrategyFactory(Map<String, NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public NotificationStrategy getStrategy(String channel) {
        NotificationStrategy strategy = strategies.get(channel.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Invalid channel: " + channel);
        }
        return strategy;
    }
}
