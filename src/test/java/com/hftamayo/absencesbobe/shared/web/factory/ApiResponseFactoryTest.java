package com.hftamayo.absencesbobe.features.shared.web.factory;

import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.factory.ApiResponseFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseFactoryTest {

    @Test
    void fromResult_throwsWhenSuccessCodeIsNull() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> ApiResponseFactory.fromResult(null, null, 123L)
        );
        assertEquals("successCode must not be null", ex.getMessage());
    }

    @Test
    void fromResult_whenResultIsNull_returnsUnknownError() {
        ResponseEntity<ApiResponseDto<?>> response =
                ApiResponseFactory.fromResult(null, SuccessApiResponse.READ, 10L);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), response.getBody().getStatusCode());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getMessageKey(), response.getBody().getResultMessage());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getResponseType(), response.getBody().getResponseType());
        assertNull(response.getBody().getData());
        assertEquals(10L, response.getBody().getCacheTTL());
    }

    @Test
    void fromResult_whenSuccess_returnsSuccessResponse() {
        Result<String, ApiResponseDescriptor> result = Result.ok("hello");

        ResponseEntity<ApiResponseDto<?>> response =
                ApiResponseFactory.fromResult(result, SuccessApiResponse.READ, 5L);

        assertEquals(SuccessApiResponse.READ.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(SuccessApiResponse.READ.getStatusCode(), response.getBody().getStatusCode());
        assertEquals(SuccessApiResponse.READ.getMessageKey(), response.getBody().getResultMessage());
        assertEquals(SuccessApiResponse.READ.getResponseType(), response.getBody().getResponseType());

        assertEquals("hello", response.getBody().getData());
        assertEquals(5L, response.getBody().getCacheTTL());
    }

    @Test
    void fromResult_whenErrorIsErrorApiResponse_returnsThatError() {
        Result<String, ApiResponseDescriptor> result = Result.error(ErrorApiResponse.NOT_FOUND);

        ResponseEntity<ApiResponseDto<?>> response =
                ApiResponseFactory.fromResult(result, SuccessApiResponse.READ, null);

        assertEquals(ErrorApiResponse.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorApiResponse.NOT_FOUND.getStatusCode(), response.getBody().getStatusCode());
        assertEquals(ErrorApiResponse.NOT_FOUND.getMessageKey(), response.getBody().getResultMessage());
        assertEquals(ErrorApiResponse.NOT_FOUND.getResponseType(), response.getBody().getResponseType());
        assertNull(response.getBody().getData());
        assertNull(response.getBody().getCacheTTL());
    }

    @Test
    void fromResult_whenErrorIsNotErrorApiResponse_fallsBackToUnknown() {
        ApiResponseDescriptor otherDescriptor = new ApiResponseDescriptor() {
            @Override public String getResponseType() { return "error"; }
            @Override public int getStatusCode() { return 418; }
            @Override public String getMessageKey() { return "SOMETHING_ELSE"; }
        };

        Result<String, ApiResponseDescriptor> result = Result.error(otherDescriptor);

        ResponseEntity<ApiResponseDto<?>> response =
                ApiResponseFactory.fromResult(result, SuccessApiResponse.READ, 1L);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getMessageKey(), response.getBody().getResultMessage());
    }

    @Test
    void responseError_nullDescriptor_returnsUnknown() {
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, ApiResponseFactory.responseError(null));
    }
}