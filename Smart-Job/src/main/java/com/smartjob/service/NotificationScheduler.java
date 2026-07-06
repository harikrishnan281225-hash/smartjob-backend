package com.smartjob.service;

import com.smartjob.entity.Application;
import com.smartjob.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    public void sendFollowUpReminders() {
        LocalDate today = LocalDate.now();
        List<Application> applications = applicationRepository.findByFollowUpDate(today);

        for (Application app : applications) {
            String userEmail = app.getUser().getEmail();
            String jobTitle = app.getJob().getTitle();

            // Send the automated email notification
            emailService.sendReminderEmail(userEmail, jobTitle);

            System.out.println("Reminder email sent to: " + userEmail + " for job: " + jobTitle);
        }
    }
}