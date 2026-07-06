package com.smartjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Application summary, used in Admin Dashboard activity feeds.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    private Long id;
    private String userName;
    private String jobTitle;

    // Optional: Add status if you want to show it in the recent activity feed
    private String status;
}