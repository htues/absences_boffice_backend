package com.hftamayo.absencesbobe.shared.web.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonTrimmingConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer trimStringsCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(String.class, new TrimmingStringDeserializer());

            // IMPORTANT: don't replace Boot's default modules (JavaTimeModule, etc.)
            builder.modulesToInstall(module);
        };
    }
}