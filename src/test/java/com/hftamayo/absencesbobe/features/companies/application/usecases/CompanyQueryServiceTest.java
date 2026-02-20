package com.hftamayo.absencesbobe.features.companies.application.usecases;

import com.hftamayo.absencesbobe.features.companies.application.ports.out.CompanyQueryRepositoryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyQueryServiceTest {

    @Mock
    private CompanyQueryRepositoryPort companyRepository;

    @InjectMocks
    private CompanyQueryService service;

    @Test
    void getActiveCompanies_shouldReturnOkResult_withPageFromRepository() {
        Pageable pageable = PageRequest.of(0, 2);
        Company c1 = mock(Company.class);
        Company c2 = mock(Company.class);
        Page<Company> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(companyRepository.getActiveCompanies(pageable)).thenReturn(page);

        Result<Page<Company>, ? extends ApiResponseDescriptor> result = service.getActiveCompanies(pageable);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isError()).isFalse();
        assertThat(result.getValueOrThrow()).isSameAs(page);

        verify(companyRepository, times(1)).getActiveCompanies(pageable);
        verifyNoMoreInteractions(companyRepository);
    }

    @Test
    void getActiveCompanies_shouldReturnUnknownErrorResult_whenRepositoryThrows() {
        Pageable pageable = PageRequest.of(0, 10);
        RuntimeException boom = new RuntimeException("boom");

        when(companyRepository.getActiveCompanies(pageable)).thenThrow(boom);

        Result<Page<Company>, ? extends ApiResponseDescriptor> result = service.getActiveCompanies(pageable);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isError()).isTrue();
        assertThat(result.getErrorOrThrow()).isEqualTo(ErrorApiResponse.UNKNOWN_ERROR);

        verify(companyRepository, times(1)).getActiveCompanies(pageable);
        verifyNoMoreInteractions(companyRepository);
    }
}
