package com.jpmns.batch.domain.services.impl;

import com.jpmns.batch.domain.models.NotificationEventModel;
import com.jpmns.batch.domain.services.interfaces.NotificationService;
import com.jpmns.batch.domain.strategies.factories.NotificationStrategyFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationStrategyFactory notificationStrategyFactory;

    public NotificationServiceImpl(NotificationStrategyFactory notificationStrategyFactory) {
        this.notificationStrategyFactory = notificationStrategyFactory;
    }

    @Override
    public void sendNotification(NotificationEventModel event) throws Exception {
        if(event.name() == null || event.channel() == null) {
            throw new Exception("Event model malformed");
        }
        notificationStrategyFactory
                .getStrategy(event.channel())
                .send(event);
    }
}
