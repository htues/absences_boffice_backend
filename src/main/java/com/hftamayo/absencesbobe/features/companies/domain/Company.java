package com.hftamayo.absencesbobe.features.companies.domain;

import com.hftamayo.absencesbobe.shared.domain.AuditInfo;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
public class Company {

    private final Long id; // null for new
    private String name;
    private String description;
    private String address;

    private boolean active;
    private boolean deleted;

    private final AuditInfo auditInfo;

    private Company(Long id, String name, String description, String address,
                    boolean active, boolean deleted, AuditInfo auditInfo) {
        this.id = id;
        this.name = requireText(name, "name");
        this.description = requireText(description, "description");
        this.address = requireText(address, "address");
        this.active = active;
        this.deleted = deleted;
        this.auditInfo = auditInfo == null ? AuditInfo.empty() : auditInfo;

    }

    public static Company createNew(String name, String description, String address) {
        return new Company(null, name, description, address, true, false, AuditInfo.empty());
    }

    public static Company rehydrate(
            Long id,
            String name,
            String description,
            String address,
            boolean active,
            boolean deleted,
            AuditInfo auditInfo
    ) {
        return new Company(id, name, description, address, active, deleted, auditInfo);
    }

    public void updateDetails(String name, String description, String address) {
        this.name = requireText(name, "name");
        this.description = requireText(description, "description");
        this.address = requireText(address, "address");
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void markDeleted() {
        this.deleted = true;
        deactivate();
    }

    public void restore(){
        this.deleted = false;
        activate();
    }

    public Long getCreatedBy() {
        return auditInfo.createdBy();
    }

    public Long getUpdatedBy() {
        return auditInfo.updatedBy();
    }

    public Instant getCreatedDate() {
        return auditInfo.createdDate();
    }

    public Instant getUpdatedDate() {
        return auditInfo.updatedDate();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }
}