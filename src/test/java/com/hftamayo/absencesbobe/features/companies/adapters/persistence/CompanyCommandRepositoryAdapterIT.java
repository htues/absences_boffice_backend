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
import java.util.UUID;

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
        String uniqueName = "itCompany-" + UUID.randomUUID();
        Company company = Company.createNew(
                uniqueName,
                "Technology company",
                "123 Main Street"
        );

        Company saved = adapter.save(company);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).startsWith("itCompany-");
        assertThat(saved.getDescription()).isEqualTo("Technology company");
        assertThat(saved.getAddress()).isEqualTo("123 Main Street");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isDeleted()).isFalse();

        CompanyJpaEntity persisted = jpaRepository.findById(saved.getId()).orElseThrow();

        assertThat(persisted.getName()).startsWith("itCompany-");
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
                true,
                false
        );

        CompanyJpaEntity savedEntity = jpaRepository.saveAndFlush(entity);

        Optional<Company> result = adapter.findById(savedEntity.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedEntity.getId());
        assertThat(result.get().getName()).startsWith("itCompany-");
        assertThat(result.get().isDeleted()).isFalse();
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("findById returns empty when row is deleted")
    void findById_returnsEmpty_whenDeleted() {
        CompanyJpaEntity entity = companyEntity(
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
                false,
                true
        );
        CompanyJpaEntity savedEntity = jpaRepository.saveAndFlush(entity);

        Optional<Company> result = adapter.findByIdIncludingDeleted(savedEntity.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedEntity.getId());
        assertThat(result.get().getName()).startsWith("itCompany-");
        assertThat(result.get().isDeleted()).isTrue();
        assertThat(result.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("existsByName is case-insensitive and ignores deleted rows")
    void existsByName_isCaseInsensitive_andIgnoresDeletedRows() {
        CompanyJpaEntity deletedEntity = companyEntity(
                false,
                true
        );
        jpaRepository.saveAndFlush(deletedEntity);
        String targetName = deletedEntity.getName();

        assertThat(adapter.existsByName(targetName)).isFalse();

        CompanyJpaEntity activeEntity = companyEntity(
                true,
                false
        );

        jpaRepository.saveAndFlush(activeEntity);
        targetName = activeEntity.getName();

        assertThat(adapter.existsByName(targetName)).isTrue();
        assertThat(adapter.existsByName("unknown")).isFalse();
    }

    @Test
    @DisplayName("existsByNameExcludingId returns false for same row and true for another row with same name")
    void existsByNameExcludingId_behavesCorrectlyForUpdates() {
        CompanyJpaEntity first = companyEntity(
                true,
                false
        );
        CompanyJpaEntity savedFirst = jpaRepository.saveAndFlush(first);
        String targetName = savedFirst.getName();

        assertThat(adapter.existsByNameExcludingId(targetName, savedFirst.getId())).isFalse();

        CompanyJpaEntity second = companyEntity(
            true,
            false
        );
        CompanyJpaEntity savedSecond = jpaRepository.saveAndFlush(second);

        assertThat(adapter.existsByNameExcludingId(savedFirst.getName(), savedSecond.getId())).isTrue();
        assertThat(adapter.existsByNameExcludingId("Unknown", savedSecond.getId())).isFalse();
    }

    private CompanyJpaEntity companyEntity(
            boolean active,
            boolean deleted
    ) {
        String uniqueName = "itCompany-" + UUID.randomUUID();
        String description = "Technology company";
        String address = "123 Main Street";
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setName(uniqueName);
        entity.setDescription(description);
        entity.setAddress(address);
        entity.setActive(active);
        entity.setDeleted(deleted);
        return entity;
    }
}