package com.hftamayo.absencesbobe.shared.web.dto;


import lombok.Builder;

@Builder
public record MalformedRequestResponseDto(
        String reason,
        String field
) {
}