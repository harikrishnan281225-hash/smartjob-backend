package com.smartjob.service;

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

    /**
     * Logic for assigning an existing job to a list of users.
     * UPDATED: jobId parameter is now String to match the JSearch hash format.
     */
    @Transactional
    public void assignJobToUsers(String jobId, List<Long> userIds) {
        System.out.println("DEBUG: Assigning job ID " + jobId + " to " +
                (userIds != null ? userIds.size() : 0) + " users.");

        // Find the job using String ID
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        for (Long userId : userIds) {
            // Find the user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Check if this user is already assigned to this job
            // Using the updated repository method that accepts String jobId
            Application existingAssignment = applicationRepository.findByUserIdAndJobId(userId, jobId);

            if (existingAssignment == null) {
                Application application = new Application();
                application.setUser(user);
                application.setJob(job);
                application.setStatus("ASSIGNED");

                applicationRepository.save(application);
                System.out.println("DEBUG: Successfully assigned job " + jobId + " to user ID: " + userId);
            } else {
                System.out.println("DEBUG: User " + userId + " is already assigned to job " + jobId + ". Skipping.");
            }
        }
    }
}