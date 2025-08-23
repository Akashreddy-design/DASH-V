package com.common.audit.service.impl;

import com.common.audit.service.PayloadSanitizer;
import org.springframework.stereotype.Component;

@Component
public class NoopPayloadSanitizer implements PayloadSanitizer {
    @Override public String sanitize(String raw) { return raw; }
}