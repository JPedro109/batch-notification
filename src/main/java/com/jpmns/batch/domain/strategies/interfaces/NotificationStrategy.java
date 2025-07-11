package com.jpmns.batch.domain.strategies.interfaces;

import com.jpmns.batch.domain.models.NotificationEventModel;

public interface NotificationStrategy {
    void send(NotificationEventModel event) throws Exception;
}
