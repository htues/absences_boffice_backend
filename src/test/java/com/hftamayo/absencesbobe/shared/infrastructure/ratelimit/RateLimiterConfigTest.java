package com.hftamayo.absencesbobe.shared.infrastructure.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterConfigTest {

    @Test
    @DisplayName("defaults: exposes the built-in fallback values")
    void defaults_exposeBuiltInValues() {
        RateLimiterConfig config = new RateLimiterConfig();

        assertEquals(100L, config.getCapacity());
        assertEquals(10L, config.getRefillRate());
        assertEquals(Duration.ofMinutes(1), config.getRefillDuration());
    }

    @Test
    @DisplayName("loadConfigurationFromProperties: updates the default values from a property map")
    void loadConfigurationFromProperties_updatesDefaults() {
        RateLimiterConfig config = new RateLimiterConfig();
        Map<String, Object> properties = new HashMap<>();
        properties.put("rate.limiter.default.capacity", 250);
        properties.put("rate.limiter.default.refill-rate", "25");
        properties.put("rate.limiter.default.refill-duration", "PT2M");

        config.loadConfigurationFromProperties(properties);

        assertEquals(250L, config.getCapacity());
        assertEquals(25L, config.getRefillRate());
        assertEquals(Duration.ofMinutes(2), config.getRefillDuration());
    }

    @Test
    @DisplayName("loadConfigurationFromProperties: ignores null input")
    void loadConfigurationFromProperties_nullInput_doesNothing() {
        RateLimiterConfig config = new RateLimiterConfig();

        config.loadConfigurationFromProperties(null);

        assertEquals(100L, config.getCapacity());
        assertEquals(10L, config.getRefillRate());
        assertEquals(Duration.ofMinutes(1), config.getRefillDuration());
    }

    @Test
    @DisplayName("loadEndpointConfiguration: stores endpoint-specific overrides")
    void loadEndpointConfiguration_storesEndpointOverrides() {
        RateLimiterConfig config = new RateLimiterConfig();
        Map<String, Object> properties = new HashMap<>();
        properties.put("rate.limiter.endpoints./api/v1/companies.capacity", "12");
        properties.put("rate.limiter.endpoints./api/v1/companies.refill-rate", 3);
        properties.put("rate.limiter.endpoints./api/v1/companies.refill-duration", "PT45S");

        config.loadEndpointConfiguration("/api/v1/companies", properties);

        RateLimiterConfig endpointConfig = config.getEndpointConfig("/api/v1/companies");
        assertNotSame(config, endpointConfig);
        assertEquals(12L, endpointConfig.getCapacity());
        assertEquals(3L, endpointConfig.getRefillRate());
        assertEquals(Duration.ofSeconds(45), endpointConfig.getRefillDuration());
    }

    @Test
    @DisplayName("loadEndpointConfiguration: ignores null endpoint or properties")
    void loadEndpointConfiguration_nullInput_doesNothing() {
        RateLimiterConfig config = new RateLimiterConfig();

        config.loadEndpointConfiguration(null, Map.of("rate.limiter.endpoints.x.capacity", 1));
        config.loadEndpointConfiguration("/api/v1/companies", null);

        assertTrue(config.getEndpointConfigs().isEmpty());
    }

    @Test
    @DisplayName("loadUserConfiguration: stores user-specific overrides")
    void loadUserConfiguration_storesUserOverrides() {
        RateLimiterConfig config = new RateLimiterConfig();
        Map<String, Object> properties = new HashMap<>();
        properties.put("rate.limiter.users.ADMIN.capacity", 8);
        properties.put("rate.limiter.users.ADMIN.refill-rate", "2");
        properties.put("rate.limiter.users.ADMIN.refill-duration", "PT1M");

        config.loadUserConfiguration("ADMIN", properties);

        RateLimiterConfig userConfig = config.getUserConfig("ADMIN");
        assertNotSame(config, userConfig);
        assertEquals(8L, userConfig.getCapacity());
        assertEquals(2L, userConfig.getRefillRate());
        assertEquals(Duration.ofMinutes(1), userConfig.getRefillDuration());
    }

    @Test
    @DisplayName("loadUserConfiguration: ignores null role or properties")
    void loadUserConfiguration_nullInput_doesNothing() {
        RateLimiterConfig config = new RateLimiterConfig();

        config.loadUserConfiguration(null, Map.of("rate.limiter.users.ADMIN.capacity", 1));
        config.loadUserConfiguration("ADMIN", null);

        assertTrue(config.getUserConfigs().isEmpty());
    }

    @Test
    @DisplayName("getEndpointConfig and getUserConfig: fall back to the base config when missing")
    void getEndpointAndUserConfig_fallBackToBaseConfig() {
        RateLimiterConfig config = new RateLimiterConfig();

        assertSame(config, config.getEndpointConfig(null));
        assertSame(config, config.getEndpointConfig("/missing"));
        assertSame(config, config.getUserConfig(null));
        assertSame(config, config.getUserConfig("MISSING"));
    }

    @Test
    @DisplayName("getCombinedConfig: merges endpoint and user configs using the more restrictive values")
    void getCombinedConfig_mergesRestrictively() {
        RateLimiterConfig config = new RateLimiterConfig();

        Map<String, Object> endpointProperties = new HashMap<>();
        endpointProperties.put("rate.limiter.endpoints./api/v1/companies.capacity", 50);
        endpointProperties.put("rate.limiter.endpoints./api/v1/companies.refill-rate", 10);
        endpointProperties.put("rate.limiter.endpoints./api/v1/companies.refill-duration", "PT30S");

        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("rate.limiter.users.ADMIN.capacity", 20);
        userProperties.put("rate.limiter.users.ADMIN.refill-rate", 5);
        userProperties.put("rate.limiter.users.ADMIN.refill-duration", "PT2M");

        config.loadEndpointConfiguration("/api/v1/companies", endpointProperties);
        config.loadUserConfiguration("ADMIN", userProperties);

        RateLimiterConfig merged = config.getCombinedConfig("/api/v1/companies", "ADMIN");
        assertEquals(20L, merged.getCapacity());
        assertEquals(5L, merged.getRefillRate());
        assertEquals(Duration.ofMinutes(2), merged.getRefillDuration());
    }

    @Test
    @DisplayName("mergeConfigurations: handles null inputs")
    void mergeConfigurations_handlesNulls() {
        RateLimiterConfig config = new RateLimiterConfig();
        RateLimiterConfig override = new RateLimiterConfig();
        override.setCapacity(9L);
        override.setRefillRate(1L);
        override.setRefillDuration(Duration.ofSeconds(10));

        assertSame(override, config.mergeConfigurations(null, override));
        assertSame(config, config.mergeConfigurations(config, null));
        assertNotNull(config.mergeConfigurations(null, null));
    }

    @Test
    @DisplayName("validateConfiguration: accepts valid values and rejects invalid ones")
    void validateConfiguration_validatesValues() {
        RateLimiterConfig config = new RateLimiterConfig();
        config.validateConfiguration();

        config.setCapacity(0L);
        IllegalArgumentException capacityEx = assertThrows(IllegalArgumentException.class, config::validateConfiguration);
        assertEquals("Capacity must be greater than 0", capacityEx.getMessage());

        config.setCapacity(10L);
        config.setRefillRate(0L);
        IllegalArgumentException refillEx = assertThrows(IllegalArgumentException.class, config::validateConfiguration);
        assertEquals("Refill rate must be greater than 0", refillEx.getMessage());

        config.setRefillRate(5L);
        config.setRefillDuration(Duration.ZERO);
        IllegalArgumentException durationEx = assertThrows(IllegalArgumentException.class, config::validateConfiguration);
        assertEquals("Refill duration must be positive", durationEx.getMessage());
    }

    @Test
    @DisplayName("parseDuration: parses ISO-8601 durations and rejects invalid input")
    void parseDuration_parsesAndRejectsInvalidInput() {
        RateLimiterConfig config = new RateLimiterConfig();

        assertEquals(Duration.ofMinutes(3), config.parseDuration("PT3M"));

        IllegalArgumentException nullEx = assertThrows(IllegalArgumentException.class, () -> config.parseDuration(null));
        assertEquals("Duration string cannot be null", nullEx.getMessage());

        IllegalArgumentException invalidEx = assertThrows(IllegalArgumentException.class, () -> config.parseDuration("bad"));
        assertTrue(invalidEx.getMessage().startsWith("Invalid duration format: bad"));
    }

    @Test
    @DisplayName("config maps: return defensive copies")
    void configMaps_returnDefensiveCopies() {
        RateLimiterConfig config = new RateLimiterConfig();
        config.loadEndpointConfiguration("/api/v1/companies", Map.of(
                "rate.limiter.endpoints./api/v1/companies.capacity", 2
        ));
        config.loadUserConfiguration("ADMIN", Map.of(
                "rate.limiter.users.ADMIN.capacity", 4
        ));

        Map<String, RateLimiterConfig> endpointConfigs = config.getEndpointConfigs();
        Map<String, RateLimiterConfig> userConfigs = config.getUserConfigs();

        endpointConfigs.clear();
        userConfigs.clear();

        assertFalse(config.getEndpointConfigs().isEmpty());
        assertFalse(config.getUserConfigs().isEmpty());
    }
}
