package com.hftamayo.absencesbobe.features.companies.adaperts.web.mapper;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.adapters.web.mapper.CompanyResponseMapper;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.domain.AuditInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyResponseMapperTest {

    private final CompanyResponseMapper mapper = new CompanyResponseMapper();

    @Test
    void toDto_whenCompanyIsNull_returnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_mapsAllFields() {
        Company company = Company.rehydrate(
                123L,
                "Acme",
                "Some description",
                "Some address",
                true,
                false,
                AuditInfo.empty()
        );

        CompanyResponseDto dto = mapper.toDto(company);

        assertNotNull(dto);
        assertEquals(123L, dto.getId());
        assertEquals("Acme", dto.getName());
        assertEquals("Some description", dto.getDescription());
        assertEquals("Some address", dto.getAddress());
        assertTrue(dto.isActive());
        assertFalse(dto.isDeleted());

        // With AuditInfo.empty(), these are expected to be whatever "empty" means in your model (often nulls).
        assertEquals(company.getCreatedBy(), dto.getCreatedBy());
        assertEquals(company.getUpdatedBy(), dto.getUpdatedBy());
        assertEquals(company.getCreatedDate(), dto.getCreatedDate());
        assertEquals(company.getUpdatedDate(), dto.getUpdatedDate());
    }
}