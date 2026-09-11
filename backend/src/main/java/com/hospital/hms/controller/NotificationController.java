package com.hospital.hms.controller;

import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.entity.NotificationLog;
import com.hospital.hms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping({"", "/recent"})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getRecentLogs() {
        return ResponseEntity.ok(ApiResponse.ok("Notification dispatch outbox retrieved", notificationService.getRecentLogs()));
    }

    @PostMapping({"/dispatch", "/dispatch-test"})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<ApiResponse<NotificationLog>> sendNotification(@RequestBody NotificationLog log) {
        NotificationLog sent = notificationService.sendNotification(log);
        return new ResponseEntity<>(ApiResponse.ok("Notification dispatched successfully", sent), HttpStatus.CREATED);
    }
}
