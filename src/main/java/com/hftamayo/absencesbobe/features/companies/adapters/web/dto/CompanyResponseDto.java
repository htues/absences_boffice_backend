package com.hftamayo.absencesbobe.features.companies.adapters.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class CompanyResponseDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private boolean isActive;
    private boolean isDeleted;
    private Long createdBy;
    private Long updatedBy;
    private Instant createdDate;
    private Instant updatedDate;
}
