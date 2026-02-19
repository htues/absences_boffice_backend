package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import com.hftamayo.absencesbobe.features.companies.application.ports.out.CompanyQueryRepositoryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyQueryRepositoryAdapter implements CompanyQueryRepositoryPort {
    private final CompanySpringDataRepository jpaRepository;
    private final CompanyPersistenceMapper mapper;

    @Override
    public Page<Company> getActiveCompanies(Pageable pageable) {
        return jpaRepository.findAllByIsDeletedFalseAndIsActiveTrue(pageable)
                .map(mapper::toDomain);
    }
}
