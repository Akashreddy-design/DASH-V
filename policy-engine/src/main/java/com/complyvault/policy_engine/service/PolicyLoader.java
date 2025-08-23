package com.complyvault.policy_engine.service;

import com.complyvault.policy_engine.model.policy.Policy;
import com.complyvault.policy_engine.model.policy.PolicyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyLoader {

    private final PolicyService policyService;

    @PostConstruct
    public void loadPolicies() {

        var entities = policyService.listActive();      // from DB
        this.policies = entities.stream().map(e -> {
            var p = new Policy();
            p.setRuleId(e.getRuleId());
            p.setVersion(e.getVersion());
            p.setType(PolicyType.valueOf(e.getType().toUpperCase()));
            p.setField("text".equalsIgnoreCase(e.getField()) ? "body" : e.getField());
            p.setPattern(e.getPattern());
            p.setDescription(e.getDescription());
            var w = new Policy.When();
            w.setNetworkEquals(e.getNetworkEquals());
            p.setWhen(w);
            return p;
        }).toList();
        log.info("✅ Loaded {} policy(ies) from DB", policies.size());
    }
    private final ObjectMapper objectMapper;

    private List<Policy> policies = new ArrayList<>();

    public List<Policy> getPolicies() {
        return policies;
    }


}
