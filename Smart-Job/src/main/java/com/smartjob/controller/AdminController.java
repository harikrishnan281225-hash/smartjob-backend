package com.smartjob.controller;

import com.smartjob.dto.*;
import com.smartjob.entity.*;
import com.smartjob.repository.*;
import com.smartjob.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private JobService jobService;
    @Autowired private ApplicationRepository applicationRepository;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalJobs", jobRepository.count());
        stats.put("pendingAssignments", jobRepository.countByStatus("Assigned"));
        stats.put("unreadMessages", messageRepository.countByReadFalse());
        return ResponseEntity.ok(stats);
    }

    // Inside AdminController.java

    @GetMapping("/user-profile-stats/{id}")
    public ResponseEntity<Map<String, Object>> getUserProfileStats(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("user", user);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/job-stats")
    public ResponseEntity<Map<String, Long>> getJobStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", jobRepository.count());
        stats.put("assigned", jobRepository.countByStatus("Assigned"));
        stats.put("posted", jobRepository.countByStatus("Posted"));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<ApplicationDTO>> getRecentActivity() {
        var appPage = applicationRepository.findTop5WithDetails(PageRequest.of(0, 5));
        List<ApplicationDTO> dtoList = appPage.getContent().stream()
                .map(app -> {
                    ApplicationDTO dto = new ApplicationDTO();
                    dto.setId(app.getId());
                    dto.setUserName(app.getUser() != null ? app.getUser().getFullName() : "Unknown");
                    dto.setJobTitle(app.getJob() != null ? app.getJob().getTitle() : "N/A");
                    dto.setStatus(app.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/post-job")
    public ResponseEntity<String> postJob(@RequestBody PostJobRequest request) {
        if (request.getJob() == null || request.getJob().getTitle() == null) {
            return ResponseEntity.badRequest().body("Job details or title are missing.");
        }

        PostJobRequest.JobData jobData = request.getJob();
        Job newJob = new Job();

        String jobId = (jobData.getId() != null && !jobData.getId().isEmpty())
                ? jobData.getId()
                : UUID.randomUUID().toString();

        newJob.setId(jobId);
        newJob.setTitle(jobData.getTitle());
        newJob.setCompanyName(jobData.getCompanyName() != null ? jobData.getCompanyName() : "Unknown");
        newJob.setLocation(jobData.getLocation() != null ? jobData.getLocation() : "Remote");

        newJob.setSalary(jobData.getSalary());
        newJob.setExperienceRequired(jobData.getExperienceRequired());
        newJob.setDescription(jobData.getDescription());
        newJob.setSkillsRequired(jobData.getSkillsRequired());
        newJob.setUrl(jobData.getUrl());

        newJob.setStatus(request.getUserIds() != null && !request.getUserIds().isEmpty() ? "Assigned" : "Posted");

        Job savedJob = jobRepository.save(newJob);

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            jobService.assignJobToUsers(savedJob.getId(), request.getUserIds());
        }
        return ResponseEntity.ok("Job posted successfully with ID: " + savedJob.getId());
    }

    @PostMapping("/assign-job")
    public ResponseEntity<String> assignJob(@RequestBody JobAssignmentRequest request) {
        if (request.getJob() == null || request.getJob().getId() == null) {
            return ResponseEntity.badRequest().body("Job ID is required for assignment.");
        }

        JobAssignmentRequest.JobData data = request.getJob();
        Job job = jobRepository.findById(data.getId()).orElseGet(() -> {
            Job newJob = new Job();
            newJob.setId(data.getId());
            newJob.setTitle(data.getTitle());
            newJob.setCompanyName(data.getCompanyName() != null ? data.getCompanyName() : "Unknown");
            newJob.setLocation(data.getLocation() != null ? data.getLocation() : "Remote");
            newJob.setDescription(data.getDescription());
            newJob.setUrl(data.getUrl());
            newJob.setStatus("Assigned");
            return jobRepository.save(newJob);
        });

        jobService.assignJobToUsers(job.getId(), request.getUserIds());
        return ResponseEntity.ok("Job assigned successfully.");
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @Data
    public static class BroadcastRequest {
        private List<Long> userIds;
        private String content;
    }
}