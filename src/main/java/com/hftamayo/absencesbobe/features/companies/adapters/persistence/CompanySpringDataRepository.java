package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanySpringDataRepository
        extends JpaRepository<CompanyJpaEntity, Long>, JpaSpecificationExecutor<CompanyJpaEntity> {

    Optional<CompanyJpaEntity> findByIdAndIsDeletedFalse(Long id);

    Optional<CompanyJpaEntity> findByNameIgnoreCaseAndIsDeletedFalse(String name);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    // For updates: "does some OTHER NON-DELETED row already use this name?"
    boolean existsByNameIgnoreCaseAndIsDeletedFalseAndIdNot(String name, Long idToExclude);
}