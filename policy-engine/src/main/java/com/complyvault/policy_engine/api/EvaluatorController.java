package com.complyvault.policy_engine.api;

import com.complyvault.policy_engine.model.CanonicalMessage;
import com.complyvault.policy_engine.model.Violation;
import com.complyvault.policy_engine.service.PolicyEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluator")
@RequiredArgsConstructor
public class EvaluatorController {

    private final PolicyEvaluator evaluator;

    @PostMapping("/test")
    public List<Violation> evaluateMessage(@RequestBody CanonicalMessage message) {
        return evaluator.evaluate(message);
    }
}
