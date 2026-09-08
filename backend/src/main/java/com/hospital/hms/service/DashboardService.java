package com.hospital.hms.service;

import com.hospital.hms.dto.dashboard.AdminDashboardDTO;
import com.hospital.hms.dto.dashboard.DoctorDashboardDTO;
import com.hospital.hms.dto.dashboard.PatientDashboardDTO;
import com.hospital.hms.dto.dashboard.ReceptionistDashboardDTO;

public interface DashboardService {

    AdminDashboardDTO getAdminDashboard();

    DoctorDashboardDTO getDoctorDashboard(Long authenticatedUserId);

    PatientDashboardDTO getPatientDashboard(Long authenticatedUserId);

    ReceptionistDashboardDTO getReceptionistDashboard();
}
