import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for rate limiting settings.
 * Manages default, endpoint-specific, and user-specific rate limiting configurations.
 */
@Component
public class RateLimiterConfig {

    // Default configuration values
    private static final long DEFAULT_CAPACITY = 100L;
    private static final long DEFAULT_REFILL_RATE = 10L;
    private static final Duration DEFAULT_REFILL_DURATION = Duration.ofMinutes(1);

    // Configuration properties
    private Long capacity;
    private Long refillRate;
    private Duration refillDuration;

    // Endpoint-specific configurations
    private final Map<String, RateLimiterConfig> endpointConfigs = new HashMap<>();

    // User-specific configurations
    private final Map<String, RateLimiterConfig> userConfigs = new HashMap<>();

    /**
     * Default constructor with default values.
     */
    public RateLimiterConfig() {
        loadDefaultConfiguration();
    }

    /**
     * Constructor with custom values.
     */
    public RateLimiterConfig(Long capacity, Long refillRate, Duration refillDuration) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.refillDuration = refillDuration;
    }

    /**
     * Loads default configuration values.
     */
    public void loadDefaultConfiguration() {
        this.capacity = DEFAULT_CAPACITY;
        this.refillRate = DEFAULT_REFILL_RATE;
        this.refillDuration = DEFAULT_REFILL_DURATION;
    }

    /**
     * Loads configuration from properties map.
     *
     * @param properties The properties map containing configuration values
     */
    public void loadConfigurationFromProperties(Map<String, Object> properties) {
        if (properties == null) {
            return;
        }

        Object capacityObj = properties.get("rate.limiter.default.capacity");
        Object refillRateObj = properties.get("rate.limiter.default.refill-rate");
        Object refillDurationObj = properties.get("rate.limiter.default.refill-duration");

        if (capacityObj != null) {
            this.capacity = parseLong(capacityObj);
        }

        if (refillRateObj != null) {
            this.refillRate = parseLong(refillRateObj);
        }

        if (refillDurationObj != null) {
            this.refillDuration = parseDuration(refillDurationObj.toString());
        }
    }

    /**
     * Loads endpoint-specific configuration.
     *
     * @param endpoint The endpoint path
     * @param properties The properties map containing endpoint configuration
     */
    public void loadEndpointConfiguration(String endpoint, Map<String, Object> properties) {
        if (endpoint == null || properties == null) {
            return;
        }

        RateLimiterConfig endpointConfig = new RateLimiterConfig();
        String prefix = "rate.limiter.endpoints." + endpoint + ".";

        Object capacityObj = properties.get(prefix + "capacity");
        Object refillRateObj = properties.get(prefix + "refill-rate");
        Object refillDurationObj = properties.get(prefix + "refill-duration");

        if (capacityObj != null) {
            endpointConfig.setCapacity(parseLong(capacityObj));
        }

        if (refillRateObj != null) {
            endpointConfig.setRefillRate(parseLong(refillRateObj));
        }

        if (refillDurationObj != null) {
            endpointConfig.setRefillDuration(parseDuration(refillDurationObj.toString()));
        }

        endpointConfigs.put(endpoint, endpointConfig);
    }

    /**
     * Loads user-specific configuration.
     *
     * @param userRole The user role
     * @param properties The properties map containing user configuration
     */
    public void loadUserConfiguration(String userRole, Map<String, Object> properties) {
        if (userRole == null || properties == null) {
            return;
        }

        RateLimiterConfig userConfig = new RateLimiterConfig();
        String prefix = "rate.limiter.users." + userRole + ".";

        Object capacityObj = properties.get(prefix + "capacity");
        Object refillRateObj = properties.get(prefix + "refill-rate");
        Object refillDurationObj = properties.get(prefix + "refill-duration");

        if (capacityObj != null) {
            userConfig.setCapacity(parseLong(capacityObj));
        }

        if (refillRateObj != null) {
            userConfig.setRefillRate(parseLong(refillRateObj));
        }

        if (refillDurationObj != null) {
            userConfig.setRefillDuration(parseDuration(refillDurationObj.toString()));
        }

        userConfigs.put(userRole, userConfig);
    }

    /**
     * Gets endpoint-specific configuration.
     *
     * @param endpoint The endpoint path
     * @return The endpoint configuration or default configuration if not found
     */
    public RateLimiterConfig getEndpointConfig(String endpoint) {
        if (endpoint == null) {
            return this;
        }

        RateLimiterConfig endpointConfig = endpointConfigs.get(endpoint);
        return endpointConfig != null ? endpointConfig : this;
    }

    /**
     * Gets user-specific configuration.
     *
     * @param userRole The user role
     * @return The user configuration or default configuration if not found
     */
    public RateLimiterConfig getUserConfig(String userRole) {
        if (userRole == null) {
            return this;
        }

        RateLimiterConfig userConfig = userConfigs.get(userRole);
        return userConfig != null ? userConfig : this;
    }

    /**
     * Gets combined configuration for endpoint and user.
     * Uses the more restrictive configuration between endpoint and user configs.
     *
     * @param endpoint The endpoint path
     * @param userRole The user role
     * @return The combined configuration
     */
    public RateLimiterConfig getCombinedConfig(String endpoint, String userRole) {
        RateLimiterConfig endpointConfig = getEndpointConfig(endpoint);
        RateLimiterConfig userConfig = getUserConfig(userRole);

        return mergeConfigurations(endpointConfig, userConfig);
    }

    /**
     * Merges two configurations, using the more restrictive values.
     *
     * @param baseConfig The base configuration
     * @param overrideConfig The override configuration
     * @return The merged configuration
     */
    public RateLimiterConfig mergeConfigurations(RateLimiterConfig baseConfig, RateLimiterConfig overrideConfig) {
        if (baseConfig == null) {
            return overrideConfig != null ? overrideConfig : new RateLimiterConfig();
        }

        if (overrideConfig == null) {
            return baseConfig;
        }

        RateLimiterConfig mergedConfig = new RateLimiterConfig();

        // Use the smaller capacity (more restrictive)
        Long baseCapacity = baseConfig.getCapacity();
        Long overrideCapacity = overrideConfig.getCapacity();
        mergedConfig.setCapacity(baseCapacity != null && overrideCapacity != null ?
                Math.min(baseCapacity, overrideCapacity) :
                (baseCapacity != null ? baseCapacity : overrideCapacity));

        // Use the smaller refill rate (more restrictive)
        Long baseRefillRate = baseConfig.getRefillRate();
        Long overrideRefillRate = overrideConfig.getRefillRate();
        mergedConfig.setRefillRate(baseRefillRate != null && overrideRefillRate != null ?
                Math.min(baseRefillRate, overrideRefillRate) :
                (baseRefillRate != null ? baseRefillRate : overrideRefillRate));

        // Use the longer duration (more restrictive)
        Duration baseDuration = baseConfig.getRefillDuration();
        Duration overrideDuration = overrideConfig.getRefillDuration();
        mergedConfig.setRefillDuration(baseDuration != null && overrideDuration != null ?
                (baseDuration.compareTo(overrideDuration) > 0 ? baseDuration : overrideDuration) :
                (baseDuration != null ? baseDuration : overrideDuration));

        return mergedConfig;
    }

    /**
     * Validates the configuration.
     *
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validateConfiguration() {
        if (capacity == null || capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        if (refillRate == null || refillRate <= 0) {
            throw new IllegalArgumentException("Refill rate must be greater than 0");
        }

        if (refillDuration == null || refillDuration.isZero() || refillDuration.isNegative()) {
            throw new IllegalArgumentException("Refill duration must be positive");
        }
    }

    /**
     * Parses a duration string in ISO 8601 format.
     *
     * @param durationString The duration string (e.g., "PT1M", "PT30S")
     * @return The parsed Duration
     * @throws IllegalArgumentException if the duration string is invalid
     */
    public Duration parseDuration(String durationString) {
        if (durationString == null) {
            throw new IllegalArgumentException("Duration string cannot be null");
        }

        try {
            return Duration.parse(durationString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid duration format: " + durationString, e);
        }
    }

    /**
     * Parses a Long value from an Object.
     *
     * @param obj The object to parse
     * @return The parsed Long value
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }

        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + obj, e);
        }
    }

    // Getters and Setters
    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public Long getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(Long refillRate) {
        this.refillRate = refillRate;
    }

    public Duration getRefillDuration() {
        return refillDuration;
    }

    public void setRefillDuration(Duration refillDuration) {
        this.refillDuration = refillDuration;
    }

    public Map<String, RateLimiterConfig> getEndpointConfigs() {
        return new HashMap<>(endpointConfigs);
    }

    public Map<String, RateLimiterConfig> getUserConfigs() {
        return new HashMap<>(userConfigs);
    }
} 