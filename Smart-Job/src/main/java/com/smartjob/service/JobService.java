package com.smartjob.service;

import com.smartjob.dto.JobData; // Correct import: uses your standalone DTO file
import com.smartjob.entity.Application;
import com.smartjob.entity.Job;
import com.smartjob.entity.User;
import com.smartjob.repository.ApplicationRepository;
import com.smartjob.repository.JobRepository;
import com.smartjob.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            return;
        }

        // 1. Check if job exists, if not, save it to the database
        Job job = jobRepository.findById(jobData.getId())
                .orElseGet(() -> {
                    Job newJob = new Job();
                    newJob.setId(jobData.getId());
                    newJob.setTitle(jobData.getTitle());
                    newJob.setCompanyName(jobData.getCompanyName() != null ? jobData.getCompanyName() : "Unknown");
                    newJob.setLocation(jobData.getLocation() != null ? jobData.getLocation() : "Unknown");

                    // Safe handling of nulls to prevent DB Constraint errors
                    newJob.setDescription(jobData.getDescription() != null ? jobData.getDescription() : "No description provided.");

                    newJob.setUrl(jobData.getUrl());
                    newJob.setSalary(jobData.getSalary());
                    newJob.setExperienceRequired(jobData.getExperienceRequired());
                    newJob.setSkillsRequired(jobData.getSkillsRequired());
                    newJob.setStatus("ASSIGNED");

                    return jobRepository.save(newJob);
                });

        // 2. Iterate through users and create applications
        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                Application existingAssignment = applicationRepository.findByUserIdAndJobId(userId, jobData.getId());

                if (existingAssignment == null) {
                    Application application = new Application();
                    application.setUser(user);
                    application.setJob(job);
                    application.setStatus("ASSIGNED");
                    applicationRepository.save(application);
                }
            } catch (Exception e) {
                System.err.println("ERROR: Could not assign job to user " + userId + ": " + e.getMessage());
            }
        }
    }
}