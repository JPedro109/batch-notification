package com.jpmns.batch.domain.services;

import com.jpmns.batch.domain.models.NotificationEventModel;
import com.jpmns.batch.domain.services.impl.NotificationServiceImpl;
import com.jpmns.batch.domain.services.interfaces.NotificationService;
import com.jpmns.batch.domain.strategies.factories.NotificationStrategyFactory;
import com.jpmns.batch.domain.strategies.interfaces.NotificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    private NotificationStrategyFactory factory;
    private NotificationStrategy strategy;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        factory = mock(NotificationStrategyFactory.class);
        strategy = mock(NotificationStrategy.class);
        service = new NotificationServiceImpl(factory);
    }

    @Test
    void shouldSendNotificationWhenEventIsValid() throws Exception {
        NotificationEventModel event = new NotificationEventModel("EMAIL", "WELCOME", "10128862-a940-465e-93ab-e7627651eab9", "email@test.com", Map.of("username", "joao"));

        when(factory.getStrategy("EMAIL")).thenReturn(strategy);

        service.sendNotification(event);

        verify(factory).getStrategy("EMAIL");
        verify(strategy).send(event);
    }

    @Test
    void shouldThrowExceptionWhenEventChannelIsNull() throws Exception {
        NotificationEventModel event = new NotificationEventModel(null, "WELCOME", "10128862-a940-465e-93ab-e7627651eab9", "email@test.com", Map.of());

        Exception exception = assertThrows(Exception.class, () -> service.sendNotification(event));
        assertEquals("Event model malformed", exception.getMessage());

        verify(factory, never()).getStrategy(any());
        verify(strategy, never()).send(any());
    }

    @Test
    void shouldThrowExceptionWhenEventNameIsNull() throws Exception {
        NotificationEventModel event = new NotificationEventModel("EMAIL", null, "10128862-a940-465e-93ab-e7627651eab9", "email@test.com", Map.of());

        Exception exception = assertThrows(Exception.class, () -> service.sendNotification(event));
        assertEquals("Event model malformed", exception.getMessage());

        verify(factory, never()).getStrategy(any());
        verify(strategy, never()).send(any());
    }
}
