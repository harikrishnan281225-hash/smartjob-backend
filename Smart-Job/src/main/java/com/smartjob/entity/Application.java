package com.smartjob.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use FetchType.LAZY for performance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @ToString.Exclude
    private Job job;

    // Status will track 'ASSIGNED', 'APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED'
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime appliedDate;

    private LocalDate followUpDate;

    /**
     * Ensures default values are set before saving to the database.
     */
    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "ASSIGNED"; // Default state when Admin assigns a job
        }
        if (this.appliedDate == null) {
            this.appliedDate = LocalDateTime.now();
        }
    }

    // Standard equals and hashCode for JPA entities using ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Application)) return false;
        Application that = (Application) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}