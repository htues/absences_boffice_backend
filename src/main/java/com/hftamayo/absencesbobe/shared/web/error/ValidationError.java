package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.error.exception.BusinessError;
import com.hftamayo.java.boabsenses.utilities.constants.ApiResponseMessages;
import java.util.HashMap;
import java.util.Map;

public record ValidationError(
        Map<String, String> inputFields,
        String[] validationErrors,
        String message
) implements BusinessError {
    @Override
    public String toString() {
        if (inputFields != null && !inputFields.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation failed for fields:");
            inputFields.forEach((field, error) -> sb.append(String.format(" [%s]: %s;", field, error)));
            return sb.toString();
        }
        if (validationErrors != null && validationErrors.length > 0) {
            return String.format("Validation failed: %s", String.join(", ", validationErrors));
        }
        return message != null ? message : "Validation failed.";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getTitle() {
        return ApiResponseMessages.VALIDATION_ERROR.getMessageKey();
    }

    @Override
    public int getStatus() {
        return ApiResponseMessages.VALIDATION_ERROR.getStatusCode();
    }

    @Override
    public String getDetail() {
        return toString();
    }

    @Override
    public String getCode() {
        return ApiResponseMessages.VALIDATION_ERROR.getMessageKey();
    }

    public Map<String, Object> getDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("inputFields", inputFields);
        details.put("validationErrors", validationErrors);
        details.put("message", message);
        return details;
    }

    public Map<String, String> getInputFields() {
        return inputFields != null ? inputFields : new HashMap<>();
    }
}