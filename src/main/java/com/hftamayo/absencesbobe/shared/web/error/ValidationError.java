package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorCode;

import java.util.HashMap;
import java.util.Map;

public record ValidationError(
        Map<String, String> inputFields,
        String[] validationErrors,
        String message
) implements ErrorLogEventDescriptor {

    @Override
    public ErrorCode getType() {
        return ErrorCode.VALIDATION_ERROR;
    }

    @Override
    public String getDetail() {
        return toString();
    }

    @Override
    public String toString() {
        Map<String, String> fields = inputFields != null ? inputFields : Map.of();
        if (!fields.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation failed for fields:");
            fields.forEach((field, error) -> sb.append(String.format(" [%s]: %s;", field, error)));
            return sb.toString();
        }

        if (validationErrors != null && validationErrors.length > 0) {
            return "Validation failed: " + String.join(", ", validationErrors);
        }

        return message != null ? message : "Validation failed.";
    }

    public Map<String, Object> getDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("inputFields", inputFields != null ? inputFields : Map.of());
        details.put("validationErrors", validationErrors);
        details.put("message", message);
        return details;
    }

    public Map<String, String> getInputFields() {
        return inputFields != null ? inputFields : Map.of();
    }
}