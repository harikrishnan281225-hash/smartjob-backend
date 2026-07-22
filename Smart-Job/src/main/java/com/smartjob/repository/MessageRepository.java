package com.smartjob.repository;

import com.smartjob.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Explicitly defining the query to ensure no ambiguity
    @Query("SELECT m FROM Message m WHERE m.receiverId = :receiverId ORDER BY m.timestamp DESC")
    List<Message> findByReceiverIdOrderByTimestampDesc(@Param("receiverId") Long receiverId);

    // Matches the field name 'read' in your Message entity
    long countByReadFalse();
}