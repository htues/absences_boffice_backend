package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DuplicateResourceError implements ErrorLogEventDescriptor {
    private final String detail;
    private final String resourceType;
    private final Long resourceId;
    private final String resourceIdentifier;
    private final String field;
    private final String value;

    @Override
    public ErrorApiResponse getType() {
        return ErrorApiResponse.ENTITY_EXISTS;
    }

    public static DuplicateResourceError withId(String resourceType, Long resourceId) {
        return new DuplicateResourceError(String.format("%s with identifier %d already exists", resourceType, resourceId),
                resourceType, resourceId, null, null, null);
    }

    public static DuplicateResourceError withIdentifier(String resourceType, String resourceIdentifier) {
        return new DuplicateResourceError(String.format("%s with identifier %s already exists",
                resourceType, resourceIdentifier), resourceType, null, resourceIdentifier, null, null);
    }

    public static DuplicateResourceError withFieldValue(String field, String value) {
        return new DuplicateResourceError(String.format("Resource with %s '%s' already exists",
                field, value), "Company", null, null, field, value);
    }
}