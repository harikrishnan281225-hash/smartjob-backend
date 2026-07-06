package com.smartjob.repository;

import com.smartjob.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByReceiverIdOrderByTimestampDesc(Long receiverId);

    // Updated to match the field name 'read'
    long countByReadFalse();
}