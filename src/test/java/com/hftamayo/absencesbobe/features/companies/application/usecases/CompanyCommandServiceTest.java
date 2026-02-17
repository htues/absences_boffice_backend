package com.hftamayo.absencesbobe.features.companies.application.usecases;

import com.hftamayo.absencesbobe.features.companies.application.ports.out.CompanyRepositoryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyCommandServiceTest {

    private CompanyRepositoryPort companyRepository;
    private CompanyCommandService service;

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepositoryPort.class);
        service = new CompanyCommandService(companyRepository);
    }

    // -------- createCompany --------

    @Test
    void createCompany_nullCompany_returnsBusinessLogicError_andDoesNotTouchRepository() {
        Result<Company, ? extends ApiResponseDescriptor> result = service.createCompany(null);

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.BUSINESS_LOGIC_ERROR, result.error());
        verifyNoInteractions(companyRepository);
    }

    @Test
    void createCompany_whenNameAlreadyExists_returnsEntityExists_andDoesNotSave() {
        Company newCompany = Company.createNew("Acme", "Some description", "Some address");
        when(companyRepository.existsByName("Acme")).thenReturn(true);

        Result<Company, ? extends ApiResponseDescriptor> result = service.createCompany(newCompany);

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.ENTITY_EXISTS, result.error());
        verify(companyRepository).existsByName("Acme");
        verify(companyRepository, never()).save(any());
    }

    @Test
    void createCompany_happyPath_savesAndReturnsOk() {
        Company newCompany = Company.createNew("Acme", "Some description", "Some address");
        when(companyRepository.existsByName("Acme")).thenReturn(false);
        when(companyRepository.save(newCompany)).thenReturn(newCompany);

        Result<Company, ? extends ApiResponseDescriptor> result = service.createCompany(newCompany);

        assertTrue(result.isSuccess());
        assertSame(newCompany, result.value());
        verify(companyRepository).existsByName("Acme");
        verify(companyRepository).save(newCompany);
    }

    @Test
    void createCompany_whenUpdateDetailsValidationWouldThrow_illegalArgument_mapsToValidationError() {
        Company invalid = Company.createNew("Acme", "Some description", "Some address");
        when(companyRepository.existsByName(anyString())).thenThrow(new IllegalArgumentException("boom"));

        Result<Company, ? extends ApiResponseDescriptor> result = service.createCompany(invalid);

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.VALIDATION_ERROR, result.error());
        verify(companyRepository).existsByName("Acme");
        verify(companyRepository, never()).save(any());
    }

    @Test
    void createCompany_dataIntegrityViolation_mapsToEntityExists() {
        Company newCompany = Company.createNew("Acme", "Some description", "Some address");
        when(companyRepository.existsByName("Acme")).thenReturn(false);
        when(companyRepository.save(newCompany)).thenThrow(new DataIntegrityViolationException("dup"));

        Result<Company, ? extends ApiResponseDescriptor> result = service.createCompany(newCompany);

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.ENTITY_EXISTS, result.error());
        verify(companyRepository).save(newCompany);
    }

    // -------- updateCompany --------

    @Test
    void updateCompany_invalidId_returnsValidationError_andDoesNotTouchRepository() {
        Result<Company, ? extends ApiResponseDescriptor> result = service.updateCompany(0L, "N", "D", "A");

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.VALIDATION_ERROR, result.error());
        verifyNoInteractions(companyRepository);
    }

    @Test
    void updateCompany_notFound_returnsNotFound() {
        when(companyRepository.findById(10L)).thenReturn(Optional.empty());

        Result<Company, ? extends ApiResponseDescriptor> result =
                service.updateCompany(10L, "New", "Desc", "Addr");

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.NOT_FOUND, result.error());
        verify(companyRepository).findById(10L);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void updateCompany_nameChanged_toExistingName_returnsEntityExists_andDoesNotSave() {
        Company existing = Company.rehydrate(
                10L, "OldName", "OldDesc", "OldAddr",
                true, false, null
        );
        when(companyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(companyRepository.existsByNameExcludingId("NewName", 10L)).thenReturn(true);

        Result<Company, ? extends ApiResponseDescriptor> result =
                service.updateCompany(10L, "NewName", "NewDesc", "NewAddr");

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.ENTITY_EXISTS, result.error());
        verify(companyRepository).existsByNameExcludingId("NewName", 10L);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void updateCompany_nameNotChangedByCase_doesNotCheckUniqueness_andSaves() {
        Company existing = Company.rehydrate(
                10L, "Acme", "OldDesc", "OldAddr",
                true, false, null
        );
        when(companyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<Company, ? extends ApiResponseDescriptor> result =
                service.updateCompany(10L, "acme", "NewDesc", "NewAddr");

        assertTrue(result.isSuccess());
        assertEquals("acme", result.value().getName());
        verify(companyRepository, never()).existsByNameExcludingId(anyString(), anyLong());

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(captor.capture());
        assertEquals("NewDesc", captor.getValue().getDescription());
        assertEquals("NewAddr", captor.getValue().getAddress());
    }

    @Test
    void updateCompany_blankName_triggersIllegalArgument_mapsToValidationError() {
        Company existing = Company.rehydrate(
                10L, "OldName", "OldDesc", "OldAddr",
                true, false, null
        );
        when(companyRepository.findById(10L)).thenReturn(Optional.of(existing));

        Result<Company, ? extends ApiResponseDescriptor> result =
                service.updateCompany(10L, "   ", "NewDesc", "NewAddr");

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.VALIDATION_ERROR, result.error());
        verify(companyRepository, never()).save(any());
    }

    // -------- deleteCompany --------

    @Test
    void deleteCompany_notFound_returnsNotFound() {
        when(companyRepository.findByIdIncludingDeleted(10L)).thenReturn(Optional.empty());

        Result<Company, ? extends ApiResponseDescriptor> result = service.deleteCompany(10L);

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.NOT_FOUND, result.error());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void deleteCompany_alreadyDeleted_returnsBusinessLogicError_andDoesNotSave() {
        Company deleted = Company.rehydrate(
                10L, "Acme", "Desc", "Addr",
                false, true, null
        );
        when(companyRepository.findByIdIncludingDeleted(10L)).thenReturn(Optional.of(deleted));

        Result<Company, ? extends ApiResponseDescriptor> result = service.deleteCompany(10L);

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.BUSINESS_LOGIC_ERROR, result.error());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void deleteCompany_happyPath_marksDeleted_deactivates_andSaves() {
        Company existing = Company.rehydrate(
                10L, "Acme", "Desc", "Addr",
                true, false, null
        );
        when(companyRepository.findByIdIncludingDeleted(10L)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<Company, ? extends ApiResponseDescriptor> result = service.deleteCompany(10L);

        assertTrue(result.isSuccess());
        assertTrue(result.value().isDeleted());
        assertFalse(result.value().isActive());
        verify(companyRepository).save(existing);
    }

    // -------- restoreCompany --------

    @Test
    void restoreCompany_whenNotDeleted_isIdempotent_returnsOkWithoutSaving() {
        Company existing = Company.rehydrate(
                10L, "Acme", "Desc", "Addr",
                true, false, null
        );
        when(companyRepository.findByIdIncludingDeleted(10L)).thenReturn(Optional.of(existing));

        Result<Company, ? extends ApiResponseDescriptor> result = service.restoreCompany(10L);

        assertTrue(result.isSuccess());
        assertSame(existing, result.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void restoreCompany_whenDeleted_restores_activates_andSaves() {
        Company deleted = Company.rehydrate(
                10L, "Acme", "Desc", "Addr",
                false, true, null
        );
        when(companyRepository.findByIdIncludingDeleted(10L)).thenReturn(Optional.of(deleted));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<Company, ? extends ApiResponseDescriptor> result = service.restoreCompany(10L);

        assertTrue(result.isSuccess());
        assertFalse(result.value().isDeleted());
        assertTrue(result.value().isActive());
        verify(companyRepository).save(deleted);
    }

    // -------- deactivateCompany / activateCompany --------

    @Test
    void deactivateCompany_whenAlreadyInactive_isIdempotent_returnsOkWithoutSaving() {
        Company existing = Company.rehydrate(
                10L, "Acme", "Desc", "Addr",
                false, false, null
        );
        when(companyRepository.findById(10L)).thenReturn(Optional.of(existing));

        Result<Company, ? extends ApiResponseDescriptor> result = service.deactivateCompany(10L);

        assertTrue(result.isSuccess());
        assertSame(existing, result.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void activateCompany_whenInactive_activates_andSaves() {
        Company existing = Company.rehydrate(
                10L, "Acme", "Desc", "Addr",
                false, false, null
        );
        when(companyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<Company, ? extends ApiResponseDescriptor> result = service.activateCompany(10L);

        assertTrue(result.isSuccess());
        assertTrue(result.value().isActive());
        verify(companyRepository).save(existing);
    }

    @Test
    void activateCompany_notFound_returnsNotFound() {
        when(companyRepository.findById(10L)).thenReturn(Optional.empty());

        Result<Company, ? extends ApiResponseDescriptor> result = service.activateCompany(10L);

        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.NOT_FOUND, result.error());
        verify(companyRepository, never()).save(any());
    }
}