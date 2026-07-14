package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompanyJpaEntityTest {

    @Test
    @DisplayName("new entity starts with expected defaults and null audit fields")
    void constructor_initializesDefaultState() {
        CompanyJpaEntity entity = new CompanyJpaEntity();

        assertNull(entity.getId());
        assertNull(entity.getName());
        assertNull(entity.getDescription());
        assertNull(entity.getAddress());

        assertTrue(entity.isActive());
        assertFalse(entity.isDeleted());

        assertNull(entity.getCreatedBy());
        assertNull(entity.getLastModifiedBy());
        assertNull(entity.getCreatedDate());
        assertNull(entity.getLastModifiedDate());
    }

    @Test
    @DisplayName("setters, toString, equals and hashCode use the entity id")
    void setters_andObjectMethods_workAsExpected() {
        CompanyJpaEntity first = new CompanyJpaEntity();
        first.setId(10L);
        first.setName("Acme");
        first.setDescription("Technology company");
        first.setAddress("Main Street");
        first.setActive(false);
        first.setDeleted(true);

        CompanyJpaEntity second = new CompanyJpaEntity();
        second.setId(10L);

        assertEquals(10L, first.getId());
        assertEquals("Acme", first.getName());
        assertEquals("Technology company", first.getDescription());
        assertEquals("Main Street", first.getAddress());
        assertFalse(first.isActive());
        assertTrue(first.isDeleted());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.toString().contains("id=10"));
        assertTrue(first.toString().contains("name=Acme"));
    }

    @Test
    @DisplayName("helper methods toggle active and deleted flags consistently")
    void helperMethods_toggleFlags() {
        CompanyJpaEntity entity = new CompanyJpaEntity();

        entity.deactivate();
        assertFalse(entity.isActive());

        entity.activate();
        assertTrue(entity.isActive());

        entity.markDeleted();
        assertTrue(entity.isDeleted());
        assertFalse(entity.isActive());

        entity.restore();
        assertFalse(entity.isDeleted());
        assertTrue(entity.isActive());
    }
}
