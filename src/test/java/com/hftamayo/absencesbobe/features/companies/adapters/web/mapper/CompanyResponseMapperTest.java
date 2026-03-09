package com.hftamayo.absencesbobe.features.companies.adapters.web.mapper;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.hftamayo.absencesbobe.shared.domain.AuditInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void toDtoList_whenNull_returnsEmptyList() {
        assertNotNull(mapper.toDtoList(null));
        assertTrue(mapper.toDtoList(null).isEmpty());
    }

    @Test
    void toDtoList_whenEmpty_returnsEmptyList() {
        assertNotNull(mapper.toDtoList(List.of()));
        assertTrue(mapper.toDtoList(List.of()).isEmpty());
    }

    @Test
    void toDtoPageContent_whenNull_returnsEmptyList() {
        assertNotNull(mapper.toDtoPageContent(null));
        assertTrue(mapper.toDtoPageContent(null).isEmpty());
    }

    @Test
    void toDtoPageContent_whenEmptyPage_returnsEmptyList() {
        Page<Company> emptyPage = Page.empty(PageRequest.of(0, 10));

        List<CompanyResponseDto> dtos = mapper.toDtoPageContent(emptyPage);

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void toDtoPageContent_mapsPageContent() {
        Company c1 = Company.rehydrate(1L, "A", "D1", "Addr1", true, false, AuditInfo.empty());
        Company c2 = Company.rehydrate(2L, "B", "D2", "Addr2", true, false, AuditInfo.empty());

        Page<Company> page = new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 10), 2);

        List<CompanyResponseDto> dtos = mapper.toDtoPageContent(page);

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals("A", dtos.get(0).getName());
        assertEquals(2L, dtos.get(1).getId());
        assertEquals("B", dtos.get(1).getName());
    }
}