package com.navigation.normalization_service.store;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class RawFileStore {

    // Base folder for raw messages
    private final Path baseDir = Paths.get("raw_messages");

    // yyyy/MM/dd for easy partitioning
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneOffset.UTC);

    public RawFileStore() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create raw storage directory: " + baseDir, e);
        }
    }

    /**
     * Stores raw JSON immutably under:
     *   raw_messages/{tenantIdSafe}/{yyyy/MM/dd}/{messageId}.json
     *
     * @return a raw URI reference like:
     *   raw://raw_messages/{tenantIdSafe}/{yyyy/MM/dd}/{messageId}.json
     */
    public String storeImmutable(String messageId, String tenantId, Instant timestampUtc, String rawJson) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId is required for immutable storage.");
        }
        String tenantIdSafe = sanitizeSegment(tenantId == null ? "unknown" : tenantId);
        String datePart = DATE_FMT.format(timestampUtc == null ? Instant.now() : timestampUtc);

        Path dir = baseDir.resolve(tenantIdSafe).resolve(datePart);
        Path finalPath = dir.resolve(messageId + ".json");

        try {
            Files.createDirectories(dir);

            // If it already exists, do NOT overwrite (immutability)
            if (Files.exists(finalPath)) {
                // Return existing reference (idempotent)
                return toRawUri(finalPath);
            }

            // Write to a temp file first, then atomically move
            Path tmp = Files.createTempFile(dir, messageId + ".", ".tmp");
            Files.write(tmp, rawJson.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tmp, finalPath, StandardCopyOption.ATOMIC_MOVE);

            // Best-effort: mark file read-only (cross-platform)
            makeReadOnly(finalPath);

            return toRawUri(finalPath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store raw message for id=" + messageId, e);
        }
    }

    private static String sanitizeSegment(String s) {
        // keep it simple: letters, numbers, dash, underscore
        String cleaned = s.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        return cleaned.isBlank() ? "unknown" : cleaned;
    }

    private static void makeReadOnly(Path path) {
        try {
            // On Windows:
            path.toFile().setReadOnly();
            // On POSIX (best-effort; ignore if not supported on Windows FS)
            try {
                Files.setPosixFilePermissions(path,
                        java.util.Set.of(
                                PosixFilePermission.OWNER_READ,
                                PosixFilePermission.GROUP_READ,
                                PosixFilePermission.OTHERS_READ
                        ));
            } catch (UnsupportedOperationException ignored) { /* non-POSIX FS */ }
        } catch (Exception ignored) { /* don't fail the pipeline just for perms */ }
    }

    private static String toRawUri(Path p) {
        // Normalize to forward slashes for consistency
        String norm = p.toString().replace(File.separatorChar, '/');
        return "raw://" + norm;
    }
}
