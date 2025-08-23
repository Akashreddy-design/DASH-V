package com.navigation.normalization_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigation.normalization_service.dedup.DedupStore;
import com.navigation.normalization_service.model.CanonicalMessage;
import com.navigation.normalization_service.store.RawFileStore;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;



@Service
public class NormalizationService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RawFileStore rawFileStore;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DedupStore dedupStore;

    private static final String POLICY_TOPIC = "canonical.policy.v1";
    private static final String ES_TOPIC = "canonical.es.v1";

    public NormalizationService(RawFileStore rawFileStore,
                                KafkaTemplate<String, String> kafkaTemplate,
                                DedupStore dedupStore) {
        this.rawFileStore = rawFileStore;
        this.kafkaTemplate = kafkaTemplate;
        this.dedupStore = dedupStore;
    }

    public void processAndStore(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);

            // 1) Dedup
            String messageId = get(root, "messageId");
            if (messageId.isBlank()) messageId = smallHash(dedupeKeyFrom(root));
            if (!dedupStore.recordIfNew(messageId)) {
                System.out.println("Duplicate detected (id=" + messageId + "). Skipping.");
                return;
            }

            // 2) Raw store (timestamp-free)
            String tenantId = get(root, "tenantId");
            String rawFileRef = rawFileStore.storeImmutable(messageId, tenantId, null, rawJson);

            // 3) Canonical
            CanonicalMessage c = toCanonical(root, rawFileRef);
            c.setId(messageId);
            c.setMessageId(messageId);
            c.setTenantId(tenantId);
            c.setNetwork(get(root, "network"));

            // 4) Produce to Policy Engine only
            String canonicalJson = mapper.writeValueAsString(c);
            kafkaTemplate.send(POLICY_TOPIC, messageId, canonicalJson);

            System.out.println("Canonical produced to " + POLICY_TOPIC + " for id=" + messageId);

        kafkaTemplate.send(ES_TOPIC,messageId,canonicalJson);

        } catch (Exception e) {
            throw new RuntimeException("Normalization failed.", e);
        }
    }

    // ---- tiny helpers ----
    private CanonicalMessage toCanonical(JsonNode root, String rawFileRef) {
        CanonicalMessage c = new CanonicalMessage();
        c.setRawFileRef(rawFileRef);

        String network = get(root, "network");

        if ("email".equalsIgnoreCase(network)) {
            JsonNode p = root.path("payload");
            c.setSender(get(p, "from"));
            c.setSubject(get(p, "subject"));
            c.setBody(get(p, "body"));
            c.setRecipients(listFromArray(p.path("to")));
        } else if ("slack".equalsIgnoreCase(network)) {
            c.setSender(get(root, "user"));
            c.setBody(get(root, "text"));
            c.setTeam(get(root, "team"));
            List<String> r = new ArrayList<>();
            String ch = get(root, "channel"); if (!ch.isEmpty()) r.add(ch);
            c.setRecipients(r);
            String providedRef = get(root, "rawReference");
            if (!providedRef.isEmpty()) c.setRawFileRef(providedRef);
        }
        return c;
    }

    private String get(JsonNode n, String field) {
        JsonNode v = (n == null) ? null : n.get(field);
        return (v == null || v.isNull()) ? "" : v.asText("");
    }

    private List<String> listFromArray(JsonNode arr) {
        List<String> out = new ArrayList<>();
        if (arr != null && arr.isArray()) for (JsonNode x : arr) {
            String s = x.asText(""); if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private String dedupeKeyFrom(JsonNode root) {
        String network = get(root, "network");
        String tenant  = get(root, "tenantId");
        String sender  = "email".equalsIgnoreCase(network) ? get(root.path("payload"), "from") : get(root, "user");
        String body    = "email".equalsIgnoreCase(network) ? get(root.path("payload"), "body") : get(root, "text");
        String ts      = "email".equalsIgnoreCase(network) ? get(root.path("payload"), "sentAt") : get(root, "timestamp");
        return String.join("|", tenant, network, sender, ts, body);
    }

    private String smallHash(String s) throws Exception {
        byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 16; i++) {
            String x = Integer.toHexString(0xff & h[i]);
            if (x.length() == 1) sb.append('0');
            sb.append(x);
        }
        return sb.toString();
    }
}
