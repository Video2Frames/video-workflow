package com.store.workflowService.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Component
public class MappingLogger {
    private static final Logger log = LoggerFactory.getLogger(MappingLogger.class);

    private final List<RequestMappingHandlerMapping> mappings;

    public MappingLogger(List<RequestMappingHandlerMapping> mappings) {
        this.mappings = mappings;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logMappings() {
        int total = mappings.stream().mapToInt(m -> m.getHandlerMethods().size()).sum();
        log.info("Listing {} registered request mappings:", total);
        for (RequestMappingHandlerMapping m : mappings) {
            m.getHandlerMethods().forEach((k, v) -> log.info("Mapped {} -> {}", k, v));
        }
    }
}
