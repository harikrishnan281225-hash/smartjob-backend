package com.smartjob.service;

import com.smartjob.entity.Application;
import com.smartjob.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationScheduler {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EmailService emailService;

    // This runs every day at 08:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendFollowUpReminders() {
        LocalDate today = LocalDate.now();
        // FIXED: Now searching via Job.followUpDate
        List<Application> applications = applicationRepository.findByJobFollowUpDate(today);

        for (Application app : applications) {
            try {
                if (app.getUser() == null || app.getJob() == null) continue;
                String userEmail = app.getUser().getEmail();
                String jobTitle = app.getJob().getTitle();
                emailService.sendReminderEmail(userEmail, jobTitle);
                System.out.println("Reminder email sent to: " + userEmail + " for job: " + jobTitle);
            } catch (Exception e) {
                System.err.println("Failed to send reminder: " + e.getMessage());
            }
        }
    }
}