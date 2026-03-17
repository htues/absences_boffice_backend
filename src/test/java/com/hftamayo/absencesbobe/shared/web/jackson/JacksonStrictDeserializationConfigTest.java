package com.hftamayo.absencesbobe.shared.web.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonStrictDeserializationConfigTest {

	private final JacksonStrictDeserializationConfig config = new JacksonStrictDeserializationConfig();

	@Test
	void strictJacksonCustomizerEnablesFailOnUnknownPropertiesFeature() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		Jackson2ObjectMapperBuilderCustomizer customizer = config.strictJacksonCustomizer();
		customizer.customize(builder);

		ObjectMapper mapper = builder.build();

		assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
	}

	@Test
	void strictJacksonCustomizerRejectsUnknownProperties() {
		ObjectMapper mapper = buildMapperWithCustomizer();

		assertThrows(
				UnrecognizedPropertyException.class,
				() -> mapper.readValue("{\"name\":\"Ana\",\"unknown\":\"value\"}", SamplePayload.class)
		);
	}

	@Test
	void strictJacksonCustomizerAllowsKnownProperties() throws Exception {
		ObjectMapper mapper = buildMapperWithCustomizer();

		SamplePayload payload = mapper.readValue("{\"name\":\"Ana\"}", SamplePayload.class);

		assertEquals("Ana", payload.getName());
	}

	private ObjectMapper buildMapperWithCustomizer() {
		Jackson2ObjectMapperBuilderCustomizer customizer = config.strictJacksonCustomizer();
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		customizer.customize(builder);
		return builder.build();
	}

	private static class SamplePayload {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
