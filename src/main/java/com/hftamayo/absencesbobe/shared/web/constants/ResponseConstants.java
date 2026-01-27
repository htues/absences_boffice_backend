package com.hftamayo.absencesbobe.shared.web.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseConstants {
    CREATED(201, "ENTITY_CREATED"),
    UPDATED(200, "ENTITY_UPDATED"),
    DELETED(200, "ENTITY_DELETED"),
    READ(200, "ENTITY_RETRIEVED"),
    BUSINESS_LOGIC_ERROR(400, "BUSINESS_LOGIC_VIOLATION"),
    UNAUTHORIZED(401, "UNAUTHORIZED_ACCESS"),
    FORBIDDEN(403, "FORBIDDEN_ACCESS"),
    NOT_FOUND(404, "ENTITY_NOT_FOUND"),
    ENTITY_EXISTS(409, "ENTITY_ALREADY_EXISTS"),
    VALIDATION_ERROR(422, "VALIDATION_ERROR"),
    RATE_LIMITED(429, "RATE_LIMIT_EXCEEDED"),
    UNKNOWN_ERROR(500, "UNKNOWN_ERROR");

    private final int statusCode;
    private final String messageKey;

    public static ResponseConstants fromKey(String key) {
        if (key == null) return UNKNOWN_ERROR;
        try {
            return ResponseConstants.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return UNKNOWN_ERROR;
        }
    }
}