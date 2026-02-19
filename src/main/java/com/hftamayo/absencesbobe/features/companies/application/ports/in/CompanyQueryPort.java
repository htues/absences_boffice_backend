package com.hftamayo.absencesbobe.features.companies.application.ports.in;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyQueryPort {
    Result<Page<Company>, ? extends ApiResponseDescriptor> getActiveCompanies(Pageable pageable);
    //Result<Company, ? extends ApiResponseDescriptor> getCompanyByCriteria();
}
