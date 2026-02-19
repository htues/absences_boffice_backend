package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import com.hftamayo.absencesbobe.features.companies.application.ports.out.CompanyCommandRepositoryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompanyCommandRepositoryAdapter implements CompanyCommandRepositoryPort {

    private final CompanySpringDataRepository jpaRepository;
    private final CompanyPersistenceMapper mapper;

    @Override
    public Optional<Company> findById(Long id) {
        return jpaRepository.findByIdAndIsDeletedFalse(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Company> findByIdIncludingDeleted(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Company save(Company company) {
        CompanyJpaEntity entity = mapper.toEntity(company);
        CompanyJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name);
    }

    @Override
    public boolean existsByNameExcludingId(String name, Long idToExclude) {
        return jpaRepository.existsByNameIgnoreCaseAndIsDeletedFalseAndIdNot(name, idToExclude);
    }
}