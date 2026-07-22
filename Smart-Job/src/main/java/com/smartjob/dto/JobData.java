package com.smartjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data model for Job information used across various DTOs.
 * Updated to include additional metadata fields for comprehensive job profiles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobData {
    // Unique identifier (typically a JSearch hash or UUID)
    private String id;

    // Core fields
    private String title;
    private String companyName;
    private String location;
    private String url;
    private String description;

    // Additional metadata fields
    private String salary;
    private String experienceRequired;
    private String skillsRequired;
}