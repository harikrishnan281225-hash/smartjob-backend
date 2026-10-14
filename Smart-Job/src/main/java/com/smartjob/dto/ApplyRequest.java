package com.smartjob.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyRequest {

    @JsonAlias({"id", "externalId", "external_id", "linkedinId", "job_id"})
    private String jobId;

    @JsonAlias({"jobTitle", "job_title", "position", "name", "role"})
    private String title;

    @JsonAlias({"company", "company_name", "employer", "organization"})
    private String companyName;

    @JsonAlias({"jobLocation", "place", "city"})
    private String location;

    @JsonAlias({"link", "jobUrl", "job_url"})
    private String url;

    @JsonAlias({"jobDescription", "desc", "summary"})
    private String description;

    private String salary;

    @JsonAlias({"skills", "requiredSkills"})
    private String skillsRequired;

    @JsonAlias({"experience", "exp"})
    private String experienceRequired;

    private String username;
    private String postedDate;

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(String skillsRequired) { this.skillsRequired = skillsRequired; }

    public String getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(String experienceRequired) { this.experienceRequired = experienceRequired; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPostedDate() { return postedDate; }
    public void setPostedDate(String postedDate) { this.postedDate = postedDate; }

    @Override
    public String toString() {
        return "ApplyRequest{jobId='" + jobId + "', title='" + title + "', companyName='" + companyName + "', location='" + location + "'}";
    }
}