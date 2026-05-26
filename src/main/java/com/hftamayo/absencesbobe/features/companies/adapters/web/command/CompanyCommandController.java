package com.hftamayo.absencesbobe.features.companies.adapters.web.command;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CreateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.UpdateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.adapters.web.mapper.CompanyResponseMapper;
import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyCommandPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.infrastructure.ratelimit.RateLimit;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.factory.ApiResponseFactory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Supplier;

@AllArgsConstructor
@Slf4j
@Validated
@RestController
@RequestMapping("/api/${version.api.current}/companies")
public class CompanyCommandController {
    private final CompanyCommandPort companyCommandPort;
    private final CompanyResponseMapper companyResponseMapper;

    @RateLimit(tokens = 5)
    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> createCompany(@RequestBody @Valid CreateCompanyRequest rawCompany,
                                                           HttpServletRequest request) {
        Company company = toCompany(rawCompany);

        return handle(
                () -> mapResult(companyCommandPort.createCompany(company), companyResponseMapper::toDto),
                SuccessApiResponse.CREATED,
                request
        );
    }

    @RateLimit(tokens = 5)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<?>> updateCompany(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateCompanyRequest body,
            HttpServletRequest request
    ) {
        return handle(
                () -> mapResult(
                        companyCommandPort.updateCompany(id, body.name(), body.description(), body.address()),
                        companyResponseMapper::toDto
                ),
                SuccessApiResponse.UPDATED,
                request
        );
    }

    @RateLimit(tokens = 5)
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponseDto<?>> deactivateCompany(
            @PathVariable @Positive Long id,
            HttpServletRequest request
    ) {
        return handle(
                () -> mapResult(
                        companyCommandPort.deactivateCompany(id),
                        companyResponseMapper::toDto
                ),
                SuccessApiResponse.UPDATED,
                request
        );
    }

    @RateLimit(tokens = 5)
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponseDto<?>> activateCompany(
            @PathVariable @Positive Long id,
            HttpServletRequest request
    ) {
        return handle(
                () -> mapResult(
                        companyCommandPort.activateCompany(id),
                        companyResponseMapper::toDto
                ),
                SuccessApiResponse.UPDATED,
                request
        );
    }

    @RateLimit(tokens = 5)
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponseDto<?>> restoreCompany(
            @PathVariable @Positive Long id,
            HttpServletRequest request
    ) {
        return handle(
                () -> mapResult(
                        companyCommandPort.restoreCompany(id),
                        companyResponseMapper::toDto
                ),
                SuccessApiResponse.UPDATED,
                request
        );
    }

    @RateLimit(tokens = 5)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteCompany(@PathVariable @Positive Long id, HttpServletRequest request) {
        return handle(
                () -> companyCommandPort.deleteCompany(id),
                SuccessApiResponse.DELETED,
                request
        );
    }

    private Company toCompany(CreateCompanyRequest body) {
        return Company.createNew(body.name(), body.description(), body.address());
    }

    private static <T, R> Result<R, ? extends ApiResponseDescriptor> mapResult(
            Result<T, ? extends ApiResponseDescriptor> result,
            java.util.function.Function<T, R> mapper
    ) {
        if (result == null) {
            return Result.error(ErrorApiResponse.UNKNOWN_ERROR);
        }
        if (!result.isSuccess()) {
            return Result.error(result.error());
        }
        return Result.ok(mapper.apply(result.value()));
    }

    private <T> ResponseEntity<ApiResponseDto<?>> handle(
            Supplier<Result<T, ? extends ApiResponseDescriptor>> action,
            SuccessApiResponse successCode,
            HttpServletRequest request
    ) {
        try {
            Result<T, ? extends ApiResponseDescriptor> result = action.get();

            if (result == null || result.isError()) {
                log.debug("Command returned error result: error={}", result != null ? result.error() : null);
            }

            return ApiResponseFactory.fromResult(result, successCode, null);
        } catch (Exception ex) {
            return catchUnknownError("CompanyCommandController.handle", ex);
        }
    }

    private ResponseEntity<ApiResponseDto<?>> catchUnknownError(String where, Exception ex) {
        log.error("Unhandled exception at {}", where, ex);
        return ApiResponseFactory.unknownError(null);
    }

}