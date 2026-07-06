package com.smartjob.controller;

import com.smartjob.entity.Message;
import com.smartjob.entity.User;
import com.smartjob.repository.MessageRepository;
import com.smartjob.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // Use hasAnyAuthority to explicitly match the "ROLE_" strings in your UserDetails
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/inbox")
    public ResponseEntity<List<Message>> getMyMessages(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        logger.info("DEBUG: Fetching inbox for user '{}' (ID: {})", username, user.getId());
        List<Message> messages = messageRepository.findByReceiverIdOrderByTimestampDesc(user.getId());

        return ResponseEntity.ok(messages);
    }

    // Admin endpoint
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/all-messages")
    public ResponseEntity<List<Message>> getAllMessagesForAdmin() {
        logger.info("DEBUG: Admin fetching all messages.");
        List<Message> messages = messageRepository.findAll();
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/admin/send")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> sendMessage(@RequestBody Message message) {
        logger.info("Admin sending message to receiver_id {}: {}", message.getReceiverId(), message.getContent());
        try {
            messageRepository.save(message);
            return ResponseEntity.ok("Message sent successfully!");
        } catch (Exception e) {
            logger.error("Error saving message: ", e);
            return ResponseEntity.internalServerError().body("Failed to save message.");
        }
    }

    // FIXED: Updated to match the "ROLE_" prefix used in UserDetailsServiceImpl
    @PutMapping("/mark-as-read/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        logger.info("DEBUG: Attempting to mark message ID {} as read", id);
        return messageRepository.findById(id)
                .map(message -> {
                    message.setRead(true);
                    messageRepository.save(message);
                    return ResponseEntity.ok("Message marked as read");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}