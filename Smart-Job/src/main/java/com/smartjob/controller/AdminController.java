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

    // --- 1. Dashboard Stats ---
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalJobs", jobRepository.count());
        stats.put("pendingAssignments", jobRepository.countByStatus("Assigned"));
        stats.put("unreadMessages", messageRepository.countByReadFalse());
        return ResponseEntity.ok(stats);
    }

    // --- 2. User Profile Stats ---
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

    // --- 3. Job Stats ---
    @GetMapping("/job-stats")
    public ResponseEntity<Map<String, Long>> getJobStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", jobRepository.count());
        stats.put("assigned", jobRepository.countByStatus("Assigned"));
        stats.put("posted", jobRepository.countByStatus("Posted"));
        return ResponseEntity.ok(stats);
    }

    // --- 4. Recent Activity ---
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

    // --- 5. All Users ---
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // --- POST & BROADCAST LOGIC ---
    @PostMapping("/post-job")
    public ResponseEntity<String> postJob(@RequestBody PostJobRequest request) {
        if (request.getJob() == null || request.getJob().getTitle() == null) {
            return ResponseEntity.badRequest().body("Job details or title are missing.");
        }
        jobService.assignJobToUsers(request.getJob(), request.getUserIds());
        return ResponseEntity.ok("Job processed successfully.");
    }

    @PostMapping("/assign-job")
    public ResponseEntity<String> assignJob(@RequestBody JobAssignmentRequest request) {
        if (request.getJob() == null || request.getJob().getId() == null) {
            return ResponseEntity.badRequest().body("Job ID is required for assignment.");
        }
        jobService.assignJobToUsers(request.getJob(), request.getUserIds());
        return ResponseEntity.ok("Job assigned successfully to " + request.getUserIds().size() + " users.");
    }

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcastMessage(@RequestBody BroadcastRequest request) {
        // 1. Validate incoming list
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body("No users selected for broadcast.");
        }

        // 2. Debugging: Log IDs to console to verify the frontend sent the full list
        System.out.println("DEBUG: Broadcasting to IDs: " + request.getUserIds());

        // 3. Batch processing with error catching
        int successCount = 0;
        for (Long userId : request.getUserIds()) {
            try {
                Message msg = new Message();
                msg.setReceiverId(userId);
                msg.setContent(request.getContent());
                msg.setRead(false);
                msg.setTimestamp(java.time.LocalDateTime.now());
                messageRepository.save(msg);
                successCount++;
            } catch (Exception e) {
                System.err.println("Error saving message for user ID " + userId + ": " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Successfully sent to " + successCount + " out of " + request.getUserIds().size() + " users.");
    }

    @Data
    public static class BroadcastRequest {
        private List<Long> userIds;
        private String content;
    }
}