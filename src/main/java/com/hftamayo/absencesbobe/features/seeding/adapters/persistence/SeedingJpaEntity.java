package com.hftamayo.absencesbobe.features.seeding.adapters.persistence;

import com.hftamayo.absencesbobe.features.seeding.domain.Seeding;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;

@Getter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "dataseeding",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dataseeding_seed_version_task",
                columnNames = {"seed_version", "task_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
public class SeedingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    @Setter
    private Long id;

    @Setter
    @Column(name = "seed_version", nullable = false)
    private Instant seedVersion;

    @Setter
    @Enumerated(STRING)
    @Column(name = "scope", nullable = false, length = 32)
    private Seeding.Scope scope;

    @Setter
    @Column(name = "task_id", nullable = false, length = 128)
    @ToString.Include
    private String taskId;

    @Setter
    @Enumerated(STRING)
    @Column(name = "status", nullable = false, length = 16)
    private Seeding.Status status;

    @Setter
    @Column(name = "active", nullable = false)
    private boolean active;

    @Setter
    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @Setter
    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private Long lastModifiedBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;
}