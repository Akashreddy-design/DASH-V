package com.pm.team1_dash_v.repository;
import com.pm.team1_dash_v.model.AuditLogs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogs, String> {
    // You can define custom query methods here if needed
}