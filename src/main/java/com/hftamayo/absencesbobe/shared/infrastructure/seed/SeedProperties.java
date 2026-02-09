package com.hftamayo.absencesbobe.shared.infrastructure.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "seed")
public record SeedProperties(
        boolean enabled,
        boolean failFast,
        List<String> catalogs
) {}