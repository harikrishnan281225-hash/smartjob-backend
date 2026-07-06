package com.smartjob.dto;

import com.smartjob.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Salary information is required")
    private String salary;

    @NotBlank(message = "Experience requirement is required")
    private String experienceRequired;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters long")
    private String description;

    @NotBlank(message = "Skills are required")
    private String skillsRequired;

    /**
     * Maps this DTO to a Job entity.
     * Centralizing this transformation ensures your entity remains
     * clean and your controller stays lightweight.
     */
    public Job toEntity() {
        Job job = new Job();
        job.setTitle(this.title);
        job.setCompanyName(this.companyName);
        job.setLocation(this.location);
        job.setSalary(this.salary);
        job.setExperienceRequired(this.experienceRequired);
        job.setDescription(this.description);
        job.setSkillsRequired(this.skillsRequired);
        return job;
    }
}