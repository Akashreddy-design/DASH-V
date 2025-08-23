package com.dharani.ingestion.repository;

import com.dharani.ingestion.model.AuditLogs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogs, String> {
    // You can define custom query methods here if needed
}