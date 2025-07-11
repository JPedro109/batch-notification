package com.jpmns.batch.domain.services.interfaces;

import com.jpmns.batch.domain.models.NotificationEventModel;

public interface NotificationService {
    void sendNotification(NotificationEventModel notificationEventModel) throws Exception;
}
