package com.smartjob.repository;

import com.smartjob.entity.Application;
import com.smartjob.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUser(User user);
    List<Application> findByUserId(Long userId);

    // Kept for backward compatibility, but be careful of "non-unique" errors
    Application findByUserIdAndJobId(Long userId, String jobId);

    // NEW: Added this to handle multiple records for the same user/job combination
    List<Application> findAllByUserIdAndJobId(Long userId, String jobId);

    List<Application> findByJobId(String jobId);
    List<Application> findByFollowUpDate(LocalDate followUpDate);

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.user JOIN FETCH a.job",
            countQuery = "SELECT count(a) FROM Application a")
    Page<Application> findTop5WithDetails(Pageable pageable);
}