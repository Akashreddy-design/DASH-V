package com.complyvault.policy_engine.db;

import org.springframework.data.jpa.repository.JpaRepository;
import com.complyvault.policy_engine.db.ViolationEntity.Status;

import java.util.List;
import java.util.UUID;

public interface ViolationRepository extends JpaRepository<ViolationEntity, UUID> {



    List<ViolationEntity> findByRuleId(String ruleId);

    List<ViolationEntity> findByNetworkIgnoreCase(String network);

    List<ViolationEntity> findByMessageId(String messageId);

    List<ViolationEntity> findByStatus(Status status);


}
