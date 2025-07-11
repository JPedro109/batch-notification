package com.jpmns.batch.domain.repositories.interfaces;

import com.jpmns.batch.domain.entities.SMSTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SMSTemplateRepository extends JpaRepository<SMSTemplateEntity, UUID> {

    Optional<SMSTemplateEntity> findByNameAndActiveTrue(String name);
}
