package com.complyvault.policy_engine.Mapper;

public final class ViolationMapper {
    private ViolationMapper() {}

    public static com.complyvault.policy_engine.model.Violation toDto(
            com.complyvault.policy_engine.db.ViolationEntity e) {
        return com.complyvault.policy_engine.model.Violation.builder()
                .id(e.getId())
                .messageId(e.getMessageId())
                .ruleId(e.getRuleId())
                .field(e.getField())
                .matchedSample(e.getMatchedSample())
                .description(e.getDescription())
                .detectedAt(e.getDetectedAt())
                .build();
    }
}
