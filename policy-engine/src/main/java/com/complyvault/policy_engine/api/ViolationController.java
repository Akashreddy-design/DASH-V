package com.complyvault.policy_engine.api;

import com.complyvault.policy_engine.db.ViolationEntity;
import com.complyvault.policy_engine.model.Violation;
import com.complyvault.policy_engine.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService service;



    @GetMapping("/rule/{ruleId}")
    public List<ViolationEntity> byRule(@PathVariable String ruleId) {
        return service.byRule(ruleId);
    }

    @GetMapping("/network/{network}")
    public List<ViolationEntity> byNetwork(@PathVariable String network) {
        return service.byNetwork(network);
    }

    @GetMapping("/message/{messageId}")
    public List<ViolationEntity> byMessage(@PathVariable String messageId) {
        return service.byMessageId(messageId);
    }

    @GetMapping()
    public List<Violation> list(@RequestParam(required = false) String status) {
        return service.list(status); // your method already handles null/blank
    }

    @PostMapping("/{id}/review")
    public Violation review(@PathVariable UUID id) {
        return service.markAsReviewed(id); // returns updated DTO
    }
}
