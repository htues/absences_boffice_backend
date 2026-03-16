package com.hftamayo.absencesbobe.features.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.error.DuplicateResourceError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DuplicateResourceErrorTest {

    @Test
    void constructor_setsAllFieldsAndDescriptorMethods() {
        DuplicateResourceError error = new DuplicateResourceError(
                "User with email already exists",
                "User",
                123L,
                "user@example.com",
                "email",
                "user@example.com"
        );

        assertEquals("User with email already exists", error.getDetail());
        assertEquals("User", error.getResourceType());
        assertEquals(123L, error.getResourceId());
        assertEquals("user@example.com", error.getResourceIdentifier());
        assertEquals("email", error.getField());
        assertEquals("user@example.com", error.getValue());

        assertEquals(ErrorApiResponse.ENTITY_EXISTS, error.getType());
    }

    @Test
    void withId_buildsExpectedDuplicateResourceError() {
        DuplicateResourceError error = DuplicateResourceError.withId("Company", 456L);

        assertEquals("Company with identifier 456 already exists", error.getDetail());
        assertEquals("Company", error.getResourceType());
        assertEquals(456L, error.getResourceId());
        assertNull(error.getResourceIdentifier());
        assertNull(error.getField());
        assertNull(error.getValue());

        assertEquals(ErrorApiResponse.ENTITY_EXISTS, error.getType());
    }

    @Test
    void withIdentifier_buildsExpectedDuplicateResourceError() {
        DuplicateResourceError error = DuplicateResourceError.withIdentifier("Department", "DEPT-001");

        assertEquals("Department with identifier DEPT-001 already exists", error.getDetail());
        assertEquals("Department", error.getResourceType());
        assertNull(error.getResourceId());
        assertEquals("DEPT-001", error.getResourceIdentifier());
        assertNull(error.getField());
        assertNull(error.getValue());

        assertEquals(ErrorApiResponse.ENTITY_EXISTS, error.getType());
    }

    @Test
    void withFieldValue_buildsExpectedDuplicateResourceError() {
        DuplicateResourceError error = DuplicateResourceError.withFieldValue("tax_id", "12345678901234");

        assertEquals("Resource with tax_id '12345678901234' already exists", error.getDetail());
        assertEquals("Company", error.getResourceType());
        assertNull(error.getResourceId());
        assertNull(error.getResourceIdentifier());
        assertEquals("tax_id", error.getField());
        assertEquals("12345678901234", error.getValue());

        assertEquals(ErrorApiResponse.ENTITY_EXISTS, error.getType());
    }
}
