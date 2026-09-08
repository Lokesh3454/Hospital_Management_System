package com.hospital.hms.service;

import com.hospital.hms.entity.NotificationLog;
import java.util.List;

public interface NotificationService {
    List<NotificationLog> getRecentLogs();
    NotificationLog sendNotification(NotificationLog log);
}
