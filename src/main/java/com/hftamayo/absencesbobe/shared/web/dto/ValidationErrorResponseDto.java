package com.hftamayo.absencesbobe.shared.web.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record ValidationErrorResponseDto(
        String reason,
        List<ValidationFieldErrorDto> errors
) {
}