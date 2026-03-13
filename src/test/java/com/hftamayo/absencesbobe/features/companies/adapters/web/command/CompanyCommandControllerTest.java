package com.hftamayo.absencesbobe.features.companies.adapters.web.command;

import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CompanyResponseDto;
import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.CreateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.adapters.web.dto.UpdateCompanyRequest;
import com.hftamayo.absencesbobe.features.companies.adapters.web.mapper.CompanyResponseMapper;
import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyCommandPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompanyCommandControllerTest {

    private static final String NAME = "Acme";
    private static final String DESCRIPTION = "Some description";
    private static final String ADDRESS = "Some address";
    private static final String UPDATED_NAME = "Acme Updated";
    private static final String UPDATED_DESCRIPTION = "Updated description";
    private static final String UPDATED_ADDRESS = "Updated address";

    @Test
    void createCompanySuccessReturnsCreatedResponseAndMapsDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        CreateCompanyRequest body = new CreateCompanyRequest(NAME, DESCRIPTION, ADDRESS);
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company saved = Company.createNew(NAME, DESCRIPTION, ADDRESS);
        when(port.createCompany(any(Company.class))).thenReturn(Result.ok(saved));

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setName(NAME);
        when(mapper.toDto(saved)).thenReturn(dto);

        var response = controller.createCompany(body, request);

        assertApiCode(response, SuccessApiResponse.CREATED);
        ApiResponseDto<?> api = response.getBody();
        assertSame(dto, api.getData());

        verify(port).createCompany(any(Company.class));
        verify(mapper).toDto(saved);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void createCompanyWhenPortReturnsErrorReturnsErrorResponseAndDoesNotMapDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        CreateCompanyRequest body = new CreateCompanyRequest(NAME, DESCRIPTION, ADDRESS);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.createCompany(any(Company.class)))
                .thenAnswer(inv -> Result.error(ErrorApiResponse.ENTITY_EXISTS));

        var response = controller.createCompany(body, request);

        assertApiCode(response, ErrorApiResponse.ENTITY_EXISTS);
        ApiResponseDto<?> api = response.getBody();
        assertNull(api.getData());

        verify(port).createCompany(any(Company.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void createCompanyWhenPortThrowsReturnsUnknownErrorResponse() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        CreateCompanyRequest body = new CreateCompanyRequest(NAME, DESCRIPTION, ADDRESS);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.createCompany(any(Company.class))).thenThrow(new RuntimeException("boom"));

        var response = controller.createCompany(body, request);

        assertApiCode(response, ErrorApiResponse.UNKNOWN_ERROR);

        verify(port).createCompany(any(Company.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void updateCompanySuccessReturnsUpdatedResponseAndMapsDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 11L;
        UpdateCompanyRequest body = new UpdateCompanyRequest(UPDATED_NAME, UPDATED_DESCRIPTION, UPDATED_ADDRESS);
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company updated = Company.createNew(body.name(), body.description(), body.address());
        when(port.updateCompany(id, body.name(), body.description(), body.address())).thenReturn(Result.ok(updated));

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setName(UPDATED_NAME);
        when(mapper.toDto(updated)).thenReturn(dto);

        var response = controller.updateCompany(id, body, request);

        assertApiCode(response, SuccessApiResponse.UPDATED);
        assertSame(dto, response.getBody().getData());

        verify(port).updateCompany(id, body.name(), body.description(), body.address());
        verify(mapper).toDto(updated);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void updateCompanyWhenPortReturnsNullReturnsUnknownErrorAndDoesNotMapDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 21L;
        UpdateCompanyRequest body = new UpdateCompanyRequest(UPDATED_NAME, UPDATED_DESCRIPTION, UPDATED_ADDRESS);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.updateCompany(id, body.name(), body.description(), body.address())).thenReturn(null);

        var response = controller.updateCompany(id, body, request);

        assertApiCode(response, ErrorApiResponse.UNKNOWN_ERROR);
        assertNull(response.getBody().getData());

        verify(port).updateCompany(id, body.name(), body.description(), body.address());
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(port);
    }

    @Test
    void deactivateCompanySuccessReturnsUpdatedResponseAndMapsDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 31L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company deactivated = Company.createNew(NAME, DESCRIPTION, ADDRESS);
        when(port.deactivateCompany(id)).thenReturn(Result.ok(deactivated));

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setName(NAME);
        when(mapper.toDto(deactivated)).thenReturn(dto);

        var response = controller.deactivateCompany(id, request);

        assertApiCode(response, SuccessApiResponse.UPDATED);
        assertSame(dto, response.getBody().getData());

        verify(port).deactivateCompany(id);
        verify(mapper).toDto(deactivated);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void deactivateCompanyWhenMapperThrowsReturnsUnknownErrorResponse() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 32L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company deactivated = Company.createNew(NAME, DESCRIPTION, ADDRESS);
        when(port.deactivateCompany(id)).thenReturn(Result.ok(deactivated));
        when(mapper.toDto(deactivated)).thenThrow(new RuntimeException("mapper boom"));

        var response = controller.deactivateCompany(id, request);

        assertApiCode(response, ErrorApiResponse.UNKNOWN_ERROR);

        verify(port).deactivateCompany(id);
        verify(mapper).toDto(deactivated);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void activateCompanySuccessReturnsUpdatedResponseAndMapsDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 41L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company activated = Company.createNew(NAME, DESCRIPTION, ADDRESS);
        when(port.activateCompany(id)).thenReturn(Result.ok(activated));

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setName(NAME);
        when(mapper.toDto(activated)).thenReturn(dto);

        var response = controller.activateCompany(id, request);

        assertApiCode(response, SuccessApiResponse.UPDATED);
        assertSame(dto, response.getBody().getData());

        verify(port).activateCompany(id);
        verify(mapper).toDto(activated);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void restoreCompanySuccessReturnsUpdatedResponseAndMapsDto() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 51L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company restored = Company.createNew(NAME, DESCRIPTION, ADDRESS);
        when(port.restoreCompany(id)).thenReturn(Result.ok(restored));

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setName(NAME);
        when(mapper.toDto(restored)).thenReturn(dto);

        var response = controller.restoreCompany(id, request);

        assertApiCode(response, SuccessApiResponse.UPDATED);
        assertSame(dto, response.getBody().getData());

        verify(port).restoreCompany(id);
        verify(mapper).toDto(restored);
        verifyNoMoreInteractions(port, mapper);
    }

    @Test
    void deleteCompanySuccessReturnsDeletedResponse() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 61L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        Company deleted = Company.createNew(NAME, DESCRIPTION, ADDRESS);
        when(port.deleteCompany(id)).thenReturn(Result.ok(deleted));

        var response = controller.deleteCompany(id, request);

        assertApiCode(response, SuccessApiResponse.DELETED);
        assertSame(deleted, response.getBody().getData());

        verify(port).deleteCompany(id);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(port);
    }

    @Test
    void deleteCompanyWhenPortReturnsErrorReturnsErrorResponse() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 71L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.deleteCompany(id)).thenAnswer(inv -> Result.error(ErrorApiResponse.NOT_FOUND));

        var response = controller.deleteCompany(id, request);

        assertApiCode(response, ErrorApiResponse.NOT_FOUND);
        assertNull(response.getBody().getData());

        verify(port).deleteCompany(id);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(port);
    }

    @Test
    void deleteCompanyWhenPortReturnsNullReturnsUnknownErrorResponse() {
        CompanyCommandPort port = mock(CompanyCommandPort.class);
        CompanyResponseMapper mapper = mock(CompanyResponseMapper.class);
        CompanyCommandController controller = new CompanyCommandController(port, mapper);

        Long id = 81L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(port.deleteCompany(id)).thenReturn(null);

        var response = controller.deleteCompany(id, request);

        assertApiCode(response, ErrorApiResponse.UNKNOWN_ERROR);
        assertNull(response.getBody().getData());

        verify(port).deleteCompany(id);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(port);
    }

    private void assertApiCode(ResponseEntity<ApiResponseDto<?>> response, ApiResponseDescriptor descriptor) {
        assertEquals(descriptor.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(descriptor.getResponseType(), response.getBody().getResponseType());
        assertEquals(descriptor.getStatusCode(), response.getBody().getStatusCode());
        assertEquals(descriptor.getMessageKey(), response.getBody().getResultMessage());
    }
}