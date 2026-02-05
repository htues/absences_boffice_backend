package com.hftamayo.absencesbobe.shared.domain;

import java.time.Instant;

public record AuditInfo(
        Long createdBy,
        Long updatedBy,
        Instant createdDate,
        Instant updatedDate
) {
    public static AuditInfo empty() {
        return new AuditInfo(null, null, null, null);
    }

    public boolean isPresent() {
        return createdDate != null || updatedDate != null || createdBy != null || updatedBy != null;
    }
}