package com.smartjob.repository;

import com.smartjob.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // NEW: Allows the Admin to filter users by role
    List<User> findByRole(String role);
}