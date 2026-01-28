package com.hftamayo.absencesbobe.shared.web.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode implements ApiAnswersDescriptor {
    CREATED(201, "ENTITY_CREATED"),
    UPDATED(200, "ENTITY_UPDATED"),
    DELETED(200, "ENTITY_DELETED"),
    READ(200, "ENTITY_RETRIEVED");

    private final int statusCode;
    private final String messageKey;
}