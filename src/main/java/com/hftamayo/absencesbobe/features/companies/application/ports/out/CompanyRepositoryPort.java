package com.hftamayo.absencesbobe.features.companies.application.ports.out;

import com.hftamayo.absencesbobe.features.companies.domain.Company;

import java.util.Optional;

public interface CompanyRepositoryPort {

    Optional<Company> findById(Long id);

    Company save(Company company);

    boolean existsByName(String name);

    /**
     * Useful for updates: "does some OTHER company already use this name?"
     */
    boolean existsByNameAndIdNot(String name, Long idToExclude);
}