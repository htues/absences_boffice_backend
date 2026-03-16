package com.hftamayo.absencesbobe.features.shared.web.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.shared.web.jackson.JacksonTrimmingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JacksonTrimmingConfigTest {

	private final JacksonTrimmingConfig config = new JacksonTrimmingConfig();

	@Test
	void trimStringsCustomizerDeserializesStringFieldsTrimmed() throws Exception {
		ObjectMapper mapper = buildMapperWithCustomizer();

		SamplePayload payload = mapper.readValue(
				"{\"name\":\"   Ana   \",\"department\":\"\\tHR\\n\"}",
				SamplePayload.class
		);

		assertEquals("Ana", payload.getName());
		assertEquals("HR", payload.getDepartment());
	}

	@Test
	void trimStringsCustomizerKeepsNullStringValuesAsNull() throws Exception {
		ObjectMapper mapper = buildMapperWithCustomizer();

		SamplePayload payload = mapper.readValue(
				"{\"name\":null,\"department\":\"  Ops  \"}",
				SamplePayload.class
		);

		assertNull(payload.getName());
		assertEquals("Ops", payload.getDepartment());
	}

	private ObjectMapper buildMapperWithCustomizer() {
		Jackson2ObjectMapperBuilderCustomizer customizer = config.trimStringsCustomizer();
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		customizer.customize(builder);
		return builder.build();
	}

	private static class SamplePayload {
		private String name;
		private String department;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDepartment() {
			return department;
		}

		public void setDepartment(String department) {
			this.department = department;
		}
	}
}
