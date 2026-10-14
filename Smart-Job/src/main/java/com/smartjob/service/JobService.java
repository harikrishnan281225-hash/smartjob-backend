package com.smartjob.service;

import com.smartjob.dto.JobData;
import com.smartjob.entity.Application;
import com.smartjob.entity.Job;
import com.smartjob.entity.User;
import com.smartjob.repository.ApplicationRepository;
import com.smartjob.repository.JobRepository;
import com.smartjob.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Transactional
    public void assignJobToUsers(JobData jobData, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new RuntimeException("No users selected");
        }

        // 1. Check if job exists by URL (don't use external LinkedIn ID as PK)
        Job job = null;
        if (jobData.getUrl() != null && !jobData.getUrl().isBlank()) {
            job = jobRepository.findByUrl(jobData.getUrl()).orElse(null);
        }

        if (job == null) {
            Job newJob = new Job();
            // IMPORTANT: Don't set ID - let Hibernate generate UUID
            // Save external ID in separate column
            newJob.setExternalId(jobData.getId());

            newJob.setTitle(jobData.getTitle() != null ? jobData.getTitle() : "No Title");
            newJob.setCompanyName(jobData.getCompanyName() != null ? jobData.getCompanyName() : "Unknown");
            newJob.setLocation(jobData.getLocation() != null ? jobData.getLocation() : "Remote");
            newJob.setDescription(jobData.getDescription() != null ? jobData.getDescription() : "No description provided.");
            newJob.setUrl(jobData.getUrl());
            newJob.setSalary(jobData.getSalary());
            newJob.setExperienceRequired(jobData.getExperienceRequired());
            newJob.setSkillsRequired(jobData.getSkillsRequired());
            newJob.setStatus("ASSIGNED");
            newJob.setPostedDate(LocalDate.now());
            newJob.setPostedBy("ADMIN");

            job = jobRepository.save(newJob);
        }

        // 2. Assign to users - use real DB ID
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Check if already assigned
            Optional<Application> existingAssignment = applicationRepository.findByUserIdAndJobId(userId, job.getId());

            if (existingAssignment.isEmpty()) {
                Application application = new Application();
                application.setUser(user);
                application.setJob(job);
                application.setStatus("ASSIGNED");
                application.setAppliedDate(LocalDateTime.now());
                applicationRepository.save(application);
            }
        }
    }
}