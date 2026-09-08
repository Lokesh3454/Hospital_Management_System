package com.hospital.hms.repository;

import com.hospital.hms.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findAllByOrderBySentAtDesc();
    List<NotificationLog> findTop20ByOrderBySentAtDesc();
}
