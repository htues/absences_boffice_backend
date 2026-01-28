package com.hftamayo.absencesbobe.shared.web.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements ApiAnswersDescriptor {
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

}
