package com.pm.team1_dash_v.interfaces;

public interface StorageServiceInterface {
    /**
     * @param network  "email" or "slack"
     * @param tenantId partition per tenant
     * @param messageId used for traceability (also stored as header)
     * @param jsonRaw   the full request body as a compact JSON string
     */
    void append(String network, String tenantId, String messageId, String jsonRaw);
}
