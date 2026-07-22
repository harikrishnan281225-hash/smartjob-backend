package com.smartjob.controller;

import com.smartjob.dto.ApplyRequest;
import com.smartjob.dto.JobDTO;
import com.smartjob.entity.Application;
import com.smartjob.entity.User;
import com.smartjob.entity.Job;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.repository.ApplicationRepository;
import com.smartjob.repository.JobRepository;
import com.smartjob.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired private JobRepository jobRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRepository applicationRepository;

    // Helper to find user by email or username (Resolves "User not found" issue)
    private User getCurrentUser(Authentication authentication) {
        String identifier = authentication.getName();
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier)));
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<List<JobDTO>> getMyJobs(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Application> userApps = applicationRepository.findByUserId(user.getId());

        List<JobDTO> jobDTOs = userApps.stream()
                .map(app -> {
                    if (app.getJob() == null) return null;
                    JobDTO dto = app.getJob().toDTO();
                    dto.setStatus(app.getStatus());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(jobDTOs);
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<JobDTO>> getAssignedJobs(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Application> userApps = applicationRepository.findByUserId(user.getId());

        List<JobDTO> assignedJobs = userApps.stream()
                .filter(app -> app.getStatus() != null && "ASSIGNED".equalsIgnoreCase(app.getStatus().trim()))
                .map(app -> (app.getJob() != null) ? app.getJob().toDTO() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(assignedJobs);
    }

    @PostMapping("/add")
    public ResponseEntity<String> applyForJob(@RequestBody ApplyRequest payload, Authentication authentication) {
        User user = getCurrentUser(authentication);
        String jobId = payload.getJobId();

        Job job = jobRepository.findById(jobId).orElseGet(() -> {
            Job newJob = new Job();
            newJob.setId(jobId);
            newJob.setTitle(payload.getTitle());
            newJob.setCompanyName(payload.getCompanyName());
            newJob.setLocation(payload.getLocation());
            newJob.setUrl(payload.getUrl());
            newJob.setDescription(payload.getDescription());
            newJob.setSalary(payload.getSalary());
            newJob.setSkillsRequired(payload.getSkillsRequired());
            newJob.setExperienceRequired(payload.getExperienceRequired());
            newJob.setPostedDate(java.time.LocalDate.now());
            return jobRepository.save(newJob);
        });

        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setStatus("APPLIED");
        applicationRepository.save(application);

        return ResponseEntity.ok("Job added successfully");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable String id, @RequestBody Map<String, String> payload, Authentication authentication) {
        String newStatus = payload.get("status");
        User user = getCurrentUser(authentication);

        List<Application> apps = applicationRepository.findAllByUserIdAndJobId(user.getId(), id);
        if (apps.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assignment not found");

        Application app = apps.get(0);
        app.setStatus(newStatus);
        applicationRepository.save(app);
        return ResponseEntity.ok("Status updated to: " + newStatus);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable String id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Application> apps = applicationRepository.findAllByUserIdAndJobId(user.getId(), id);
        if (apps.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Application not found");
        applicationRepository.delete(apps.get(0));
        return ResponseEntity.ok("Application deleted.");
    }
}