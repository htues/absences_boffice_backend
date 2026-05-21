package com.hftamayo.absencesbobe.features.companies.adapters.web.query;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.adapters.web.mapper.CompanyResponseMapper;
import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyQueryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.infrastructure.ratelimit.RateLimit;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.dto.PaginationDto;
import com.hftamayo.absencesbobe.shared.web.factory.ApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/${version.api.current}/companies")
public class CompanyQueryController {
    private final CompanyQueryPort companyQueryPort;
    private final CompanyResponseMapper companyResponseMapper;

    private final int defaultPageSize;
    private final int maxPageSize;

    @Autowired
    public CompanyQueryController(
            CompanyQueryPort companyQueryPort,
            CompanyResponseMapper companyResponseMapper,
            @Value("${pagination.default-page-size:10}") int defaultPageSize,
            @Value("${pagination.max-page-size:100}") int maxPageSize
    ) {
        this.companyQueryPort = companyQueryPort;
        this.companyResponseMapper = companyResponseMapper;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    // Convenience ctor for unit tests (and non-Spring instantiation)
    public CompanyQueryController(CompanyQueryPort companyQueryPort, CompanyResponseMapper companyResponseMapper) {
        this(companyQueryPort, companyResponseMapper, 10, 100);
    }

    @RateLimit(tokens = 1)
    @GetMapping
    public ResponseEntity<ApiResponseDto<?>> getActiveCompanies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request
    ) {
        try {
            return getActiveCompaniesFormatted(page, size);

        } catch (Exception ex) {
            log.error("Unhandled exception at CompanyQueryController.getActiveCompanies", ex);
            return ApiResponseFactory.unknownError(null);
        }
    }

    private ResponseEntity<ApiResponseDto<?>> getActiveCompaniesFormatted(Integer page, Integer size) {
        Pageable pageable = resolvePageable(page, size, defaultPageSize, maxPageSize);

        Result<Page<Company>, ? extends ApiResponseDescriptor> result =
                companyQueryPort.getActiveCompanies(pageable);

        if (result == null) {
            return ApiResponseFactory.unknownError(null);
        }

        if (!result.isSuccess()) {
            return ApiResponseFactory.fromResult(Result.error(result.error()),
                    SuccessApiResponse.READ, null);
        }

        Page<Company> companiesPage = result.value();

        List<CompanyResponseDto> data = companyResponseMapper.toDtoPageContent(companiesPage);
        PaginationDto pagination = PaginationDto.from(companiesPage);

        ApiResponseDto<List<CompanyResponseDto>> body =
                ApiResponseDto.ok(SuccessApiResponse.READ, data, pagination, null);

        return ResponseEntity.status(SuccessApiResponse.READ.getStatusCode()).body(body);
    }

    private static Pageable resolvePageable(Integer page, Integer size, int defaultPageSize, int maxPageSize) {
        int safeDefaultPageSize = defaultPageSize > 0 ? defaultPageSize : 10;
        int safeMaxPageSize = maxPageSize > 0 ? maxPageSize : 100;

        int pageNumber = (page != null && page >= 0) ? page : 0;

        int rawSize = (size != null && size > 0) ? size : safeDefaultPageSize;
        int pageSize = Math.min(rawSize, safeMaxPageSize);

        return PageRequest.of(pageNumber, pageSize);
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