package com.complyvault.policy_engine.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<PolicyEntity, UUID> {
    Optional<PolicyEntity> findByRuleId(String ruleId);
    List<PolicyEntity> findByEnabledTrue();
}
