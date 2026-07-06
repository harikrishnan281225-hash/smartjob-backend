package com.smartjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for Job assignment requests.
 * Uses the same JobData structure as PostJobRequest to maintain consistency.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAssignmentRequest {

    private JobData job;
    private List<Long> userIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobData {
        private String id;
        private String title;
        private String companyName;
        private String location;
        private String salary;
        private String experienceRequired;
        private String description;
        private String skillsRequired;
        private String url;
    }
}