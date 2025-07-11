package com.jpmns.batch.domain.strategies.impl;

import com.jpmns.batch.domain.cache.interfaces.Cache;
import com.jpmns.batch.domain.entities.EmailTemplateEntity;
import com.jpmns.batch.domain.models.NotificationEventModel;
import com.jpmns.batch.domain.repositories.interfaces.EmailTemplateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailNotificationStrategyImplTest {

    private EmailTemplateRepository emailTemplateRepository;
    private Cache<String, EmailTemplateEntity> cache;
    private EmailNotificationStrategyImpl strategy;
    private NotificationEventModel event;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        emailTemplateRepository = mock(EmailTemplateRepository.class);
        cache = mock(Cache.class);
        strategy = new EmailNotificationStrategyImpl(emailTemplateRepository, cache);
        event = mock(NotificationEventModel.class);
    }

    @Test
    void shouldSendEmailIfTemplateExistsInRepository() throws Exception {
        when(event.name()).thenReturn("WELCOME");
        when(event.channel()).thenReturn("EMAIL");
        when(cache.get("EMAIL-WELCOME")).thenReturn(Optional.empty());

        EmailTemplateEntity template = new EmailTemplateEntity();
        when(emailTemplateRepository.findByNameAndActiveTrue("WELCOME"))
                .thenReturn(Optional.of(template));

        assertDoesNotThrow(() -> strategy.send(event));

        verify(cache).get("EMAIL-WELCOME");
        verify(emailTemplateRepository).findByNameAndActiveTrue("WELCOME");
        verify(cache).set("WELCOME", template);
    }

    @Test
    void shouldThrowExceptionIfTemplateDoesNotExistAnywhere() {
        when(event.name()).thenReturn("NOT_FOUND");
        when(event.channel()).thenReturn("EMAIL");
        when(cache.get("EMAIL-NOT_FOUND")).thenReturn(Optional.empty());
        when(emailTemplateRepository.findByNameAndActiveTrue("NOT_FOUND"))
                .thenReturn(Optional.empty());

        Exception e = assertThrows(Exception.class, () -> strategy.send(event));
        assertEquals("Template is not found", e.getMessage());

        verify(cache).get("EMAIL-NOT_FOUND");
        verify(emailTemplateRepository).findByNameAndActiveTrue("NOT_FOUND");
        verify(cache, never()).set(any(), any());
    }

    @Test
    void shouldUseTemplateFromCacheAndNotCallRepository() throws Exception {
        when(event.name()).thenReturn("CACHED");
        when(event.channel()).thenReturn("EMAIL");
        EmailTemplateEntity cachedTemplate = new EmailTemplateEntity();
        when(cache.get("EMAIL-CACHED")).thenReturn(Optional.of(cachedTemplate));

        assertDoesNotThrow(() -> strategy.send(event));

        verify(cache).get("EMAIL-CACHED");
        verifyNoInteractions(emailTemplateRepository);
    }
}
