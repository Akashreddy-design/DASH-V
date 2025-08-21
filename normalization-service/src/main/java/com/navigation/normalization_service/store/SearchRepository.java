package com.navigation.normalization_service.store;

import com.navigation.normalization_service.model.CanonicalMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

@Service
public interface SearchRepository extends ElasticsearchRepository<CanonicalMessage, String> {
}
