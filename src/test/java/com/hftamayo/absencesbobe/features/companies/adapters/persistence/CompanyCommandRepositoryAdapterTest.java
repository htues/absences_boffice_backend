package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyCommandRepositoryAdapterTest {

    @Mock
    private CompanySpringDataRepository jpaRepository;

    @Mock
    private CompanyPersistenceMapper mapper;

    @InjectMocks
    private CompanyCommandRepositoryAdapter adapter;

    @Test
    @DisplayName("findById: uses findByIdAndIsDeletedFalse and maps entity to domain")
    void findById_usesNonDeletedQuery_andMaps() {
        Long id = 10L;

        CompanyJpaEntity entity = new CompanyJpaEntity();
        Company domain = Company.rehydrate(id, "Name", "Desc", "Addr", true, false, null);

        when(jpaRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Company> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(jpaRepository).findByIdAndIsDeletedFalse(id);
        verify(jpaRepository, never()).findById(anyLong());
        verify(mapper).toDomain(entity);
        verifyNoMoreInteractions(jpaRepository, mapper);
    }

    @Test
    @DisplayName("findById: returns Optional.empty when repository returns empty")
    void findById_emptyWhenNotFound() {
        Long id = 10L;
        when(jpaRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());

        Optional<Company> result = adapter.findById(id);

        assertTrue(result.isEmpty());

        verify(jpaRepository).findByIdAndIsDeletedFalse(id);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(jpaRepository);
    }

    @Test
    @DisplayName("findByIdIncludingDeleted: uses findById and maps entity to domain")
    void findByIdIncludingDeleted_usesFindById_andMaps() {
        Long id = 20L;

        CompanyJpaEntity entity = new CompanyJpaEntity();
        Company domain = Company.rehydrate(id, "Name", "Desc", "Addr", true, true, null);

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Company> result = adapter.findByIdIncludingDeleted(id);

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(jpaRepository).findById(id);
        verify(jpaRepository, never()).findByIdAndIsDeletedFalse(anyLong());
        verify(mapper).toDomain(entity);
        verifyNoMoreInteractions(jpaRepository, mapper);
    }

    @Test
    @DisplayName("save: maps domain->entity, calls repository save, maps saved entity->domain")
    void save_mapsBothWays() {
        Company inputDomain = Company.rehydrate(123L, "Name", "Desc", "Addr", true, false, null);

        CompanyJpaEntity toSaveEntity = new CompanyJpaEntity();
        CompanyJpaEntity savedEntity = new CompanyJpaEntity();

        Company savedDomain = Company.rehydrate(123L, "Name", "Desc", "Addr", true, false, null);

        when(mapper.toEntity(inputDomain)).thenReturn(toSaveEntity);
        when(jpaRepository.save(toSaveEntity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        Company result = adapter.save(inputDomain);

        assertSame(savedDomain, result);

        verify(mapper).toEntity(inputDomain);
        verify(jpaRepository).save(toSaveEntity);
        verify(mapper).toDomain(savedEntity);
        verifyNoMoreInteractions(jpaRepository, mapper);
    }

    @Test
    @DisplayName("existsByName: delegates to existsByNameIgnoreCaseAndIsDeletedFalse")
    void existsByName_delegates() {
        String name = "Acme";

        when(jpaRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name)).thenReturn(true);

        boolean result = adapter.existsByName(name);

        assertTrue(result);

        verify(jpaRepository).existsByNameIgnoreCaseAndIsDeletedFalse(name);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(jpaRepository);
    }

    @Test
    @DisplayName("existsByNameExcludingId: delegates to existsByNameIgnoreCaseAndIsDeletedFalseAndIdNot")
    void existsByNameExcludingId_delegates() {
        String name = "Acme";
        Long idToExclude = 50L;

        when(jpaRepository.existsByNameIgnoreCaseAndIsDeletedFalseAndIdNot(name, idToExclude)).thenReturn(false);

        boolean result = adapter.existsByNameExcludingId(name, idToExclude);

        assertFalse(result);

        verify(jpaRepository).existsByNameIgnoreCaseAndIsDeletedFalseAndIdNot(name, idToExclude);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(jpaRepository);
    }
}