package com.hftamayo.absencesbobe.features.companies.adaperts.persistence;

import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanyJpaEntity;
import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanyPersistenceMapper;
import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanyQueryRepositoryAdapter;
import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanySpringDataRepository;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyQueryRepositoryAdapterTest {
    @Mock
    private CompanySpringDataRepository jpaRepository;

    @Mock
    private CompanyPersistenceMapper mapper;

    @InjectMocks
    private CompanyQueryRepositoryAdapter adapter;

    @Test
    void getActiveCompanies_shouldQueryRepositoryAndMapToDomain() {
        Pageable pageable = PageRequest.of(0, 2);

        CompanyJpaEntity e1 = new CompanyJpaEntity();
        e1.setId(1L);
        CompanyJpaEntity e2 = new CompanyJpaEntity();
        e2.setId(2L);

        Page<CompanyJpaEntity> entityPage = new PageImpl<>(List.of(e1, e2), pageable, 2);

        Company c1 = mock(Company.class);
        Company c2 = mock(Company.class);

        when(jpaRepository.findAllByIsDeletedFalseAndIsActiveTrue(pageable)).thenReturn(entityPage);
        when(mapper.toDomain(e1)).thenReturn(c1);
        when(mapper.toDomain(e2)).thenReturn(c2);

        Page<Company> result = adapter.getActiveCompanies(pageable);

        assertThat(result.getContent()).containsExactly(c1, c2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(jpaRepository, times(1)).findAllByIsDeletedFalseAndIsActiveTrue(pageable);
        verify(mapper, times(1)).toDomain(e1);
        verify(mapper, times(1)).toDomain(e2);
        verifyNoMoreInteractions(jpaRepository, mapper);
    }

    @Test
    void getActiveCompanies_shouldReturnEmptyPageWhenRepositoryReturnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CompanyJpaEntity> emptyEntityPage = Page.empty(pageable);

        when(jpaRepository.findAllByIsDeletedFalseAndIsActiveTrue(pageable)).thenReturn(emptyEntityPage);

        Page<Company> result = adapter.getActiveCompanies(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(jpaRepository, times(1)).findAllByIsDeletedFalseAndIsActiveTrue(pageable);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(jpaRepository);
    }
}
