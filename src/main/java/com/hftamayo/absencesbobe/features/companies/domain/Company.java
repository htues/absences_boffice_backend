package com.hftamayo.absencesbobe.features.companies.domain;

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

    private final Long createdBy;
    private final Long updatedBy;
    private final Instant createdDate;
    private final Instant updatedDate;

    private Company(Long id, String name, String description, String address, boolean active, boolean deleted, Long createdBy, Long updatedBy, Instant createdDate, Instant updatedDate) {
        this.id = id;
        this.name = requireText(name, "name");
        this.description = requireText(description, "description");
        this.address = requireText(address, "address");
        this.active = active;
        this.deleted = deleted;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;

    }

    public static Company createNew(String name, String description, String address) {
        return new Company(null, name, description, address, true, false, null, null, null, null);
    }

    public static Company rehydrate(
            Long id,
            String name,
            String description,
            String address,
            boolean active,
            boolean deleted,
            Long createdBy,
            Long updatedBy,
            Instant createdDate,
            Instant updatedDate
    ) {
        return new Company(id, name, description, address, active, deleted, createdBy, updatedBy, createdDate, updatedDate);
    }

    public void updateDetails(String name, String description, String address) {
        this.name = requireText(name, "name");
        this.description = requireText(description, "description");
        this.address = requireText(address, "address");
    }

    public void deactivate() {
        this.active = false;
    }

    public void markDeleted() {
        this.deleted = true;
        this.active = false;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }
}