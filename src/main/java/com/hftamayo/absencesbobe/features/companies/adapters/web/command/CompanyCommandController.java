package com.hftamayo.absencesbobe.features.companies.adapters.web.command;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CreateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.UpdateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.CodeDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessCode;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.factory.ApiResponseFactory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Supplier;

@AllArgsConstructor
@RestController
@RequestMapping("/api/${version.api.current}/companies")
public class CompanyCommandController {
    private final CompanyCommandUseCase companyCommandUseCase;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> saveCompany(@RequestBody @Valid CreateCompanyRequest rawCompany,
                                                         HttpServletRequest request) {
        Company company = toCompany(rawCompany);

        return handle(
                () -> companyCommandUseCase.saveCompany(company),
                SuccessCode.CREATED,
                request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<?>> updateCompany(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCompanyRequest body,
            HttpServletRequest request
    ) {
        return handle(
                () -> companyCommandUseCase.updateCompany(id, body.name(), body.description(), body.address()),
                SuccessCode.UPDATED,
                request
        );
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteCompany(@PathVariable @Positive Long id, HttpServletRequest request) {
        return handle(
                () -> companyCommandUseCase.deleteCompany(id),
                SuccessCode.DELETED,
                request
        );
    }

    private Company toCompany(CreateCompanyRequest body) {
        return Company.createNew(body.name(), body.description(), body.address());
    }

    private <T> ResponseEntity<ApiResponseDto<?>> handle(
            Supplier<Result<T, ? extends CodeDescriptor>> action,
            SuccessCode successCode,
            HttpServletRequest request
    ) {
        try {
            Result<T, ? extends CodeDescriptor> result = action.get();
            return ApiResponseFactory.fromResult(result, successCode, null);
        } catch (Exception ignored) {
            // Optionally log here (or build an ErrorLogEventDto via ApiResponseFactory.buildErrorEvent(...))
            return ApiResponseFactory.unknownError(null);
        }
    }
}