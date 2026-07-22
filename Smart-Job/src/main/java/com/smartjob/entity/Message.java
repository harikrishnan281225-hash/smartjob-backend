package com.smartjob.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages") // Changed from "message" to "messages" to match your DB error
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    // Ensure timestamp is initialized before persisting
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}