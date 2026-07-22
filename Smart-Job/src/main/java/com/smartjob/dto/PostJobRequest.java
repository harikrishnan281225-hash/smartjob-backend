package com.smartjob.dto;

import lombok.Data;
import java.util.List;

/**
 * Request DTO for posting a new job.
 * Now references the standalone JobData class to ensure type consistency.
 */
@Data
public class PostJobRequest {

    private JobData job;
    private List<Long> userIds;

    // REMOVED: The static class JobData block has been deleted from here.
}