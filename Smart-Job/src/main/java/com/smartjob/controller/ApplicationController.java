package com.smartjob.controller;

import com.smartjob.entity.Application;
import com.smartjob.entity.Job;
import com.smartjob.entity.User;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.repository.ApplicationRepository;
import com.smartjob.repository.JobRepository;
import com.smartjob.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private UserRepository userRepository;

    // UPDATED: jobId is now String to match the Job entity ID
    @PostMapping("/apply/{jobId}")
    public String applyForJob(
            @PathVariable String jobId,
            @RequestBody(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate followUpDate) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // jobRepository.findById now correctly expects a String ID
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setFollowUpDate(followUpDate);

        applicationRepository.save(application);

        return "Application submitted successfully!";
    }

    @PutMapping("/{id}/follow-up-date")
    public ResponseEntity<String> updateFollowUpDate(
            @PathVariable Long id,
            @RequestBody @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate followUpDate) {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        application.setFollowUpDate(followUpDate);
        applicationRepository.save(application);

        return ResponseEntity.ok("Follow-up date updated successfully!");
    }

    @GetMapping("/my-applications")
    public List<Application> getMyApplications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return applicationRepository.findByUser(user);
    }

    // UPDATED: jobId is now String
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Application> getApplicationsByJob(@PathVariable String jobId) {
        // Ensure your ApplicationRepository method is findByJobId(String jobId)
        return applicationRepository.findByJobId(jobId);
    }
}