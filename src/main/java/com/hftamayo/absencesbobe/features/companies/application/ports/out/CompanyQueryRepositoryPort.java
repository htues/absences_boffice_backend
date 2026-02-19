package com.hftamayo.absencesbobe.features.companies.application.ports.out;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyQueryRepositoryPort {
    Page<Company> getActiveCompanies(Pageable pageable);
}
