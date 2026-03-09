package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CompanyPersistenceMapperTest {

    private final CompanyPersistenceMapper mapper = new CompanyPersistenceMapper();

    @Test
    @DisplayName("toDomain: returns null when entity is null")
    void toDomain_nullEntity_returnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    @DisplayName("toEntity: returns null when domain is null")
    void toEntity_nullDomain_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    @DisplayName("toDomain: maps persistence fields and builds AuditInfo from entity audit fields (null-safe)")
    void toDomain_mapsFields_andAuditNullSafe() {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setId(99L);
        entity.setName("ACME");
        entity.setDescription("Desc");
        entity.setAddress("Address");
        entity.setActive(false);
        entity.setDeleted(true);

        Company domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(99L, domain.getId());
        assertEquals("ACME", domain.getName());
        assertEquals("Desc", domain.getDescription());
        assertEquals("Address", domain.getAddress());
        assertFalse(domain.isActive());
        assertTrue(domain.isDeleted());

        // Audit fields: in this entity class they are typically populated by JPA auditing,
        // and may be null in a plain unit test. Mapper should still work.
        assertNull(domain.getCreatedBy());
        assertNull(domain.getUpdatedBy());
        assertNull(domain.getCreatedDate());
        assertNull(domain.getUpdatedDate());
    }

    @Test
    @DisplayName("toEntity: maps fields and carries over id (important for updates)")
    void toEntity_mapsFields_andCopiesId() {
        Company domain = Company.rehydrate(
                123L,
                "Name",
                "Description",
                "Address",
                true,
                false,
                null // mapper does not use auditInfo when mapping to entity
        );

        CompanyJpaEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(123L, entity.getId());
        assertEquals("Name", entity.getName());
        assertEquals("Description", entity.getDescription());
        assertEquals("Address", entity.getAddress());
        assertTrue(entity.isActive());
        assertFalse(entity.isDeleted());
    }
}

