package com.smartjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartjob.dto.JobDTO;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Job {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String location;

    // Metadata fields
    private String salary;
    private String experienceRequired;
    private String skillsRequired;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String status = "Applied";

    private String username;

    @Column(nullable = false)
    private LocalDate postedDate = LocalDate.now();

    private LocalDate followUpDate;

    @Column(nullable = false)
    private String postedBy = "USER_SEARCH";

    private Long ownerId;
    private Long userId;
    private String url;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Application> applications;

    /**
     * Maps this Entity to a DTO including all new metadata fields.
     */
    public JobDTO toDTO() {
        JobDTO dto = new JobDTO();
        dto.setId(this.id);
        dto.setTitle(this.title);
        dto.setCompanyName(this.companyName);
        dto.setLocation(this.location);
        dto.setDescription(this.description);
        dto.setUrl(this.url);
        dto.setStatus(this.status);

        // Map new metadata fields
        dto.setSalary(this.salary);
        dto.setExperienceRequired(this.experienceRequired);
        dto.setSkillsRequired(this.skillsRequired);

        dto.setPostedDate(this.postedDate != null ? this.postedDate.toString() : null);
        return dto;
    }

    @PrePersist
    protected void onCreate() {
        if (this.postedDate == null) {
            this.postedDate = LocalDate.now();
        }
        if (this.status == null || this.status.isEmpty()) {
            this.status = "Applied";
        }
        if (this.postedBy == null || this.postedBy.isEmpty()) {
            this.postedBy = "USER_SEARCH";
        }
    }
}