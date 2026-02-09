package com.hftamayo.absencesbobe.features.seeding.domain;

import com.hftamayo.absencesbobe.shared.domain.AuditInfo;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
public class Seeding {

    private final Long id; // null for new
    private final Instant seedVersion;

    private final Scope scope;
    private final String taskId;
    private final Status status;
    private final boolean active;

    private final AuditInfo auditInfo;

    private Seeding(
            Long id,
            Instant seedVersion,
            Scope scope,
            String taskId,
            Status status,
            AuditInfo auditInfo,
            boolean active
    ) {
        this.id = id;
        this.seedVersion = requireNonNull(seedVersion, "seedVersion");
        this.scope = requireNonNull(scope, "scope");
        this.taskId = requireText(taskId, "taskId");
        this.status = requireNonNull(status, "status");
        this.auditInfo = auditInfo == null ? AuditInfo.empty() : auditInfo;
        this.active = active;
    }

    public static Seeding createNew(Instant seedVersion, Scope scope, String taskId, Status status, boolean active) {
        return new Seeding(null, seedVersion, scope, taskId, status, AuditInfo.empty(), active);
    }

    public static Seeding rehydrate(
            Long id,
            Instant seedVersion,
            Scope scope,
            String taskId,
            Status status,
            AuditInfo auditInfo,
            boolean active
    ) {
        return new Seeding(id, seedVersion, scope, taskId, status, auditInfo, active);
    }

    // Same “audit convenience getters” pattern as Company
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

    public enum Scope {
        ALL, USERS, CATALOGS
    }

    public enum Status {
        SUCCESS, FAILED
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }
}