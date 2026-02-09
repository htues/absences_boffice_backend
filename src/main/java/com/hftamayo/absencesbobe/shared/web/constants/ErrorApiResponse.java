package com.hftamayo.absencesbobe.shared.web.constants;

import lombok.Getter;

@Getter
public enum ErrorApiResponse implements ApiResponseDescriptor {
    BUSINESS_LOGIC_ERROR(400, "BUSINESS_LOGIC_VIOLATION"),
    UNAUTHORIZED(401, "UNAUTHORIZED_ACCESS"),
    FORBIDDEN(403, "FORBIDDEN_ACCESS"),
    NOT_FOUND(404, "ENTITY_NOT_FOUND"),
    ENTITY_EXISTS(409, "ENTITY_ALREADY_EXISTS"),
    VALIDATION_ERROR(422, "VALIDATION_ERROR"),
    RATE_LIMITED(429, "RATE_LIMIT_EXCEEDED"),
    UNKNOWN_ERROR(500, "UNKNOWN_ERROR"),
    SEEDING_ERROR(501, "SEEDING_DISABLED");

    private static final String DEFAULT_RESPONSE_TYPE = "error";

    private final String responseType;
    private final int statusCode;
    private final String messageKey;

    ErrorApiResponse(int statusCode, String messageKey) {
        this(DEFAULT_RESPONSE_TYPE, statusCode, messageKey);
    }

    ErrorApiResponse(String responseType, int statusCode, String messageKey) {
        this.responseType = responseType;
        this.statusCode = statusCode;
        this.messageKey = messageKey;
    }

}
