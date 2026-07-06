package com.smartjob.repository;

import com.smartjob.entity.UserJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing the relationship between Users and Jobs.
 */
@Repository
public interface UserJobRepository extends JpaRepository<UserJob, Long> {

    /**
     * Finds all records of users associated with a specific job.
     * The @Param jobId must be a String because the Job entity ID is a String.
     */
    @Query("SELECT uj FROM UserJob uj WHERE uj.job.id = :jobId")
    List<UserJob> findByJobId(@Param("jobId") String jobId);

    /**
     * Finds all records of jobs associated with a specific user.
     * userId remains a Long.
     */
    @Query("SELECT uj FROM UserJob uj WHERE uj.user.id = :userId")
    List<UserJob> findByUserId(@Param("userId") Long userId);
}