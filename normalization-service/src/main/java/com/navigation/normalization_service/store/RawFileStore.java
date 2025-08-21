package com.navigation.normalization_service.store;

import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class RawFileStore {
    private final String storagePath = "raw_messages/";

    public RawFileStore() {
        new File(storagePath).mkdirs();
    }

    public String store(String rawData) throws Exception {
        String fileName = UUID.randomUUID().toString() + ".json";
        Path filePath = Paths.get(storagePath + fileName);
        Files.write(filePath, rawData.getBytes());
        return filePath.toString();
    }
}