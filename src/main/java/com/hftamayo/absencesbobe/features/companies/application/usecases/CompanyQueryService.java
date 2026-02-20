package com.hftamayo.absencesbobe.features.companies.application.usecases;

import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyQueryPort;
import com.hftamayo.absencesbobe.features.companies.application.ports.out.CompanyQueryRepositoryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.errors.UnknownErrorHandler;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyQueryService implements CompanyQueryPort {
    private final CompanyQueryRepositoryPort companyRepository;

    @Override
    public Result<Page<Company>, ? extends ApiResponseDescriptor> getActiveCompanies(Pageable pageable) {
        try {
            Page<Company> page = companyRepository.getActiveCompanies(pageable);
            return Result.ok(page);
        } catch (Exception ex) {
            return UnknownErrorHandler.catchUnknownError(log,"getActiveCompanies", null, ex);
        }
    }

}
