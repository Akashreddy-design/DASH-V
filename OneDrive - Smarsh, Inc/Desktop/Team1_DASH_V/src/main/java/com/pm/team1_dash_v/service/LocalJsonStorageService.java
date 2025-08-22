package com.pm.team1_dash_v.service;
import com.pm.team1_dash_v.interfaces.StorageServiceInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LocalJsonStorageService implements StorageServiceInterface {

    private final Path baseDir;
    // One lock per file to avoid interleaving lines under parallel load
    private final ConcurrentHashMap<Path, ReentrantLock> locks = new ConcurrentHashMap<>();

    public LocalJsonStorageService(
            @Value("${cv.storage.local.base-dir:./data}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public void append(String network, String tenantId, String messageId, String jsonRaw) {
        try {
            // Partition by network/tenant/date; write to a daily NDJSON file
            LocalDate today = LocalDate.now();
            String yyyy = String.valueOf(today.getYear());
            String mm   = String.format("%02d", today.getMonthValue());
            String dd   = String.format("%02d", today.getDayOfMonth());

            Path dir = baseDir.resolve(network).resolve(tenantId).resolve(yyyy).resolve(mm);
            Files.createDirectories(dir);

            String fileName = dd + ".ndjson"; // e.g., .../email/acme/2025/08/20.ndjson
            Path file = dir.resolve(fileName);

            // Each line: {"messageId":"...","ts":"2025-08-20","body":<raw JSON>}
            String envelope = "{\"messageId\":\"" + escape(messageId) + "\","
                    + "\"ts\":\"" + today.format(DateTimeFormatter.ISO_DATE) + "\","
                    + "\"body\":" + jsonRaw + "}\n";

            ReentrantLock lock = locks.computeIfAbsent(file, f -> new ReentrantLock());
            lock.lock();
            try (BufferedWriter bw = Files.newBufferedWriter(
                    file,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                bw.write(envelope);
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to local storage", e);
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
