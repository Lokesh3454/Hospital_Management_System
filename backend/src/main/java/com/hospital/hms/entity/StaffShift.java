package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_shifts")
public class StaffShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String staffName;

    @Column(nullable = false)
    private String roleTitle; // e.g. "Senior Surgeon", "Resident Doctor", "Head Nurse"

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String dayOfWeek; // "Monday" .. "Sunday"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType shiftType = ShiftType.MORNING;

    private String startTime = "08:00 AM";
    private String endTime = "04:00 PM";

    private Boolean onCall = false;
    private String contactPhone;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ShiftType {
        MORNING,
        EVENING,
        NIGHT
    }

    public StaffShift() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = shiftType; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Boolean getOnCall() { return onCall; }
    public void setOnCall(Boolean onCall) { this.onCall = onCall; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
