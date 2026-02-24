package com.hftamayo.absencesbobe.features.companies.adapters.web.mapper;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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

    public List<CompanyResponseDto> toDtoList(Collection<Company> companies) {
        if (companies == null || companies.isEmpty()) {
            return List.of();
        }
        return companies.stream()
                .map(this::toDto)
                .toList();
    }

    public List<CompanyResponseDto> toDtoPageContent(Page<Company> page) {
        if (page == null || page.isEmpty()) {
            return List.of();
        }
        return page.getContent().stream()
                .map(this::toDto)
                .toList();
    }
}