package com.hftamayo.absencesbobe.features.companies.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        String name,

        @NotBlank
        @Size(min = 5, max = 200)
        String description,

        @NotBlank
        @Size(min = 5, max = 100)
        String address
) {
}