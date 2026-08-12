package com.hftamayo.absencesbobe.shared.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuditInfoTest {

    @Test
    @DisplayName("empty: creates audit information without populated fields")
    void empty_createsAuditInfoWithoutValues() {
        AuditInfo auditInfo = AuditInfo.empty();

        assertNull(auditInfo.createdBy());
        assertNull(auditInfo.updatedBy());
        assertNull(auditInfo.createdDate());
        assertNull(auditInfo.updatedDate());
        assertFalse(auditInfo.isPresent());
    }

    @Test
    @DisplayName("constructor: retains every supplied audit value")
    void constructor_retainsSuppliedValues() {
        Instant createdDate = Instant.parse("2026-01-10T10:15:30Z");
        Instant updatedDate = Instant.parse("2026-02-20T12:30:45Z");

        AuditInfo auditInfo = new AuditInfo(11L, 22L, createdDate, updatedDate);

        assertAll(
                () -> assertEquals(11L, auditInfo.createdBy()),
                () -> assertEquals(22L, auditInfo.updatedBy()),
                () -> assertEquals(createdDate, auditInfo.createdDate()),
                () -> assertEquals(updatedDate, auditInfo.updatedDate()),
                () -> assertTrue(auditInfo.isPresent())
        );
    }

    @Test
    @DisplayName("isPresent: detects each audit field independently")
    void isPresent_detectsEachFieldIndependently() {
        Instant timestamp = Instant.parse("2026-03-15T08:00:00Z");

        assertAll(
                () -> assertTrue(new AuditInfo(null, null, timestamp, null).isPresent()),
                () -> assertTrue(new AuditInfo(null, null, null, timestamp).isPresent()),
                () -> assertTrue(new AuditInfo(1L, null, null, null).isPresent()),
                () -> assertTrue(new AuditInfo(null, 2L, null, null).isPresent())
        );
    }
}
