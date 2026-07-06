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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired private JobRepository jobRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRepository applicationRepository;

    @GetMapping("/my-jobs")
    public ResponseEntity<List<JobDTO>> getMyJobs(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Application> userApps = applicationRepository.findByUserId(user.getId());

        List<JobDTO> jobDTOs = userApps.stream()
                .map(app -> {
                    JobDTO dto = app.getJob().toDTO();
                    dto.setStatus(app.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(jobDTOs);
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<JobDTO>> getAssignedJobs(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Application> userApps = applicationRepository.findByUserId(user.getId());

        List<JobDTO> assignedJobs = userApps.stream()
                .filter(app -> app.getStatus() != null && "ASSIGNED".equalsIgnoreCase(app.getStatus().trim()))
                .map(app -> app.getJob().toDTO())
                .collect(Collectors.toList());

        return ResponseEntity.ok(assignedJobs);
    }

    @PostMapping("/add")
    public ResponseEntity<String> applyForJob(@RequestBody ApplyRequest payload, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String jobId = payload.getJobId();

        // 1. Check if job exists, if not, create it (Upsert)
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
            newJob.setPostedDate(java.time.LocalDate.now()); // Fallback to current date if missing
            newJob.setUsername(payload.getUsername());
            return jobRepository.save(newJob); // Persist the new job
        });

        // 2. Create the application record
        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setStatus("APPLIED");
        applicationRepository.save(application);

        return ResponseEntity.ok("Job added to Applied box successfully");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {

        String newStatus = payload.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: 'status' key missing in request body.");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Application> apps = applicationRepository.findAllByUserIdAndJobId(user.getId(), id);

        if (apps.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assignment not found for Job ID: " + id);
        }

        Application app = apps.get(0);
        app.setStatus(newStatus);
        applicationRepository.save(app);

        return ResponseEntity.ok("Status successfully updated to: " + newStatus);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Application> apps = applicationRepository.findAllByUserIdAndJobId(user.getId(), id);

        if (apps.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Application not found for Job ID: " + id);
        }

        applicationRepository.delete(apps.get(0));
        return ResponseEntity.ok("Job application deleted successfully.");
    }
}