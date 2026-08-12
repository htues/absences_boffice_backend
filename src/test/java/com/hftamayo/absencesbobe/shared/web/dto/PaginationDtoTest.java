package com.hftamayo.absencesbobe.shared.web.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationDtoTest {

    @Test
    void from_returnsNull_whenPageIsNull() {
        assertNull(PaginationDto.from(null));
    }

    @Test
    void from_mapsPageMetadata() {
        Page<String> page = new PageImpl<>(
                List.of("absence"),
                PageRequest.of(1, 1),
                3
        );

        PaginationDto pagination = PaginationDto.from(page);

        assertEquals(1, pagination.getPageIndex());
        assertEquals(1, pagination.getPageSize());
        assertEquals(3, pagination.getTotalCount());
        assertEquals(3, pagination.getTotalPages());
        assertTrue(pagination.isHasNext());
        assertTrue(pagination.isHasPrev());
    }

    @Test
    void from_mapsBoundaryPageFlags() {
        Page<String> page = new PageImpl<>(List.of("absence"), PageRequest.of(0, 10), 1);

        PaginationDto pagination = PaginationDto.from(page);

        assertFalse(pagination.isHasNext());
        assertFalse(pagination.isHasPrev());
    }
}
