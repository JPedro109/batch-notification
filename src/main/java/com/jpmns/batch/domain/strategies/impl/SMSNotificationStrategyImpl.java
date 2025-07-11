package com.jpmns.batch.domain.strategies.impl;

import com.jpmns.batch.domain.cache.interfaces.Cache;
import com.jpmns.batch.domain.entities.SMSTemplateEntity;
import com.jpmns.batch.domain.models.NotificationEventModel;
import com.jpmns.batch.domain.repositories.interfaces.SMSTemplateRepository;
import com.jpmns.batch.domain.strategies.interfaces.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("SMS")
public class SMSNotificationStrategyImpl implements NotificationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(SMSNotificationStrategyImpl.class);

    private final SMSTemplateRepository smsTemplateRepository;
    private final Cache<String, SMSTemplateEntity> cache;

    public SMSNotificationStrategyImpl(
            SMSTemplateRepository smsTemplateRepository,
            Cache<String, SMSTemplateEntity> cache
    ) {
        this.smsTemplateRepository = smsTemplateRepository;
        this.cache = cache;
    }

    @Override
    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3
    )
    public void send(NotificationEventModel event) throws Exception {
        String name = event.name();
        String key = event.channel() + "-" + event.name();
        Optional<SMSTemplateEntity> template = cache.get(key);

        if(template.isEmpty()) {
            template = smsTemplateRepository.findByNameAndActiveTrue(name);

            if(template.isEmpty()) {
                throw new Exception("Template is not found");
            }

            cache.set(name, template.get());
        }

        SMSTemplateEntity smsTemplateEntity = template.get();
        String content = "";

        if(smsTemplateEntity.getVariables() != null) {
            for (String variable : smsTemplateEntity.getVariables()) {
                String placeholder = "{{" + variable + "}}";
                if(content.isEmpty()) {
                    content = smsTemplateEntity.getContent().replace(placeholder, event.data().get(variable));
                } else {
                    content = content.replace(placeholder, event.data().get(variable));
                }
            }
        }

        // TODO: If an API were called, there would be a ternary to define whether the content would be empty, if it was, the value of the content would be sent.

        Thread.sleep(2000);
        logger.info("Send SMS with template {} to user {}", smsTemplateEntity.getName(), event.userId());
    }
}
