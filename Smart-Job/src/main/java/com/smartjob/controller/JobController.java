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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private User getCurrentUser(Authentication authentication) {
        String identifier = authentication.getName();
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier)));
    }

    @GetMapping("/my-jobs")
    @Transactional
    public ResponseEntity<List<JobDTO>> getMyJobs(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Application> userApps = applicationRepository.findByUserId(user.getId());

        System.out.println("=== MY-JOBS for user " + user.getId() + " count: " + userApps.size());

        List<JobDTO> jobDTOs = userApps.stream()
                .map(app -> {
                    try {
                        if (app.getJob() == null) return null;
                        Job job = app.getJob();
                        JobDTO dto = new JobDTO();
                        dto.setId(job.getId());
                        dto.setTitle(job.getTitle());
                        dto.setCompanyName(job.getCompanyName());
                        dto.setLocation(job.getLocation());
                        dto.setDescription(job.getDescription());
                        dto.setUrl(job.getUrl());
                        dto.setStatus(app.getStatus() != null ? app.getStatus() : job.getStatus());
                        dto.setSalary(job.getSalary());
                        dto.setSkillsRequired(job.getSkillsRequired());
                        dto.setExperienceRequired(job.getExperienceRequired());
                        dto.setPostedDate(job.getPostedDate() != null ? job.getPostedDate().toString() : null);
                        dto.setFollowUpDate(job.getFollowUpDate() != null ? job.getFollowUpDate().toString() : null);
                        return dto;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(jobDTOs);
    }

    @GetMapping("/assigned")
    @Transactional
    public ResponseEntity<List<JobDTO>> getAssignedJobs(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Application> userApps = applicationRepository.findByUserId(user.getId());
        List<JobDTO> assignedJobs = userApps.stream()
                .filter(app -> app.getStatus() != null && "ASSIGNED".equalsIgnoreCase(app.getStatus().trim()))
                .map(app -> app.getJob() != null ? app.getJob().toDTO() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignedJobs);
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<String> applyForJob(@RequestBody ApplyRequest payload, Authentication authentication) {
        User user = getCurrentUser(authentication);
        String incomingId = payload.getJobId();
        String incomingUrl = payload.getUrl();

        System.out.println("=== ADD JOB REQUEST user=" + user.getId() + " payload=" + payload);

        // FIXED: Now payload will have values thanks to @JsonAlias
        String title = payload.getTitle();
        String company = payload.getCompanyName();

        // Final safety - never allow blank
        if (title == null || title.isBlank()) {
            System.out.println("WARNING: Title is null, using fallback");
            title = "Job - " + (incomingId != null ? incomingId : "Unknown");
        }
        if (company == null || company.isBlank()) {
            company = "Company Not Specified";
        }

        Job job = null;
        if (incomingUrl != null && !incomingUrl.isBlank()) {
            job = jobRepository.findByUrl(incomingUrl).orElse(null);
        }
        if (job == null && incomingId != null && !incomingId.isBlank()) {
            job = jobRepository.findByExternalId(incomingId).orElse(null);
        }

        if (job == null) {
            Job newJob = new Job();
            newJob.setExternalId(incomingId != null ? incomingId : incomingUrl);
            newJob.setTitle(title);
            newJob.setCompanyName(company);
            newJob.setLocation(payload.getLocation() != null && !payload.getLocation().isBlank() ? payload.getLocation() : "Remote");
            newJob.setUrl(payload.getUrl());
            newJob.setDescription(payload.getDescription() != null ? payload.getDescription() : "No description provided");
            newJob.setSalary(payload.getSalary());
            newJob.setSkillsRequired(payload.getSkillsRequired());
            newJob.setExperienceRequired(payload.getExperienceRequired());
            newJob.setPostedDate(LocalDate.now());
            newJob.setStatus("APPLIED");
            newJob.setPostedBy(user.getUsername() != null ? user.getUsername() : user.getEmail());
            newJob.setOwnerId(user.getId());
            job = jobRepository.save(newJob);
            System.out.println("=== NEW JOB SAVED UUID=" + job.getId() + " TITLE=" + job.getTitle() + " COMPANY=" + job.getCompanyName());
        }

        List<Application> existing = applicationRepository.findAllByUserIdAndJobId(user.getId(), job.getId());
        if (!existing.isEmpty()) {
            return ResponseEntity.ok("Job already in dashboard");
        }

        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setStatus("APPLIED");
        application.setAppliedDate(LocalDateTime.now());
        applicationRepository.save(application);

        System.out.println("=== APPLICATION SAVED user=" + user.getId() + " job=" + job.getId());
        return ResponseEntity.ok("Job added successfully");
    }

    @PutMapping("/{id}/status")
    @Transactional
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
    @Transactional
    public ResponseEntity<String> deleteJob(@PathVariable String id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Application> apps = applicationRepository.findAllByUserIdAndJobId(user.getId(), id);
        if (apps.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Application not found");
        applicationRepository.delete(apps.get(0));
        return ResponseEntity.ok("Application deleted.");
    }
}