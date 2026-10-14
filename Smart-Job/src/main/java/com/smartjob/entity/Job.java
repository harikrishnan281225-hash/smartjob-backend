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
@ToString(exclude = "applications")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 100)
    private String id;

    // Original LinkedIn / External ID - can be 600+ chars
    @Column(name = "external_id", length = 1000)
    private String externalId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "company_name", nullable = false, length = 1000)
    private String companyName;

    @Column(nullable = false, length = 1000)
    private String location;

    // Metadata fields - increased size
    @Column(length = 1000)
    private String salary;

    @Column(length = 1000)
    private String experienceRequired;

    @Column(length = 2000)
    private String skillsRequired;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, length = 50)
    private String status = "Applied";

    private String username;

    @Column(nullable = false)
    private LocalDate postedDate = LocalDate.now();

    private LocalDate followUpDate;

    @Column(nullable = false, length = 100)
    private String postedBy = "USER_SEARCH";

    private Long ownerId;
    private Long userId;

    @Column(length = 2048)
    private String url;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<Application> applications;

    public JobDTO toDTO() {
        JobDTO dto = new JobDTO();
        dto.setId(this.id);
        dto.setTitle(this.title != null ? this.title : "No Title");
        dto.setCompanyName(this.companyName != null ? this.companyName : "UNKNOWN");
        dto.setLocation(this.location != null ? this.location : "Not Specified");
        dto.setDescription(this.description != null ? this.description : "");
        dto.setUrl(this.url);
        dto.setStatus(this.status != null ? this.status : "APPLIED");
        dto.setSalary(this.salary);
        dto.setExperienceRequired(this.experienceRequired);
        dto.setSkillsRequired(this.skillsRequired);
        dto.setPostedDate(this.postedDate != null ? this.postedDate.toString() : LocalDate.now().toString());
        dto.setFollowUpDate(this.followUpDate != null ? this.followUpDate.toString() : null);
        dto.setUsername(this.username);
        dto.setPostedBy(this.postedBy);
        dto.setOwnerId(this.ownerId);
        dto.setUserId(this.userId);
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
        if (this.title == null || this.title.isBlank()) {
            this.title = "No Title";
        }
        if (this.companyName == null || this.companyName.isBlank()) {
            this.companyName = "UNKNOWN";
        }
        if (this.location == null || this.location.isBlank()) {
            this.location = "Remote";
        }
        if (this.description == null || this.description.isBlank()) {
            this.description = "No description provided";
        }
    }
}