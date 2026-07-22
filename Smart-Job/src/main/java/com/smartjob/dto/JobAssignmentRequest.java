package com.smartjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for Job assignment requests.
 * Uses the standalone JobData class for consistent type mapping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAssignmentRequest {

    // This now correctly references the standalone JobData.java file
    private JobData job;
    private List<Long> userIds;

    // REMOVED: The nested "public static class JobData" has been deleted.
}