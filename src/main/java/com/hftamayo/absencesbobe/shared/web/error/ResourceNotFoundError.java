package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.error.exception.BusinessError;
import com.hftamayo.java.boabsenses.utilities.constants.ApiResponseMessages;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResourceNotFoundError implements BusinessError {
    private final String errorCode;
    private final String operation;
    private final String resource;
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getTitle() {
        return ApiResponseMessages.NOT_FOUND.getMessageKey();
    }

    @Override
    public int getStatus() {
        return ApiResponseMessages.NOT_FOUND.getStatusCode();
    }

    @Override
    public String getDetail() {
        return message;
    }

    @Override
    public String getCode() {
        return errorCode != null ? errorCode : ApiResponseMessages.NOT_FOUND.getMessageKey();
    }

    public String toString() {
        return String.format("Error Code: %s, Operation: %s, Resource: %s, Message: %s",
                errorCode, operation, resource, message);
    }

    public static ResourceNotFoundError withId(Long id, String operation, String resource) {
        return new ResourceNotFoundError(
                "NOT_FOUND",
                operation,
                resource,
                resource + " with id " + id + " was not found"
        );
    }

}
