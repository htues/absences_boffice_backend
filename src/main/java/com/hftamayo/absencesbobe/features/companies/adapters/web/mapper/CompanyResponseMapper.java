package com.hftamayo.absencesbobe.features.companies.adapters.web.mapper;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyResponseMapper {

    public CompanyResponseDto toDto(Company company) {
        if (company == null) {
            return null;
        }

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setAddress(company.getAddress());
        dto.setActive(company.isActive());
        dto.setDeleted(company.isDeleted());

        dto.setCreatedBy(company.getCreatedBy());
        dto.setUpdatedBy(company.getUpdatedBy());
        dto.setCreatedDate(company.getCreatedDate());
        dto.setUpdatedDate(company.getUpdatedDate());

        return dto;
    }
}