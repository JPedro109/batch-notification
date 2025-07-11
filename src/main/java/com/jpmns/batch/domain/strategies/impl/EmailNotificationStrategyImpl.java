package com.jpmns.batch.domain.strategies.impl;

import com.jpmns.batch.domain.cache.interfaces.Cache;
import com.jpmns.batch.domain.entities.EmailTemplateEntity;
import com.jpmns.batch.domain.models.NotificationEventModel;
import com.jpmns.batch.domain.repositories.interfaces.EmailTemplateRepository;
import com.jpmns.batch.domain.strategies.interfaces.NotificationStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("EMAIL")
public class EmailNotificationStrategyImpl implements NotificationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationStrategyImpl.class);

    private final EmailTemplateRepository emailTemplateRepository;
    private final Cache<String, EmailTemplateEntity> cache;

    public EmailNotificationStrategyImpl(
            EmailTemplateRepository emailTemplateRepository,
            Cache<String, EmailTemplateEntity> cache
    ) {
        this.emailTemplateRepository = emailTemplateRepository;
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
        Optional<EmailTemplateEntity> template = cache.get(key);

        if(template.isEmpty()) {
            template = emailTemplateRepository.findByNameAndActiveTrue(name);

            if(template.isEmpty()) {
                throw new Exception("Template is not found");
            }

            cache.set(name, template.get());
        }

        EmailTemplateEntity emailTemplateEntity = template.get();
        String content = "";

        if(emailTemplateEntity.getVariables() != null) {
            for (String variable : emailTemplateEntity.getVariables()) {
                String placeholder = "{{" + variable + "}}";
                if(content.isEmpty()) {
                    content = emailTemplateEntity.getBodyHtml().replace(placeholder, event.data().get(variable));
                } else {
                    content = content.replace(placeholder, event.data().get(variable));
                }
            }
        }

        // TODO: If an API were called, there would be a ternary to define whether the content would be empty, if it was, the value of the body html would be sent.

        Thread.sleep(2000);
        logger.info("Send EMAIL with template {} to user {}", emailTemplateEntity.getName(), event.userId());
    }
}
