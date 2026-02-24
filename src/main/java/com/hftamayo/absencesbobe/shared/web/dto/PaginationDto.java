package com.hftamayo.absencesbobe.shared.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Builder
@Getter
@AllArgsConstructor
public class PaginationDto {
    private final int pageIndex;
    private final int pageSize;
    private final long totalCount;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrev;

    public static PaginationDto from(Page<?> page) {
        if (page == null) return null;

        return PaginationDto.builder()
                .pageIndex(page.getNumber())
                .pageSize(page.getSize())
                .totalCount(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrev(page.hasPrevious())
                .build();
    }
}