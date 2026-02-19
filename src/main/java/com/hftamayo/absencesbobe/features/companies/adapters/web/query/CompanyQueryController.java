package com.hftamayo.absencesbobe.features.companies.adapters.web.query;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.adapters.web.mapper.CompanyResponseMapper;
import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyQueryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.factory.ApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/${version.api.current}/companies")
public class CompanyQueryController {
    private final CompanyQueryPort companyQueryPort;
    private final CompanyResponseMapper companyResponseMapper;

    @GetMapping
    public ResponseEntity<ApiResponseDto<?>> getActiveCompanies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request
    ) {
        try {
            int pageNumber = (page != null && page >= 0) ? page : 0;
            int pageSize = (size != null && size > 0) ? size : 20;

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            Result<Page<Company>, ? extends ApiResponseDescriptor> result =
                    companyQueryPort.getActiveCompanies(pageable);

            Result<Page<CompanyResponseDto>, ? extends ApiResponseDescriptor> mapped =
                    mapResult(result, p -> p.map(companyResponseMapper::toDto));

            return ApiResponseFactory.fromResult(mapped, SuccessApiResponse.READ, null);
        } catch (Exception ex) {
            log.error("Unhandled exception at CompanyQueryController.getActiveCompanies", ex);
            return ApiResponseFactory.unknownError(null);
        }
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
}