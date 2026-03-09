package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.features.shared.test.AbstractPostgresIT;
import com.hftamayo.absencesbobe.shared.infrastructure.audit.AuditorAwareConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        CompanyCommandRepositoryAdapter.class,
        CompanyPersistenceMapper.class,
        AuditorAwareConfig.class
})
class CompanyCommandRepositoryAdapterIT extends AbstractPostgresIT {

    @Autowired
    private CompanyCommandRepositoryAdapter adapter;

    @Autowired
    private CompanySpringDataRepository jpaRepository;

    @Test
    @DisplayName("save persists company and returns mapped domain object")
    void save_persistsCompanyCorrectly() {
        Company company = Company.createNew(
                "Acme",
                "Technology company",
                "123 Main Street"
        );

        Company saved = adapter.save(company);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Acme");
        assertThat(saved.getDescription()).isEqualTo("Technology company");
        assertThat(saved.getAddress()).isEqualTo("123 Main Street");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isDeleted()).isFalse();

        CompanyJpaEntity persisted = jpaRepository.findById(saved.getId()).orElseThrow();

        assertThat(persisted.getName()).isEqualTo("Acme");
        assertThat(persisted.getDescription()).isEqualTo("Technology company");
        assertThat(persisted.getAddress()).isEqualTo("123 Main Street");
        assertThat(persisted.isActive()).isTrue();
        assertThat(persisted.isDeleted()).isFalse();
        assertThat(persisted.getCreatedBy()).isEqualTo(0L);
        assertThat(persisted.getCreatedDate()).isNotNull();
    }

    @Test
    @DisplayName("findById returns company when row is not deleted")
    void findById_returnsCompany_whenNotDeleted() {
        CompanyJpaEntity entity = companyEntity(
                "Acme",
                "Technology company",
                "123 Main Street",
                true,
                false
        );

        CompanyJpaEntity savedEntity = jpaRepository.saveAndFlush(entity);

        Optional<Company> result = adapter.findById(savedEntity.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedEntity.getId());
        assertThat(result.get().getName()).isEqualTo("Acme");
        assertThat(result.get().isDeleted()).isFalse();
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("findById returns empty when row is deleted")
    void findById_returnsEmpty_whenDeleted() {
        CompanyJpaEntity entity = companyEntity(
                "Deleted Co",
                "Soft deleted company",
                "456 Sunset Blvd",
                false,
                true
        );

        CompanyJpaEntity savedEntity = jpaRepository.saveAndFlush(entity);

        Optional<Company> result = adapter.findById(savedEntity.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdIncludingDeleted returns company even when row is deleted")
    void findByIdIncludingDeleted_returnsDeletedCompany() {
        CompanyJpaEntity entity = companyEntity(
                "Deleted Co",
                "Soft deleted company",
                "456 Sunset Blvd",
                false,
                true
        );
        CompanyJpaEntity savedEntity = jpaRepository.saveAndFlush(entity);

        Optional<Company> result = adapter.findByIdIncludingDeleted(savedEntity.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedEntity.getId());
        assertThat(result.get().getName()).isEqualTo("Deleted Co");
        assertThat(result.get().isDeleted()).isTrue();
        assertThat(result.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("existsByName is case-insensitive and ignores deleted rows")
    void existsByName_isCaseInsensitive_andIgnoresDeletedRows() {
        CompanyJpaEntity deletedEntity = companyEntity(
                "Acme",
                "Deleted company",
                "Old address",
                false,
                true
        );
        jpaRepository.saveAndFlush(deletedEntity);

        assertThat(adapter.existsByName("acme")).isFalse();

        CompanyJpaEntity activeEntity = companyEntity(
                "Globex",
                "Active company",
                "New address",
                true,
                false
        );

        jpaRepository.saveAndFlush(activeEntity);

        assertThat(adapter.existsByName("gLoBeX")).isTrue();
        assertThat(adapter.existsByName("unknown")).isFalse();
    }

    @Test
    @DisplayName("existsByNameExcludingId returns false for same row and true for another row with same name")
    void existsByNameExcludingId_behavesCorrectlyForUpdates() {
        CompanyJpaEntity first = companyEntity(
                "Acme",
                "First company",
                "Address 1",
                true,
                false
        );
        CompanyJpaEntity savedFirst = jpaRepository.saveAndFlush(first);

        assertThat(adapter.existsByNameExcludingId("ACME", savedFirst.getId())).isFalse();

        CompanyJpaEntity second = companyEntity(
            "Initech",
            "Second company",
            "Address 2",
            true,
            false
        );
        CompanyJpaEntity savedSecond = jpaRepository.saveAndFlush(second);

        assertThat(adapter.existsByNameExcludingId("Acme", savedSecond.getId())).isTrue();
        assertThat(adapter.existsByNameExcludingId("Unknown", savedSecond.getId())).isFalse();
    }

    private CompanyJpaEntity companyEntity(
            String name,
            String description,
            String address,
            boolean active,
            boolean deleted
    ) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setAddress(address);
        entity.setActive(active);
        entity.setDeleted(deleted);
        return entity;
    }
}