package com.smartjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data model for Job information used across various DTOs.
 * Updated to include additional metadata fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobData {
    // Unique identifier (typically a JSearch hash)
    private String id;

    private String title;
    private String companyName;
    private String location;
    private String url;
    private String description;

    // Added to match Job entity and maintain consistency across requests
    private String salary;
    private String experienceRequired;
    private String skillsRequired;
}