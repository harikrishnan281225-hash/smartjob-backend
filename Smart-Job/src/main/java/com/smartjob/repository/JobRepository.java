package com.smartjob.repository;

import com.smartjob.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JobRepository interface for managing Job entities.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    // --- Core Methods needed for Assign Fix ---
    Optional<Job> findByUrl(String url);

    Optional<Job> findByExternalId(String externalId);

    // --- Search & Filter Methods ---
    List<Job> findAllByTitle(String title);

    // Assuming Job entity has a 'username' or 'userId' field
    List<Job> findByUsername(String username);

    List<Job> findByUserIdAndStatus(Long userId, String status);

    // --- Stats Methods ---
    long countByStatus(String status);

    // --- Ownership/Admin Methods ---
    List<Job> findByPostedBy(String postedBy);

    List<Job> findByPostedByAndOwnerId(String postedBy, Long ownerId);
}