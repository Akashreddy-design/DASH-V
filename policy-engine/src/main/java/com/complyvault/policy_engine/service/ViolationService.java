package com.complyvault.policy_engine.service;

import com.complyvault.policy_engine.Mapper.ViolationMapper;
import com.complyvault.policy_engine.db.ViolationEntity;
import com.complyvault.policy_engine.db.ViolationRepository;
import com.complyvault.policy_engine.model.Violation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ViolationRepository repo;

    public void saveAll(List<ViolationEntity> violations) {
        repo.saveAll(violations);
    }

    public List<ViolationEntity> byRule(String ruleId) {
        return repo.findByRuleId(ruleId);
    }

    public List<ViolationEntity> byNetwork(String network) {
        return repo.findByNetworkIgnoreCase(network);
    }

    public List<ViolationEntity> byMessageId(String messageId) {
        return repo.findByMessageId(messageId);
    }

    public List<Violation> list(String statusOpt) {
        List<ViolationEntity> rows;
        if (statusOpt == null || statusOpt.isBlank()) {
            rows = repo.findAll();
        } else {
            rows =repo.findByStatus(ViolationEntity.Status.valueOf(statusOpt.toUpperCase()));
        }
        return rows.stream().map(ViolationMapper::toDto).toList();
    }

    public Violation markAsReviewed(UUID id) {
        var v = repo.findById(id).orElseThrow(() -> new RuntimeException("Violation not found"));
        v.setStatus(ViolationEntity.Status.REVIEWED);
        return ViolationMapper.toDto(repo.save(v));
    }



}
