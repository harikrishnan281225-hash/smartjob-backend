package com.smartjob.service;

import com.smartjob.dto.UserStatsDTO;
import com.smartjob.entity.Application;
import com.smartjob.entity.User;
import com.smartjob.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    public UserStatsDTO getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Application> apps = user.getApplications();

        UserStatsDTO dto = new UserStatsDTO();
        dto.setUser(user);
        dto.setTotalApplied(apps != null ? apps.size() : 0);

        if (apps != null) {
            // Updated filtering logic to handle null statuses safely
            dto.setInterviews(apps.stream()
                    .filter(a -> a.getStatus() != null && "INTERVIEW".equalsIgnoreCase(a.getStatus()))
                    .count());

            dto.setOffers(apps.stream()
                    .filter(a -> a.getStatus() != null && "OFFER".equalsIgnoreCase(a.getStatus()))
                    .count());

            dto.setRejected(apps.stream()
                    .filter(a -> a.getStatus() != null && "REJECTED".equalsIgnoreCase(a.getStatus()))
                    .count());
        } else {
            // Explicitly set to 0 if no applications list exists
            dto.setInterviews(0L);
            dto.setOffers(0L);
            dto.setRejected(0L);
        }

        return dto;
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    public User processOAuth2User(String email, String name) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setUsername(email);
            newUser.setRole("USER");
            newUser.setProvider("GOOGLE");
            return userRepository.save(newUser);
        });
    }

    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("GOOGLE".equals(user.getProvider())) {
            throw new RuntimeException("Cannot change password for social login users");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}