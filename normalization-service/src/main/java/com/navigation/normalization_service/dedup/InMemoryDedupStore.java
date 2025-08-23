// src/main/java/com/navigation/normalization_service/dedupe/InMemoryDedupStore.java
package com.navigation.normalization_service.dedup;

import com.navigation.normalization_service.dedup.DedupStore;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDedupStore implements DedupStore {
    private final Set<String> seen = ConcurrentHashMap.newKeySet();
    @Override public boolean recordIfNew(String key) {
        if (key == null || key.isBlank()) return true; // if no key, don't hard-fail
        return seen.add(key);
    }
}
