package com.jpmns.batch.domain.strategies.impl;

import com.jpmns.batch.domain.cache.interfaces.Cache;
import com.jpmns.batch.domain.entities.SMSTemplateEntity;
import com.jpmns.batch.domain.models.NotificationEventModel;
import com.jpmns.batch.domain.repositories.interfaces.SMSTemplateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SMSNotificationStrategyImplTest {

    private SMSTemplateRepository smsTemplateRepository;
    private Cache<String, SMSTemplateEntity> cache;
    private SMSNotificationStrategyImpl strategy;
    private NotificationEventModel event;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        smsTemplateRepository = mock(SMSTemplateRepository.class);
        cache = mock(Cache.class);
        strategy = new SMSNotificationStrategyImpl(smsTemplateRepository, cache);
        event = mock(NotificationEventModel.class);
    }

    @Test
    void shouldSendSMSIfTemplateExistsInRepository() throws Exception {
        when(event.name()).thenReturn("OTP_SMS");
        when(cache.get("OTP_SMS")).thenReturn(Optional.empty());

        SMSTemplateEntity template = new SMSTemplateEntity();
        when(smsTemplateRepository.findByNameAndActiveTrue("OTP_SMS"))
                .thenReturn(Optional.of(template));

        assertDoesNotThrow(() -> strategy.send(event));

        verify(cache).get("OTP_SMS");
        verify(smsTemplateRepository).findByNameAndActiveTrue("OTP_SMS");
        verify(cache).set("OTP_SMS", template);
    }

    @Test
    void shouldThrowExceptionIfTemplateDoesNotExistAnywhere() {
        when(event.name()).thenReturn("UNKNOWN_SMS");
        when(cache.get("UNKNOWN_SMS")).thenReturn(Optional.empty());
        when(smsTemplateRepository.findByNameAndActiveTrue("UNKNOWN_SMS"))
                .thenReturn(Optional.empty());

        Exception e = assertThrows(Exception.class, () -> strategy.send(event));
        assertEquals("Template is not found", e.getMessage());

        verify(cache).get("UNKNOWN_SMS");
        verify(smsTemplateRepository).findByNameAndActiveTrue("UNKNOWN_SMS");
        verify(cache, never()).set(any(), any());
    }

    @Test
    void shouldUseTemplateFromCacheAndNotCallRepository() throws Exception {
        when(event.name()).thenReturn("CACHED_SMS");
        SMSTemplateEntity cachedTemplate = new SMSTemplateEntity();
        when(cache.get("CACHED_SMS")).thenReturn(Optional.of(cachedTemplate));

        assertDoesNotThrow(() -> strategy.send(event));

        verify(cache).get("CACHED_SMS");
        verifyNoInteractions(smsTemplateRepository);
    }
}
