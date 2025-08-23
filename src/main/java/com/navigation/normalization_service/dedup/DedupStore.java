// src/main/java/com/navigation/normalization_service/dedupe/DedupStore.java
package com.navigation.normalization_service.dedup;

public interface DedupStore {
    /** @return true if this key is new (NOT a duplicate). */
    boolean recordIfNew(String key);
}
