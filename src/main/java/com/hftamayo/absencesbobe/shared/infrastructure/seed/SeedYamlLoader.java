package com.hftamayo.absencesbobe.shared.infrastructure.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class SeedYamlLoader {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public <T> T loadFromClasspath(String classpathLocation, Class<T> type) {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            return yamlMapper.readValue(in, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load seed resource: " + classpathLocation, e);
        }
    }
}