package com.smartjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Job information.
 * Updated to include additional job metadata for the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private String id;
    private String title;
    private String companyName;
    private String location;
    private String description;
    private String url;
    private String status;

    // Newly added fields to match the Job Entity
    private String salary;
    private String experienceRequired;
    private String skillsRequired;

    // Added this field to match the mapping in Job.java
    private String postedDate;
}