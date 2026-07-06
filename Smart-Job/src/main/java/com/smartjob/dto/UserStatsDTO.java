package com.smartjob.dto;

import com.smartjob.entity.User;
import lombok.Data;

@Data
public class UserStatsDTO {
    private User user;
    private long totalApplied;
    private long interviews;
    private long offers;
    private long rejected;
}