package com.hftamayo.absencesbobe.features.seeding.adapters.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@AllArgsConstructor
public class SeedYamlLoader {
    private final ObjectMapper yamlMapper;

    public <T> T loadFromClasspath(String classpathLocation, Class<T> type) {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            return yamlMapper.readValue(in, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load seed resource: " + classpathLocation, e);
        }
    }
}