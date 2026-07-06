package com.smartjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One: Many records in UserJob can point to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Prevents JSON recursion
    @ToString.Exclude
    private User user;

    // Many-to-One: Many records in UserJob can point to one Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore // Prevents JSON recursion
    @ToString.Exclude
    private Job job;

    // UPDATED: Now returns String to match the new VARCHAR(255) job_id column
    public String getJobId() {
        return job != null ? job.getId() : null;
    }

    // Helper method to access the User ID directly
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    // e.g., "SAVED", "APPLIED", "INTERVIEW"
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime addedDate;

    // Pre-persist hook to set the timestamp automatically
    @PrePersist
    protected void onCreate() {
        if (this.addedDate == null) {
            this.addedDate = LocalDateTime.now();
        }
    }

    // Override equals/hashCode using ONLY the ID to prevent recursion
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserJob)) return false;
        UserJob userJob = (UserJob) o;
        return id != null && id.equals(userJob.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}