package com.hftamayo.absencesbobe.features.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorTest {

    @Test
    void recordComponents_storesAllFields() {
        Map<String, String> fields = Map.of("email", "Invalid format", "age", "Must be positive");
        String[] errors = {"Field validation failed"};
        String message = "Input validation error";

        ValidationError error = new ValidationError(fields, errors, message);

        assertEquals(fields, error.inputFields());
        assertArrayEquals(errors, error.validationErrors());
        assertEquals(message, error.message());
    }

    @Test
    void getType_returnsValidationErrorType() {
        ValidationError error = new ValidationError(null, null, "Validation failed");

        assertEquals(ErrorApiResponse.VALIDATION_ERROR, error.getType());
    }

    @Test
    void getDetail_delegatesToString() {
        ValidationError error = new ValidationError(
                Map.of("username", "Already taken"),
                null,
                "User registration error"
        );

        assertEquals(error.toString(), error.getDetail());
    }

    @Test
    void toString_whenInputFieldsPresent_formatsFieldErrors() {
        Map<String, String> fields = Map.of(
                "email", "Invalid format",
                "password", "Too short"
        );

        ValidationError error = new ValidationError(fields, null, "Registration failed");

        String result = error.toString();
        assertTrue(result.contains("Validation failed for fields:"));
        assertTrue(result.contains("[email]: Invalid format;"));
        assertTrue(result.contains("[password]: Too short;"));
    }

    @Test
    void toString_whenInputFieldsEmpty_usesValidationErrors() {
        String[] errors = {"Field A is required", "Field B format is invalid"};

        ValidationError error = new ValidationError(Map.of(), errors, "Validation failed");

        String result = error.toString();
        assertEquals("Validation failed: Field A is required, Field B format is invalid", result);
    }

    @Test
    void toString_whenInputFieldsAndErrorsNull_usesMessage() {
        ValidationError error = new ValidationError(null, null, "Custom validation message");

        assertEquals("Custom validation message", error.toString());
    }

    @Test
    void toString_whenAllFieldsNull_returnsDefaultMessage() {
        ValidationError error = new ValidationError(null, null, null);

        assertEquals("Validation failed.", error.toString());
    }

    @Test
    void toString_whenValidationErrorsEmpty_usesMessage() {
        ValidationError error = new ValidationError(
                Map.of(),
                new String[]{},
                "Custom message for validation"
        );

        assertEquals("Custom message for validation", error.toString());
    }

    @Test
    void getDetails_returnsMapWithAllComponents() {
        Map<String, String> fields = Map.of("name", "Cannot be blank");
        String[] errors = {"Validation constraint violated"};

        ValidationError error = new ValidationError(fields, errors, "Check your input");

        Map<String, Object> details = error.getDetails();

        assertEquals(fields, details.get("inputFields"));
        assertArrayEquals(errors, (String[]) details.get("validationErrors"));
        assertEquals("Check your input", details.get("message"));
    }

    @Test
    void getInputFields_returnsFieldMapWhenPresent() {
        Map<String, String> fields = Map.of("email", "Invalid", "phone", "Wrong length");

        ValidationError error = new ValidationError(fields, null, "Validation error");

        assertEquals(fields, error.getInputFields());
    }

    @Test
    void getInputFields_returnsEmptyMapWhenNull() {
        ValidationError error = new ValidationError(null, null, "Validation error");

        assertEquals(Map.of(), error.getInputFields());
    }

    @Test
    void getInputFields_returnsEmptyMapWhenEmpty() {
        ValidationError error = new ValidationError(Map.of(), null, "Validation error");

        assertEquals(Map.of(), error.getInputFields());
    }

    @Test
    void getDetails_returnsEmptyMapForInputFieldsWhenNull() {
        ValidationError error = new ValidationError(null, new String[]{"Error 1"}, "Message");

        Map<String, Object> details = error.getDetails();

        assertEquals(Map.of(), details.get("inputFields"));
    }
}
