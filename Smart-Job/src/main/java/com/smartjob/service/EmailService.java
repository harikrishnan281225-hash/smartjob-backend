package com.smartjob.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // This pulls your email from application.properties so you don't hardcode it here
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendReminderEmail(String to, String jobTitle) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Job Application Follow-up Reminder");
        message.setText("Hi! This is a friendly reminder to follow up on your application for the position: " + jobTitle + ". Good luck!");

        mailSender.send(message);
    }
}