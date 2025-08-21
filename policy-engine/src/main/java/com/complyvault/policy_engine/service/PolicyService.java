package com.complyvault.policy_engine.service;

import com.complyvault.policy_engine.db.PolicyEntity;
import com.complyvault.policy_engine.db.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository repo;

    @Transactional(readOnly = true)
    public List<PolicyEntity> listActive() {
        return repo.findByEnabledTrue();
    }

    @Transactional(readOnly = true)
    public List<PolicyEntity> listAll() {
        return repo.findAll();
    }

    @Transactional
    public PolicyEntity upsert(PolicyEntity incoming) {
        if (incoming.getType() != null) {
            incoming.setType(incoming.getType().toUpperCase());
        }
        PolicyEntity e = repo.findByRuleId(incoming.getRuleId())
                .map(existing -> {
                    existing.setVersion(incoming.getVersion());
                    existing.setType(incoming.getType());
                    existing.setNetworkEquals(incoming.getNetworkEquals());
                    existing.setField(incoming.getField());
                    existing.setPattern(incoming.getPattern());
                    existing.setDescription(incoming.getDescription());
                    existing.setEnabled(incoming.getEnabled() != null ? incoming.getEnabled() : existing.getEnabled());
                    return existing;
                })
                .orElseGet(() -> repo.save(incoming));
        return repo.save(e);
    }

    @Transactional
    public void setEnabled(String ruleId, boolean enabled) {
        PolicyEntity e = repo.findByRuleId(ruleId).orElseThrow();
        e.setEnabled(enabled);
        repo.save(e);
    }
}
