package com.hftamayo.absencesbobe.features.companies.adapters.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "companies")
@EntityListeners(AuditingEntityListener.class)
public class CompanyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    @Setter
    private Long id;

    @Setter
    @Column(name = "name", nullable = false, unique = true, length = 50)
    @ToString.Include
    private String name;

    @Setter
    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Setter
    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Setter
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Setter
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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

    // Optional: domain-friendly helpers (keeps calling code from touching fields directly)
    public void markDeleted() {
        this.isDeleted = true;
        this.isActive = false;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

}