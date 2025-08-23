// src/main/java/com/navigation/normalization_service/store/SearchRepository.java
package com.navigation.normalization_service.store;

import com.navigation.normalization_service.model.CanonicalMessage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends ElasticsearchRepository<CanonicalMessage, String> {
    // no methods needed for now
}
