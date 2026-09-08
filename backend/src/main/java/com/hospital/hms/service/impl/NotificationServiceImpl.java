package com.hospital.hms.service.impl;

import com.hospital.hms.entity.NotificationLog;
import com.hospital.hms.repository.NotificationLogRepository;
import com.hospital.hms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLog> getRecentLogs() {
        return notificationLogRepository.findTop20ByOrderBySentAtDesc();
    }

    @Override
    public NotificationLog sendNotification(NotificationLog log) {
        if (log.getSentAt() == null) {
            log.setSentAt(LocalDateTime.now());
        }
        if (log.getStatus() == null) {
            log.setStatus(NotificationLog.DeliveryStatus.DELIVERED);
        }
        return notificationLogRepository.save(log);
    }
}
