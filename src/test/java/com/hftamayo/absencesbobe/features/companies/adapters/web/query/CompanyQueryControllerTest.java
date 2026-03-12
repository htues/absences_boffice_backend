package com.hftamayo.absencesbobe.features.companies.adapters.web.query;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.adapters.web.mapper.CompanyResponseMapper;
import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyQueryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompanyQueryControllerTest {

    @Test
    void getActiveCompanies_success_usesDefaults_whenParamsNull_andMapsDtos() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper);

        HttpServletRequest request = mock(HttpServletRequest.class);

        Page<Company> page = new PageImpl<>(List.of(mock(Company.class), mock(Company.class)));

        when(port.getActiveCompanies(any(Pageable.class)))
                .thenReturn(Result.ok(page));

        CompanyResponseDto dto1 = new CompanyResponseDto();
        CompanyResponseDto dto2 = new CompanyResponseDto();
        when(mapper.toDtoPageContent(page)).thenReturn(List.of(dto1, dto2));

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(null, null, request);

        assertEquals(SuccessApiResponse.READ.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        ApiResponseDto<?> api = response.getBody();
        assertEquals(SuccessApiResponse.READ.getResponseType(), api.getResponseType());
        assertEquals(SuccessApiResponse.READ.getStatusCode(), api.getStatusCode());
        assertEquals(SuccessApiResponse.READ.getMessageKey(), api.getResultMessage());

        Object data = api.getData();
        assertNotNull(data);
        assertTrue(data instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<CompanyResponseDto> dtos = (List<CompanyResponseDto>) data;
        assertEquals(List.of(dto1, dto2), dtos);

        assertNotNull(api.getPagination());
        assertEquals(0, api.getPagination().getPageIndex());
        assertEquals(2, api.getPagination().getPageSize());

        verify(port).getActiveCompanies(argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        verify(mapper).toDtoPageContent(page);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void getActiveCompanies_success_usesProvidedPageAndSize_whenValid() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper);

        HttpServletRequest request = mock(HttpServletRequest.class);

        Page<Company> empty = Page.empty();

        when(port.getActiveCompanies(any(Pageable.class)))
                .thenReturn(Result.ok(empty));
        when(mapper.toDtoPageContent(empty)).thenReturn(List.of());

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(1, 5, request);

        assertEquals(SuccessApiResponse.READ.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(port).getActiveCompanies(argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 5));
        verify(mapper).toDtoPageContent(empty);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void getActiveCompanies_success_capsSizeToMax_whenTooLarge() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper);

        HttpServletRequest request = mock(HttpServletRequest.class);

        Page<Company> empty = Page.empty();

        when(port.getActiveCompanies(any(Pageable.class)))
                .thenReturn(Result.ok(empty));
        when(mapper.toDtoPageContent(empty)).thenReturn(List.of());

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(0, 9999, request);

        assertEquals(SuccessApiResponse.READ.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(port).getActiveCompanies(argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 100));
        verify(mapper).toDtoPageContent(empty);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void getActiveCompanies_success_normalizesNegativePageToZero_andInvalidSizeToDefault() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper);

        HttpServletRequest request = mock(HttpServletRequest.class);

        Page<Company> empty = Page.empty();

        when(port.getActiveCompanies(any(Pageable.class)))
                .thenReturn(Result.ok(empty));
        when(mapper.toDtoPageContent(empty)).thenReturn(List.of());

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(-1, 0, request);

        assertEquals(SuccessApiResponse.READ.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(port).getActiveCompanies(argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        verify(mapper).toDtoPageContent(empty);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void getActiveCompanies_whenPortReturnsError_returnsErrorResponse_andDoesNotMapDtos() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper);

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.getActiveCompanies(any(Pageable.class)))
                .thenAnswer(inv -> Result.error(ErrorApiResponse.UNKNOWN_ERROR));

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(0, 20, request);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getResponseType(), response.getBody().getResponseType());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), response.getBody().getStatusCode());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getMessageKey(), response.getBody().getResultMessage());
        assertNull(response.getBody().getData());
        assertNull(response.getBody().getPagination());

        verify(port).getActiveCompanies(any(Pageable.class));
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(port);
    }

    @Test
    void getActiveCompanies_whenPortThrows_returnsUnknownErrorResponse() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper);

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.getActiveCompanies(any(Pageable.class)))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(0, 20, request);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getMessageKey(), response.getBody().getResultMessage());

        verify(port).getActiveCompanies(any(Pageable.class));
        verifyNoInteractions(mapper);
    }
}