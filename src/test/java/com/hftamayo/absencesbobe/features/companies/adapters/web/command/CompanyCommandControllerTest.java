package com.hftamayo.absencesbobe.features.companies.adapters.web.command;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CreateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.adapters.web.mapper.CompanyResponseMapper;
import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyCommandPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompanyCommandControllerTest {

    @Test
    void createCompany_success_returnsCreatedResponse_andMapsDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        CreateCompanyRequest body = new CreateCompanyRequest("Acme", "Some description", "Some address");
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company saved = Company.createNew("Acme", "Some description", "Some address");
        when(port.createCompany(any(Company.class))).thenReturn(Result.ok(saved));

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setName("Acme");
        when(mapper.toDto(saved)).thenReturn(dto);

        var response = controller.createCompany(body, request);

        assertEquals(SuccessApiResponse.CREATED.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        ApiResponseDto<?> api = response.getBody();
        assertEquals(SuccessApiResponse.CREATED.getResponseType(), api.getResponseType());
        assertEquals(SuccessApiResponse.CREATED.getStatusCode(), api.getStatusCode());
        assertEquals(SuccessApiResponse.CREATED.getMessageKey(), api.getResultMessage());
        assertSame(dto, api.getData());

        verify(port).createCompany(any(Company.class));
        verify(mapper).toDto(saved);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void createCompany_whenPortReturnsError_returnsErrorResponse_andDoesNotMapDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        CreateCompanyRequest body = new CreateCompanyRequest("Acme", "Some description", "Some address");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.createCompany(any(Company.class)))
                .thenAnswer(inv -> Result.error(ErrorApiResponse.ENTITY_EXISTS));

        var response = controller.createCompany(body, request);

        assertEquals(ErrorApiResponse.ENTITY_EXISTS.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        ApiResponseDto<?> api = response.getBody();
        assertEquals(ErrorApiResponse.ENTITY_EXISTS.getResponseType(), api.getResponseType());
        assertEquals(ErrorApiResponse.ENTITY_EXISTS.getStatusCode(), api.getStatusCode());
        assertEquals(ErrorApiResponse.ENTITY_EXISTS.getMessageKey(), api.getResultMessage());
        assertNull(api.getData());

        verify(port).createCompany(any(Company.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void createCompany_whenPortThrows_returnsUnknownErrorResponse() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        CreateCompanyRequest body = new CreateCompanyRequest("Acme", "Some description", "Some address");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.createCompany(any(Company.class))).thenThrow(new RuntimeException("boom"));

        var response = controller.createCompany(body, request);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getMessageKey(), response.getBody().getResultMessage());

        verify(port).createCompany(any(Company.class));
        verifyNoInteractions(mapper);
    }
}