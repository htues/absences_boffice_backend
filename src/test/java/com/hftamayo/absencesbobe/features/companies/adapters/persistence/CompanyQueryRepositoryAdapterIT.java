package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.features.shared.test.AbstractPostgresIT;
import com.hftamayo.absencesbobe.shared.infrastructure.audit.AuditorAwareConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        CompanyQueryRepositoryAdapter.class,
        CompanyPersistenceMapper.class,
        AuditorAwareConfig.class
})
class CompanyQueryRepositoryAdapterIT extends AbstractPostgresIT {

    @Autowired
    private CompanyQueryRepositoryAdapter adapter;

    @Autowired
    private CompanySpringDataRepository jpaRepository;

    @Test
    @DisplayName("getActiveCompanies returns only active and non-deleted companies")
    void getActiveCompanies_returnsOnlyActiveAndNonDeletedCompanies() {
        jpaRepository.saveAndFlush(companyEntity("Acme", true, false));
        jpaRepository.saveAndFlush(companyEntity("Globex", true, false));
        jpaRepository.saveAndFlush(companyEntity("Inactive Co", false, false));
        jpaRepository.saveAndFlush(companyEntity("Deleted Co", true, true));
        jpaRepository.saveAndFlush(companyEntity("Inactive Deleted Co", false, true));

        Page<Company> result = adapter.getActiveCompanies(PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Company::getName)
                .map(name -> name.substring(0, name.indexOf('-')))
                .containsExactlyInAnyOrder("Acme", "Globex");

        assertThat(result.getContent())
                .allSatisfy(company -> {
                    assertThat(company.isActive()).isTrue();
                    assertThat(company.isDeleted()).isFalse();
                });

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("getActiveCompanies respects pagination")
    void getActiveCompanies_respectsPagination() {
        jpaRepository.saveAndFlush(companyEntity("Acme", true, false));
        jpaRepository.saveAndFlush(companyEntity("Globex", true, false));
        jpaRepository.saveAndFlush(companyEntity("Initech", true, false));

        Page<Company> firstPage = adapter.getActiveCompanies(PageRequest.of(0, 2));
        Page<Company> secondPage = adapter.getActiveCompanies(PageRequest.of(1, 2));

        assertThat(firstPage.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(firstPage.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(firstPage.getTotalPages()).isGreaterThanOrEqualTo(2);

        assertThat(secondPage.getContent()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(secondPage.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(secondPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("getActiveCompanies returns mapped domain objects with persisted values")
    void getActiveCompanies_returnsMappedDomainObjects() {
        CompanyJpaEntity savedEntity = jpaRepository.saveAndFlush(companyEntity("Acme", true, false));

        Page<Company> result = adapter.getActiveCompanies(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);

        Company company = result.getContent().getFirst();

        assertThat(company.getId()).isEqualTo(savedEntity.getId());
        assertThat(company.getName()).startsWith("Acme");
        assertThat(company.getDescription()).isEqualTo("Description for Acme");
        assertThat(company.getAddress()).isEqualTo("Address for Acme");
        assertThat(company.isActive()).isTrue();
        assertThat(company.isDeleted()).isFalse();
        assertThat(company.getCreatedBy()).isEqualTo(0L);
        assertThat(company.getCreatedDate()).isNotNull();
    }

    private CompanyJpaEntity companyEntity(String name, boolean active, boolean deleted) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        String targetName = name + "-" + UUID.randomUUID();
        entity.setName(targetName);
        entity.setDescription("Description for " + name);
        entity.setAddress("Address for " + name);
        entity.setActive(active);
        entity.setDeleted(deleted);
        return entity;
    }
}