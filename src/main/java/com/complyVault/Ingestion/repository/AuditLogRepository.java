package com.complyVault.Ingestion.repository;

import com.complyVault.Ingestion.model.audits.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
}