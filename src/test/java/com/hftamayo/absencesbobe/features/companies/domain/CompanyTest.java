package com.hftamayo.absencesbobe.features.companies.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

public class CompanyTest {
    @Test
    @DisplayName("createNew: sets id null, active=true, deleted=false, and trims text fields")
    void createNew_setsDefaults_andTrims() {
        Company company = Company.createNew("  ACME  ", "  Desc  ", "  Address  ");

        assertNull(company.getId());
        assertEquals("ACME", company.getName());
        assertEquals("Desc", company.getDescription());
        assertEquals("Address", company.getAddress());

        assertTrue(company.isActive());
        assertFalse(company.isDeleted());

        assertNotNull(company.getAuditInfo(), "auditInfo should never be null (defaults to AuditInfo.empty())");
    }

    @Test
    @DisplayName("createNew: rejects blank or null name/description/address")
    void createNew_rejectsBlankOrNullFields() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> Company.createNew(null, "d", "a"));
        assertTrue(ex1.getMessage().contains("name"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> Company.createNew("   ", "d", "a"));
        assertTrue(ex2.getMessage().contains("name"));

        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
                () -> Company.createNew("n", "", "a"));
        assertTrue(ex3.getMessage().contains("description"));

        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
                () -> Company.createNew("n", "d", "   "));
        assertTrue(ex4.getMessage().contains("address"));
    }

    @Test
    @DisplayName("rehydrate: preserves provided id/state and defaults auditInfo to empty when null")
    void rehydrate_preservesState_andDefaultsAuditInfo() {
        Company company = Company.rehydrate(
                10L,
                "Name",
                "Description",
                "Address",
                false,
                true,
                null
        );

        assertEquals(10L, company.getId());
        assertEquals("Name", company.getName());
        assertEquals("Description", company.getDescription());
        assertEquals("Address", company.getAddress());

        assertFalse(company.isActive());
        assertTrue(company.isDeleted());

        assertNotNull(company.getAuditInfo(), "auditInfo should default to AuditInfo.empty() when null");
    }

    @Test
    @DisplayName("updateDetails: updates and trims text fields")
    void updateDetails_updatesAndTrims() {
        Company company = Company.createNew("A", "B", "C");

        company.updateDetails("  New Name  ", "  New Desc  ", "  New Address  ");

        assertEquals("New Name", company.getName());
        assertEquals("New Desc", company.getDescription());
        assertEquals("New Address", company.getAddress());
    }

    @Test
    @DisplayName("updateDetails: rejects blank or null inputs")
    void updateDetails_rejectsBlankOrNull() {
        Company company = Company.createNew("A", "B", "C");

        assertThrows(IllegalArgumentException.class, () -> company.updateDetails(null, "d", "a"));
        assertThrows(IllegalArgumentException.class, () -> company.updateDetails("n", "   ", "a"));
        assertThrows(IllegalArgumentException.class, () -> company.updateDetails("n", "d", ""));
    }

    @Test
    @DisplayName("activate/deactivate: toggle active flag")
    void activateDeactivate_toggleActive() {
        Company company = Company.createNew("A", "B", "C");

        assertTrue(company.isActive());

        company.deactivate();
        assertFalse(company.isActive());

        company.activate();
        assertTrue(company.isActive());
    }

    @Test
    @DisplayName("markDeleted: sets deleted=true and active=false")
    void markDeleted_setsDeletedAndDeactivates() {
        Company company = Company.createNew("A", "B", "C");

        company.markDeleted();

        assertTrue(company.isDeleted());
        assertFalse(company.isActive());
    }

    @Test
    @DisplayName("restore: sets deleted=false and active=true")
    void restore_clearsDeletedAndActivates() {
        Company company = Company.createNew("A", "B", "C");
        company.markDeleted();

        company.restore();

        assertFalse(company.isDeleted());
        assertTrue(company.isActive(), "restore should also activate the company");
    }

}
