package com.hftamayo.absencesbobe.features.companies.application.ports.in;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.CodeDescriptor;

public interface CompanyCommandPort {
    Result<Company, ? extends CodeDescriptor> createCompany(Company company);

    Result<Company, ? extends CodeDescriptor> updateCompany(Long id, String name, String description, String address);

    Result<Void, ? extends CodeDescriptor> deleteCompany(Long id);

    Result<Company, ? extends CodeDescriptor> deactivateCompany(Long id);

}
