package com.smartjob.controller;

import com.smartjob.entity.Message;
import com.smartjob.entity.User;
import com.smartjob.repository.MessageRepository;
import com.smartjob.repository.UserRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;

    // UPDATED: Added better error handling to see exactly what string fails
    private User getCurrentUser(String identifier) {
        logger.info("Attempting lookup for identifier: '{}'", identifier);
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> {
                    logger.error("CRITICAL: User NOT FOUND in DB for identifier: '{}'", identifier);
                    return new RuntimeException("User lookup failed for: " + identifier);
                });
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/inbox")
    public ResponseEntity<?> getMyMessages(Principal principal) {
        if (principal == null) {
            logger.error("Unauthorized attempt: Principal is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String identifier = principal.getName();
            logger.info("DEBUG: Principal name (from JWT/Session) is: '{}'", identifier);

            User user = getCurrentUser(identifier);
            logger.info("DEBUG: Successfully resolved user: {} with DB ID: {}", user.getUsername(), user.getId());

            List<Message> messages = messageRepository.findByReceiverIdOrderByTimestampDesc(user.getId());

            logger.info("DEBUG: Query returned {} messages for User ID: {}",
                    messages != null ? messages.size() : 0, user.getId());

            return ResponseEntity.ok(messages != null ? messages : Collections.emptyList());

        } catch (RuntimeException e) {
            // This is likely where your 400/500 is coming from
            logger.error("DEBUG: User resolution failed. Check if Principal name matches Username/Email in DB. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("DEBUG: Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    // ... (rest of your methods remain same)

    @PutMapping("/mark-as-read/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        return messageRepository.findById(id)
                .map(message -> {
                    message.setRead(true);
                    messageRepository.save(message);
                    return ResponseEntity.ok("Message marked as read");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/all-messages")
    public ResponseEntity<List<Message>> getAllMessagesForAdmin() {
        return ResponseEntity.ok(messageRepository.findAll());
    }

    @PostMapping("/admin/send")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> sendMessage(@RequestBody Message message) {
        if (message.getTimestamp() == null) message.setTimestamp(java.time.LocalDateTime.now());
        messageRepository.save(message);
        return ResponseEntity.ok("Message sent!");
    }

    @PostMapping("/admin/broadcast")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> broadcastMessage(@RequestBody BroadcastRequest request) {
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) return ResponseEntity.badRequest().body("No users.");

        for (Long userId : request.getUserIds()) {
            Message msg = new Message();
            msg.setReceiverId(userId);
            msg.setContent(request.getContent());
            msg.setRead(false);
            msg.setTimestamp(java.time.LocalDateTime.now());
            messageRepository.save(msg);
        }
        return ResponseEntity.ok("Broadcast success.");
    }

    @Data
    public static class BroadcastRequest {
        private List<Long> userIds;
        private String content;
    }
}