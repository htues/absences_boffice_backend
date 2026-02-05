package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.domain.AuditInfo;
import org.springframework.stereotype.Component;


@Component
public class CompanyPersistenceMapper {

    public Company toDomain(CompanyJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        AuditInfo auditInfo = new AuditInfo(
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );

        return Company.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.isActive(),
                entity.isDeleted(),
                auditInfo
                );
    }

    public CompanyJpaEntity toEntity(Company domain) {
        if (domain == null) {
            return null;
        }

        CompanyJpaEntity entity = new CompanyJpaEntity();

        // IMPORTANT: needed for updates (otherwise JPA will treat it like a new row)
        entity.setId(domain.getId());

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setAddress(domain.getAddress());
        entity.setActive(domain.isActive());
        entity.setDeleted(domain.isDeleted());

        return entity;
    }
}