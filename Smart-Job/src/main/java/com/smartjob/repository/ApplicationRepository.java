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
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUser(User user);
    List<Application> findByUserId(Long userId);
    List<Application> findAllByUserIdAndJobId(Long userId, String jobId);
    Optional<Application> findByUserIdAndJobId(Long userId, String jobId);
    List<Application> findByJobId(String jobId);
    boolean existsByUserIdAndJobId(Long userId, String jobId);
    void deleteByUserIdAndJobId(Long userId, String jobId);

    // FIX for NotificationScheduler - followUpDate is in Job
    List<Application> findByJobFollowUpDate(LocalDate followUpDate);

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.user JOIN FETCH a.job",
            countQuery = "SELECT count(a) FROM Application a")
    Page<Application> findTop5WithDetails(Pageable pageable);
}