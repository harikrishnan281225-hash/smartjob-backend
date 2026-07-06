package com.smartjob.controller;

import com.smartjob.entity.User;
import com.smartjob.repository.UserRepository;
import com.smartjob.dto.PasswordChangeRequest;
import com.smartjob.dto.ProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    // Use absolute pathing based on the project's root directory
    private final Path root = Paths.get(System.getProperty("user.dir"), "uploads");

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserProfile() {
        return ResponseEntity.ok(getCurrentUser());
    }

    @PutMapping("/details")
    public ResponseEntity<String> updateDetails(@RequestBody ProfileUpdateRequest details) {
        User user = getCurrentUser();
        user.setFullName(details.getFullName());
        user.setPhone(details.getPhone());
        user.setBio(details.getBio());
        user.setLinkedInUrl(details.getLinkedInUrl());
        user.setGithubUrl(details.getGithubUrl());
        userRepository.save(user);
        return ResponseEntity.ok("Portfolio details updated successfully");
    }

    @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Ensure the 'uploads' directory exists at the absolute project root
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // 2. Sanitize the filename to avoid %20 (space) and URL encoding issues
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String safeFileName = originalFileName.replaceAll("\\s+", "_");

            // 3. Save the file physically
            Files.copy(file.getInputStream(), this.root.resolve(safeFileName), StandardCopyOption.REPLACE_EXISTING);

            // 4. Update the database with the safe path
            User user = getCurrentUser();
            user.setResumePath("uploads/" + safeFileName);
            userRepository.save(user);

            return ResponseEntity.ok("Resume uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload the file: " + e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordChangeRequest dto) {
        return ResponseEntity.ok("Password updated");
    }
}