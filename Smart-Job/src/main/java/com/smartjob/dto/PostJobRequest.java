package com.smartjob.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostJobRequest {

    private JobData job;
    private List<Long> userIds;

    @Data
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