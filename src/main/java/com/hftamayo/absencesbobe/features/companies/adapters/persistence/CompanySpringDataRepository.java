package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanySpringDataRepository
        extends JpaRepository<CompanyJpaEntity, Long>, JpaSpecificationExecutor<CompanyJpaEntity> {

    Optional<CompanyJpaEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // For updates: "does some OTHER row already use this name?"
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long idToExclude);
}