package com.complyvault.policy_engine.api;

import com.complyvault.policy_engine.db.PolicyEntity;
import com.complyvault.policy_engine.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyAdminController {

    private final PolicyService service;

    // List all (or make another endpoint for only active)
    @GetMapping
    public List<PolicyEntity> list() {
        return service.listAll();
    }

    // Create or update
    @PostMapping
    public PolicyEntity upsert(@RequestBody PolicyEntity body) {
        return service.upsert(body);
    }

    // Toggle enabled
    @PostMapping("/{ruleId}/enable")
    public void enable(@PathVariable String ruleId) {
        service.setEnabled(ruleId, true);
    }

    @PostMapping("/{ruleId}/disable")
    public void disable(@PathVariable String ruleId) {
        service.setEnabled(ruleId, false);
    }
}
