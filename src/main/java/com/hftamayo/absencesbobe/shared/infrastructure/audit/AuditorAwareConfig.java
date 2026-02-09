package com.hftamayo.absencesbobe.shared.infrastructure.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        // TODO: Replace with real authenticated user id once auth module is ready
        return () -> Optional.of(0L);
    }
}