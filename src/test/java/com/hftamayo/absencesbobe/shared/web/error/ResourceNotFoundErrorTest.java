package com.hftamayo.absencesbobe.features.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.error.ResourceNotFoundError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundErrorTest {

    @Test
    void constructor_setsFieldsAndDescriptorMethods() {
        ResourceNotFoundError error = new ResourceNotFoundError(
                "RESOURCE_MISSING",
                "RETRIEVE",
                "Company",
                "Company with id 789 does not exist in the system"
        );

        assertEquals("RESOURCE_MISSING", error.getErrorCode());
        assertEquals("RETRIEVE", error.getOperation());
        assertEquals("Company", error.getResource());
        assertEquals("Company with id 789 does not exist in the system", error.getMessage());

        assertEquals(ErrorApiResponse.NOT_FOUND, error.getType());
        assertEquals("Company with id 789 does not exist in the system", error.getDetail());
    }

    @Test
    void withId_buildsExpectedResourceNotFoundError() {
        ResourceNotFoundError error = ResourceNotFoundError.withId(100L, "DELETE", "Department");

        assertEquals("NOT_FOUND", error.getErrorCode());
        assertEquals("DELETE", error.getOperation());
        assertEquals("Department", error.getResource());
        assertEquals("Department with id 100 was not found", error.getMessage());

        assertEquals(ErrorApiResponse.NOT_FOUND, error.getType());
        assertEquals("Department with id 100 was not found", error.getDetail());
    }

    @Test
    void withId_buildsExpectedResourceNotFoundError_withDifferentResource() {
        ResourceNotFoundError error = ResourceNotFoundError.withId(999L, "UPDATE", "User");

        assertEquals("NOT_FOUND", error.getErrorCode());
        assertEquals("UPDATE", error.getOperation());
        assertEquals("User", error.getResource());
        assertEquals("User with id 999 was not found", error.getMessage());

        assertEquals(ErrorApiResponse.NOT_FOUND, error.getType());
    }

    @Test
    void toString_returnsFormattedSummary() {
        ResourceNotFoundError error = new ResourceNotFoundError(
                "NOT_FOUND",
                "RETRIEVE",
                "Employee",
                "Employee with id 42 was not found"
        );

        assertEquals(
                "Error Code: NOT_FOUND, Operation: RETRIEVE, Resource: Employee, Message: Employee with id 42 was not found",
                error.toString()
        );
    }
}
