package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResourceNotFoundError implements ErrorLogEventDescriptor {
    private final String errorCode;
    private final String operation;
    private final String resource;
    private final String message;

    @Override
    public ErrorApiResponse getType() {
        return ErrorApiResponse.NOT_FOUND;
    }

    @Override
    public String getDetail() {
        return message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    public static ResourceNotFoundError withId(Long id, String operation, String resource) {
        return new ResourceNotFoundError(
                "NOT_FOUND",
                operation,
                resource,
                resource + " with id " + id + " was not found"
        );
    }

    public String toString() {
        return String.format("Error Code: %s, Operation: %s, Resource: %s, Message: %s",
                errorCode, operation, resource, message);
    }


}
