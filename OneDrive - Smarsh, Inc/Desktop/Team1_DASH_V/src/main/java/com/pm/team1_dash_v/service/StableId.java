package com.pm.team1_dash_v.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class StableId {
    private static final ObjectMapper M =
            new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public static String forEmail(String tenantId, String from, List<String> to, String subject, String body, String sentAt) {
        ObjectNode n = M.createObjectNode();
        n.put("tenantId", tenantId);
        n.put("network", "email");
        n.put("from", from);
        ArrayNode toArr = n.putArray("to");
        to.forEach(toArr::add);
        n.put("subject", subject);
        n.put("body", body);
        n.put("sentAt", sentAt);
        return sha256(json(n));
    }

    public static String forSlack(String tenantId, String user, String text, String timestamp, String team, String channel) {
        ObjectNode n = M.createObjectNode();
        n.put("tenantId", tenantId);
        n.put("network", "slack");
        n.put("user", user);
        n.put("text", text);
        n.put("timestamp", timestamp);
        n.put("team", team);
        n.put("channel", channel);
        return sha256(json(n));
    }

    private static String json(ObjectNode n) {
        try { return M.writeValueAsString(n); } catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
