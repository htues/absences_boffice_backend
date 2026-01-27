package com.hftamayo.absencesbobe.features.companies.adapters.web.command;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CreateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.UpdateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.java.boabsenses.dto.ApiResultDto;
import com.hftamayo.java.boabsenses.dto.response.CompanyResponseDto;
import com.hftamayo.java.boabsenses.mapper.ApiResponseMapper;
import com.hftamayo.java.boabsenses.utilities.web.ApiResponseUtils;
import com.hftamayo.java.boabsenses.exception.BusinessError;
import com.hftamayo.java.boabsenses.utilities.web.UnexpectedExceptionHandler;
import com.hftamayo.java.boabsenses.utilities.notification.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@AllArgsConstructor
@RestController
@RequestMapping("/api/${version.api.current}/companies")
public class CompanyCommandController {
    private final CompanyCommandUseCase companyCommandUseCase;
    private final UnexpectedExceptionHandler unexpectedExceptionHandler;

    @PostMapping
    public ResponseEntity<ApiResultDto<?>> saveCompany(@RequestBody @Valid CreateCompanyRequest rawCompany,
                                                       HttpServletRequest request) {
        Company company = toCompany(rawCompany);

        return handle(request,
                () -> companyCommandService.saveCompany(company),
                ApiResponseMapper.OperationType.CREATE);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResultDto<?>> updateCompany(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCompanyRequest body,
            HttpServletRequest request
    ) {
        return handle(request,
                () -> companyCommandService.updateCompany(id, body.name(), body.description(), body.address()),
                ApiResponseMapper.OperationType.UPDATE);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ApiResultDto<?>> deleteCompany(@PathVariable @Positive Long id, HttpServletRequest request) {
        return handle(request,
                () -> companyCommandService.deleteCompany(id),
                ApiResponseMapper.OperationType.DELETE);
    }

    private Company toCompany(CreateCompanyRequest body) {
        return Company.createNew(body.name(), body.description(), body.address());
    }

private <T> ResponseEntity<ApiResultDto<?>> handle(
        HttpServletRequest request,
        Supplier<Result<T, BusinessError>> action,
        ApiResponseMapper.OperationType operationType
) {
    try {
        Result<T, BusinessError> result = action.get();
        return ApiResponseUtils.build(request, result, operationType, CompanyCommandController.class);
    } catch (Exception e) {
        return unexpectedExceptionHandler.handle(request, e, CompanyCommandController.class);
    }
}

}
