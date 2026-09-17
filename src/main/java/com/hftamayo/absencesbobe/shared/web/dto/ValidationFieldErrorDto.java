package com.hftamayo.absencesbobe.shared.web.dto;

import lombok.Builder;

@Builder
public record ValidationFieldErrorDto(
        String field,
        String message
) {
}