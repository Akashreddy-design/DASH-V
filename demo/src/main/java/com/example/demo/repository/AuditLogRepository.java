package com.example.demo.repository;

import com.example.demo.model.AuditLogs;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogs, String> {
}
