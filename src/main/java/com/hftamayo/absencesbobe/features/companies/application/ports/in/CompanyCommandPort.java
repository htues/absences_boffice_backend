package com.hftamayo.absencesbobe.features.companies.application.ports.in;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;

public interface CompanyCommandPort {
    Result<Company, ? extends ApiResponseDescriptor> createCompany(Company company);

    Result<Company, ? extends ApiResponseDescriptor> updateCompany(Long id, String name, String description, String address);

    Result<Company, ? extends ApiResponseDescriptor> deleteCompany(Long id);

    Result<Company, ? extends ApiResponseDescriptor> restoreCompany(Long id);

    Result<Company, ? extends ApiResponseDescriptor> deactivateCompany(Long id);

    Result<Company, ? extends ApiResponseDescriptor> activateCompany(Long id);

}
