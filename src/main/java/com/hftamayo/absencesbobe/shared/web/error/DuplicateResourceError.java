package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.error.exception.BusinessError;
import com.hftamayo.java.boabsenses.utilities.constants.ApiResponseMessages;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DuplicateResourceError implements BusinessError {
    private final String message;
    private final String resourceType;
    private final Long resourceId;
    private final String resourceIdentifier;
    private final String field;
    private final String value;

    public static DuplicateResourceError withId(String resourceType, Long resourceId) {
        return new DuplicateResourceError(String.format("%s with id %d already exists", resourceType, resourceId),
                resourceType, resourceId, null, null, null);
    }

    public static DuplicateResourceError withIdentifier(String resourceType, String resourceIdentifier) {
        return new DuplicateResourceError(String.format("%s with id %s already exists",
                resourceType, resourceIdentifier), resourceType, null, resourceIdentifier, null, null);
    }

    public static DuplicateResourceError withFieldValue(String field, String value) {
        return new DuplicateResourceError(String.format("Resource with %s '%s' already exists",
                field, value), "Company", null, null, field, value);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getTitle() {
        return ApiResponseMessages.ENTITY_EXISTS.getMessageKey();
    }

    @Override
    public int getStatus() {
        return ApiResponseMessages.ENTITY_EXISTS.getStatusCode();
    }

    @Override
    public String getDetail() {
        return message;
    }

    @Override
    public String getCode() {
        return ApiResponseMessages.ENTITY_EXISTS.getMessageKey();
    }
}
