package com.hftamayo.absencesbobe.features.shared.web.dto;

import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.dto.PaginationDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseDtoTest {

    @Test
    void response_copiesDescriptorFields_setsData_cache_andTimestamp() {
        ApiResponseDescriptor code = new ApiResponseDescriptor() {
            @Override public String getResponseType() { return "success"; }
            @Override public int getStatusCode() { return 200; }
            @Override public String getMessageKey() { return "ENTITY_RETRIEVED"; }
        };

        Instant before = Instant.now();
        ApiResponseDto<String> dto = ApiResponseDto.response(code, "payload", 10L);
        Instant after = Instant.now();

        assertEquals("success", dto.getResponseType());
        assertEquals(200, dto.getStatusCode());
        assertEquals("ENTITY_RETRIEVED", dto.getResultMessage());
        assertEquals("payload", dto.getData());
        assertNull(dto.getPagination());
        assertEquals(10L, dto.getCacheTTL());

        assertNotNull(dto.getTimestamp());
        assertFalse(dto.getTimestamp().isBefore(before));
        assertFalse(dto.getTimestamp().isAfter(after));
    }

    @Test
    void ok_buildsSuccessDto() {
        ApiResponseDto<String> dto = ApiResponseDto.ok(SuccessApiResponse.READ, "hello", null);

        assertEquals(SuccessApiResponse.READ.getResponseType(), dto.getResponseType());
        assertEquals(SuccessApiResponse.READ.getStatusCode(), dto.getStatusCode());
        assertEquals(SuccessApiResponse.READ.getMessageKey(), dto.getResultMessage());
        assertEquals("hello", dto.getData());
        assertNull(dto.getPagination());
        assertNull(dto.getCacheTTL());
        assertNotNull(dto.getTimestamp());
    }

    @Test
    void ok_withPagination_buildsSuccessDto_andSetsPagination() {
        PaginationDto pagination = PaginationDto.builder()
                .pageIndex(0)
                .pageSize(10)
                .totalCount(4)
                .totalPages(1)
                .hasNext(false)
                .hasPrev(false)
                .build();

        ApiResponseDto<String> dto = ApiResponseDto.ok(SuccessApiResponse.READ, "hello", pagination, null);

        assertEquals(SuccessApiResponse.READ.getResponseType(), dto.getResponseType());
        assertEquals(SuccessApiResponse.READ.getStatusCode(), dto.getStatusCode());
        assertEquals(SuccessApiResponse.READ.getMessageKey(), dto.getResultMessage());
        assertEquals("hello", dto.getData());
        assertSame(pagination, dto.getPagination());
        assertNull(dto.getCacheTTL());
        assertNotNull(dto.getTimestamp());
    }

    @Test
    void fail_buildsErrorDto_withNullData() {
        ApiResponseDto<Void> dto = ApiResponseDto.fail(ErrorApiResponse.NOT_FOUND, 5L);

        assertEquals(ErrorApiResponse.NOT_FOUND.getResponseType(), dto.getResponseType());
        assertEquals(ErrorApiResponse.NOT_FOUND.getStatusCode(), dto.getStatusCode());
        assertEquals(ErrorApiResponse.NOT_FOUND.getMessageKey(), dto.getResultMessage());
        assertNull(dto.getData());
        assertNull(dto.getPagination());
        assertEquals(5L, dto.getCacheTTL());
        assertNotNull(dto.getTimestamp());
    }
}