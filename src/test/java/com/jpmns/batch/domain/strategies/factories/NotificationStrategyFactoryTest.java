package com.jpmns.batch.domain.strategies.factories;

import com.jpmns.batch.domain.strategies.interfaces.NotificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationStrategyFactoryTest {

    private NotificationStrategy emailStrategy;
    private NotificationStrategy smsStrategy;
    private NotificationStrategyFactory factory;

    @BeforeEach
    void setUp() {
        emailStrategy = mock(NotificationStrategy.class);
        smsStrategy = mock(NotificationStrategy.class);
        Map<String, NotificationStrategy> strategies = Map.of(
                "EMAIL", emailStrategy,
                "SMS", smsStrategy
        );
        factory = new NotificationStrategyFactory(strategies);
    }

    @Test
    void shouldReturnEmailStrategy() {
        NotificationStrategy result = factory.getStrategy("email");

        assertSame(emailStrategy, result);
    }

    @Test
    void shouldReturnSMSStrategy() {
        NotificationStrategy result = factory.getStrategy("SMS");

        assertSame(smsStrategy, result);
    }

    @Test
    void shouldThrowExceptionForInvalidChannel() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy("WHATSAPP")
        );

        assertEquals("Invalid channel: WHATSAPP", exception.getMessage());
    }
}
