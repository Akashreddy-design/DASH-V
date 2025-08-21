package com.complyvault.policy_engine.service;

import com.complyvault.policy_engine.db.ViolationEntity;
import com.complyvault.policy_engine.model.CanonicalMessage;
import com.complyvault.policy_engine.model.Violation;
import com.complyvault.policy_engine.model.policy.Policy;
import com.complyvault.policy_engine.service.PolicyLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyEvaluator {

    private final PolicyLoader policyLoader;
    private final ViolationService violationService;

    public List<Violation> evaluate(CanonicalMessage message) {
        var violations = new ArrayList<Violation>();
        var toPersist = new ArrayList<ViolationEntity>();

        for (Policy policy : policyLoader.getPolicies()) {
            try {
                // 1. Check "when" condition (network)
                if (policy.getWhen() != null &&
                        policy.getWhen().getNetworkEquals() != null &&
                        !policy.getWhen().getNetworkEquals().equalsIgnoreCase(message.getNetwork())) {
                    continue; // skip this policy
                }

                // 2. Extract the field value from message
                String fieldValue = extractField(message, policy.getField());
                if (fieldValue == null) continue;

                // 3. Apply regex
                Pattern regex = Pattern.compile(policy.getPattern());
                Matcher matcher = regex.matcher(fieldValue);

                if (matcher.find()) {
                    Instant now= Instant.now();
                   violations.add(Violation.builder()
                            .messageId(message.getMessageId())
                            .ruleId(policy.getRuleId())
                            .field(policy.getField())
                            .matchedSample(matcher.group())
                            .description(policy.getDescription())
                            .detectedAt(now)
                            .build());

                    // ENTITY for DB

                    toPersist.add(ViolationEntity.builder()
                            .messageId(message.getMessageId())
                            .ruleId(policy.getRuleId())
                            .field(policy.getField())
                            .matchedSample(matcher.group())
                            .description(policy.getDescription())
                            .network(message.getNetwork())
                            .detectedAt(now)
                            // .status defaults to NEW in your entity; set explicitly if needed:
                            .status(ViolationEntity.Status.FLAGGED)
                            .build());
                }
            } catch (Exception e) {
                log.error("Error evaluating policy {}: {}", policy.getRuleId(), e.getMessage());
            }
        }
        if (!toPersist.isEmpty()) {
            violationService.saveAll(toPersist);
        }

        return violations;
    }

    private String extractField(CanonicalMessage msg, String fieldName) {
        String f = fieldName == null ? "" : fieldName.toLowerCase();


        return switch (f) {
            case "subject"    -> msg.getSubject();
            case "body", "text" -> msg.getBody();           // text -> body (compat)
            case "from", "sender" -> msg.getSender();
            case "recipients" -> msg.getRecipients() != null ? String.join(",", msg.getRecipients()) : null;
            default -> null;
        };
    }


}
