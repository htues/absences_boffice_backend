package com.hftamayo.absencesbobe.features.shared.web.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.shared.web.jackson.TrimmingStringDeserializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrimmingStringDeserializerTest {

	private final TrimmingStringDeserializer deserializer = new TrimmingStringDeserializer();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void deserializeReturnsTrimmedValue() throws Exception {
		try (JsonParser parser = objectMapper.getFactory().createParser("\"   Ana   \"")) {
			parser.nextToken();

			String result = deserializer.deserialize(parser, null);

			assertEquals("Ana", result);
		}
	}

	@Test
	void deserializeReturnsEmptyStringWhenInputIsOnlyWhitespace() throws Exception {
		try (JsonParser parser = objectMapper.getFactory().createParser("\"   \\t  \\n  \"")) {
			parser.nextToken();

			String result = deserializer.deserialize(parser, null);

			assertEquals("", result);
		}
	}

	@Test
	void deserializeReturnsNullWhenInputIsJsonNull() throws Exception {
		try (JsonParser parser = objectMapper.getFactory().createParser("null")) {
			parser.nextToken();

			String result = deserializer.deserialize(parser, null);

			assertNull(result);
		}
	}
}
