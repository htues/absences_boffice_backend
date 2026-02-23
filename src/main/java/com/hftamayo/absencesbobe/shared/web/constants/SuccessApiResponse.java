package com.hftamayo.absencesbobe.shared.web.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessApiResponse implements ApiResponseDescriptor {
    CREATED(201, "ENTITY_CREATED"),
    UPDATED("ENTITY_UPDATED"),
    DELETED("ENTITY_DELETED"),
    READ("ENTITY_RETRIEVED");

    private static final String DEFAULT_RESPONSE_TYPE = "success";
    private static final int DEFAULT_STATUS_CODE = 200;

    private final String responseType;
    private final int statusCode;
    private final String messageKey;

    SuccessApiResponse(String messageKey) {
        this(DEFAULT_STATUS_CODE, messageKey);
    }

    SuccessApiResponse(int statusCode, String messageKey) {
        this(DEFAULT_RESPONSE_TYPE, statusCode, messageKey);
    }

}