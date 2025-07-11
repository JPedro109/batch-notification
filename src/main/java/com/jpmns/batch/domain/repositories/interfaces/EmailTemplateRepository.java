package com.jpmns.batch.domain.repositories.interfaces;

import com.jpmns.batch.domain.entities.EmailTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplateEntity, UUID> {

    Optional<EmailTemplateEntity> findByNameAndActiveTrue(String name);
}
