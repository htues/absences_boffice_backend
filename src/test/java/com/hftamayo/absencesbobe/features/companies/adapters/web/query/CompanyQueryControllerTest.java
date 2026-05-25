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
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(-1, null, request);

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

    @Test
    void getActiveCompanies_whenPortReturnsNullResult_returnsUnknownErrorResponse() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper);

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.getActiveCompanies(any(Pageable.class))).thenReturn(null);

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
    }

    @Test
    void getActiveCompanies_success_usesSafeFallbacks_whenConfiguredPageSizesAreInvalid() {
        CompanyQueryPort port = mock(CompanyQueryPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyQueryController controller = new CompanyQueryController(port, mapper, 0, -1);

        HttpServletRequest request = mock(HttpServletRequest.class);

        Page<Company> empty = Page.empty();
        when(port.getActiveCompanies(any(Pageable.class))).thenReturn(Result.ok(empty));
        when(mapper.toDtoPageContent(empty)).thenReturn(List.of());

        ResponseEntity<ApiResponseDto<?>> response = controller.getActiveCompanies(1, 999, request);

        assertEquals(SuccessApiResponse.READ.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(port).getActiveCompanies(argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 100));
        verify(mapper).toDtoPageContent(empty);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void mapResult_whenResultIsNull_returnsUnknownError() throws Exception {
        AtomicInteger mapperCalls = new AtomicInteger(0);

        Result<String, ? extends ApiResponseDescriptor> mapped = invokeMapResult(
                null,
                value -> {
                    mapperCalls.incrementAndGet();
                    return String.valueOf(value);
                }
        );

        assertTrue(mapped.isError());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, mapped.error());
        assertEquals(0, mapperCalls.get());
    }

    @Test
    void mapResult_whenResultIsError_propagatesErrorWithoutCallingMapper() throws Exception {
        AtomicInteger mapperCalls = new AtomicInteger(0);
        Result<Integer, ? extends ApiResponseDescriptor> original = Result.error(ErrorApiResponse.NOT_FOUND);

        Result<String, ? extends ApiResponseDescriptor> mapped = invokeMapResult(
                original,
                value -> {
                    mapperCalls.incrementAndGet();
                    return "mapped-" + value;
                }
        );

        assertTrue(mapped.isError());
        assertEquals(ErrorApiResponse.NOT_FOUND, mapped.error());
        assertEquals(0, mapperCalls.get());
    }

    @Test
    void mapResult_whenResultIsSuccess_mapsValueAndReturnsSuccess() throws Exception {
        Result<Integer, ? extends ApiResponseDescriptor> original = Result.ok(7);

        Result<String, ? extends ApiResponseDescriptor> mapped = invokeMapResult(
                original,
                value -> "company-" + value
        );

        assertTrue(mapped.isSuccess());
        assertEquals("company-7", mapped.value());
    }

    @SuppressWarnings("unchecked")
    private static <T, R> Result<R, ? extends ApiResponseDescriptor> invokeMapResult(
            Result<T, ? extends ApiResponseDescriptor> result,
            Function<T, R> mapper
    ) throws Exception {
        Method method = CompanyQueryController.class
                .getDeclaredMethod("mapResult", Result.class, Function.class);
        method.setAccessible(true);
        return (Result<R, ? extends ApiResponseDescriptor>) method.invoke(null, result, mapper);
    }
}